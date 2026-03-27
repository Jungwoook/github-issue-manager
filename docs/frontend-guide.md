# Frontend Guide

## 1. 문서 목적

이 문서는 현재 구현된 백엔드 API와 도메인 문서를 바탕으로 프론트엔드 구현 방향을 정리한다.
목표는 프론트엔드 개발자가 화면 구조, 사용자 흐름, API 연동 방식, 예외 처리 기준을 빠르게 이해하고 구현에 바로 착수할 수 있도록 하는 것이다.

이 문서는 현재 구현된 백엔드 범위를 기준으로 작성한다. 아직 백엔드에 없는 기능은 프론트엔드에서 이미 제공되는 것처럼 가정하지 않는다.

---

## 2. 구현 범위 기준

현재 백엔드에서 제공하는 도메인은 다음과 같다.

* Repository
* User
* Issue
* Comment
* Label

현재 프론트엔드는 다음 범위를 우선 지원하는 것을 목표로 한다.

* Repository 목록 및 관리
* Repository별 Issue 목록 및 상세
* Issue 생성, 수정, 상태 변경, 우선순위 변경, 담당자 변경
* Comment 작성 및 삭제
* Label 생성, 조회, Issue 연결 및 해제
* User 관리 및 Issue/Comment 작성자 선택

현재 백엔드에 없으므로 프론트 초기 범위에서 제외하거나 제한적으로 다뤄야 하는 항목은 다음과 같다.

* 로그인, 로그아웃, 권한 분기
* 페이지네이션
* 정렬 옵션
* GitHub 외부 연동
* 알림, 실시간 업데이트

---

## 3. 핵심 사용자 흐름

### 3.1 기본 흐름

1. 사용자는 Repository 목록에 진입한다.
2. 특정 Repository를 선택한다.
3. 선택한 Repository의 Issue 목록을 본다.
4. 검색/필터로 원하는 Issue를 찾는다.
5. Issue 상세 화면에서 내용, 담당자, 상태, 우선순위, Label, Comment를 확인하고 수정한다.

### 3.2 생성 흐름

1. 사용자는 Repository를 생성한다.
2. Repository 안에서 Issue를 생성한다.
3. 필요하면 User를 먼저 생성해 담당자로 지정한다.
4. Label을 생성한 뒤 Issue에 연결한다.
5. Comment를 추가해 진행 상황을 기록한다.

### 3.3 관리 흐름

1. 사용자는 User 목록에서 담당자 후보를 관리한다.
2. 사용자는 Repository 단위로 Label을 관리한다.
3. 사용자는 Issue 상태와 우선순위를 빠르게 변경한다.

---

## 4. 추천 화면 구조

### 4.1 필수 화면

* Repository 목록 화면
* Repository 생성/수정 화면 또는 모달
* Repository별 Issue 목록 화면
* Issue 상세 화면
* Issue 생성/수정 화면 또는 모달
* User 목록 화면
* User 생성/수정 화면 또는 모달
* Repository별 Label 관리 화면 또는 패널

### 4.2 화면 역할

#### Repository 목록 화면

역할

* Repository 전체 목록 조회
* Repository 생성
* Repository 수정 및 삭제 진입
* 특정 Repository의 Issue 목록 화면으로 이동

#### Issue 목록 화면

역할

* 특정 Repository의 Issue 목록 조회
* 키워드, 상태, 우선순위, 담당자, Label 기준 필터
* Issue 생성
* Issue 상세 이동

#### Issue 상세 화면

역할

* Issue 기본 정보 조회
* title, content 수정
* status, priority, assignee 변경
* Label 연결 및 해제
* Comment 목록 조회, 생성, 삭제

#### User 목록 화면

역할

* User 목록 조회
* keyword, role 필터
* User 생성, 수정, 삭제
* Issue 담당자 선택에 필요한 데이터 관리

#### Label 관리 화면

역할

* Repository 기준 Label 목록 조회
* Label 생성
* Issue에 연결 가능한 Label 확인

---

## 5. 추천 라우팅 구조

현재 API 구조와 화면 흐름을 기준으로 아래 라우팅을 권장한다.

```text
/
/repositories
/repositories/new
/repositories/:repositoryId
/repositories/:repositoryId/edit
/repositories/:repositoryId/issues
/repositories/:repositoryId/issues/new
/repositories/:repositoryId/issues/:issueId
/repositories/:repositoryId/issues/:issueId/edit
/repositories/:repositoryId/labels
/users
/users/new
/users/:userId/edit
```

