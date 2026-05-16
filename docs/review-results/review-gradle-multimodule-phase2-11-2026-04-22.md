# Code Review: Gradle 멀티 모듈 2차~11차 전환
날짜: 2026-04-22
참조: docs/reviews/gradle-multimodule-phase2-11-review.md

---

## 1. [런타임 실패 위험] `SharedKernelConfig` — `@EnableJpaRepositories` scan base가 `com.jw.github_issue_manager.repository` 전체

**파일**: `backend/shared-kernel/src/main/java/com/jw/github_issue_manager/shared/config/SharedKernelConfig.java:13-19`

```java
@EnableJpaRepositories(
    basePackageClasses = SyncStateRepository.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.jw\\.github_issue_manager\\.repository\\.internal\\..*"
    )
)
```

`SyncStateRepository`가 `com.jw.github_issue_manager.repository` 패키지에 있어 `basePackageClasses`로 지정하면 이 패키지 전체가 scan 기반이 된다. `RepositoryModuleConfig`도 같은 root 하위 패키지(`*.internal.repository`)를 `@EnableJpaRepositories`로 독립 등록한다.

`excludeFilters`의 `FilterType.REGEX`가 정상 동작할 경우 `RepositoryCacheRepository`는 제외되고 중복 등록은 없다. 그러나 Spring Data JPA의 `@EnableJpaRepositories`는 `@ComponentScan`과 달리 repository interface detection 경로가 다르며, REGEX exclude filter가 fully qualified name 기준으로 동작하는지 버전별 차이가 있다. Spring Boot JPA autoconfiguration이 back-off된 상태에서 두 config가 동일 EntityManagerFactory를 공유하며 각각 factory를 초기화하려 할 때 충돌이 발생할 수 있다.

리뷰 문서 [잠재 리스크] 항목에서도 인정한 위험이다. 향후 `com.jw.github_issue_manager.repository.*`에 새 repository가 추가되면 `SharedKernelConfig`가 의도치 않게 등록할 수 있다.

---

## 2. [런타임 실패 위험] 5개 모듈 config에 분산된 `@EntityScan` — Spring Boot 4.x merge 보장 불명확

**파일**: `SharedKernelConfig.java`, `ConnectionModuleConfig.java`, `RepositoryModuleConfig.java`, `IssueModuleConfig.java`, `CommentModuleConfig.java`

각 모듈 config가 독립적으로 `@EntityScan`을 선언한다:

```java
// SharedKernelConfig
@EntityScan(basePackageClasses = SyncState.class)

// ConnectionModuleConfig
@EntityScan(basePackageClasses = {PlatformConnection.class, User.class})

// RepositoryModuleConfig
@EntityScan(basePackageClasses = RepositoryCache.class)
...
```

Spring Boot의 `@EntityScan`은 `EntityScanRegistrar`가 처리하는데, 여러 `@Configuration`에 분산된 경우 Spring Boot가 이를 merge하여 단일 `LocalContainerEntityManagerFactoryBean`에 등록한다. Spring Boot 4.x에서 이 merge가 보장되는지는 공식 문서에 명시되지 않는다.

만약 merge 대신 마지막으로 처리된 `@EntityScan`만 적용되면 나머지 모듈의 entity가 JPA context에서 누락된다. 이 경우 `EntityNotFoundException`이나 schema mismatch로 startup 또는 런타임에 실패한다.

단일 `@EntityScan`으로 통합하거나, `@SpringBootApplication(scanBasePackages = ...)` 방식으로 app 모듈에서 통합 entity scan을 제어하는 것이 안전하다.

---

## 3. [테스트 경계 누락] `ModuleBoundaryTest` — shared-kernel 내부 패키지가 boundary 검사 대상 외

**파일**: `backend/app/src/test/java/com/jw/github_issue_manager/ModuleBoundaryTest.java:30-35`

```java
private static final Map<String, String> MODULE_API_PREFIXES = Map.of(
    "comment", "com.jw.github_issue_manager.comment.api.",
    "connection", "com.jw.github_issue_manager.connection.api.",
    "issue", "com.jw.github_issue_manager.issue.api.",
    "repository", "com.jw.github_issue_manager.repository.api."
);
```

`publicApiPackagesOnlyUseTheirOwnInternalImplementation` 테스트는 4개 업무 모듈만 검사하고 shared-kernel을 제외한다. 그러나 `SyncStateService`는 `com.jw.github_issue_manager.service` 패키지에, `SyncStateRepository`는 `com.jw.github_issue_manager.repository` 패키지에 있어 `*.internal.*` 패턴으로 보호받지 않는다.

결과: connection, repository, issue, comment 모듈의 internal 코드가 `SyncStateService`나 `SyncStateRepository`를 직접 import해도 어떤 테스트도 이를 감지하지 못한다. `appModuleUsesOnlyPublicModuleApis`는 `app/src/main/java`만 검사하고, `publicApiPackagesOnlyUseTheirOwnInternalImplementation`은 shared-kernel을 대상에서 제외한다.

shared-kernel의 internal 클래스가 `com.jw.github_issue_manager.shared.internal.*` 패턴으로 이동하거나, `MODULE_API_PREFIXES`에 shared-kernel을 추가하고 해당 패키지 경계 검사를 보강해야 한다.

---

## 4. [API 계약 불일치] `IssueFacade.requireIssue` — `IssueAccess` 반환 타입이 `issue` 모듈 `api` 패키지에 존재하는지 불명확

**파일**: `backend/issue/src/main/java/com/jw/github_issue_manager/issue/api/IssueFacade.java:71-73`

```java
public IssueAccess requireIssue(PlatformType platform, String repositoryId, String issueNumberOrKey, HttpSession session) {
    return issueService.requireIssue(platform, repositoryId, issueNumberOrKey, session);
}
```

