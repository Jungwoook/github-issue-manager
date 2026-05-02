# Use Case Sequence Diagrams

## 1. 개요

이 문서는 현재 구현 기준의 주요 유스케이스 흐름을 시퀀스 다이어그램으로 정리한다.

공통 기준은 다음과 같다.

- App: Spring MVC controller
- Application: use case orchestration 계층
- Connection: 세션, 플랫폼 연결, token access
- Platform: `PlatformCredentialFacade`, `PlatformGatewayResolver`, GitHub/GitLab gateway
- Repository / Issue / Comment: 로컬 cache 소유 모듈
- SyncState: application 모듈 내부 sync 상태 저장소

## UC-01 플랫폼 토큰 등록

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Platform
    participant Connection
    participant DB
    participant Remote

    User->>Frontend: 플랫폼/PAT 입력
    Frontend->>App: POST /api/platforms/{platform}/token
    App->>Application: registerPlatformToken(...)
    Application->>Platform: validateCredential(platform, token, baseUrl)
    Platform->>Remote: 현재 사용자 조회
    Remote-->>Platform: 사용자 프로필
    Platform-->>Application: 검증 결과
    Application->>Connection: registerPlatformToken(...)
    Connection->>DB: 사용자/플랫폼 연결 저장, PAT 암호화
    Connection->>Connection: 세션 currentUserId/currentPlatform 설정
    Connection-->>Application: MeResponse
    Application-->>App: MeResponse
    App-->>Frontend: 현재 사용자 정보
```

## UC-02 토큰 상태 / 현재 사용자 / 연결 종료

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant DB

    User->>Frontend: 연결 상태 또는 현재 사용자 확인
    Frontend->>App: GET /api/platforms/{platform}/token/status 또는 GET /api/me
    App->>Application: 상태 조회 use case
    Application->>Connection: 세션 기준 연결 조회
    Connection->>DB: platform_connections 조회
    DB-->>Connection: 연결 정보
    Connection-->>Application: 상태/현재 사용자 응답
    Application-->>App: 응답
    App-->>Frontend: 응답

    User->>Frontend: 연결 해제 또는 로그아웃
    Frontend->>App: DELETE /token 또는 POST /api/auth/logout
    App->>Application: 종료 use case
    Application->>Connection: token 제거 또는 session invalidate
    Connection-->>Application: 완료
    Application-->>App: 완료
    App-->>Frontend: 204 No Content
```

## UC-06 저장소 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Platform
    participant Repository
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 저장소 새로고침 클릭
    Frontend->>App: POST /api/platforms/{platform}/repositories/refresh
    App->>Application: refreshRepositories(platform, session)
    Application->>Connection: requireCurrentConnection + requireTokenAccess
    Connection-->>Application: accountLogin/token/baseUrl
    Application->>Platform: getAccessibleRepositories(token, baseUrl)
    Platform->>Remote: 저장소 목록 조회
    Remote-->>Platform: 원격 저장소 목록
    Platform-->>Application: RemoteRepository 목록
    Application->>Repository: upsertRepositories(platform, accountLogin, remote list)
    Repository->>DB: repository_caches upsert
    Repository-->>Application: RepositoryResponse 목록
    Application->>SyncState: recordSuccess(REPOSITORY_LIST)
    SyncState->>DB: sync_states 저장
    Application-->>App: 저장소 목록
    App-->>Frontend: 저장소 목록
```

## UC-07 저장소 조회 / UC-09 저장소 동기화 상태 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant SyncState
    participant DB

    User->>Frontend: 저장소 목록 또는 상세 조회
    Frontend->>App: GET /api/platforms/{platform}/repositories...
    App->>Application: getRepositories 또는 getRepository
    Application->>Connection: requireCurrentConnection
    Connection-->>Application: accountLogin
    Application->>Repository: cache 조회 또는 접근 확인
    Repository->>DB: repository_caches 조회
    DB-->>Repository: 저장소 캐시
    Repository-->>Application: RepositoryResponse
    Application-->>App: 응답
    App-->>Frontend: 응답

    User->>Frontend: 저장소 동기화 상태 조회
    Frontend->>App: GET /repositories/{repositoryId}/sync-state
    App->>Application: getRepositorySyncState(...)
    Application->>Repository: requireAccessibleRepository(...)
    Application->>SyncState: getSyncState(REPOSITORY, key)
    SyncState->>DB: sync_states 조회
    DB-->>SyncState: 동기화 상태
    SyncState-->>Application: SyncStateResponse
    Application-->>App: SyncStateResponse
    App-->>Frontend: SyncStateResponse
```

