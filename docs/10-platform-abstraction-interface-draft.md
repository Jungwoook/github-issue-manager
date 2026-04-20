# Platform Abstraction Interface

## Summary

- 목표: GitHub 전용 구조를 `공통 코어 + 플랫폼 어댑터` 구조로 분리한다.
- 현재 상태: 백엔드 공통 포트, Remote DTO, GitHub/GitLab gateway, 플랫폼 공통 API 경로가 반영되어 있다.
- 현재 한계: 새 플랫폼 추가 시 공통 enum, resolver, 프론트 플랫폼 목록에 수정이 발생하므로 완전한 모듈 구조는 아니다.
- 다음 목표: 플랫폼 모듈 registry와 capability 선언 방식으로 전환해 새 플랫폼 추가 시 공통 코어 변경을 최소화한다.
- 남은 범위: 모듈화 전환, 접근 제어, unique 제약, GitLab 표시/식별자 세부 모델, 미구현 확장 기능 정리.

## 1. 개요

현재 프로젝트는 GitHub 중심 구현에서 플랫폼 어댑터 분리 구조로 전환되었다. 이 단계는 공통 포트 뒤에 GitHub/GitLab 구현을 숨긴 1차 추상화이며, 독립 모듈이나 플러그인 구조는 아직 아니다.

- 인증 연결은 `PlatformConnection` 중심이다.
- 원격 API 연동은 `PlatformGateway` 중심이다.
- 캐시 식별자는 `platform + externalId` 기준이다.
- API 경로는 `/api/platforms/{platform}/...` 기준이다.
- 프론트엔드는 플랫폼 라우트와 query key를 사용한다.
- 플랫폼 목록과 등록 방식은 아직 공통 코드에 고정되어 있다.

## 2. 완료된 목표

- GitHub 기능 유지
- GitHub API client를 gateway 뒤로 이동
- GitLab gateway 추가
- 서비스 레이어의 GitHub 직접 의존 축소
- DTO 필드명을 플랫폼 공통 이름으로 전환
- 프론트 API 호출 경로와 라우트를 플랫폼 기준으로 전환

## 3. 공통 패키지 구조

```text
backend/src/main/java/com/jw/github_issue_manager
  core
    platform
      PlatformType.java
      PlatformGateway.java
      PlatformGatewayResolver.java
      DefaultPlatformGatewayResolver.java
    remote
      RemoteUserProfile.java
      RemoteRepository.java
      RemoteIssue.java
      RemoteComment.java
  github
    GitHubPlatformGateway.java
    DefaultGitHubApiClient.java
  gitlab
    GitLabPlatformGateway.java
    DefaultGitLabApiClient.java
  domain
    PlatformConnection.java
    RepositoryCache.java
    IssueCache.java
    CommentCache.java
```

## 4. 공통 타입

### 4.1 플랫폼 종류

```java
public enum PlatformType {
    GITHUB,
    GITLAB
}
```

### 4.2 원격 사용자

```java
public record RemoteUserProfile(
    PlatformType platform,
    String externalUserId,
    String login,
    String displayName,
    String email,
    String avatarUrl
) {
}
```

### 4.3 원격 저장소

```java
public record RemoteRepository(
    PlatformType platform,
    String externalId,
    String ownerKey,
    String name,
    String fullName,
    String description,
    boolean isPrivate,
    String webUrl
) {
}
```

### 4.4 원격 이슈

```java
public record RemoteIssue(
    PlatformType platform,
    String externalId,
    String numberOrKey,
    String title,
    String body,
    String state,
    String authorLogin,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt,
    java.time.LocalDateTime closedAt
) {
}
```

### 4.5 원격 댓글

```java
public record RemoteComment(
    PlatformType platform,
    String externalId,
    String authorLogin,
    String body,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
}
```

## 5. PlatformGateway

```java
public interface PlatformGateway {

    PlatformType getPlatformType();

    RemoteUserProfile getAuthenticatedUser(String accessToken, String baseUrl);

    java.util.List<RemoteRepository> getAccessibleRepositories(String accessToken, String baseUrl);

    java.util.List<RemoteIssue> getRepositoryIssues(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName
    );

    RemoteIssue createIssue(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String title,
        String body
    );

    RemoteIssue updateIssue(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    );

    java.util.List<RemoteComment> getIssueComments(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String issueKey
    );

    RemoteComment createComment(
        String accessToken,
        String baseUrl,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String body
    );
}
```

## 6. 현재 매핑

### GitHub

- `externalId`: GitHub numeric id
- `ownerKey`: repository owner login
- `name`: repository name
- `numberOrKey`: issue number
- `baseUrl`: 사용하지 않음

### GitLab

- `externalId`: GitLab project/issue/note id
- `ownerKey`: 현재 연결 계정 login
- `name`: `path_with_namespace`
- `fullName`: `path_with_namespace`
- `numberOrKey`: issue `iid`
- `baseUrl`: 연결별 GitLab API base URL