`IssueAccess`가 `issue.api` 패키지에 있어야 `comment` 모듈이 `issue`를 `implementation`으로 의존할 때 compile scope에서 접근 가능하다. `comment/build.gradle`이 `issue`를 `implementation`으로 선언한 이후, `comment.internal.*` 코드가 `IssueFacade.requireIssue()`를 호출하고 반환된 `IssueAccess`를 사용한다면 `IssueAccess`가 `issue.api` 패키지에 있지 않으면 컴파일 오류가 발생한다.

같은 패턴으로 `RepositoryFacade.requireAccessibleRepository()`가 반환하는 `RepositoryAccess`도 `repository.api` 패키지에 있어야 한다. 이 타입들의 실제 패키지 위치를 확인해야 한다.

---

## 치명적인 문제 없음 — Gradle scope 변경 자체는 정상

**`issue/build.gradle`**: `api project(':repository')` → `implementation project(':repository')` 전환은 `IssueFacade` public 메서드 시그니처에 `repository.api.*` 타입이 없으므로 올바른 변경이다.

**`comment/build.gradle`**: `api project(':issue')`, `api project(':shared-kernel')` → `implementation` 전환은 `CommentFacade` public 메서드가 `issue.api.*`나 `shared.api.*` 타입을 반환하지 않으므로 올바른 변경이다.

**`ModuleBoundaryTest.gradleModuleApiDependenciesExposeOnlyPublicContracts`**: 기대값과 실제 build.gradle scope가 일치한다.

---

## Codex 검증 결과

검증 일시: 2026-04-22

검증 명령:

```powershell
.\gradlew.bat clean :app:bootJar test
```

검증 결과: 성공

- app bootJar 생성 성공
- app / connection / platform 테스트 성공
- Spring context load 성공
- facade bean 등록 테스트 성공
- JPA managed entity 등록 테스트 성공
- Gradle 의존 방향과 `api` scope 경계 테스트 성공

### 1. `SharedKernelConfig` repository scan base 리스크

판정: 부분 유효

- 현재 코드 기준으로는 런타임 실패가 재현되지 않았다.
- `.\gradlew.bat clean :app:bootJar test`가 성공했고, 이전에 발생했던 `RepositoryCacheRepository` 중복 등록 문제는 현재 exclude filter로 차단된다.
- 다만 `SyncStateRepository`가 `com.jw.github_issue_manager.repository` 패키지에 있어 scan base가 넓게 잡히는 구조적 리스크는 맞다.
- 향후 `com.jw.github_issue_manager.repository.*` 하위에 새 repository가 추가되면 shared-kernel scan에 의도치 않게 포함될 수 있다.
- 결론: 즉시 수정이 필요한 현재 버그라기보다 shared-kernel 패키지 정리 시 함께 해소해야 할 설계 부채로 보는 것이 적절하다.

### 2. 분산된 `@EntityScan` merge 리스크

판정: 현재 기준 미재현

- 현재 Spring Boot 4.0.4 환경에서는 여러 module config의 `@EntityScan`이 함께 반영된다.
- `GithubIssueManagerApplicationTests.moduleEntitiesAreManagedByJpa()`가 `PlatformConnection`, `User`, `RepositoryCache`, `IssueCache`, `CommentCache`, `SyncState` 전체가 JPA metamodel에 등록되어 있음을 검증한다.
- 전체 Gradle 검증도 성공했으므로 “마지막 `@EntityScan`만 적용되어 entity가 누락된다”는 문제는 현재 코드에서는 재현되지 않는다.
- 결론: 리뷰에서 제기한 우려는 보수적인 설계 관점에서는 참고할 수 있지만, 현재 변경을 막는 결함으로 보기는 어렵다.

### 3. shared-kernel 경계 테스트 누락

판정: 유효

- `ModuleBoundaryTest`의 public API/import 경계 검사는 connection / repository / issue / comment 중심으로 구성되어 있고 shared-kernel의 구 패키지 구조는 별도 보호를 받지 않는다.
- 실제로 `repository`, `issue`, `comment` internal service가 `com.jw.github_issue_manager.service.SyncStateService`를 직접 import한다.
- 현재 구조에서는 이것이 의도된 shared-kernel 사용이지만, 패키지명만 보면 public API인지 internal 구현인지 구분되지 않는다.
- 결론: shared-kernel을 `shared.api` / `shared.internal` 계열로 정리하거나, 현재 허용되는 shared-kernel public surface를 테스트에 명시하는 후속 작업이 필요하다.

### 4. `IssueAccess` / `RepositoryAccess` API 패키지 위치

판정: 기각

- `IssueAccess`는 `com.jw.github_issue_manager.issue.api` 패키지에 존재한다.
- `RepositoryAccess`는 `com.jw.github_issue_manager.repository.api` 패키지에 존재한다.
- `comment` 모듈은 `issue`와 `repository`를 `implementation`으로 의존하며, 자기 internal 코드에서 해당 API 타입을 정상 컴파일한다.
- 전체 Gradle 검증도 성공했으므로 반환 타입 위치 문제나 compile scope 문제는 없다.

### 종합 판정

- 즉시 실패를 만드는 치명적 결함은 확인되지 않았다.
- 1번과 3번은 shared-kernel 구 패키지 구조에서 비롯된 설계 리스크로 유효하다.
- 2번은 현재 테스트와 런타임 검증 기준으로 미재현이다.
- 4번은 현재 코드 기준으로 사실과 다르다.
- 후속 우선순위는 shared-kernel 패키지 경계 정리와 그에 맞춘 boundary test 보강이 가장 높다.