단일 페이지 애플리케이션으로 구현할 경우 `new`, `edit`는 페이지 대신 모달로 처리해도 된다.

---

## 6. 화면별 데이터 요구사항

### 6.1 Repository 목록 화면

필요 데이터

* Repository 목록
* 각 Repository의 id, name, description, createdAt, updatedAt

필요 액션

* 목록 조회
* 생성
* 수정
* 삭제

### 6.2 Issue 목록 화면

필요 데이터

* Repository 기본 정보
* Issue 목록
* 필터 UI에 필요한 User 목록
* 필터 UI에 필요한 Label 목록

목록 아이템에 필요한 필드

* id
* title
* status
* priority
* assignee 요약 정보
* createdAt
* updatedAt

### 6.3 Issue 상세 화면

필요 데이터

* Issue 상세 정보
* Repository 내 Label 목록
* 담당자 선택용 User 목록
* Comment 목록

상세 화면에서 다뤄야 하는 주요 필드

* title
* content
* status
* priority
* assignee
* labels
* createdAt
* updatedAt

### 6.4 User 목록 화면

필요 데이터

* User 목록
* id, username, displayName, email, role, createdAt, updatedAt

### 6.5 Label 관리 화면

필요 데이터

* 특정 Repository의 Label 목록
* id, name, color

---

## 7. API 연동 가이드

기본 Base URL은 `/api` 이다.

### 7.1 Repository API 매핑

* Repository 목록 조회: `GET /api/repositories`
* Repository 생성: `POST /api/repositories`
* Repository 단건 조회: `GET /api/repositories/{repositoryId}`
* Repository 수정: `PUT /api/repositories/{repositoryId}`
* Repository 삭제: `DELETE /api/repositories/{repositoryId}`

### 7.2 User API 매핑

* User 목록 조회: `GET /api/users?keyword=&role=`
* User 생성: `POST /api/users`
* User 단건 조회: `GET /api/users/{userId}`
* User 수정: `PUT /api/users/{userId}`
* User 삭제: `DELETE /api/users/{userId}`

### 7.3 Issue API 매핑

* Issue 목록 조회: `GET /api/repositories/{repositoryId}/issues`
* Issue 생성: `POST /api/repositories/{repositoryId}/issues`
* Issue 상세 조회: `GET /api/repositories/{repositoryId}/issues/{issueId}`
* Issue 수정: `PUT /api/repositories/{repositoryId}/issues/{issueId}`
* Issue 삭제: `DELETE /api/repositories/{repositoryId}/issues/{issueId}`
* 상태 변경: `PATCH /api/repositories/{repositoryId}/issues/{issueId}/status`
* 우선순위 변경: `PATCH /api/repositories/{repositoryId}/issues/{issueId}/priority`
* 담당자 변경: `PATCH /api/repositories/{repositoryId}/issues/{issueId}/assignee`

Issue 목록 조회 쿼리 파라미터

* `keyword`
* `status`
* `priority`
* `labelId`
* `assigneeId`

### 7.4 Comment API 매핑

* Comment 목록 조회: `GET /api/repositories/{repositoryId}/issues/{issueId}/comments`
* Comment 생성: `POST /api/repositories/{repositoryId}/issues/{issueId}/comments`
* Comment 삭제: `DELETE /api/repositories/{repositoryId}/issues/{issueId}/comments/{commentId}`

### 7.5 Label API 매핑

* Label 목록 조회: `GET /api/repositories/{repositoryId}/labels`
* Label 생성: `POST /api/repositories/{repositoryId}/labels`
* Issue에 Label 연결: `POST /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`
* Issue에서 Label 해제: `DELETE /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`

---

## 8. 프론트엔드 상태 관리 기준

전역 상태로 둘 후보

* 현재 선택된 Repository 정보
* 공통 에러 메시지
* User 목록 캐시
* Repository별 Label 목록 캐시

화면 단위 상태로 둘 후보

* 목록 필터 입력값
* 모달 열림/닫힘 상태
* 폼 입력값
* 임시 선택된 Label
* 삭제 확인 상태

현재 인증 기능이 없으므로 사용자 세션 상태는 필수 전역 상태가 아니다.

---

## 9. 폼 및 검증 기준