## 7. 서비스 적용 방식

- `AuthService`: 플랫폼별 현재 사용자 검증과 연결 저장
- `RepositoryService`: gateway로 저장소 목록 조회 후 캐시 갱신
- `IssueService`: gateway로 이슈 조회/생성/수정 후 캐시 갱신
- `CommentService`: gateway로 댓글 조회/작성 후 캐시 갱신

## 8. 프론트 적용 상태

- 연결 API: `entities/platform-connection/api/platformConnectionApi.ts`
- 라우트: `/settings/platforms/:platform`, `/platforms/:platform/repositories/...`
- query key: platform 인자를 포함
- legacy GitHub 라우트는 기본 플랫폼으로 redirect
- GitHub/GitLab 탭과 GitLab base URL 입력 흐름 반영

## 9. 남은 후속 범위

### 9.1 플랫폼 모듈화

- 현재 `PlatformType` enum에 플랫폼이 고정되어 있다.
- 현재 resolver는 등록된 gateway를 찾아주지만, 새 플랫폼 추가 시 공통 코드 수정이 필요하다.
- 프론트도 `SUPPORTED_PLATFORMS`, `PLATFORM_METADATA`를 직접 수정해야 한다.
- 다음 단계에서는 플랫폼 모듈이 스스로 id, 표시명, 연결 입력값, capability, gateway를 제공하도록 바꾼다.

목표 계약 예시:

```java
public interface PlatformModule {
    String id();
    String displayName();
    java.util.Set<PlatformCapability> capabilities();
    PlatformGateway gateway();
}
```

capability 예시:

```java
public enum PlatformCapability {
    REPOSITORY_LIST,
    ISSUE_LIST,
    ISSUE_MUTATION,
    COMMENT_LIST,
    COMMENT_MUTATION,
    LABEL_MANAGEMENT
}
```

목표 구조:

```text
platform-core
platform-github
platform-gitlab
platform-{new}
```

### 9.2 접근 제어

- 현재 저장소 접근 검증은 `ownerKey == accountLogin` 기준이다.
- GitHub organization, GitLab group/subgroup 저장소를 안정적으로 다루려면 접근 권한 캐시 또는 연결-저장소 관계가 필요하다.

### 9.3 연결 unique 제약

- 현재 `externalUserId`, `accountLogin` 단일 unique 제약은 self-managed GitLab에서 충돌 가능성이 있다.
- 장기적으로 `platform + baseUrl + externalUserId` 기준이 필요하다.

### 9.4 GitLab 저장소 식별자

- 현재 GitLab은 `path_with_namespace`를 API 호출용 이름으로 사용한다.
- 장기적으로 `displayName`, `pathWithNamespace`, `ownerKey`, `repositorySlug` 역할 분리가 필요하다.

### 9.5 확장 기능

- 라벨, 담당자, 우선순위, milestone, sub-issue는 공통 포트 설계가 아직 없다.
- 해당 기능은 별도 문서와 별도 API 계약으로 진행한다.

## 10. 모듈화 진행 단계

### 10.1 1단계: 현재 구조 명확화

- 문서상 현재 구조를 "완전한 모듈 구조"가 아니라 "플랫폼 어댑터 분리 단계"로 정의한다.
- 새 플랫폼 추가 시 공통 코드 수정이 발생하는 지점을 명시한다.
- GitHub/GitLab 구현은 유지하고 동작 변경은 만들지 않는다.

### 10.2 2단계: Platform registry 도입

- `PlatformType` enum 직접 의존을 축소한다.
- 플랫폼 식별자는 문자열 기반 `platformId` 또는 값 객체로 다룬다.
- Spring bean으로 등록된 `PlatformModule` 목록을 registry가 수집한다.
- resolver는 enum switch가 아니라 registry 조회로 gateway를 찾는다.

### 10.3 3단계: capability 기반 서비스 계약 분리

- `PlatformGateway` 하나가 모든 기능을 담당하는 구조를 기능별 capability로 나눈다.
- 예: repository, issue, comment, label capability
- 지원하지 않는 기능은 공통 서비스에서 명확한 오류로 처리한다.
- 새 플랫폼은 가능한 capability만 구현한다.

### 10.4 4단계: 플랫폼 metadata API 제공

- 백엔드는 등록된 플랫폼 목록, 표시명, 토큰 입력 안내, base URL 필요 여부, 지원 capability를 반환한다.
- 프론트는 이 metadata로 플랫폼 탭과 연결 폼을 구성한다.
- 새 플랫폼 추가 시 프론트 하드코딩 수정 범위를 줄인다.

### 10.5 5단계: 패키지 또는 빌드 모듈 분리

- 우선 패키지 경계를 `core`, `platform.github`, `platform.gitlab`처럼 정리한다.
- 이후 필요 시 Gradle 멀티 모듈로 분리한다.
- 최종 목표는 새 플랫폼 모듈 추가가 기존 플랫폼 구현과 공통 코어에 미치는 영향을 최소화하는 것이다.
