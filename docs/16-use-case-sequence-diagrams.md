# Use Case Sequence Diagrams

## 1. 개요

이 문서는 `06-core-use-cases.md`의 각 유스케이스가 현재 구현에서 어떻게 동작하는지 시퀀스 다이어그램으로 정리한다.

공통 참여자는 다음과 같다.

- User: 사용자
- Frontend: React 화면과 API client
- App: Spring MVC controller
- Connection: connection 모듈
- Repository: repository 모듈
- Issue: issue 모듈
- Comment: comment 모듈
- Platform: platform facade/gateway
- DB: 로컬 DB와 캐시
- Remote: GitHub/GitLab API

## UC-01 플랫폼 토큰 등록

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Platform
    participant Connection
    participant DB
    participant Remote

    User->>Frontend: 플랫폼/PAT 입력
    Frontend->>App: POST /api/platforms/{platform}/token
    App->>Platform: validateCredential(platform, token, baseUrl)
    Platform->>Remote: 현재 사용자 조회
    Remote-->>Platform: 사용자 프로필
    Platform-->>App: 검증 결과
    App->>Connection: registerPlatformToken(platform, command, session)
    Connection->>DB: 사용자/플랫폼 연결 저장, PAT 암호화
    Connection->>Connection: 세션 currentUserId/currentPlatform 설정
    Connection-->>App: MeResponse
    App-->>Frontend: 현재 사용자 정보
```

## UC-02 토큰 상태 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Connection
    participant DB

    User->>Frontend: 연결 상태 화면 진입
    Frontend->>App: GET /api/platforms/{platform}/token/status
    App->>Connection: getPlatformTokenStatus(platform, session)
    Connection->>DB: 세션 사용자 기준 플랫폼 연결 조회
    DB-->>Connection: 연결 정보
    Connection-->>App: connected/accountLogin/baseUrl
    App-->>Frontend: 토큰 상태
```

## UC-03 현재 사용자 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Connection
    participant DB

    User->>Frontend: 앱 진입
    Frontend->>App: GET /api/me
    App->>Connection: getCurrentUser(session)
    Connection->>Connection: 세션 currentPlatform 확인
    Connection->>DB: 현재 사용자 플랫폼 연결 조회
    DB-->>Connection: 연결 계정
    Connection-->>App: MeResponse
    App-->>Frontend: 현재 사용자 정보
```

## UC-04 플랫폼 연결 해제

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Connection
    participant DB

    User->>Frontend: 연결 해제 실행
    Frontend->>App: DELETE /api/platforms/{platform}/token
    App->>Connection: disconnectPlatformToken(platform, session)
    Connection->>DB: 암호화 토큰/scope 제거
    Connection->>Connection: 현재 플랫폼이면 세션 연결 값 제거
    App-->>Frontend: 204 No Content
```

## UC-05 로그아웃

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Connection

    User->>Frontend: 로그아웃 실행
    Frontend->>App: POST /api/auth/logout
    App->>Connection: logout(session)
    Connection->>Connection: session.invalidate()
    App-->>Frontend: 204 No Content
```

## UC-06 저장소 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Repository
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 저장소 새로고침 클릭
    Frontend->>App: POST /api/platforms/{platform}/repositories/refresh
    App->>Repository: refreshRepositories(platform, session)
    Repository->>Platform: getAccessibleRepositories(platform, session)
    Platform->>Connection: requireTokenAccess(platform, session)
    Connection-->>Platform: token/baseUrl/accountLogin
    Platform->>Remote: 저장소 목록 조회
    Remote-->>Platform: 원격 저장소 목록
    Platform-->>Repository: RemoteRepository 목록
    Repository->>DB: repository_caches upsert
    Repository->>DB: sync-state 성공 기록
    Repository-->>App: 캐시 기준 저장소 목록
    App-->>Frontend: 저장소 목록
```

## UC-07 저장소 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Repository
    participant Platform
    participant DB

    User->>Frontend: 저장소 목록 화면 진입
    Frontend->>App: GET /api/platforms/{platform}/repositories
    App->>Repository: getRepositories(platform, session)
    Repository->>Platform: requireCurrentConnection(platform, session)
    Platform->>Connection: requireCurrentConnectionInfo(platform, session)
    Connection-->>Platform: accountLogin
    Platform-->>Repository: accountLogin
    Repository->>DB: ownerKey 기준 repository_caches 조회
    DB-->>Repository: 저장소 캐시 목록
    Repository-->>App: RepositoryResponse 목록
    App-->>Frontend: 저장소 목록
