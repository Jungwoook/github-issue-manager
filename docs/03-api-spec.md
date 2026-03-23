<!-- 데이터 레이어 정의 (API 명세서) -->

# API Specification

## 1. 개요

이 문서는 1차 구현 범위의 이슈 관리 시스템 API 명세를 정의한다.
현재 범위에서는 Repository, Issue, Comment, Label, User 도메인을 다루며,
인증은 아직 포함하지 않지만 사용자 리소스는 별도로 관리한다.

기본 규칙

* Base URL 예시: `/api`
* 요청과 응답은 JSON 형식 사용
* 성공 응답은 HTTP 상태 코드를 명확히 사용
* 오류 응답은 공통 포맷을 사용
* Entity를 직접 노출하지 않고 DTO 기반으로 응답한다

---

## 2. 공통 응답 형식

### 2.1 성공 응답

단건 조회 또는 생성 예시

```json
{
  "id": 1,
  "name": "backend-service",
  "description": "백엔드 이슈 관리 저장소",
  "createdAt": "2026-03-23T10:00:00",
  "updatedAt": "2026-03-23T10:00:00"
}
```

목록 조회 예시

```json
[
  {
    "id": 1,
    "name": "backend-service",
    "description": "백엔드 이슈 관리 저장소",
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
]
```

### 2.2 오류 응답

```json
{
  "code": "ISSUE_NOT_FOUND",
  "message": "Issue not found",
  "timestamp": "2026-03-23T10:00:00"
}
```

