# Data Model

## 1. 개요

이 문서는 1차 구현 범위의 이슈 관리 시스템 데이터 모델을 정의한다.
현재 범위에서는 다음 도메인을 다룬다.

* Repository
* User
* Issue
* Comment
* Label
* IssueLabel

인증은 아직 범위에 포함하지 않지만, 사용자 데이터는 독립 엔터티로 관리한다.
Issue는 선택적으로 담당 사용자와 연결될 수 있고, Comment는 작성 사용자와 연결된다.

---

## 2. 설계 원칙

### 2.1 Repository 중심 구조

모든 Issue는 반드시 하나의 Repository에 속한다.
Label도 Repository 단위로 관리된다.

### 2.2 명시적 관계 관리

* Repository 1:N Issue
* Repository 1:N Label
* User 1:N Issue
* User 1:N Comment
* Issue 1:N Comment
* Issue N:M Label

### 2.3 확장성 고려

추후 GitHub 연동 시에도 내부 데이터 모델을 유지할 수 있도록 도메인 중심으로 설계한다.
필요하면 GitHub 식별자 컬럼을 추가할 수 있는 구조를 고려한다.

---

## 3. 엔터티 정의

## 3.1 Repository

설명

프로젝트 또는 저장소를 나타낸다.
이슈와 라벨은 반드시 특정 Repository에 속한다.

필드

* id
* name
* description
* createdAt
* updatedAt

예시

```text
Repository
- id: Long
- name: String
- description: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

제약 조건

* name은 null 또는 blank 불가
* name 길이 제한 필요
* createdAt, updatedAt은 자동 관리 권장

비고

코드 구현 시 Spring Data Repository와 이름 충돌을 피하기 위해 엔터티명은 `RepositoryEntity` 또는 `ProjectRepository`를 고려할 수 있다.

---

## 3.2 User

설명

이슈 담당자와 댓글 작성자에 사용되는 사용자 엔터티다.
인증 정보와 분리된 프로필성 데이터로 시작하고, 추후 인증 계정과 연결할 수 있다.

필드

* id
* username
* displayName
* email
* role
* createdAt
* updatedAt

예시

```text
User
- id: Long
- username: String
- displayName: String
- email: String
- role: UserRole
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

제약 조건

* username은 null 또는 blank 불가
* displayName은 null 또는 blank 불가
* email은 null 또는 blank 불가
* username은 유니크
* email은 유니크
* role은 ADMIN 또는 MEMBER

---

## 3.3 Issue

설명

Repository 내부에서 관리되는 작업 단위 또는 버그 리포트다.
담당자는 선택적으로 연결할 수 있다.

필드

* id
* repository
* title
* content
* status
* priority
* assignee
* createdAt
* updatedAt

예시

