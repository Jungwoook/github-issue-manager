# API Specification

## 1. 공통

- Base URL: `/api`
- 요청/응답 형식: JSON
- 인증 방식: 세션 + 플랫폼별 등록 PAT
- 플랫폼 경로 값: `github`, `gitlab`

## 2. 인증 및 플랫폼 연결

### POST `/api/platforms/{platform}/token`

- 플랫폼 PAT 등록 및 현재 세션 연결
- GitLab은 `baseUrl`을 선택적으로 전달할 수 있다.

요청 예시

```json
{
  "accessToken": "github_pat_xxx",
  "baseUrl": "https://gitlab.com/api/v4"
}
```

### GET `/api/platforms/{platform}/token/status`

- 현재 세션 기준 플랫폼 PAT 연결 상태 조회

### DELETE `/api/platforms/{platform}/token`

- 플랫폼 PAT 연결 해제

### POST `/api/auth/logout`

- 현재 세션 종료

### GET `/api/me`

- 현재 세션 사용자 조회

## 3. 저장소

### GET `/api/platforms/{platform}/repositories`

- 캐시된 저장소/프로젝트 목록 조회

응답 예시

```json
[
  {
    "platform": "GITHUB",
    "repositoryId": "123",
    "ownerKey": "okjun",
    "name": "github-issue-manager",
    "fullName": "okjun/github-issue-manager",
    "description": "Issue manager",
    "webUrl": "https://github.com/okjun/github-issue-manager",
    "isPrivate": false,
    "lastSyncedAt": "2026-04-20T12:00:00"
  }
]
```

### POST `/api/platforms/{platform}/repositories/refresh`

- 외부 플랫폼 저장소/프로젝트 목록 강제 동기화

### GET `/api/platforms/{platform}/repositories/{repositoryId}`

- 저장소 상세 조회

### GET `/api/platforms/{platform}/repositories/{repositoryId}/sync-state`

- 저장소 마지막 동기화 상태 조회

## 4. 이슈

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues`

- 이슈 목록 조회

쿼리 파라미터

- `keyword`
- `state`

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/refresh`

- 이슈 목록 강제 동기화

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues`

- 외부 플랫폼 이슈 생성

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

- 실제 삭제가 아니라 이슈 닫기 처리

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/sync-state`

- 이슈 마지막 동기화 상태 조회

## 5. 댓글

### GET `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`

- 댓글 목록 조회

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments/refresh`

- 댓글 목록 강제 동기화

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`

- 외부 플랫폼 댓글 작성

요청 예시

```json
{
  "body": "Comment refresh has been implemented."
}
```

## 6. 현재 미구현 API

- 라벨 조회/생성/연결/해제 백엔드 API
- 담당자 변경
- 우선순위 변경
- milestone 관리
- sub-issue 관리
- GitLab merge request 연동
