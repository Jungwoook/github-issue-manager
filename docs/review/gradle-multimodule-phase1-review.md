[변경 목적]
문서 13의 모듈러 모놀리스 전환 순서에 따라 backend를 Gradle 멀티 모듈 구조로 전환하는 1차 기반을 만든다.

[핵심 변경]
- `backend`를 단일 Spring Boot 프로젝트에서 Gradle multi-project 구조로 전환
- `app`, `platform`, `shared-kernel`, `connection`, `repository`, `issue`, `comment` 모듈 등록
- 기존 Spring Boot 실행 코드, controller, service, domain, repository, DTO, exception, resource는 `app` 모듈로 이동
- 기존 `core`, `github`, `gitlab` 패키지는 `platform` 모듈로 이동
- GitLab API client/gateway 테스트는 `platform` 모듈 테스트로 이동
- 배포 workflow는 `:app:bootJar`와 `backend/app/build/libs` 기준으로 조정
- backend README의 로컬 실행 명령을 `./gradlew :app:bootRun`으로 수정
- 문서 13에 1차 적용 상태와 남은 이동 범위 기록

[현재 전환 범위]
- 완료: Gradle 멀티 모듈 골격
- 완료: `platform` 물리 모듈 분리
- 완료: `app` 실행 모듈 분리
- 보류: `connection`, `repository`, `issue`, `comment` 업무 코드 물리 이동
- 보류 이유: public API facade와 entity 경계가 아직 정리되지 않아 바로 분리하면 순환 의존과 JPA scan 리스크가 큼

[리뷰 대상 - 중요]
"이 파일들의 diff를 중심으로 리뷰"
- `backend/settings.gradle`
  - 실제 Gradle 모듈 등록 범위 확인
- `backend/build.gradle`
  - root project / subproject 공통 설정, Spring Boot BOM import 방식 확인
- `backend/app/build.gradle`
  - 실행 모듈 의존성과 Spring Boot plugin 적용 확인
- `backend/platform/build.gradle`
  - platform 모듈의 `java-library` 구성과 dependency 노출 범위 확인
- `.github/workflows/deploy.yml`
  - 멀티 모듈 전환 이후 boot jar 빌드/복사 경로 확인

[리뷰 대상 - 이동 확인]
"파일 이동이 의도와 맞는지 확인"
- `backend/app/src/main/java/com/jw/github_issue_manager/**`
  - 실행 코드와 기존 업무 코드가 `app` 모듈에 남아 있는지 확인
- `backend/platform/src/main/java/com/jw/github_issue_manager/core/**`
  - 플랫폼 공통 gateway / remote DTO가 `platform` 모듈에 있는지 확인
- `backend/platform/src/main/java/com/jw/github_issue_manager/github/**`
  - GitHub adapter가 `platform` 모듈에 있는지 확인
- `backend/platform/src/main/java/com/jw/github_issue_manager/gitlab/**`
  - GitLab adapter가 `platform` 모듈에 있는지 확인
- `backend/platform/src/test/java/com/jw/github_issue_manager/gitlab/**`
  - GitLab platform 테스트가 platform 모듈에서 실행되는지 확인

[참고 파일]
"필요할 때만 참고"
- `docs/13-platform-modularization-design.md`
  - 이번 1차 전환이 문서의 단계와 일치하는지 확인
- `docs/task/260421.md`
  - 작업 기록과 실제 변경 범위 일치 여부 확인
- `backend/README.md`
  - 사용자 실행 명령 변경 확인

[리뷰 포인트]
1. Gradle 모듈 경계
   - `app`이 `platform`에 의존하고, `platform`은 독립 jar로 빌드되는지
   - 빈 업무 모듈이 현재 빌드에 불필요한 실패를 만들지 않는지
   - root `test`가 모든 하위 모듈 테스트를 실행하는지
2. dependency 설정
   - Spring Boot BOM이 모든 하위 모듈에 적용되는지
   - `platform` 모듈이 필요한 Spring Web / RestClient / Jackson 의존성을 얻는지
   - `app` 모듈이 JPA, validation, webmvc, H2, test 의존성을 유지하는지
3. Spring Boot 실행 구조
   - main class가 `app` 모듈에 있고 `:app:bootRun`, `:app:bootJar` 기준으로 동작하는지
   - `ConfigurationPropertiesScan`이 `platform` 모듈의 GitHub/GitLab properties까지 스캔하는지
   - component scan이 `platform` 모듈의 gateway/client bean까지 포함하는지
4. CI/CD 경로
   - CI의 `./gradlew test`가 멀티 모듈에서도 정상 동작하는지
   - deploy의 jar 탐색 경로가 `app/build/libs`로 바뀐 상태에서 `backend/build/libs/app.jar` 산출물을 다시 만드는지
   - artifact/scp 경로가 기존 운영 흐름과 호환되는지
