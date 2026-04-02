# API Specification

## 1. 공통

- Base URL: `/api`
- 요청/응답 형식: JSON
- 인증 방식: 세션 + 사용자 등록 PAT

## 2. 인증 및 연결

### POST `/api/github/token`

- PAT 등록 및 현재 사용자 연결

요청 예시

```json
{
  "accessToken": "github_pat_xxx"
}
```

### GET `/api/github/token/status`

- 현재 PAT 연결 상태 조회

### DELETE `/api/github/token`

- PAT 연결 해제

### GET `/api/me`

- 현재 세션 사용자 조회

## 3. 저장소

### GET `/api/repositories`

- 캐시된 저장소 목록 조회

응답 예시

```json
[
  {
    "githubRepositoryId": 123,
    "ownerLogin": "okjun",
    "name": "github-issue-manager",
    "fullName": "okjun/github-issue-manager",
    "description": "GitHub issue manager",
    "htmlUrl": "https://github.com/okjun/github-issue-manager",
    "private": false,
    "lastSyncedAt": "2026-04-02T12:00:00"
  }
]
```

### POST `/api/repositories/refresh`

- GitHub 저장소 목록 강제 동기화

### GET `/api/repositories/{repositoryId}`

- 저장소 상세 조회

### GET `/api/repositories/{repositoryId}/sync-state`

- 저장소 마지막 동기화 상태 조회

## 4. 이슈

### GET `/api/repositories/{repositoryId}/issues`

- 이슈 목록 조회

쿼리 파라미터

- `keyword`
- `state`

### POST `/api/repositories/{repositoryId}/issues/refresh`

- 이슈 목록 강제 동기화

### POST `/api/repositories/{repositoryId}/issues`

- GitHub 이슈 생성

요청 예시

```json
{
  "title": "Add issue refresh button",
  "body": "Implement manual issue refresh."
}
```

### GET `/api/repositories/{repositoryId}/issues/{issueNumber}`

- 이슈 상세 조회

### PATCH `/api/repositories/{repositoryId}/issues/{issueNumber}`

- 이슈 제목, 본문, 상태 수정

요청 예시

```json
{
  "title": "Update issue refresh flow",
  "body": "Sync GitHub issue cache manually.",
  "state": "CLOSED"
}
```

### DELETE `/api/repositories/{repositoryId}/issues/{issueNumber}`

- 현재 구현에서는 GitHub 이슈 삭제 대신 닫기 처리

### GET `/api/repositories/{repositoryId}/issues/{issueNumber}/sync-state`

- 이슈 마지막 동기화 상태 조회

## 5. 댓글

### GET `/api/repositories/{repositoryId}/issues/{issueNumber}/comments`

- 댓글 목록 조회

### POST `/api/repositories/{repositoryId}/issues/{issueNumber}/comments/refresh`

- 댓글 목록 강제 동기화

### POST `/api/repositories/{repositoryId}/issues/{issueNumber}/comments`

- GitHub 댓글 작성

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
- sub-issue 관리
