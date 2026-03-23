<!-- 어떻게 만들지 정의하는 아키텍처 설계 문서 -->

# Architecture

## 1. 개요

이 프로젝트는 GitHub 이슈 관리 방식을 참고한 내부 이슈 관리 백엔드 시스템이다.
현재 1차 구현 범위에서는 외부 GitHub API와 직접 연동하지 않고, 내부 데이터베이스를 기반으로 Repository, User, Issue, Comment, Label 도메인을 관리한다.

설계의 주요 목표는 다음과 같다.

* Repository 중심의 이슈 관리 구조를 만든다.
* User를 별도 리소스로 관리해 담당자와 작성자 정보를 일관되게 다룬다.
* REST API 기반 백엔드 아키텍처를 구현한다.
* 계층 분리를 명확히 해 유지보수성과 테스트 용이성을 높인다.
* 이후 GitHub API 연동과 인증 기능 확장이 가능하도록 구조를 열어 둔다.

---

## 2. 아키텍처 스타일

본 프로젝트는 전형적인 Spring Boot 계층형 아키텍처를 따른다.

구성 계층은 다음과 같다.

* Controller
* Service
* Repository
* Domain Entity
* DTO
* Exception Handling

이 구조를 통해 요청 처리, 비즈니스 로직, 데이터 접근 책임을 분리한다.

---

## 3. 전체 흐름

클라이언트 요청은 Controller에서 수신한다.
Controller는 요청 DTO를 검증하고 Service에 전달한다.
Service는 비즈니스 규칙을 수행하고 Repository를 통해 데이터를 조회 또는 저장한다.
Service는 결과를 응답 DTO로 변환하고 Controller는 JSON 응답을 반환한다.

흐름 예시

1. 클라이언트가 특정 Repository에 Issue 생성 요청을 보낸다.
2. IssueController가 요청을 수신한다.
3. IssueService가 Repository 존재 여부를 확인한다.
4. assigneeId가 있으면 UserService 또는 UserRepository를 통해 사용자 존재 여부를 확인한다.
5. IssueService가 Issue 엔터티를 생성하고 저장한다.
6. 저장 결과를 IssueResponse DTO로 변환한다.
7. Controller가 응답을 반환한다.

---

## 4. 계층별 역할

### 4.1 Controller 계층

역할

* HTTP 요청 수신
* 요청 파라미터와 Body 검증
* Service 호출
* HTTP 상태 코드와 응답 반환

원칙

* 비즈니스 로직을 포함하지 않는다.
* Entity를 직접 반환하지 않는다.
* 요청과 응답은 DTO를 사용한다.

예상 Controller

* RepositoryController
* UserController
* IssueController
* CommentController
* LabelController

---

### 4.2 Service 계층

역할

* 비즈니스 로직 처리
* 도메인 규칙 검증
* 트랜잭션 처리
* Entity와 DTO 간 변환 조합
* 여러 Repository 호출 조합

원칙

* 도메인 비즈니스 흐름은 Service에 둔다.
* 단순 조회와 수정도 도메인 규칙이 반영되면 Service에서 처리한다.
* 이후 인증 또는 외부 연동이 추가돼도 Service 인터페이스는 안정적으로 유지한다.

예상 Service

* RepositoryService
* UserService
* IssueService
* CommentService
* LabelService

확장 고려

추후 GitHub 연동 시 다음과 같은 Client 또는 Adapter 계층을 추가할 수 있다.

* GithubRepositoryClient
* GithubIssueClient
* GithubCommentClient
* GithubLabelClient
* GithubUserClient

현재 1차 구현에서는 사용하지 않지만 Service가 외부 API 상세 구현에 직접 의존하지 않도록 설계한다.

---

### 4.3 Repository 계층

역할

* 데이터베이스 접근
* Entity 저장, 조회, 삭제
* 조건 검색

원칙

* Spring Data JPA 기반으로 구현한다.
* 복잡한 도메인 판단은 Repository가 아니라 Service에서 수행한다.

예상 Repository

* RepositoryEntityRepository
* UserRepository
* IssueRepository
* CommentRepository
* LabelRepository
* IssueLabelRepository 또는 Issue 엔터티 기반 연관관계 관리

주의

도메인 이름으로 Repository를 사용하므로 Spring Data Repository와 이름 충돌을 피하기 위해 Repository 도메인 엔터티명은 `RepositoryEntity` 또는 `ProjectRepository`를 고려할 수 있다.

