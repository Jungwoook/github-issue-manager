# Platform Abstraction Interface Draft

## Summary

- 목표: GitHub 전용 구조를 `공통 코어 + GitHub 어댑터` 구조로 분리한다.
- 1차 범위: 백엔드에서 `PlatformGateway`, `PlatformType`, `Remote*` DTO 같은 공통 인터페이스를 먼저 도입한다.
- 원칙: 기존 GitHub 기능은 유지하고, 서비스 레이어가 GitHub 구현체 대신 공통 포트에 의존하도록 바꾼다.
- 데이터 모델: `githubRepositoryId`, `githubIssueId`, `GitHubAccount` 같은 GitHub 전용 식별자를 장기적으로 `platform + externalId` 구조로 일반화한다.
- 프론트 방향: 프론트는 이번 단계에서 부분 수정하지 않고, 백엔드 공통 모듈 분리 리팩토링이 모두 완료된 이후 별도 작업으로 진행한다.

## 1. 개요

현재 프로젝트는 GitHub 중심 구조로 구현되어 있다.

- 인증 연결이 `GitHubAccount` 중심이다.
- 원격 API 연동이 `GitHubApiClient` 중심이다.
- 캐시 식별자가 `githubRepositoryId`, `githubIssueId`처럼 GitHub 전용 필드명에 묶여 있다.
- 프론트 설정 화면도 `GitHubTokenPage`, `githubTokenApi`처럼 GitHub 전용 명칭을 사용한다.

다른 플랫폼을 추가하려면 GitHub 구현을 제거하는 것이 아니라, GitHub를 첫 번째 플랫폼 어댑터로 재배치해야 한다.

## 2. 목표

- GitHub 기능은 그대로 유지한다.
- 서비스 레이어가 GitHub 구체 타입 대신 공통 인터페이스에 의존하도록 바꾼다.
- 향후 GitLab, Jira 같은 플랫폼을 추가할 수 있는 최소 공통 모델을 만든다.
- 초기 단계에서는 API 스펙을 크게 깨지 않고 내부 구조부터 분리한다.

## 3. 리팩토링 원칙

### 3.1 공통 코어와 플랫폼 어댑터 분리

- `core`: 플랫폼과 무관한 도메인 흐름
- `platform`: 플랫폼별 구현
- `github`: GitHub 전용 구현체

### 3.2 서비스는 공통 포트만 사용

- `IssueService`, `RepositoryService`, `AuthService`는 GitHub 타입을 직접 알지 않는다.
- 어떤 플랫폼을 쓸지는 `PlatformGatewayResolver` 같은 선택 컴포넌트가 결정한다.

### 3.3 식별자는 내부 ID와 외부 ID를 분리

- 내부 DB 기본키와 외부 플랫폼 ID를 구분한다.
- 외부 리소스 식별에는 `platform + externalId` 조합을 사용한다.

## 4. 제안 패키지 구조

```text
backend/src/main/java/com/jw/github_issue_manager
  core
    platform
      PlatformType.java
      PlatformGateway.java
      PlatformGatewayResolver.java
      PlatformConnectionService.java
    remote
      RemoteUserProfile.java
      RemoteRepository.java
      RemoteIssue.java
      RemoteComment.java
    connection
      PlatformConnection.java
    resource
      ExternalResourceRef.java
  github
    GithubPlatformGateway.java
    GithubRestClient.java
    GithubProperties.java
    GithubMapper.java
```

초기 단계에서는 기존 `github` 패키지를 유지한 채, 그 앞에 `core.platform` 인터페이스를 두는 방식이 가장 안전하다.

## 5. 공통 타입 초안

### 5.1 플랫폼 종류

```java
public enum PlatformType {
    GITHUB,
    GITLAB,
    JIRA
}
```

### 5.2 외부 리소스 참조

```java
public record ExternalResourceRef(
    PlatformType platform,
    String externalId,
    String externalKey
) {
}
```

- `externalId`: 플랫폼 고유 ID
- `externalKey`: 사용자가 식별하는 번호나 키

### 5.3 원격 사용자 프로필

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

### 5.4 원격 저장소

```java
public record RemoteRepository(
    PlatformType platform,
    String externalId,
    String ownerKey,
    String name,
    String fullName,
    String description,
    boolean isPrivate,
    String webUrl,
    String defaultBranch,
    java.time.LocalDateTime pushedAt
) {
}
```

