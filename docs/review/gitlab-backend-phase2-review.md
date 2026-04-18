[변경 목적]
GitLab 2차 연동을 위해 연결별 `baseUrl` 계약을 추가하고, GitLab 호출이 연결 설정을 실제로 사용하도록 백엔드 흐름을 확장

[핵심 변경]
- `PlatformGateway` 주요 메서드에 `baseUrl` 파라미터 추가
- `PlatformConnection`에 `baseUrl` 필드 추가
- 토큰 등록 요청/응답 DTO에 `baseUrl` 추가
- `AuthService`에 플랫폼별 기본 `baseUrl` 결정 및 저장 로직 추가
- `RepositoryService` / `IssueService` / `CommentService`가 현재 연결의 `baseUrl`을 gateway 호출에 전달하도록 변경
- `DefaultGitLabApiClient`가 고정 properties 대신 요청별 `baseUrl`을 사용하도록 변경
- 관련 테스트를 새 계약에 맞게 수정

[리뷰 대상 - 중요]
"이 파일들의 diff를 중심으로 리뷰"
- `backend/src/main/java/com/jw/github_issue_manager/service/AuthService.java`
  - 토큰 등록 시 `baseUrl` 기본값 결정, 저장, 응답 반영의 중심 파일
- `backend/src/main/java/com/jw/github_issue_manager/core/platform/PlatformGateway.java`
  - 플랫폼 공통 계약이 실제로 어떻게 바뀌었는지 보는 기준 파일
- `backend/src/main/java/com/jw/github_issue_manager/gitlab/DefaultGitLabApiClient.java`
  - 요청별 `baseUrl` 사용, URL 조합, GitLab API 호출 방식이 모인 파일

[참고 파일]
"필요할 때만 참고하고, 기본적으로는 리뷰 대상 파일에 집중"
- `backend/src/main/java/com/jw/github_issue_manager/service/RepositoryService.java`
  - 현재 연결의 `baseUrl` 전달과 접근 제어 전제를 함께 보는 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/service/IssueService.java`
  - 이슈 refresh/create/update/delete에서 `baseUrl` 전달 확인용 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/service/CommentService.java`
  - 댓글 refresh/create에서 `baseUrl` 전달 확인용 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/domain/PlatformConnection.java`
  - 엔티티 스키마 확장과 기존 데이터 호환성 확인용 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/dto/auth/RegisterPlatformTokenRequest.java`
  - 요청 계약 변경 확인용 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/dto/auth/PlatformConnectionResponse.java`
  - 연결 응답 계약 변경 확인용 참고 파일
- `backend/src/main/java/com/jw/github_issue_manager/dto/auth/PlatformTokenStatusResponse.java`
  - 토큰 상태 응답 계약 변경 확인용 참고 파일

[리뷰 포인트]
1. `baseUrl` 전달 흐름 누락 여부
   - `AuthService`에서 저장된 `baseUrl`이 `RepositoryService` / `IssueService` / `CommentService`를 거쳐 실제 gateway 호출까지 빠짐없이 전달되는지
2. URL 조합 및 정규화
   - `DefaultGitLabApiClient.resolveApiBaseUrl`, `projectApiPath`가 `/api/v4`, trailing slash, 빈 값 처리에서 안전한지
3. 플랫폼별 처리 일관성
   - `PlatformGateway` 계약 변경 이후 GitHub/GitLab 구현체가 동일한 방식으로 동작하는지
4. 기존 데이터 호환성
   - `PlatformConnection.baseUrl` 추가가 기존 연결 데이터, 운영 DB, 스키마 생성 흐름에 문제 없는지

[계약 변경]
- API request
  - `POST /api/platforms/{platform}/token` body에 `baseUrl` 선택 필드 추가
- API response
  - 플랫폼 연결 응답에 `baseUrl` 추가
  - 플랫폼 토큰 상태 응답에 `baseUrl` 추가
- Internal contract
  - `PlatformGateway` 모든 주요 메서드가 `baseUrl` 인자를 받도록 변경
  - `GitLabApiClient` 주요 메서드가 `apiBaseUrl` 인자를 받도록 변경

[잠재 리스크]
- 버그 가능성
  - `baseUrl` 정규화가 단순해서 `/api/v4` 누락, trailing slash, 잘못된 scheme 입력 시 런타임 오류 가능
  - GitHub gateway는 `baseUrl` 인자를 받지만 실제로 무시하므로 플랫폼별 처리 일관성 착각 가능
  - 세션 기반 현재 연결에서 `baseUrl`을 읽는 흐름이 누락되면 GitLab 요청 일부만 기본 URL로 돌아갈 가능성
- 누락 가능성
  - `PlatformConnection`의 `baseUrl` 컬럼 추가에 대한 마이그레이션/운영 DB 반영 누락 가능
  - 다른 테스트나 컨트롤러에서 `RegisterPlatformTokenRequest` 생성 시 새 필드 반영 누락 가능
  - self-managed GitLab 입력값 검증/에러 메시지 세부 처리 누락 가능
- 회귀 가능성
  - `PlatformGateway` 시그니처 변경으로 기존 GitHub 흐름 호출부 일부가 누락되면 컴파일 또는 런타임 회귀 가능
  - `AuthService` 응답 DTO 변경으로 프론트/테스트가 이전 필드 구조를 가정하면 회귀 가능
  - `RepositoryService` 접근 흐름은 여전히 `ownerKey == accountLogin` 전제라 GitLab group/subgroup 프로젝트에서 조회 회귀 가능

[diff]
```java
// AuthService.java
// 설명: 토큰 등록 시 요청의 `baseUrl`을 정규화하고, 연결 저장/응답 흐름 전체에 반영하도록 변경
- RemoteUserProfile userProfile = platformGatewayResolver.getGateway(platform)
-     .getAuthenticatedUser(request.accessToken());
+ String baseUrl = resolvePlatformBaseUrl(platform, request.baseUrl());
+ RemoteUserProfile userProfile = platformGatewayResolver.getGateway(platform)
+     .getAuthenticatedUser(request.accessToken(), baseUrl);
 
