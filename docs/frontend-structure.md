# Frontend Structure

## 1. 문서 목적

이 문서는 현재 백엔드 API를 소비하는 프론트엔드 애플리케이션의 권장 구조를 정의한다.
구현 기술은 아직 확정되지 않았더라도, 페이지 구성, 도메인 분리, API 계층, 상태 관리 위치를 일관되게 가져갈 수 있도록 기준을 제공한다.

---

## 2. 기본 방향

프론트엔드는 다음 원칙을 따른다.

* 화면보다 도메인 단위로 기능을 묶는다.
* API 호출 코드와 UI 코드를 분리한다.
* 재사용 가능한 선택 컴포넌트와 폼 컴포넌트를 분리한다.
* 서버 상태와 UI 상태를 구분한다.
* 현재 범위에서는 인증 전제를 두지 않는다.

---

## 3. 권장 기술 방향

프론트엔드 구현체는 아래 구성을 권장한다.

* SPA 기반
* 라우터 사용
* 서버 상태 캐시 도구 사용
* 폼 상태와 검증 도구 분리

예시 조합

* React
* React Router
* TanStack Query
* React Hook Form
* Zod 또는 유사 검증 도구

다만 이 문서는 특정 라이브러리 사용을 강제하지 않는다.

---

## 4. 권장 디렉터리 구조

```text
src
├─ app
│  ├─ router
│  ├─ providers
│  └─ layout
├─ pages
│  ├─ repositories
│  ├─ issues
│  ├─ users
│  └─ labels
├─ features
│  ├─ repository
│  ├─ issue
│  ├─ user
│  ├─ comment
│  └─ label
├─ entities
│  ├─ repository
│  ├─ issue
│  ├─ user
│  ├─ comment
│  └─ label
├─ shared
│  ├─ api
│  ├─ ui
│  ├─ lib
│  ├─ constants
│  └─ types
└─ widgets
   ├─ navigation
   ├─ issue-list
   ├─ issue-detail
   └─ forms
```

이 구조는 페이지, 기능, 공통 요소를 분리하기 위한 기준이다.

---

## 5. 레이어별 역할

### 5.1 `app`

역할

* 앱 초기화
* 라우터 연결
* Query Client, Theme, Error Boundary 같은 전역 provider 등록
* 공통 레이아웃 정의

### 5.2 `pages`

역할

* 라우트 단위 진입 컴포넌트
* 페이지 수준 데이터 조합
* 화면 배치 결정

원칙

* 페이지는 비즈니스 로직을 최소화한다.
* 도메인별 세부 로직은 `features`, `widgets`로 위임한다.

### 5.3 `features`

역할

* 사용자 액션 중심 기능 구현
* 생성, 수정, 삭제, 상태 변경 같은 인터랙션 처리
* 폼 로직 및 액션 훅 제공

예시

* `features/issue/create-issue`
* `features/issue/update-issue-status`
* `features/comment/create-comment`
* `features/label/attach-label`

### 5.4 `entities`

역할

* 도메인 타입 정의
* 도메인별 API 함수
* 도메인별 조회 훅
* 포맷터와 작은 표현 컴포넌트

예시

* `entities/issue/model/types.ts`
* `entities/issue/api/issueApi.ts`
* `entities/issue/model/useIssueDetail.ts`
* `entities/label/ui/LabelBadge.tsx`

### 5.5 `widgets`

역할

* 여러 feature와 entity를 조합한 중간 수준 UI 블록
* 페이지에서 직접 재사용 가능한 화면 조각 구성

예시

* IssueFilterBar
* IssueDetailSection
* RepositorySidebar
* CommentSection

### 5.6 `shared`

역할

* 공통 API 클라이언트
* 디자인 시스템 기초 컴포넌트
* 유틸 함수
* 에러 변환 로직
* 상수 및 공통 타입

---

## 6. 도메인별 구조 예시

Issue 도메인을 기준으로 보면 다음처럼 나눌 수 있다.

```text
src/features/issue
├─ create-issue
├─ update-issue
├─ update-issue-status
├─ update-issue-priority
├─ update-issue-assignee
└─ delete-issue

src/entities/issue
├─ api
├─ model
└─ ui
```

Repository, User, Comment, Label도 동일한 방식으로 맞춘다.

---

## 7. API 계층 구조

### 7.1 공통 API 클라이언트

`shared/api`에는 공통 HTTP 클라이언트를 둔다.

예시 역할

* base URL 설정
* 공통 헤더 처리
* JSON 직렬화/역직렬화
* 공통 에러 응답 파싱

### 7.2 도메인별 API 파일

각 도메인의 실제 엔드포인트 함수는 `entities/*/api`에 둔다.

예시

* `entities/repository/api/repositoryApi.ts`
* `entities/user/api/userApi.ts`
* `entities/issue/api/issueApi.ts`
* `entities/comment/api/commentApi.ts`
* `entities/label/api/labelApi.ts`

### 7.3 API 함수 원칙

* 함수 이름은 동작이 드러나야 한다.
* 서버 응답 타입을 명시한다.
* UI 친화적 데이터 가공은 API 함수가 아니라 model 또는 selector 층에서 한다.

예시 함수

* `getRepositories`
* `createRepository`
* `getIssues`
* `getIssueDetail`
* `updateIssueStatus`
* `createComment`
* `attachLabelToIssue`

---

## 8. 타입 구조

도메인 타입은 백엔드 DTO 기준으로 맞추되, 프론트에서 필요한 입력 타입과 조회 타입을 분리하는 것이 좋다.

예시