5. 다음 단계 확장성
   - `connection/repository/issue/comment` 모듈이 빈 골격으로 존재해 다음 public API 분리 작업의 대상이 명확한지
   - 업무 코드를 `app`에 남긴 결정이 문서 13의 "1차 적용 상태"와 일치하는지

[계약 변경]
- 외부 HTTP API 계약 변경 없음
- 내부 Java package 이름 변경 없음
- Gradle task 사용 방식 변경
  - 로컬 실행: `./gradlew :app:bootRun`
  - boot jar 생성: `./gradlew :app:bootJar`
  - 전체 테스트: `./gradlew test`
- 배포 workflow 내부 jar 빌드 경로 변경
  - 생성 위치: `backend/app/build/libs/*.jar`
  - 배포 호환 산출물: `backend/build/libs/app.jar`

[잠재 리스크]
- 빌드/배포 리스크
  - 루트 `bootJar` task가 더 이상 직접 실행 대상이 아니므로 외부 스크립트가 `./gradlew bootJar`를 호출하면 실패 가능
  - 배포 workflow는 수정됐지만, 다른 수동 배포 문서나 운영 스크립트가 `backend/build/libs/*.jar` 생성을 직접 기대할 수 있음
  - 빈 하위 모듈이 CI 시간과 로그를 늘릴 수 있음
- Spring scan 리스크
  - `platform` 모듈이 다른 jar로 분리되면서 component scan 범위에서 빠지면 gateway/client bean 누락 가능
  - `ConfigurationPropertiesScan`이 모듈 jar의 properties record를 스캔하지 못하면 GitHub/GitLab 설정 주입 실패 가능
  - 현재 테스트는 통과했지만, 운영 profile에서 별도 component scan 조건이 있으면 추가 확인 필요
- 구조 리스크
  - `connection/repository/issue/comment`는 아직 빈 모듈이라 "완전한 업무 모듈 분리"로 오해할 가능성
  - `app` 모듈이 여전히 대부분의 domain/service/repository를 소유하므로 다음 단계에서 public API facade 분리가 필요
  - 현재는 `app -> platform`만 실제 의존하므로 문서의 최종 의존 그래프와는 아직 차이가 있음
- 리뷰 리스크
  - 파일 이동량이 많아 GitHub diff가 삭제/추가로 보일 수 있음
  - 리뷰 시 로직 변경보다 "이동 위치와 Gradle 경계" 확인에 집중해야 함

[검증]
- 실행 명령
```powershell
cd backend
.\gradlew.bat clean :app:bootJar test
```
- 결과
  - 성공
  - `backend/app/build/libs/app-0.0.1-SNAPSHOT.jar` 생성 확인
  - `backend/platform/build/libs/platform-0.0.1-SNAPSHOT.jar` 생성 확인

[diff]
```gradle
// backend/settings.gradle
// 설명: backend를 Gradle multi-project로 전환하고 목표 모듈을 등록
 rootProject.name = 'github-issue-manager'
+
+include 'app'
+include 'shared-kernel'
+include 'platform'
+include 'connection'
+include 'repository'
+include 'issue'
+include 'comment'
```

```gradle
// backend/build.gradle
// 설명: root는 공통 plugin/BOM/테스트 설정만 담당하고, 실행 모듈은 app으로 이동
- id 'org.springframework.boot' version '4.0.4'
- id 'io.spring.dependency-management' version '1.1.7'
+id 'org.springframework.boot' version '4.0.4' apply false
+id 'io.spring.dependency-management' version '1.1.7' apply false

+subprojects {
+    apply plugin: 'java'
+    apply plugin: 'io.spring.dependency-management'
+
+    dependencyManagement {
+        imports {
+            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
+        }
+    }
+}
```

```gradle
// backend/app/build.gradle
// 설명: Spring Boot 실행 모듈. 기존 application/service/domain/controller/resource는 app에 위치
plugins {
    id 'org.springframework.boot'
}

dependencies {
    implementation project(':platform')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-webmvc'
    runtimeOnly 'com.h2database:h2'
}
```

```gradle
// backend/platform/build.gradle
// 설명: 플랫폼 공통 port/remote DTO와 GitHub/GitLab adapter를 담는 library 모듈
plugins {
    id 'java-library'
}

dependencies {
    api 'org.springframework.boot:spring-boot-starter-webmvc'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.boot:spring-boot-webmvc-test'
}
```

```yaml
# .github/workflows/deploy.yml
# 설명: 멀티 모듈 전환 후 app 모듈 bootJar를 빌드하고 기존 배포 경로로 복사
- run: ./gradlew clean bootJar
+ run: ./gradlew clean :app:bootJar

- JAR_PATH="$(find build/libs -maxdepth 1 -type f -name '*.jar' | head -n 1)"
+ JAR_PATH="$(find app/build/libs -maxdepth 1 -type f -name '*.jar' | head -n 1)"

+ mkdir -p build/libs
  cp "$JAR_PATH" "build/libs/${JAR_NAME}"
```