Validation 오류 예시

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "errors": [
    {
      "field": "title",
      "reason": "must not be blank"
    }
  ],
  "timestamp": "2026-03-23T10:00:00"
}
```

---

## 3. Repository API

## 3.1 Repository 생성

### POST /api/repositories

설명

새로운 Repository를 생성한다.

요청 본문

```json
{
  "name": "backend-service",
  "description": "백엔드 이슈 관리 저장소"
}
```

필드 설명

* name: 필수
* description: 선택

성공 응답

* 201 Created

```json
{
  "id": 1,
  "name": "backend-service",
  "description": "백엔드 이슈 관리 저장소",
  "createdAt": "2026-03-23T10:00:00",
  "updatedAt": "2026-03-23T10:00:00"
}
```

오류 응답

* 400 Bad Request

---

## 3.2 Repository 목록 조회

### GET /api/repositories

설명

Repository 목록을 조회한다.

성공 응답

* 200 OK

```json
[
  {
    "id": 1,
    "name": "backend-service",
    "description": "백엔드 이슈 관리 저장소",
    "createdAt": "2026-03-23T10:00:00",
    "updatedAt": "2026-03-23T10:00:00"
  }
]
```

---

## 3.3 Repository 단건 조회

### GET /api/repositories/{repositoryId}

설명

특정 Repository를 조회한다.

경로 변수

* repositoryId: Repository 식별자

성공 응답

* 200 OK

오류 응답

* 404 Not Found

---

## 3.4 Repository 수정

### PUT /api/repositories/{repositoryId}

설명

Repository 이름 또는 설명을 수정한다.

요청 본문

```json
{
  "name": "backend-api",
  "description": "수정된 설명"
}
```

성공 응답

* 200 OK

```json
{
  "id": 1,
  "name": "backend-api",
  "description": "수정된 설명",
  "createdAt": "2026-03-23T10:00:00",
  "updatedAt": "2026-03-23T11:00:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 3.5 Repository 삭제

### DELETE /api/repositories/{repositoryId}

설명

Repository와 하위 데이터를 삭제한다.

삭제 대상

* Repository
* 해당 Repository의 Issue
* 해당 Issue의 Comment
* 해당 Repository의 Label
* Issue와 Label 연결 정보

성공 응답

* 204 No Content

오류 응답

* 404 Not Found

---

## 4. User API

## 4.1 User 생성

### POST /api/users

설명

시스템에서 사용할 사용자를 생성한다.

요청 본문

```json
{
  "username": "jane.doe",
  "displayName": "Jane Doe",
  "email": "jane.doe@example.com",
  "role": "MEMBER"
}
```

필드 설명

* username: 필수, 시스템 내 고유값
* displayName: 필수
* email: 필수, 시스템 내 고유값
* role: 선택, 기본값은 MEMBER

성공 응답

* 201 Created

```json
{
  "id": 7,
  "username": "jane.doe",
  "displayName": "Jane Doe",
  "email": "jane.doe@example.com",
  "role": "MEMBER",
  "createdAt": "2026-03-23T09:30:00",
  "updatedAt": "2026-03-23T09:30:00"
}
```

오류 응답

* 400 Bad Request
* 409 Conflict

---

## 4.2 User 목록 조회

### GET /api/users

설명

사용자 목록을 조회한다.

쿼리 파라미터

* keyword: username 또는 displayName 부분 검색
* role: ADMIN 또는 MEMBER

예시

`GET /api/users?keyword=jane&role=MEMBER`

성공 응답

* 200 OK

```json
[
  {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe",
    "email": "jane.doe@example.com",
    "role": "MEMBER",
    "createdAt": "2026-03-23T09:30:00",
    "updatedAt": "2026-03-23T09:30:00"
  }
]
```

---

## 4.3 User 단건 조회

### GET /api/users/{userId}

설명

특정 사용자를 조회한다.

경로 변수

* userId: User 식별자

성공 응답

* 200 OK

오류 응답

* 404 Not Found

---

## 4.4 User 수정

### PUT /api/users/{userId}

설명

사용자 기본 정보를 수정한다.

요청 본문

```json
{
  "displayName": "Jane Kim",
  "email": "jane.kim@example.com",
  "role": "ADMIN"
}
```

성공 응답

* 200 OK

```json
{
  "id": 7,
  "username": "jane.doe",
  "displayName": "Jane Kim",
  "email": "jane.kim@example.com",
  "role": "ADMIN",
  "createdAt": "2026-03-23T09:30:00",
  "updatedAt": "2026-03-23T10:10:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found
* 409 Conflict

---

## 4.5 User 삭제

### DELETE /api/users/{userId}

설명

사용자를 삭제한다.

정책

* assignee로 지정된 Issue가 있으면 삭제를 거부한다.
* 작성한 Comment가 있으면 삭제를 거부한다.

성공 응답

* 204 No Content

오류 응답

* 404 Not Found
* 409 Conflict

---

## 5. Issue API

## 5.1 Issue 생성

### POST /api/repositories/{repositoryId}/issues

설명

특정 Repository에 새로운 Issue를 생성한다.

요청 본문

```json
{
  "title": "로그인 오류 수정",
  "content": "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
  "priority": "HIGH",
  "assigneeId": 7
}
```

필드 설명

* title: 필수
* content: 선택 또는 필수 여부를 구현 시 명확히 정의
* priority: 선택, 기본값은 MEDIUM 권장
* assigneeId: 선택, 담당 사용자 식별자

기본 규칙

* status는 생성 시 OPEN으로 설정한다

성공 응답

* 201 Created

```json
{
  "id": 10,
  "repositoryId": 1,
  "title": "로그인 오류 수정",
  "content": "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "labels": [],
  "createdAt": "2026-03-23T10:30:00",
  "updatedAt": "2026-03-23T10:30:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 5.2 Issue 목록 조회

### GET /api/repositories/{repositoryId}/issues

설명

특정 Repository의 Issue 목록을 조회한다.

쿼리 파라미터

* keyword: 제목 부분 검색
* status: OPEN 또는 CLOSED
* priority: LOW, MEDIUM, HIGH
* labelId: 특정 Label 기준 필터
* assigneeId: 특정 담당자 기준 필터

예시

`GET /api/repositories/1/issues?keyword=로그인&status=OPEN&priority=HIGH&assigneeId=7`

성공 응답

* 200 OK

```json
[
  {
    "id": 10,
    "repositoryId": 1,
    "title": "로그인 오류 수정",
    "status": "OPEN",
    "priority": "HIGH",
    "assignee": {
      "id": 7,
      "username": "jane.doe",
      "displayName": "Jane Doe"
    },
    "createdAt": "2026-03-23T10:30:00",
    "updatedAt": "2026-03-23T10:30:00"
  }
]
```

오류 응답

* 404 Not Found

---

## 5.3 Issue 단건 조회

### GET /api/repositories/{repositoryId}/issues/{issueId}

설명

특정 Issue 상세 정보를 조회한다.

성공 응답

* 200 OK

```json
{
  "id": 10,
  "repositoryId": 1,
  "title": "로그인 오류 수정",
  "content": "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "labels": [
    {
      "id": 3,
      "name": "bug",
      "color": "#d73a4a"
    }
  ],
  "createdAt": "2026-03-23T10:30:00",
  "updatedAt": "2026-03-23T10:30:00"
}
```

오류 응답

* 404 Not Found

---

## 5.4 Issue 수정

### PUT /api/repositories/{repositoryId}/issues/{issueId}

설명

Issue 제목과 내용, 담당자를 수정한다.

요청 본문

```json
{
  "title": "로그인 오류 수정 필요",
  "content": "리다이렉트 URL 처리 로직 점검 필요",
  "assigneeId": 7
}
```

성공 응답

* 200 OK

```json
{
  "id": 10,
  "repositoryId": 1,
  "title": "로그인 오류 수정 필요",
  "content": "리다이렉트 URL 처리 로직 점검 필요",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "labels": [],
  "createdAt": "2026-03-23T10:30:00",
  "updatedAt": "2026-03-23T11:00:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 5.5 Issue 삭제

### DELETE /api/repositories/{repositoryId}/issues/{issueId}

설명

Issue를 삭제한다.

삭제 시 정리 대상

* 해당 Issue의 Comment
* 해당 Issue와 Label 연결 정보

성공 응답

* 204 No Content

오류 응답

* 404 Not Found

---

## 5.6 Issue 상태 변경

### PATCH /api/repositories/{repositoryId}/issues/{issueId}/status

설명

Issue 상태를 변경한다.

요청 본문

```json
{
  "status": "CLOSED"
}
```

허용 값

* OPEN
* CLOSED

성공 응답

* 200 OK

```json
{
  "id": 10,
  "repositoryId": 1,
  "title": "로그인 오류 수정",
  "content": "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
  "status": "CLOSED",
  "priority": "HIGH",
  "assignee": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "labels": [],
  "createdAt": "2026-03-23T10:30:00",
  "updatedAt": "2026-03-23T11:10:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 5.7 Issue 우선순위 변경

### PATCH /api/repositories/{repositoryId}/issues/{issueId}/priority

설명

Issue 우선순위를 변경한다.

요청 본문

```json
{
  "priority": "LOW"
}
```

허용 값

* LOW
* MEDIUM
* HIGH

성공 응답

* 200 OK

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 5.8 Issue 담당자 변경

### PATCH /api/repositories/{repositoryId}/issues/{issueId}/assignee

설명

Issue 담당자를 지정하거나 해제한다.

요청 본문

```json
{
  "assigneeId": 7
}
```

담당자 해제 예시

```json
{
  "assigneeId": null
}
```

성공 응답

* 200 OK

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 6. Comment API

## 6.1 Comment 생성

### POST /api/repositories/{repositoryId}/issues/{issueId}/comments

설명

특정 Issue에 댓글을 작성한다.

요청 본문

```json
{
  "content": "재현 경로 확인 중입니다.",
  "authorId": 7
}
```

필드 설명

* content: 필수
* authorId: 필수, 작성 사용자 식별자

성공 응답

* 201 Created

```json
{
  "id": 100,
  "issueId": 10,
  "content": "재현 경로 확인 중입니다.",
  "author": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "createdAt": "2026-03-23T11:20:00"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found

---

## 6.2 Comment 목록 조회

### GET /api/repositories/{repositoryId}/issues/{issueId}/comments

설명

특정 Issue의 댓글 목록을 조회한다.

성공 응답

* 200 OK

```json
[
  {
    "id": 100,
    "issueId": 10,
    "content": "재현 경로 확인 중입니다.",
    "author": {
      "id": 7,
      "username": "jane.doe",
      "displayName": "Jane Doe"
    },
    "createdAt": "2026-03-23T11:20:00"
  }
]
```

오류 응답

* 404 Not Found

---

## 6.3 Comment 삭제

### DELETE /api/repositories/{repositoryId}/issues/{issueId}/comments/{commentId}

설명

특정 Comment를 삭제한다.

성공 응답

* 204 No Content

오류 응답

* 404 Not Found

---

## 7. Label API

## 7.1 Label 생성

### POST /api/repositories/{repositoryId}/labels

설명

특정 Repository의 Label을 생성한다.

요청 본문

```json
{
  "name": "bug",
  "color": "#d73a4a"
}
```

필드 설명

* name: 필수
* color: 필수 또는 선택 정책 정의 가능

제약 조건

* 같은 Repository 내에 같은 name의 Label은 허용하지 않는다

성공 응답

* 201 Created

```json
{
  "id": 3,
  "repositoryId": 1,
  "name": "bug",
  "color": "#d73a4a"
}
```

오류 응답

* 400 Bad Request
* 404 Not Found
* 409 Conflict

---

## 7.2 Label 목록 조회

### GET /api/repositories/{repositoryId}/labels

설명

특정 Repository의 Label 목록을 조회한다.

성공 응답

* 200 OK

```json
[
  {
    "id": 3,
    "repositoryId": 1,
    "name": "bug",
    "color": "#d73a4a"
  }
]
```

오류 응답

* 404 Not Found

---

## 7.3 Issue에 Label 추가

### POST /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}

설명

특정 Issue에 Label을 연결한다.

규칙

* Label은 같은 Repository에 속해야 한다
* 이미 연결된 경우 중복 연결하지 않는다

성공 응답

* 200 OK

```json
{
  "id": 10,
  "repositoryId": 1,
  "title": "로그인 오류 수정",
  "content": "OAuth 로그인 리다이렉트 처리에서 오류가 발생합니다.",
  "status": "OPEN",
  "priority": "HIGH",
  "assignee": {
    "id": 7,
    "username": "jane.doe",
    "displayName": "Jane Doe"
  },
  "labels": [
    {
      "id": 3,
      "name": "bug",
      "color": "#d73a4a"
    }
  ],
  "createdAt": "2026-03-23T10:30:00",
  "updatedAt": "2026-03-23T11:30:00"
}
```

오류 응답

* 404 Not Found
* 409 Conflict

---

## 7.4 Issue에서 Label 제거

### DELETE /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}

설명

특정 Issue에서 Label 연결을 제거한다.

성공 응답

* 204 No Content

오류 응답

* 404 Not Found

---

## 8. 상태값과 열거형 정의

### Issue Status

* OPEN
* CLOSED

### Issue Priority

* LOW
* MEDIUM
* HIGH

### User Role

* ADMIN
* MEMBER

---

## 9. 주요 오류 코드 예시

* REPOSITORY_NOT_FOUND
* USER_NOT_FOUND
* ISSUE_NOT_FOUND
* COMMENT_NOT_FOUND
* LABEL_NOT_FOUND
* DUPLICATE_USER_USERNAME
* DUPLICATE_USER_EMAIL
* USER_DELETE_CONFLICT
* DUPLICATE_LABEL_NAME
* LABEL_ALREADY_ATTACHED
* VALIDATION_ERROR
* INVALID_STATUS
* INVALID_PRIORITY
* INVALID_USER_ROLE

---

## 10. 구현 메모

* Repository 도메인 개념과 데이터 접근 계층의 이름 충돌 가능성이 있으므로 코드에서는 `RepositoryEntity` 같은 명칭을 고려한다.
* User는 인증 기능과 분리된 관리 리소스로 두고, 추후 인증/인가가 붙어도 사용자 기본 정보 API는 유지할 수 있도록 설계한다.
* Issue 목록 조회 시 검색 조건이 늘어나면 Specification 또는 QueryDSL 확장을 고려한다.
* Comment 작성자는 문자열이 아니라 User 식별자를 기준으로 저장하고, 응답에서 요약 사용자 정보를 함께 내려준다.
* 이후 GitHub API 연동 시에도 현재 API 형태를 최대한 유지할 수 있도록 내부 DTO 중간 계층으로 설계한다.