## UC-10 이슈 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Platform
    participant Issue
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 이슈 새로고침 클릭
    Frontend->>App: POST /api/platforms/{platform}/repositories/{repositoryId}/issues/refresh
    App->>Application: refreshIssues(platform, repositoryId, session)
    Application->>Repository: requireAccessibleRepository(...)
    Repository-->>Application: RepositoryAccess
    Application->>Connection: requireTokenAccess(platform, session)
    Connection-->>Application: token/baseUrl
    Application->>Platform: getRepositoryIssues(token, baseUrl, owner, name)
    Platform->>Remote: 이슈 목록 조회
    Remote-->>Platform: 원격 이슈 목록
    Platform-->>Application: RemoteIssue 목록
    Application->>Issue: upsertIssues(platform, repositoryId, remote list)
    Issue->>DB: issue_caches upsert
    Issue-->>Application: IssueSummaryResponse 목록
    Application->>SyncState: recordSuccess(REPOSITORY)
    SyncState->>DB: sync_states 저장
    Application-->>App: 이슈 목록
    App-->>Frontend: 이슈 목록
```

## UC-11~16 이슈 조회 / 생성 / 수정 / 닫기 / 동기화 상태

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Platform
    participant Issue
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 이슈 조회
    Frontend->>App: GET /repositories/{repositoryId}/issues...
    App->>Application: getIssues 또는 getIssue
    Application->>Repository: requireAccessibleRepository(...)
    Application->>Issue: cache 조회
    Issue->>DB: issue_caches 조회
    Issue-->>Application: 이슈 응답
    Application-->>App: 이슈 응답
    App-->>Frontend: 이슈 응답

    User->>Frontend: 이슈 생성/수정/닫기
    Frontend->>App: POST/PATCH/DELETE issue API
    App->>Application: issue 변경 use case
    Application->>Repository: requireAccessibleRepository(...)
    Application->>Issue: 필요 시 현재 이슈 cache 조회
    Application->>Connection: requireTokenAccess(platform, session)
    Connection-->>Application: token/baseUrl
    Application->>Platform: createIssue/updateIssue(...)
    Platform->>Remote: 원격 이슈 변경
    Remote-->>Platform: 변경된 이슈
    Platform-->>Application: RemoteIssue
    Application->>Issue: upsertIssue(...)
    Issue->>DB: issue_caches upsert
    Issue-->>Application: IssueDetailResponse
    Application->>SyncState: recordSuccess(ISSUE)
    SyncState->>DB: sync_states 저장
    Application-->>App: 응답 또는 204
    App-->>Frontend: 응답

    User->>Frontend: 이슈 동기화 상태 조회
    Frontend->>App: GET /issues/{issueNumberOrKey}/sync-state
    App->>Application: getIssueSyncState(...)
    Application->>Repository: requireAccessibleRepository(...)
    Application->>Issue: requireIssue(...)
    Application->>SyncState: getSyncState(ISSUE, key)
    SyncState-->>Application: SyncStateResponse
    Application-->>App: SyncStateResponse
    App-->>Frontend: SyncStateResponse
```

## UC-17~19 댓글 새로고침 / 조회 / 작성

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Issue
    participant Platform
    participant Comment
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 댓글 조회
    Frontend->>App: GET /issues/{issueNumberOrKey}/comments
    App->>Application: getComments(...)
    Application->>Issue: requireIssue(...)
    Application->>Comment: getComments(platform, issueExternalId)
    Comment->>DB: comment_caches 조회
    Comment-->>Application: CommentResponse 목록
    Application-->>App: 댓글 목록
    App-->>Frontend: 댓글 목록

    User->>Frontend: 댓글 새로고침 또는 작성
    Frontend->>App: POST comments/refresh 또는 POST comments
    App->>Application: comment 변경 use case
    Application->>Repository: requireAccessibleRepository(...)
    Application->>Issue: requireIssue(...)
    Application->>Connection: requireTokenAccess(platform, session)
    Connection-->>Application: token/baseUrl
    Application->>Platform: getIssueComments 또는 createComment
    Platform->>Remote: 원격 댓글 API 호출
    Remote-->>Platform: 원격 댓글 결과
    Platform-->>Application: RemoteComment 결과
    Application->>Comment: upsertComments 또는 upsertComment
    Comment->>DB: comment_caches upsert
    Comment-->>Application: CommentResponse
    Application->>SyncState: recordSuccess(COMMENT_LIST)
    SyncState->>DB: sync_states 저장
    Application-->>App: 응답
    App-->>Frontend: 응답
```