### 5.5 원격 이슈

```java
public record RemoteIssue(
    PlatformType platform,
    String externalId,
    String repositoryExternalId,
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

### 5.6 원격 댓글

```java
public record RemoteComment(
    PlatformType platform,
    String externalId,
    String issueExternalId,
    String authorLogin,
    String body,
    java.time.LocalDateTime createdAt,
    java.time.LocalDateTime updatedAt
) {
}
```

## 6. 플랫폼 게이트웨이 인터페이스 초안

```java
public interface PlatformGateway {

    PlatformType getPlatformType();

    RemoteUserProfile getAuthenticatedUser(String accessToken);

    java.util.List<RemoteRepository> getAccessibleRepositories(String accessToken);

    java.util.List<RemoteIssue> getRepositoryIssues(
        String accessToken,
        String ownerKey,
        String repositoryName
    );

    RemoteIssue createIssue(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String title,
        String body
    );

    RemoteIssue updateIssue(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String title,
        String body,
        String state
    );

    java.util.List<RemoteComment> getIssueComments(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String issueKey
    );

    RemoteComment createComment(
        String accessToken,
        String ownerKey,
        String repositoryName,
        String issueKey,
        String body
    );
}
```

초기 버전에서는 메서드 파라미터를 현재 GitHub 구조에 맞춰 `ownerKey`, `repositoryName`, `issueKey` 형태로 유지하는 것이 현실적이다.

## 7. 플랫폼 선택 인터페이스 초안

```java
public interface PlatformGatewayResolver {

    PlatformGateway getGateway(PlatformType platform);
}
```

현재는 `GITHUB -> GithubPlatformGateway` 하나만 있어도 충분하다.

## 8. 연결 관리 인터페이스 초안

```java
public interface PlatformConnectionService {

    PlatformConnection connect(PlatformType platform, String accessToken);

    PlatformConnection requireConnection(PlatformType platform, jakarta.servlet.http.HttpSession session);

    String requireAccessToken(PlatformType platform, jakarta.servlet.http.HttpSession session);

    void disconnect(PlatformType platform, jakarta.servlet.http.HttpSession session);
}
```

### 공통 연결 엔티티 초안

```java
public class PlatformConnection {

    private Long id;
    private User user;
    private PlatformType platform;
    private String externalUserId;
    private String accountLogin;
    private String avatarUrl;
    private String accessTokenEncrypted;
    private String tokenScopes;
    private java.time.LocalDateTime tokenVerifiedAt;
    private java.time.LocalDateTime connectedAt;
    private java.time.LocalDateTime lastAuthenticatedAt;
}
```

## 9. 현재 클래스와 목표 인터페이스 매핑

### 9.1 인증

- 현재: `AuthService`, `GitHubAccount`, `GitHubAccountRepository`
- 목표: `PlatformConnectionService`, `PlatformConnection`, `PlatformConnectionRepository`

### 9.2 원격 API

- 현재: `GitHubApiClient`, `DefaultGitHubApiClient`
- 목표: `PlatformGateway`, `GithubPlatformGateway`

### 9.3 저장소 캐시

- 현재: `RepositoryCache.githubRepositoryId`
- 목표: `RepositoryCache.platform`, `RepositoryCache.externalId`

### 9.4 이슈 캐시

- 현재: `IssueCache.githubIssueId`, `IssueCache.githubRepositoryId`
- 목표: `IssueCache.platform`, `IssueCache.externalId`, `IssueCache.repositoryExternalId`

### 9.5 댓글 캐시

- 현재: GitHub 댓글 중심 캐시 구조
- 목표: `CommentCache.platform`, `CommentCache.externalId`, `CommentCache.issueExternalId`

## 10. 엔티티 필드 전환 초안

### 10.1 RepositoryCache

- 현재: `githubRepositoryId`, `ownerLogin`
- 제안: `platform`, `externalId`, `ownerKey`

### 10.2 IssueCache

- 현재: `githubIssueId`, `githubRepositoryId`, `number`
- 제안: `platform`, `externalId`, `repositoryExternalId`, `numberOrKey`

### 10.3 GitHubAccount

- 현재: `githubUserId`, `login`
- 제안: `platform`, `externalUserId`, `accountLogin`

## 11. 서비스 레이어 적용 방식

### 11.1 RepositoryService 초안

```java
PlatformType platform = PlatformType.GITHUB;
PlatformGateway gateway = platformGatewayResolver.getGateway(platform);
String accessToken = platformConnectionService.requireAccessToken(platform, session);
List<RemoteRepository> repositories = gateway.getAccessibleRepositories(accessToken);
```

### 11.2 IssueService 초안

```java
RepositoryCache repository = requireAccessibleRepository(repositoryId, session);
PlatformGateway gateway = platformGatewayResolver.getGateway(repository.getPlatform());
String accessToken = platformConnectionService.requireAccessToken(repository.getPlatform(), session);