```text
Issue
- id: Long
- repository: Repository
- title: String
- content: String
- status: IssueStatus
- priority: IssuePriority
- assignee: User
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

제약 조건

* repository는 필수
* title은 null 또는 blank 불가
* status는 OPEN 또는 CLOSED
* priority는 LOW, MEDIUM, HIGH
* assignee는 null 가능
* 생성 시 status 기본값은 OPEN
* 생성 시 priority 기본값은 MEDIUM 권장

---

## 3.4 Comment

설명

Issue의 처리 과정, 논의, 진행 기록을 나타낸다.
모든 Comment는 작성 사용자와 연결된다.

필드

* id
* issue
* author
* content
* createdAt

예시

```text
Comment
- id: Long
- issue: Issue
- author: User
- content: String
- createdAt: LocalDateTime
```

제약 조건

* issue는 필수
* author는 필수
* content는 null 또는 blank 불가

---

## 3.5 Label

설명

Issue를 분류하기 위한 태그다.
Repository 단위로 관리된다.

필드

* id
* repository
* name
* color
* createdAt
* updatedAt

예시

```text
Label
- id: Long
- repository: Repository
- name: String
- color: String
- createdAt: LocalDateTime
- updatedAt: LocalDateTime
```

제약 조건

* repository는 필수
* name은 null 또는 blank 불가
* 같은 repository 내 label name 중복 불가
* color는 hex 문자열 형식 검증 고려 가능

---

## 3.6 IssueLabel

설명

Issue와 Label은 다대다 관계이므로 중간 매핑 테이블 또는 매핑 엔터티가 필요하다.

필드

* id 또는 복합 키
* issue
* label

예시

```text
IssueLabel
- issue: Issue
- label: Label
```

제약 조건

* 같은 issue와 label 조합은 중복 불가
* issue와 label은 동일 repository 소속이어야 한다

구현 선택지

1. JPA ManyToMany 직접 사용
2. 명시적 매핑 엔터티 사용

권장

명시적 매핑 엔터티 또는 조인 테이블을 명확히 두는 방식이 유지보수와 확장 측면에서 더 유리하다.

---

## 4. 열거형 정의

## 4.1 UserRole

```text
ADMIN
MEMBER
```

설명

* ADMIN: 사용자 및 운영성 데이터 관리 권한을 가진 역할
* MEMBER: 일반 사용자 역할

---

## 4.2 IssueStatus

```text
OPEN
CLOSED
```

설명

* OPEN: 진행 중 또는 미해결 상태
* CLOSED: 완료 또는 종료 상태

---

## 4.3 IssuePriority

```text
LOW
MEDIUM
HIGH
```

설명

* LOW: 낮은 우선순위
* MEDIUM: 보통 우선순위
* HIGH: 높은 우선순위

---

## 5. 관계 정의

## 5.1 Repository 와 Issue

관계

* Repository 1:N Issue

설명

* 하나의 Repository는 여러 개의 Issue를 가진다.
* 하나의 Issue는 반드시 하나의 Repository에 속한다.

삭제 정책

* Repository 삭제 시 하위 Issue를 함께 삭제한다.

---

## 5.2 Repository 와 Label

관계

* Repository 1:N Label

설명

* 하나의 Repository는 여러 Label을 가진다.
* 하나의 Label은 반드시 하나의 Repository에 속한다.

삭제 정책

* Repository 삭제 시 하위 Label을 함께 삭제한다.

---

## 5.3 User 와 Issue

관계

* User 1:N Issue

설명

* 하나의 User는 여러 Issue의 담당자가 될 수 있다.
* 하나의 Issue는 0명 또는 1명의 담당자를 가진다.

삭제 정책

* 담당 중인 Issue가 있는 User는 삭제를 제한한다.

---

## 5.4 User 와 Comment

관계

* User 1:N Comment

설명

* 하나의 User는 여러 Comment를 작성할 수 있다.
* 하나의 Comment는 반드시 한 명의 작성자를 가진다.

삭제 정책

* 작성 Comment가 있는 User는 삭제를 제한한다.

---

## 5.5 Issue 와 Comment

관계

* Issue 1:N Comment

설명

* 하나의 Issue에는 여러 Comment가 달릴 수 있다.
* 하나의 Comment는 반드시 하나의 Issue에 속한다.

삭제 정책

* Issue 삭제 시 하위 Comment를 함께 삭제한다.

---

## 5.6 Issue 와 Label

관계

* Issue N:M Label

설명

* 하나의 Issue는 여러 Label을 가질 수 있다.
* 하나의 Label은 여러 Issue에 연결될 수 있다.
* 단, 같은 Repository 범위 내에서만 연결 가능하다.

삭제 정책

* Issue 삭제 시 IssueLabel 매핑 제거
* Label 삭제 시 IssueLabel 매핑 제거

---

## 6. ERD 개념 표현

```text
Repository
  +--< Issue >--0..1 User
  |      |
  |      +--< Comment >-- User
  |
  +--< Label

Issue >--< IssueLabel >-- Label
```

---

## 7. 테이블 설계 예시

## 7.1 repositories

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | Repository 식별자 |
| name | varchar | not null | Repository 이름 |
| description | varchar | null 가능 | Repository 설명 |
| created_at | datetime | not null | 생성 시각 |
| updated_at | datetime | not null | 수정 시각 |

권장 인덱스

* unique index 또는 일반 index on name 검토

주의

Repository 이름의 전역 유니크 여부는 요구사항에 따라 결정한다.
현재 1차 범위에서는 전역 유니크 제약은 선택 사항이다.

---

## 7.2 users

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | User 식별자 |
| username | varchar | not null, unique | 로그인 또는 계정 식별용 이름 |
| display_name | varchar | not null | 사용자 표시 이름 |
| email | varchar | not null, unique | 사용자 이메일 |
| role | varchar | not null | ADMIN 또는 MEMBER |
| created_at | datetime | not null | 생성 시각 |
| updated_at | datetime | not null | 수정 시각 |

권장 인덱스

* unique index on username
* unique index on email
* index on role

---

## 7.3 issues

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | Issue 식별자 |
| repository_id | bigint | FK, not null | 소속 Repository |
| assignee_id | bigint | FK, null 가능 | 담당 User |
| title | varchar | not null | Issue 제목 |
| content | text | null 가능 | Issue 내용 |
| status | varchar | not null | OPEN 또는 CLOSED |
| priority | varchar | not null | LOW, MEDIUM, HIGH |
| created_at | datetime | not null | 생성 시각 |
| updated_at | datetime | not null | 수정 시각 |

권장 인덱스

* index on repository_id
* index on assignee_id
* index on status
* index on priority
* composite index on repository_id, status
* composite index on repository_id, assignee_id

---

## 7.4 comments

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | Comment 식별자 |
| issue_id | bigint | FK, not null | 소속 Issue |
| author_id | bigint | FK, not null | 작성 User |
| content | text | not null | 댓글 내용 |
| created_at | datetime | not null | 생성 시각 |

권장 인덱스

* index on issue_id
* index on author_id

---

## 7.5 labels

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| id | bigint | PK | Label 식별자 |
| repository_id | bigint | FK, not null | 소속 Repository |
| name | varchar | not null | Label 이름 |
| color | varchar | not null 또는 정책에 따라 null 가능 | 색상 코드 |
| created_at | datetime | not null | 생성 시각 |
| updated_at | datetime | not null | 수정 시각 |

권장 제약

* unique(repository_id, name)

권장 인덱스

* index on repository_id

---

## 7.6 issue_labels

| 컬럼명 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| issue_id | bigint | FK, not null | 연결된 Issue |
| label_id | bigint | FK, not null | 연결된 Label |

권장 제약

* primary key 또는 unique(issue_id, label_id)

권장 인덱스

* index on issue_id
* index on label_id

---

## 8. JPA 매핑 예시 방향

## 8.1 Repository 와 Issue

* Repository has many Issue
* Issue belongs to Repository

예시 방향

```text
RepositoryEntity
  - List<Issue> issues

