# Code Review: Gradle 멀티 모듈 1차 전환
날짜: 2026-04-21
참조: docs/reviews/gradle-multimodule-phase1-review.md

---

## 1. [의존성 계약 위반] `platform/build.gradle` — `spring-boot-starter-webmvc`를 `api`로 노출

**파일**: `backend/platform/build.gradle:6`

```gradle
api 'org.springframework.boot:spring-boot-starter-webmvc'
```

`java-library`에서 `api`는 의존성을 compile scope에 전이 노출(transitive)한다. `PlatformGateway` 인터페이스의 모든 메서드 시그니처는 `Remote*` 타입만 반환하며 webmvc 타입을 노출하지 않는다. 즉 `api` 선언의 전제 조건을 충족하지 않는다.

결과: `platform`에 의존하는 `connection`, `repository`, `issue`, `comment` 모두 webmvc를 compile scope에 암묵적으로 갖게 된다. 향후 이 모듈들에 업무 코드가 추가될 때 webmvc 클래스(`@RestController`, `MockMvc` 등)를 실수로 사용해도 컴파일 오류 없이 통과한다.

`implementation`으로 바꿔야 한다.

---

## 2. [설계 계약 위반] 빈 업무 모듈 — 모든 의존성 `api` 선언

**파일**: `backend/connection/build.gradle`, `backend/repository/build.gradle`, `backend/issue/build.gradle`, `backend/comment/build.gradle`

```gradle
// comment/build.gradle 예시
api project(':issue')
api project(':connection')
api project(':platform')
api project(':shared-kernel')
```

모든 모듈 간 의존이 `api`로 선언되어 있다. `comment`가 `issue`를 `api`로 가지면 `comment`의 모든 의존 모듈이 `issue`의 public API를 compile scope에 갖는다. 이로 인해 `comment`의 의존 모듈이 `issue` 내부 타입을 직접 참조해도 컴파일 타임에 허용된다.

doc 13 원칙: "한 모듈의 내부 domain, repository, infrastructure를 다른 모듈이 직접 참조하지 않는다." `api` 선언은 이 경계를 Gradle 빌드 레벨에서 무력화한다.

현재 코드가 없어 즉각적인 런타임 오류는 없으나, 골격 의존 설정 자체가 doc 13 모듈 경계 원칙과 배치된다. 의존성 선언은 `implementation`이 기본이고, public API 타입이 시그니처에 노출될 때만 `api`를 사용해야 한다.

---

## 3. [빌드 구조 오류] `backend/build.gradle` — root project에 `id 'java'` 잔류

**파일**: `backend/build.gradle:2`

```gradle
plugins {
    id 'java'  // ← 멀티 모듈 전환 후에도 제거되지 않음
    id 'org.springframework.boot' version '4.0.4' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}
```

멀티 모듈 전환 후 root project는 빌드 조율자 역할만 해야 한다. `id 'java'` 잔류로 root project에 `compileJava`, `jar`, `test` task가 생성된다. `backend/src/`는 비어있어 실제 컴파일/테스트는 없지만, `./gradlew jar`가 root project에 대해 빈 jar를 생성할 수 있다.

더 중요한 것: root project에도 `io.spring.dependency-management` plugin이 적용되지 않아 BOM 관리가 없다. 향후 root project에 의존성이 실수로 추가될 경우 버전 관리 없이 추가된다.

`id 'java'`를 root `plugins` 블록에서 제거해야 한다.

---

## 4. [런타임 실패 위험] `app/build.gradle` — `platform` 모듈 Properties bean 스캔 검증 필요

**파일**: `backend/app/src/main/java/com/jw/github_issue_manager/GithubIssueManagerApplication.java:6`

```java
@ConfigurationPropertiesScan
```

`@ConfigurationPropertiesScan`은 base package(`com.jw.github_issue_manager`)를 스캔해 `@ConfigurationProperties` 클래스를 bean으로 등록한다. `platform` 모듈의 `GitHubIntegrationProperties`, `GitLabIntegrationProperties`도 같은 base package에 있어 classpath 스캔 범위에 포함된다.

단, 이 동작은 `platform` jar가 `app` classpath에 있어야 보장된다. `app/build.gradle`이 `implementation project(':platform')`으로 의존하므로 현재는 정상이다.