- .map(existing -> updateExistingConnection(platform, existing, userProfile, encryptedToken, now))
- .orElseGet(() -> createConnection(platform, userProfile, encryptedToken, now));
+ .map(existing -> updateExistingConnection(platform, existing, userProfile, encryptedToken, baseUrl, now))
+ .orElseGet(() -> createConnection(platform, userProfile, encryptedToken, baseUrl, now));
 
+ public String resolvePlatformBaseUrl(PlatformType platform, String requestedBaseUrl) {
+     if (platform == PlatformType.GITLAB) {
+         if (requestedBaseUrl == null || requestedBaseUrl.isBlank()) {
+             return "https://gitlab.com/api/v4";
+         }
+         return requestedBaseUrl.endsWith("/")
+             ? requestedBaseUrl.substring(0, requestedBaseUrl.length() - 1)
+             : requestedBaseUrl;
+     }
+     return null;
+ }
```

```java
// PlatformGateway.java
// 설명: 플랫폼 어댑터 공통 계약에 연결별 `baseUrl`을 명시적으로 전달하도록 변경
- RemoteUserProfile getAuthenticatedUser(String accessToken);
+ RemoteUserProfile getAuthenticatedUser(String accessToken, String baseUrl);
 
- List<RemoteRepository> getAccessibleRepositories(String accessToken);
+ List<RemoteRepository> getAccessibleRepositories(String accessToken, String baseUrl);
 
- List<RemoteIssue> getRepositoryIssues(String accessToken, String ownerKey, String repositoryName);
+ List<RemoteIssue> getRepositoryIssues(String accessToken, String baseUrl, String ownerKey, String repositoryName);
 
- RemoteIssue createIssue(String accessToken, String ownerKey, String repositoryName, String title, String body);
+ RemoteIssue createIssue(String accessToken, String baseUrl, String ownerKey, String repositoryName, String title, String body);
```

```java
// DefaultGitLabApiClient.java
// 설명: 고정 properties 기반 호출에서 요청별 `apiBaseUrl` 기반 호출로 전환
- String uri = UriComponentsBuilder.fromUriString(properties.apiBaseUrl() + "/projects")
-     .queryParam("membership", "true")
-     ...
-     .toUriString();
+ String uri = UriComponentsBuilder.fromUriString(resolveApiBaseUrl(apiBaseUrl) + "/projects")
+     .queryParam("membership", "true")
+     ...
+     .toUriString();
 
- private String projectApiPath(String projectPath) {
-     return properties.apiBaseUrl() + "/projects/" + encodeProjectPath(projectPath);
- }
+ private String projectApiPath(String apiBaseUrl, String projectPath) {
+     return resolveApiBaseUrl(apiBaseUrl) + "/projects/" + encodeProjectPath(projectPath);
+ }
 
+ private String resolveApiBaseUrl(String apiBaseUrl) {
+     if (apiBaseUrl == null || apiBaseUrl.isBlank()) {
+         return properties.apiBaseUrl();
+     }
+     return apiBaseUrl;
+ }
```