프론트엔드 검증은 백엔드 검증을 대체하지 않고, 사용성 개선 목적의 1차 검증으로 둔다.

### 9.1 Repository

* `name`: 필수
* `description`: 선택

### 9.2 User

* `username`: 필수
* `displayName`: 필수
* `email`: 필수
* `role`: `ADMIN`, `MEMBER` 중 하나

### 9.3 Issue

* `title`: 필수
* `content`: 선택
* `priority`: `LOW`, `MEDIUM`, `HIGH`
* `assigneeId`: 선택

상태는 생성 시 직접 입력하지 않고, 기본값 `OPEN`으로 시작하는 흐름을 권장한다.

### 9.4 Comment

* `content`: 필수
* `authorId`: 필수

### 9.5 Label

* `name`: 필수
* `color`: 필수로 다루는 UI를 권장

색상은 프론트에서 hex 색상 입력 또는 color picker로 제한하는 방식이 안전하다.

---

## 10. 로딩 및 에러 처리 기준

### 10.1 로딩 처리

* 목록 화면은 초기 진입 시 skeleton 또는 loading indicator 표시
* 생성/수정/삭제 액션 중에는 버튼 중복 클릭 방지
* Issue 상세 화면에서는 상세 정보와 Comment를 분리 로딩할 수 있다

### 10.2 에러 처리

백엔드는 공통 에러 응답 구조를 반환한다.

```json
{
  "code": "ISSUE_NOT_FOUND",
  "message": "Issue not found",
  "timestamp": "2026-03-23T10:00:00"
}
```

Validation 오류의 경우 `errors` 배열이 추가될 수 있으므로, 폼 화면에서는 field 단위 메시지 매핑을 고려한다.

주요 처리 방침

* `404`: 이미 삭제되었거나 잘못된 경로로 판단하고 목록으로 유도
* `409`: 중복 또는 삭제 충돌 상황으로 사용자에게 이유를 명시
* `400`: 입력값 검증 실패로 필드 오류를 우선 노출

---

## 11. 화면 구현 우선순위 제안

### 1차 구현

* Repository 목록
* Issue 목록
* Issue 상세
* Issue 생성/수정

### 2차 구현

* User 관리 화면
* Label 관리 화면
* Comment 작성/삭제 UX 개선

### 3차 구현

* 필터 UX 개선
* 라벨/담당자 선택 컴포넌트 고도화
* 낙관적 업데이트 또는 캐시 전략 개선

---

## 12. 미구현 백엔드 기능에 대한 프론트 처리 방침

현재 백엔드 제약을 프론트에서 명확하게 드러내야 한다.

* 로그인 UI는 만들지 않거나, 추후 기능으로 표시한다.
* 권한별 버튼 노출 제어는 아직 적용하지 않는다.
* 페이지네이션 UI는 우선 제외하거나, 목록이 많아질 경우 차후 추가 대상으로 둔다.
* 정렬 UI는 백엔드 지원 전까지 기본 정렬만 사용한다.
* GitHub 연동 버튼이나 동기화 UI는 현재 넣지 않는다.

---

## 13. 권장 컴포넌트 단위

재사용 가능한 컴포넌트 후보는 다음과 같다.

* RepositoryList
* IssueList
* IssueFilterBar
* IssueForm
* IssueDetailPanel
* IssueStatusSelect
* IssuePrioritySelect
* UserSelect
* LabelSelect
* LabelBadge
* CommentList
* CommentForm
* ConfirmDialog
* ErrorBanner

---

## 14. 문서 간 참조

프론트엔드 구현 시 아래 문서를 함께 참고한다.

* `docs/01-prd.md`: 제품 목표와 범위
* `docs/02-architecture.md`: 백엔드 구조와 계층 책임
* `docs/03-api-spec.md`: 엔드포인트 및 요청/응답 형식
* `docs/04-data-model.md`: 도메인 관계와 필드 구조
* `docs/05-implementation-summary.md`: 실제 구현 반영 범위

---

## 15. 요약

프론트엔드는 Repository 중심으로 Issue를 탐색하고 관리하는 흐름에 맞춰 구성한다.
초기 구현은 Repository, Issue, Comment, Label, User CRUD와 필터 기능을 안정적으로 연결하는 데 집중하고, 인증, GitHub 연동, 페이지네이션 같은 기능은 별도 확장 범위로 둔다.