```

## UC-08 저장소 상세 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Repository
    participant Platform
    participant DB

    User->>Frontend: 저장소 선택
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}
    App->>Repository: getRepository(platform, repositoryId, session)
    Repository->>Platform: requireCurrentConnection(platform, session)
    Platform->>Connection: requireCurrentConnectionInfo(platform, session)
    Connection-->>Platform: accountLogin
    Platform-->>Repository: accountLogin
    Repository->>DB: platform + repositoryId 조회
    DB-->>Repository: 저장소 캐시
    Repository->>Repository: ownerKey 접근 검증
    Repository-->>App: RepositoryResponse
    App-->>Frontend: 저장소 상세
```

## UC-09 저장소 동기화 상태 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Repository
    participant SyncState
    participant DB

    User->>Frontend: 저장소 동기화 상태 확인
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}/sync-state
    App->>Repository: getRepositorySyncState(platform, repositoryId, session)
    Repository->>Repository: 저장소 접근 검증
    Repository->>SyncState: getSyncState(REPOSITORY, platform:repositoryId)
    SyncState->>DB: sync_states 조회
    DB-->>SyncState: 동기화 상태
    SyncState-->>Repository: SyncStateResponse
    Repository-->>App: SyncStateResponse
    App-->>Frontend: 동기화 상태
```

## UC-10 이슈 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 이슈 새로고침 클릭
    Frontend->>App: POST /api/platforms/{platform}/repositories/{repositoryId}/issues/refresh
    App->>Issue: refreshIssues(platform, repositoryId, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Repository-->>Issue: RepositoryAccess
    Issue->>Platform: getRepositoryIssues(platform, session, owner, name)
    Platform->>Remote: 원격 이슈 목록 조회
    Remote-->>Platform: 원격 이슈 목록
    Platform-->>Issue: RemoteIssue 목록
    Issue->>DB: issue_caches upsert
    Issue->>DB: sync-state 성공 기록
    Issue-->>App: 캐시 기준 이슈 목록
    App-->>Frontend: 이슈 목록
```

## UC-11 이슈 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant DB

    User->>Frontend: 이슈 목록 화면 진입
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}/issues?keyword&state
    App->>Issue: getIssues(platform, repositoryId, keyword, state, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Issue->>DB: repositoryId 기준 issue_caches 조회
    DB-->>Issue: 이슈 캐시 목록
    Issue->>Issue: keyword/state 필터링
    Issue-->>App: IssueSummaryResponse 목록
    App-->>Frontend: 이슈 목록
```

## UC-12 이슈 생성

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 제목/본문 입력 후 생성
    Frontend->>App: POST /api/platforms/{platform}/repositories/{repositoryId}/issues
    App->>Issue: createIssue(platform, repositoryId, request, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Repository-->>Issue: RepositoryAccess
    Issue->>Platform: createIssue(platform, session, owner, name, title, body)
    Platform->>Remote: 원격 이슈 생성
    Remote-->>Platform: 생성된 이슈
    Platform-->>Issue: RemoteIssue
    Issue->>DB: issue_caches upsert
    Issue->>DB: sync-state 성공 기록
    Issue-->>App: IssueDetailResponse
    App-->>Frontend: 생성된 이슈 상세
```

## UC-13 이슈 상세 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant DB

    User->>Frontend: 이슈 상세 진입
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}
    App->>Issue: getIssue(platform, repositoryId, issueNumberOrKey, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Issue->>DB: repositoryId + numberOrKey 기준 issue_caches 조회
    DB-->>Issue: 이슈 캐시
    Issue-->>App: IssueDetailResponse
    App-->>Frontend: 이슈 상세
```

## UC-14 이슈 수정

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 이슈 수정 저장
    Frontend->>App: PATCH /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}
    App->>Issue: updateIssue(platform, repositoryId, issueNumberOrKey, request, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Issue->>DB: 현재 이슈 캐시 조회
    Issue->>Issue: 누락 필드는 기존 값으로 보완
    Issue->>Platform: updateIssue(...)
    Platform->>Remote: 원격 이슈 수정
    Remote-->>Platform: 수정된 이슈
    Platform-->>Issue: RemoteIssue
    Issue->>DB: issue_caches upsert
    Issue->>DB: sync-state 성공 기록
    Issue-->>App: IssueDetailResponse
    App-->>Frontend: 수정된 이슈 상세
```

## UC-15 이슈 닫기

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant Repository
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 이슈 닫기 실행
    Frontend->>App: DELETE /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}
    Note over Frontend,App: 현재 계약상 DELETE지만 실제 의미는 이슈 닫기
    App->>Issue: deleteIssue(platform, repositoryId, issueNumberOrKey, session)
    Issue->>Repository: requireAccessibleRepository(...)
    Repository-->>Issue: RepositoryAccess
    Issue->>Platform: updateIssue(state=CLOSED)
    Platform->>Remote: 원격 이슈 상태 CLOSED 변경
    Remote-->>Platform: 닫힌 이슈
    Issue->>Issue: refreshIssues(platform, repositoryId, session)
    Issue->>Platform: getRepositoryIssues(...)
    Platform->>Remote: 원격 이슈 목록 조회
    Remote-->>Platform: 원격 이슈 목록
    Issue->>DB: issue_caches upsert
    Issue->>DB: sync-state 성공 기록
    App-->>Frontend: 204 No Content