Issue
  - RepositoryEntity repository
```

---

## 8.2 User 와 Issue

* User has many assigned Issue
* Issue optionally belongs to User as assignee

예시 방향

```text
User
  - List<Issue> assignedIssues

Issue
  - User assignee
```

---

## 8.3 Issue 와 Comment

* Issue has many Comment
* Comment belongs to Issue

예시 방향

```text
Issue
  - List<Comment> comments

Comment
  - Issue issue
```

---

## 8.4 User 와 Comment

* User has many Comment
* Comment belongs to User

예시 방향

```text
User
  - List<Comment> comments

Comment
  - User author
```

---

## 8.5 Repository 와 Label

* Repository has many Label
* Label belongs to Repository

예시 방향

```text
RepositoryEntity
  - List<Label> labels

Label
  - RepositoryEntity repository
```

---

## 8.6 Issue 와 Label

권장 방향

* 직접 ManyToMany보다는 조인 테이블 또는 매핑 엔터티 사용

예시 방향

```text
Issue
  - Set<Label> labels

Label
  - Set<Issue> issues
```

또는

```text
IssueLabel
  - Issue issue
  - Label label
```

---

## 9. 감사 필드

권장 공통 필드

* createdAt
* updatedAt

적용 대상

* Repository
* User
* Issue
* Label

Comment는 수정 기능이 없다면 createdAt만으로 충분하다.
추후 댓글 수정 기능이 생기면 updatedAt 추가를 고려할 수 있다.

구현 방법

* BaseTimeEntity 공통 상속
* JPA Auditing 사용 고려

---

## 10. 삭제 및 무결성 정책

### Repository 삭제

삭제 대상

* issues
* comments
* labels
* issue_labels

### User 삭제

삭제 제한 대상

* assignee로 연결된 issues
* author로 연결된 comments

### Issue 삭제

삭제 대상

* comments
* issue_labels

### Label 삭제

삭제 대상

* issue_labels

무결성 장치

* Issue는 존재하지 않는 Repository를 참조할 수 없다.
* Issue의 assignee는 존재하는 User여야 한다.
* Comment는 존재하지 않는 Issue를 참조할 수 없다.
* Comment의 author는 존재하는 User여야 한다.
* Label은 존재하지 않는 Repository를 참조할 수 없다.
* IssueLabel은 같은 Repository 범위를 벗어나는 연결을 허용하지 않는다.

---

## 11. 향후 확장 고려사항

1차 범위에서는 사용하지 않지만 이후 확장을 고려해 다음 컬럼을 추가할 수 있다.

### User 확장 예시

* authProvider
* externalUserId
* avatarUrl
* lastLoginAt

### Repository 확장 예시

* githubOwner
* githubRepoName
* githubRepoId
* integrationType

### Issue 확장 예시

* externalIssueId
* externalUrl
* syncedAt

### Comment 확장 예시

* externalCommentId

### Label 확장 예시

* externalLabelId

이 구조를 통해 내부 데이터 모델을 유지하면서도 GitHub API와 연결할 수 있다.

---

## 12. 요약

1차 구현 범위의 데이터 모델은 Repository 중심 구조를 따르며,
User를 별도 엔터티로 도입해 Issue 담당자와 Comment 작성자를 명시적으로 관리한다.
Issue는 Repository 하위 리소스이고 Comment는 Issue에 종속되며,
Label은 Repository 단위로 관리되고 Issue와는 다대다 관계를 가진다.

현재는 내부 DB 기반 모델로 구현하되, 이후 GitHub 연동과 인증 기능 확장을 고려해 다음을 유지한다.

* 명시적인 도메인 관계
* DTO와 Entity 분리 가능 구조
* 매핑 엔터티 확장 가능성
* 외부 식별자 추가 가능성