* 조회 응답 타입
* 생성 요청 타입
* 수정 요청 타입
* 필터 파라미터 타입
* enum 타입

Issue 기준 예시

* `IssueSummary`
* `IssueDetail`
* `CreateIssuePayload`
* `UpdateIssuePayload`
* `IssueFilter`
* `IssueStatus`
* `IssuePriority`

---

## 9. 서버 상태와 UI 상태 분리

### 9.1 서버 상태

서버 상태는 API에서 내려오는 데이터이며 캐시 대상이다.

예시

* Repository 목록
* 특정 Repository 정보
* Issue 목록
* Issue 상세
* User 목록
* Label 목록
* Comment 목록

### 9.2 UI 상태

UI 상태는 사용자 인터랙션에 한정되는 로컬 상태이다.

예시

* 검색 입력값
* 드롭다운 열림 여부
* 선택된 Label
* 수정 모드 여부
* 삭제 확인 다이얼로그 상태

원칙

* 서버에서 재조회 가능한 데이터는 전역 UI 상태로 중복 저장하지 않는다.

---

## 10. Query Key 제안

서버 상태 캐시 도구를 사용할 경우 Query Key를 일관되게 정의한다.

예시

* `['repositories']`
* `['repository', repositoryId]`
* `['issues', repositoryId, filters]`
* `['issue', repositoryId, issueId]`
* `['comments', repositoryId, issueId]`
* `['users', filters]`
* `['labels', repositoryId]`

이 기준이 있으면 생성/수정/삭제 후 무효화 전략을 단순하게 가져갈 수 있다.

---

## 11. 페이지 조합 예시

### 11.1 Repository 목록 페이지

구성 예시

* `RepositoryPage`
* `RepositoryListWidget`
* `CreateRepositoryButton`
* `EditRepositoryDialog`
* `DeleteRepositoryDialog`

### 11.2 Issue 목록 페이지

구성 예시

* `IssueListPage`
* `RepositoryHeader`
* `IssueFilterBar`
* `IssueListWidget`
* `CreateIssueButton`

### 11.3 Issue 상세 페이지

구성 예시

* `IssueDetailPage`
* `IssueSummaryCard`
* `IssueStatusControl`
* `IssuePriorityControl`
* `IssueAssigneeControl`
* `IssueLabelSection`
* `CommentSection`
* `DeleteIssueButton`

---

## 12. 폼 구조 원칙

폼은 생성과 수정을 최대한 재사용 가능하게 구성한다.

예시

* `RepositoryForm`
* `IssueForm`
* `UserForm`
* `LabelForm`
* `CommentForm`

원칙

* 입력 필드와 제출 로직을 분리한다.
* 서버 검증 오류와 클라이언트 검증 오류를 함께 처리한다.
* 생성과 수정 차이는 초기값과 제출 함수로 분리한다.

---

## 13. 에러 처리 구조

### 13.1 공통 에러 변환

`shared/lib` 또는 `shared/api`에 서버 에러를 UI 메시지로 바꾸는 함수를 둔다.

예시 매핑

* `DUPLICATE_USER_USERNAME` -> 이미 사용 중인 사용자명입니다.
* `DUPLICATE_USER_EMAIL` -> 이미 사용 중인 이메일입니다.
* `USER_DELETE_CONFLICT` -> 담당 중인 이슈 또는 작성한 댓글이 있어 삭제할 수 없습니다.
* `DUPLICATE_LABEL_NAME` -> 같은 저장소에 동일한 이름의 라벨이 이미 존재합니다.

### 13.2 화면 처리 원칙

* 폼 오류는 필드 근처에 노출
* 시스템 오류는 배너 또는 toast로 노출
* 치명적 로딩 실패는 페이지 단위 오류 상태로 처리

---

## 14. 스타일 구조 제안

스타일 방식은 팀 선택에 따르되, 구조 기준은 명확히 유지한다.

권장 방식 예시

* CSS Modules
* Tailwind CSS
* Vanilla Extract
* Styled Components

원칙

* 페이지 전용 스타일과 공통 UI 스타일을 분리한다.
* 상태 스타일은 컴포넌트 내부에 응집시킨다.
* Label 색상처럼 도메인 기반 시각 정보는 공통 유틸 또는 UI 컴포넌트로 묶는다.

---

## 15. 프론트 초기 구현 단위 제안

1. 공통 앱 구조와 라우터 설정
2. API 클라이언트와 공통 에러 처리
3. Repository 조회/생성/수정/삭제
4. Issue 목록과 필터
5. Issue 상세와 상태 변경
6. Comment 및 Label 섹션
7. User 관리

---

## 16. 예시 폴더 매핑

```text
src/pages/issues/IssueDetailPage.tsx
src/widgets/issue-detail/IssueDetailSection.tsx
src/features/issue/update-issue-status/ui/IssueStatusControl.tsx
src/features/issue/update-issue-status/model/useUpdateIssueStatus.ts
src/entities/issue/api/issueApi.ts
src/entities/issue/model/types.ts
```

이런 식으로 페이지, 위젯, 기능, 엔티티 레이어를 분리하면 규모가 커져도 유지보수가 쉽다.

---

## 17. 요약

프론트 구조는 페이지 진입점, 도메인 기능, 공통 인프라를 분리하는 방향으로 설계하는 것이 적절하다.
현재 프로젝트는 Repository와 Issue 중심 흐름이 핵심이므로, API 계층과 Issue 상세 조합 구조를 먼저 안정화하는 것이 전체 프론트 아키텍처의 기준점이 된다.