리뷰 포인트: `platform`이 독립 모듈이 된 이후 `application.properties`에 `github.api-base-url`, `gitlab.api-base-url` 설정이 없으면 `@ConfigurationProperties` 바인딩 실패로 startup 오류가 발생한다. `app` 모듈의 테스트 자원(`application.properties` 또는 `application-test.properties`)에 platform 설정값이 포함되어 있는지 확인이 필요하다.

---

## 5. [CI 경로 위험] `deploy.yml` — `./gradlew test`와 `./gradlew clean :app:bootJar` 분리 실행

**파일**: `.github/workflows/deploy.yml:41-44`

```yaml
- name: Run tests
  run: ./gradlew test

- name: Build boot jar
  run: ./gradlew clean :app:bootJar
```

`:app:bootJar` 단계에서 `clean`이 먼저 실행된다. `clean`은 `:platform`의 컴파일 결과물도 삭제한다. 이후 `:app:bootJar`는 `platform` 모듈을 다시 컴파일하므로 실제 빌드 실패는 없다.

그러나 `./gradlew test`와 `./gradlew clean :app:bootJar`가 별개의 Gradle 실행이므로 Gradle build cache가 두 번 소비된다. 멀티 모듈에서 하위 모듈 수가 늘어날수록 CI 빌드 시간이 증가한다. 단일 Gradle 실행 (`./gradlew clean :app:bootJar test` 순서 또는 별도 step 없이 합치기)으로 개선 가능하지만, 현재 구조에서 빌드 실패 위험은 없다.

심각도: 낮음. 단, `test` step 실패 시 `bootJar` step이 실행되지 않아 배포가 차단되므로 테스트 선행 실행 구조 자체는 의도된 것으로 볼 수 있다.

---

## Codex 검증 결과
검증일: 2026-04-21

### 검증 요약

- 1번 `platform`의 webmvc `api` 노출: 유효
- 2번 빈 업무 모듈의 전부 `api` 의존: 유효하나 현재는 예방성 지적
- 3번 root project `java` plugin 잔류: 유효
- 4번 platform properties scan: 현재 테스트/설정 기준 정상
- 5번 deploy workflow 분리 실행: 낮은 심각도의 성능/운영 효율 지적

### 1. `platform/build.gradle` webmvc `api` 노출

판정: 유효

- 확인: `backend/platform/build.gradle`에서 `api 'org.springframework.boot:spring-boot-starter-webmvc'` 사용 중
- 확인: `PlatformGateway` public signature는 `Remote*`, `PlatformType`, Java collection 중심이며 webmvc 타입을 노출하지 않음
- 판단: `platform` 내부 구현의 `RestClient`, Spring component 사용을 위해 필요한 의존성은 맞지만, downstream 모듈에 webmvc를 전이 노출할 이유는 약함
- 권장: `api` → `implementation` 전환

### 2. 빈 업무 모듈의 모든 의존성 `api` 선언

판정: 유효하나 현재는 예방성 지적

- 확인: `connection`, `repository`, `issue`, `comment` 모듈이 project dependency를 모두 `api`로 선언 중
- 현재 상태: 해당 모듈들은 아직 업무 코드가 없으므로 즉시 컴파일/런타임 오류는 없음
- 판단: 이후 public API가 실제로 생기기 전까지는 `implementation`을 기본값으로 두는 편이 문서 13의 모듈 경계 원칙에 더 맞음
- 권장: public API method signature에 노출되는 dependency만 `api`로 승격하고, 현재 골격 단계에서는 `implementation`으로 낮춤

### 3. root project `java` plugin 잔류

판정: 유효

- 확인: `backend/build.gradle` root `plugins` 블록에 `id 'java'` 존재
- 검증 명령:

```powershell
cd backend
.\gradlew.bat jar
```

- 결과: `backend/build/libs/github-issue-manager-0.0.1-SNAPSHOT.jar` 생성
- 확인: 생성된 jar 크기 261 bytes로 root project의 빈 jar 산출물
- 판단: 멀티 모듈 root는 조율자 역할만 하는 편이 명확하므로 root `java` plugin 제거가 적절
- 권장: root `plugins`에서 `id 'java'` 제거