List<RemoteIssue> issues = gateway.getRepositoryIssues(
    accessToken,
    repository.getOwnerKey(),
    repository.getName()
);
```

핵심은 서비스가 더 이상 `GitHubApiClient`, `GitHubIssueInfo`, `GitHubRepositoryInfo`를 직접 모르도록 만드는 것이다.

## 12. 프론트 리팩토링 방침

현재 프론트는 다음처럼 GitHub 전용 구조를 가진다.

- `entities/github/api/githubTokenApi.ts`
- `pages/settings/GitHubTokenPage.tsx`
- `widgets/github-token/*`
- `queryKeys.githubTokenStatus`

프론트는 이번 공통 모듈 분리 단계에서 함께 수정하지 않는다.

- 백엔드 공통 인터페이스와 데이터 모델 분리가 먼저 끝나야 한다.
- 중간 단계에서 프론트까지 같이 건드리면 API 명세와 화면 구조가 반복 변경될 가능성이 크다.
- 따라서 프론트는 백엔드 구조가 안정화된 이후 별도 작업으로 진행한다.

## 13. 1차 리팩토링 범위

1차에서는 백엔드 서비스 레이어의 의존성 분리까지 진행한다.

1. `PlatformType`, `Remote*` DTO, `PlatformGateway` 도입
2. `GitHubApiClient` 구현을 `PlatformGateway` 구현체로 감싸기
3. `AuthService`, `RepositoryService`, `IssueService`가 공통 포트를 사용하도록 변경

이 단계에서는 DB 테이블명과 API 경로를 모두 바꾸지 않아도 된다.
프론트는 이 단계에 포함하지 않는다.

## 14. 2차 리팩토링 범위

2차에서는 백엔드 데이터 모델까지 일반화한다.

1. `GitHubAccount` -> `PlatformConnection`
2. `githubRepositoryId` -> `externalId`
3. `githubIssueId` -> `externalId`
4. 캐시 엔티티에 `platform` 필드 추가
5. 리포지토리 메서드를 `findByPlatformAndExternalId` 형태로 전환

이 단계부터는 마이그레이션 스크립트와 API 응답 필드명 정리가 필요하다.
프론트는 여전히 범위에 포함하지 않는다.

## 15. 프론트 후속 리팩토링 단계

프론트는 백엔드 구조 정리가 끝난 뒤 별도 작업으로 진행한다.

1. 설정 화면을 공통 플랫폼 연결 구조로 전환
2. GitHub 전용 API 모듈을 플랫폼 연결 모듈로 재구성
3. 쿼리 키, 라우트, 화면 네이밍을 공통 구조로 일괄 정리

즉, 프론트 리팩토링은 2차 백엔드 리팩토링 완료 후 후속 단계로 분리한다.

## 16. 현재 코드 기준 추천 시작점

현재 코드 기준으로 가장 좋은 시작점은 아래 순서다.

1. `GitHubApiClient`를 대체하지 말고, 그 앞에 `PlatformGateway` 인터페이스를 추가한다.
2. `GitHubIssueInfo`, `GitHubRepositoryInfo`, `GitHubCommentInfo`, `GitHubUserProfile`를 `Remote*` DTO로 변환하는 매퍼를 만든다.
3. `RepositoryService`, `IssueService`, `AuthService`의 의존성을 GitHub 타입에서 공통 포트로 바꾼다.
4. 구조가 안정화되면 백엔드 엔티티와 API 이름을 일반화한다.
5. 백엔드 구조 정리가 끝난 뒤 프론트 공통화 작업을 별도 단계로 진행한다.

이 순서로 가면 GitHub 기능을 유지하면서도 다중 플랫폼 확장을 위한 기반을 가장 낮은 리스크로 만들 수 있다.