```

## UC-16 이슈 동기화 상태 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Issue
    participant SyncState
    participant DB

    User->>Frontend: 이슈 동기화 상태 확인
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/sync-state
    App->>Issue: getIssueSyncState(platform, repositoryId, issueNumberOrKey, session)
    Issue->>Issue: 이슈 접근 검증
    Issue->>SyncState: getSyncState(ISSUE, platform:repositoryId:issueNumberOrKey)
    SyncState->>DB: sync_states 조회
    DB-->>SyncState: 동기화 상태
    SyncState-->>Issue: SyncStateResponse
    Issue-->>App: SyncStateResponse
    App-->>Frontend: 동기화 상태
```

## UC-17 댓글 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Comment
    participant Repository
    participant Issue
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 댓글 새로고침 클릭
    Frontend->>App: POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments/refresh
    App->>Comment: refreshComments(platform, repositoryId, issueNumberOrKey, session)
    Comment->>Repository: requireAccessibleRepository(...)
    Comment->>Issue: requireIssue(...)
    Issue-->>Comment: IssueAccess
    Comment->>Platform: getIssueComments(platform, session, owner, name, issueNumberOrKey)
    Platform->>Remote: 원격 댓글 목록 조회
    Remote-->>Platform: 원격 댓글 목록
    Platform-->>Comment: RemoteComment 목록
    Comment->>DB: comment_caches upsert
    Comment->>DB: sync-state 성공 기록
    Comment-->>App: 캐시 기준 댓글 목록
    App-->>Frontend: 댓글 목록
```

## UC-18 댓글 목록 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Comment
    participant Issue
    participant DB

    User->>Frontend: 이슈 상세 댓글 영역 진입
    Frontend->>App: GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments
    App->>Comment: getComments(platform, repositoryId, issueNumberOrKey, session)
    Comment->>Issue: requireIssue(...)
    Issue-->>Comment: IssueAccess
    Comment->>DB: issueExternalId 기준 comment_caches 조회
    DB-->>Comment: 댓글 캐시 목록
    Comment-->>App: CommentResponse 목록
    App-->>Frontend: 댓글 목록
```

## UC-19 댓글 작성

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Comment
    participant Repository
    participant Issue
    participant Platform
    participant DB
    participant Remote

    User->>Frontend: 댓글 입력 후 작성
    Frontend->>App: POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments
    App->>Comment: createComment(platform, repositoryId, issueNumberOrKey, request, session)
    Comment->>Repository: requireAccessibleRepository(...)
    Comment->>Issue: requireIssue(...)
    Issue-->>Comment: IssueAccess
    Comment->>Platform: createComment(platform, session, owner, name, issueNumberOrKey, body)
    Platform->>Remote: 원격 댓글 생성
    Remote-->>Platform: 생성된 댓글
    Platform-->>Comment: RemoteComment
    Comment->>DB: comment_caches 저장
    Comment->>DB: sync-state 성공 기록
    Comment-->>App: CommentResponse
    App-->>Frontend: 생성된 댓글
```