### 4. platform properties bean 스캔

판정: 현재 기준 정상

- 확인: `app` 모듈은 `implementation project(':platform')`으로 platform jar를 runtime classpath에 포함
- 확인: main `application.yaml`에 아래 기본값 존재

```yaml
app:
  github:
    api-base-url: ${GITHUB_API_BASE_URL:https://api.github.com}
    pat-encryption-key: ${GITHUB_PAT_ENCRYPTION_KEY:local-dev-pat-key}
  gitlab:
    api-base-url: ${GITLAB_API_BASE_URL:https://gitlab.com/api/v4}
```

- 검증 명령:

```powershell
cd backend
.\gradlew.bat :app:test --tests com.jw.github_issue_manager.GithubIssueManagerApplicationTests
```

- 결과: 성공
- 판단: `ConfigurationPropertiesScan`과 component scan은 현재 테스트 context에서 platform 모듈 bean/properties를 정상 로딩함
- 추가 확인: `bootRun`은 서버 프로세스가 계속 유지되어 제한 시간으로 종료됐으나, 이는 startup 실패 판정으로 보지 않음
- 남은 리스크: prod/ec2 profile은 `app.gitlab` override를 별도로 선언하지 않지만 main `application.yaml` 기본값이 있어 현재 구조에서는 바인딩 실패 가능성이 낮음

### 5. deploy workflow 분리 실행

판정: 유효하나 심각도 낮음

- 확인: `.github/workflows/deploy.yml`에서 `./gradlew test` 후 별도 step으로 `./gradlew clean :app:bootJar` 실행
- 판단: 두 번째 step의 `clean`이 앞선 테스트 산출물을 지우고 `:app:bootJar`에 필요한 모듈을 다시 컴파일하므로 실패 위험보다는 CI 시간 증가 리스크가 큼
- 현재 구조상 장점: 테스트 실패 시 bootJar와 배포가 실행되지 않아 배포 차단 흐름은 명확함
- 권장: CI 시간이 문제되면 단일 Gradle 실행으로 합치는 개선 검토

### 추가 검증

등록 모듈 확인:

```powershell
cd backend
.\gradlew.bat projects
```

결과:

```text
Root project 'github-issue-manager'
+--- Project ':app'
+--- Project ':comment'
+--- Project ':connection'
+--- Project ':issue'
+--- Project ':platform'
+--- Project ':repository'
\--- Project ':shared-kernel'
```

전체 빌드/테스트는 이전 1차 전환 검증에서 통과:

```powershell
cd backend
.\gradlew.bat clean :app:bootJar test
```

결과:

- 성공
- `backend/app/build/libs/app-0.0.1-SNAPSHOT.jar` 생성 확인
- `backend/platform/build/libs/platform-0.0.1-SNAPSHOT.jar` 생성 확인

### 후속 조치 제안

1. `platform/build.gradle`의 webmvc dependency를 `implementation`으로 변경
2. 빈 업무 모듈의 project dependency를 기본 `implementation`으로 변경
3. root `build.gradle`에서 `id 'java'` 제거
4. 변경 후 `.\gradlew.bat clean :app:bootJar test` 재실행

---

## Codex 조치 결과
조치일: 2026-04-21

### 반영 항목

- 반영: 1번 `platform/build.gradle`의 webmvc dependency를 `api`에서 `implementation`으로 변경
- 반영: 3번 root `build.gradle`의 `id 'java'` 제거
- 보류: 2번 빈 업무 모듈의 `api` 의존성 조정
- 보류: 4번 properties scan 추가 보강
- 보류: 5번 deploy workflow 단일 Gradle 실행 개선

### 검증

```powershell
cd backend
.\gradlew.bat clean :app:bootJar test
```

- 결과: 성공

```powershell
cd backend
.\gradlew.bat :jar
```

- 결과: 실패
- 의미: root project의 `jar` task가 제거되어 더 이상 root 빈 jar를 새로 생성하지 않음

### 참고

- `.\gradlew.bat jar`는 Gradle의 task name expansion으로 하위 모듈들의 `jar` task를 실행할 수 있다.
- root project 자체의 jar 제거 여부는 `.\gradlew.bat :jar`로 확인했다.
