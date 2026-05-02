# API Specification

## 1. 공통

- Base URL: `/api`
- 요청/응답 형식: JSON
- 인증 방식: 세션 + 사용자 등록 플랫폼 PAT
- 기본 플랫폼 값: `github`
- 플랫폼 경로 형식: `/api/platforms/{platform}/...`

## 2. 인증 및 연결

### POST `/api/platforms/{platform}/token`

- 플랫폼 PAT 등록 및 현재 사용자 연결
- app controller가 platform 모듈로 토큰을 검증한 뒤 connection 모듈에 저장을 위임한다.

요청 예시

```json
{
  "accessToken": "github_pat_xxx",
  "baseUrl": null
}
```

응답 예시

```json
{
  "id": 1,
  "displayName": "okjun",
  "platform": "GITHUB",
  "accountLogin": "okjun",
  "avatarUrl": "https://avatars.githubusercontent.com/u/1?v=4"
}
```

### GET `/api/platforms/{platform}/token/status`

- 현재 세션의 플랫폼 연결 상태 조회

응답 예시

```json
{
  "platform": "GITHUB",
  "connected": true,
  "accountLogin": "okjun",
  "tokenScopes": "repo",
  "baseUrl": "https://api.github.com",
  "tokenVerifiedAt": "2026-04-28T12:00:00"
}
```

### DELETE `/api/platforms/{platform}/token`

- 현재 세션의 플랫폼 연결 해제
- 응답: `204 No Content`

### POST `/api/auth/logout`

- 현재 세션 로그아웃
- 응답: `204 No Content`

### GET `/api/me`

- 현재 세션 사용자 조회

## 3. 저장소

### GET `/api/platforms/{platform}/repositories`

- 캐시된 저장소 목록 조회

응답 예시

```json
[
  {
    "platform": "GITHUB",
    "repositoryId": "123",
    "ownerKey": "okjun",
    "name": "github-issue-manager",
    "fullName": "okjun/github-issue-manager",
    "description": "GitHub issue manager",
    "webUrl": "https://github.com/okjun/github-issue-manager",
    "isPrivate": false,
    "lastSyncedAt": "2026-04-28T12:00:00"
  }
]
```

### POST `/api/platforms/{platform}/repositories/refresh`

- 원격 플랫폼 저장소 목록 강제 동기화
- 응답: 캐시 기준 저장소 목록

### GET `/api/platforms/{platform}/repositories/{repositoryId}`

- 저장소 상세 조회

### GET `/api/platforms/{platform}/repositories/{repositoryId}/sync-state`

- 저장소 마지막 동기화 상태 조회

응답 예시

```json
{
  "resourceType": "REPOSITORY",
  "resourceKey": "GITHUB:123",
  "lastSyncedAt": "2026-04-28T12:00:00",
  "lastSyncStatus": "SUCCESS",
  "lastSyncMessage": null
}
```

## 4. 이슈

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues`

- 이슈 목록 조회

쿼리 파라미터

- `keyword`
- `state`

응답 예시

```json
[
  {
    "platform": "GITHUB",
    "issueId": "987",
    "numberOrKey": "12",
    "title": "Update issue refresh flow",
    "state": "OPEN",
    "authorLogin": "okjun",
    "createdAt": "2026-04-28T12:00:00",
    "updatedAt": "2026-04-28T12:00:00",
    "lastSyncedAt": "2026-04-28T12:00:00"
  }
]
```

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/refresh`

- 이슈 목록 강제 동기화

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues`

- 원격 플랫폼 이슈 생성

요청 예시

```json
{
  "title": "Add issue refresh button",
  "body": "Implement manual issue refresh."
}
```

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`

- 이슈 상세 조회

### PATCH `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`

- 이슈 제목, 본문, 상태 수정

요청 예시

```json
{
  "title": "Update issue refresh flow",
  "body": "Sync issue cache manually.",
  "state": "CLOSED"
}
```

### DELETE `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`

- 현재 구현에서는 실제 삭제가 아니라 이슈 닫기 처리
- 응답: `204 No Content`

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/sync-state`

- 이슈 마지막 동기화 상태 조회

## 5. 댓글

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`

- 댓글 목록 조회

응답 예시

```json
[
  {
    "platform": "GITHUB",
    "commentId": "456",
    "authorLogin": "okjun",
    "body": "Comment refresh has been implemented.",
    "createdAt": "2026-04-28T12:00:00",
    "updatedAt": "2026-04-28T12:00:00",
    "lastSyncedAt": "2026-04-28T12:00:00"
  }
]
```

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments/refresh`

- 댓글 목록 강제 동기화

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`

- 원격 플랫폼 댓글 작성

요청 예시

```json
{
  "body": "Comment refresh has been implemented."
}
```

## 6. 현재 미지원 API

아래 범위는 현재 구현되어 있지 않다.

- 라벨 조회/생성/연결/해제
- 담당자 변경
- 우선순위 변경
- 마일스톤 관리
- sub-issue 관리