---

## 5. 도메인 중심 구조

### 5.1 주요 도메인 관계

* 하나의 Repository는 여러 Issue를 가진다.
* 하나의 Repository는 여러 Label을 가진다.
* 하나의 User는 여러 Issue의 담당자가 될 수 있다.
* 하나의 User는 여러 Comment를 작성할 수 있다.
* 하나의 Issue는 여러 Comment를 가진다.
* 하나의 Issue는 여러 Label을 가질 수 있다.
* 하나의 Label은 여러 Issue에 연결될 수 있다.

즉 다음 관계를 가진다.

* Repository 1:N Issue
* Repository 1:N Label
* User 1:N Issue
* User 1:N Comment
* Issue 1:N Comment
* Issue N:M Label

---

## 6. 패키지 구조 예시

```text
com.example.issuetracker
+-- controller
|   +-- RepositoryController
|   +-- UserController
|   +-- IssueController
|   +-- CommentController
|   +-- LabelController
+-- service
|   +-- RepositoryService
|   +-- UserService
|   +-- IssueService
|   +-- CommentService
|   +-- LabelService
+-- repository
|   +-- RepositoryEntityRepository
|   +-- UserRepository
|   +-- IssueRepository
|   +-- CommentRepository
|   +-- LabelRepository
+-- domain
|   +-- RepositoryEntity
|   +-- User
|   +-- Issue
|   +-- Comment
|   +-- Label
|   +-- IssueStatus
|   +-- IssuePriority
|   +-- UserRole
+-- dto
|   +-- repository
|   +-- user
|   +-- issue
|   +-- comment
|   +-- label
+-- exception
|   +-- GlobalExceptionHandler
|   +-- ErrorResponse
|   +-- RepositoryNotFoundException
|   +-- UserNotFoundException
|   +-- IssueNotFoundException
|   +-- CommentNotFoundException
|   +-- LabelNotFoundException
|   +-- DuplicateLabelNameException
|   +-- DuplicateUserUsernameException
|   +-- DuplicateUserEmailException
|   +-- UserDeleteConflictException
+-- config
```

---

## 7. 트랜잭션 전략

Service 계층에서 트랜잭션을 관리한다.

기본 원칙

* 조회 로직은 readOnly 트랜잭션 사용 고려
* 생성, 수정, 삭제는 일반 트랜잭션 사용
* 하나의 유스케이스에서 여러 엔터티가 함께 변경되면 하나의 트랜잭션으로 묶는다

예시

* Repository 삭제 시 하위 Issue, Comment, Label 정리
* Issue 생성 시 Repository 검증과 assignee 검증 후 저장
* User 삭제 시 assignee 또는 comment author 참조 여부 확인
* Issue 삭제 시 Comment 및 Label 연결 정보 정리

---

## 8. 예외 처리 전략

예외 처리는 Global Exception Handler에서 일관되게 수행한다.

주요 예외 예시

* RepositoryNotFoundException
* UserNotFoundException
* IssueNotFoundException
* CommentNotFoundException
* LabelNotFoundException
* DuplicateUserUsernameException
* DuplicateUserEmailException
* UserDeleteConflictException
* DuplicateLabelNameException
* InvalidIssueStatusException
* InvalidIssuePriorityException
* InvalidUserRoleException

응답 원칙

* 공통 에러 응답 포맷 사용
* HTTP 상태 코드와 비즈니스 오류 메시지 구분
* Validation 오류와 도메인 오류 분리

예시 응답

```json
{
  "code": "USER_NOT_FOUND",
  "message": "User not found",
  "timestamp": "2026-03-23T10:00:00"
}
```

---

## 9. DTO 사용 원칙

Entity를 외부에 직접 노출하지 않는다.

요청 DTO 예시

* CreateRepositoryRequest
* UpdateRepositoryRequest
* CreateUserRequest
* UpdateUserRequest
* CreateIssueRequest
* UpdateIssueRequest
* UpdateIssueStatusRequest
* UpdateIssuePriorityRequest
* UpdateIssueAssigneeRequest
* CreateCommentRequest
* CreateLabelRequest

응답 DTO 예시

* RepositoryResponse
* UserResponse
* UserSummaryResponse
* IssueResponse
* IssueSummaryResponse
* CommentResponse
* LabelResponse

이 원칙을 통해 다음을 확보한다.

* API 스펙과 내부 모델 분리
* 응답 구조 제어 가능
* 이후 인증, GitHub 연동 시에도 내부 모델 변경 여지 확보

---

## 10. 검색 및 조회 전략

1차 구현 범위에서는 Repository 내부의 Issue와 사용자 목록에 대해 기본 검색과 필터링을 제공한다.

지원 조건

* Issue 제목 부분 검색
* Issue status 필터
* Issue priority 필터
* Issue label 필터
* Issue assignee 필터
* User username 부분 검색
* User displayName 부분 검색
* User role 필터

구현 방향

* 초기에는 Spring Data JPA 메서드 또는 단순 조건 조합 사용
* 검색 조건이 늘어나면 Specification 또는 QueryDSL로 확장 고려

---

## 11. 삭제 정책

### Repository 삭제

Repository 삭제 시 해당 Repository에 속한 다음 데이터도 함께 삭제한다.

* Issue
* Comment
* Label
* Issue와 Label 연결 정보

### User 삭제

User 삭제는 참조 무결성을 고려해 제한한다.

* 담당 중인 Issue가 있으면 삭제 불가
* 작성한 Comment가 있으면 삭제 불가

### Issue 삭제

Issue 삭제 시 다음 데이터를 함께 삭제 또는 정리한다.

* 해당 Issue의 Comment
* 해당 Issue의 Label 연결 정보

### Label 삭제

Label 삭제 시 연결된 Issue와의 매핑만 제거하고 Label 자체를 삭제한다.

---

## 12. 로깅 전략

주요 비즈니스 흐름은 로그를 남긴다.

기록 대상 예시

* User 생성, 수정, 삭제 요청
* Repository 생성 요청
* Issue 생성 및 상태 변경
* Issue assignee 변경
* Label 추가 및 제거
* 예외 발생 상황

원칙

* 요청 성공과 실패 흐름을 구분해 기록한다.
* 민감 데이터는 로그에 남기지 않는다.
* 추적 가능한 최소 식별자만 포함한다.

예시

* userId
* repositoryId
* issueId
* labelId

---

## 13. 테스트 전략

테스트는 다음 수준을 고려한다.

### Service 테스트

도메인 비즈니스 로직 검증

예시

* 존재하지 않는 User를 assignee로 지정하면 예외 발생
* Issue 생성 시 기본 상태가 OPEN인지 확인
* 같은 username 또는 email로 User 생성 시 예외 발생
* User 삭제 시 참조 중인 Issue 또는 Comment가 있으면 실패하는지 확인

### Controller 테스트

API 요청과 응답 검증

예시

* 잘못된 요청 DTO에 대한 Validation 오류 응답
* 정상 User 생성 시 HTTP 201 반환 확인
* 존재하지 않는 리소스 요청 시 적절한 에러 코드 반환 확인

---

## 14. GitHub 연동 및 인증 확장 고려사항

현재 단계에서는 내부 DB가 source of truth다.
이후 GitHub 연동 또는 인증 기능 추가 시 다음 방향으로 확장할 수 있다.

### 확장 방향

* Repository를 GitHub repository와 연결
* 내부 User와 외부 계정 매핑
* 외부 GitHub API 호출용 Client 추가
* 인증 계정과 프로필성 User 데이터를 분리하거나 연결

### 설계 원칙

* Controller는 GitHub API를 직접 호출하지 않는다.
* Service는 외부 연동 구현 상세에 과도하게 의존하지 않는다.
* 인증 정보와 사용자 프로필 데이터를 필요 시 분리 가능하게 유지한다.

예시 확장 구조

```text
Controller
  -> Service
    -> Internal Repository
    -> Auth Adapter
    -> Github Client Adapter
```

이 구조를 통해 초기 내부 구현과 이후 외부 연동을 자연스럽게 연결할 수 있다.

---

## 15. 요약

이 프로젝트의 1차 아키텍처는 내부 DB 기반의 Spring Boot 계층형 구조를 따른다.
Repository를 중심으로 Issue, Comment, Label을 관리하고, User를 별도 도메인으로 추가해 담당자와 작성자 정보를 명시적으로 다룬다.

현재는 내부 서비스 구현에 집중하지만, 이후 GitHub API 연동과 인증 기능 확장이 가능하도록 다음 원칙을 유지한다.

* 도메인 중심 설계
* 계층 분리
* DTO 기반 API
* 일관된 예외 처리
* 확장 가능한 서비스 구조
