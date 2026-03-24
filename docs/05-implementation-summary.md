# Implementation Summary

## 1. 개요

이 문서는 `docs`에 정의된 아키텍처, API, 데이터 모델을 바탕으로
현재 코드베이스에 실제로 반영된 구현 사항을 정리한다.

이번 변경에서는 다음 도메인을 구현했다.

* Repository
* User
* Issue
* Comment
* Label

구현 범위는 Spring Boot 기반 REST API, JPA 엔터티, 서비스 계층, DTO, 예외 처리, 기본 통합 테스트까지 포함한다.

---

## 2. 구현된 패키지 구조

현재 주요 소스 구조는 다음과 같다.

```text
com.jw.github_issue_manager
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
|   +-- UserRole
|   +-- IssueStatus
|   +-- IssuePriority
+-- dto
|   +-- repository
|   +-- user
|   +-- issue
|   +-- comment
|   +-- label
+-- exception
|   +-- ErrorResponse
|   +-- GlobalExceptionHandler
|   +-- RepositoryNotFoundException
|   +-- UserNotFoundException
|   +-- IssueNotFoundException
|   +-- CommentNotFoundException
|   +-- LabelNotFoundException
|   +-- DuplicateUserUsernameException
|   +-- DuplicateUserEmailException
|   +-- UserDeleteConflictException
|   +-- DuplicateLabelNameException
|   +-- LabelAlreadyAttachedException
```

---

## 3. 도메인별 구현 사항

### 3.1 Repository

구현 내용

* Repository 생성, 목록 조회, 단건 조회, 수정, 삭제 API 구현
* `RepositoryEntity` 엔터티 구현
* `createdAt`, `updatedAt` 자동 갱신 처리
* 하위 `Issue`, `Label`에 대해 cascade 및 orphan removal 설정

관련 클래스

* `domain/RepositoryEntity`
* `repository/RepositoryEntityRepository`
* `service/RepositoryService`
* `controller/RepositoryController`
* `dto/repository/*`

### 3.2 User

구현 내용

* User 생성, 목록 조회, 단건 조회, 수정, 삭제 API 구현
* `username`, `email` 중복 검증 추가
* `displayName`, `email`, `role` 수정 기능 구현
* assignee 또는 comment author로 참조 중인 경우 삭제 차단

관련 클래스

* `domain/User`
* `domain/UserRole`
* `repository/UserRepository`
* `service/UserService`
* `controller/UserController`
* `dto/user/*`

### 3.3 Issue

구현 내용

* Issue 생성, 목록 조회, 단건 조회, 수정, 삭제 API 구현
* 상태 변경 API 구현
* 우선순위 변경 API 구현
* 담당자 변경 API 구현
* assignee, labels를 포함한 응답 DTO 구현
* keyword, status, priority, labelId, assigneeId 기준 필터 조회 지원

관련 클래스

* `domain/Issue`
* `domain/IssueStatus`
* `domain/IssuePriority`
* `repository/IssueRepository`
* `service/IssueService`
* `controller/IssueController`
* `dto/issue/*`

### 3.4 Comment

구현 내용

* Comment 생성, 목록 조회, 삭제 API 구현
* 작성자는 `authorId` 기반으로 연결
* 응답에는 작성자 요약 정보 포함

관련 클래스

* `domain/Comment`
* `repository/CommentRepository`
* `service/CommentService`
* `controller/CommentController`
* `dto/comment/*`

### 3.5 Label

구현 내용

* Label 생성, 목록 조회 API 구현
* 같은 Repository 내 이름 중복 검증 추가
* Issue에 Label 연결 API 구현
* Issue에서 Label 제거 API 구현

관련 클래스

* `domain/Label`
* `repository/LabelRepository`
* `service/LabelService`
* `controller/LabelController`
* `dto/label/*`

---

## 4. 관계 및 영속성 매핑

현재 구현된 주요 JPA 관계는 다음과 같다.

* `RepositoryEntity` 1:N `Issue`
* `RepositoryEntity` 1:N `Label`
* `User` 1:N `Issue` assignee
* `User` 1:N `Comment` author
* `Issue` 1:N `Comment`
* `Issue` N:M `Label` via `issue_labels`

구현 메모

* `Issue`와 `Label`은 JPA `@ManyToMany` + `issue_labels` 조인 테이블로 구성했다.
* `RepositoryEntity` 삭제 시 하위 `Issue`, `Label`이 함께 제거되도록 cascade 설정을 적용했다.
* `Issue` 삭제 시 하위 `Comment`가 함께 제거되도록 cascade 설정을 적용했다.

---

## 5. 구현된 API 목록

### 5.1 Repository API

* `POST /api/repositories`
* `GET /api/repositories`
* `GET /api/repositories/{repositoryId}`
* `PUT /api/repositories/{repositoryId}`
* `DELETE /api/repositories/{repositoryId}`

### 5.2 User API

* `POST /api/users`
* `GET /api/users`
* `GET /api/users/{userId}`
* `PUT /api/users/{userId}`
* `DELETE /api/users/{userId}`

### 5.3 Issue API

* `POST /api/repositories/{repositoryId}/issues`
* `GET /api/repositories/{repositoryId}/issues`
* `GET /api/repositories/{repositoryId}/issues/{issueId}`
* `PUT /api/repositories/{repositoryId}/issues/{issueId}`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/status`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/priority`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/assignee`

### 5.4 Comment API

* `POST /api/repositories/{repositoryId}/issues/{issueId}/comments`
* `GET /api/repositories/{repositoryId}/issues/{issueId}/comments`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}/comments/{commentId}`

### 5.5 Label API

* `POST /api/repositories/{repositoryId}/labels`
* `GET /api/repositories/{repositoryId}/labels`
* `POST /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`

---

## 6. DTO 구성

각 도메인에 대해 요청/응답 DTO를 분리했다.

구현된 대표 DTO

* `CreateRepositoryRequest`, `UpdateRepositoryRequest`, `RepositoryResponse`
* `CreateUserRequest`, `UpdateUserRequest`, `UserResponse`, `UserSummaryResponse`
* `CreateIssueRequest`, `UpdateIssueRequest`, `UpdateIssueStatusRequest`, `UpdateIssuePriorityRequest`, `UpdateIssueAssigneeRequest`, `IssueResponse`, `IssueSummaryResponse`
* `CreateCommentRequest`, `CommentResponse`
* `CreateLabelRequest`, `LabelResponse`

응답 특징

* `IssueResponse`는 assignee와 labels를 포함한다.
* `CommentResponse`는 author 요약 정보를 포함한다.
* `IssueSummaryResponse`는 목록 조회용으로 축약된 구조를 사용한다.

---

## 7. 예외 처리

공통 예외 응답 포맷은 `ErrorResponse`로 통일했다.

구현된 주요 예외 코드

* `REPOSITORY_NOT_FOUND`
* `USER_NOT_FOUND`
* `ISSUE_NOT_FOUND`
* `COMMENT_NOT_FOUND`
* `LABEL_NOT_FOUND`
* `DUPLICATE_USER_USERNAME`
* `DUPLICATE_USER_EMAIL`
* `USER_DELETE_CONFLICT`
* `DUPLICATE_LABEL_NAME`
* `LABEL_ALREADY_ATTACHED`
* `VALIDATION_ERROR`

처리 방식

* 도메인 미존재 예외는 `404 Not Found`
* 중복 및 참조 충돌은 `409 Conflict`
* Bean Validation 실패는 `400 Bad Request`

---

## 8. 테스트 및 검증

현재 테스트는 서비스 통합 테스트 중심으로 구성했다.

구현된 테스트

* `RepositoryServiceIntegrationTest`
* `DomainFlowIntegrationTest`

검증한 항목

* Repository CRUD 동작
* User 생성/수정/삭제 제약
* Issue 생성/조회/상태 변경/우선순위 변경/담당자 변경
* Label 생성 및 Issue 연결/해제
* Comment 생성/조회/삭제
* 참조 중인 User 삭제 차단

실행 결과

* `./gradlew test` 통과

---

## 9. 현재 구현과 문서 간 차이

현재 기준에서 확인된 차이는 다음과 같다.

* Controller 레벨 HTTP 통합 테스트는 넣지 않았고, 서비스 통합 테스트 중심으로 검증했다.
* `Issue`와 `Label` 관계는 문서의 "매핑 엔터티 권장" 대신 현재 구현에서는 단순화를 위해 `@ManyToMany` 조인 테이블 방식으로 구현했다.
* `User` 수정 API는 문서대로 `displayName`, `email`, `role`만 수정하고 `username` 변경은 지원하지 않는다.

---

## 10. 다음 작업 후보

다음 단계에서 고려할 수 있는 작업은 다음과 같다.

* Controller 기반 API 통합 테스트 추가
* 검색 조건 확장 시 Specification 또는 QueryDSL 적용
* `IssueLabel` 명시적 매핑 엔터티로 리팩터링
* 공통 감사 필드 Base Entity 추출
* 인증/인가 기능 추가

---

## 11. 요약

현재 코드베이스에는 `docs`에 정의된 핵심 도메인인 Repository, User, Issue, Comment, Label이 실제 구현되어 있다.
REST API, DTO, 서비스, JPA 엔터티, 예외 처리, 기본 통합 테스트까지 포함해 최소 동작 가능한 이슈 관리 백엔드 구조를 갖췄다.

문서 기준의 주요 요구사항은 대부분 반영되었으며,
남은 보완 포인트는 테스트 범위 확대와 관계 모델 정교화, 인증 확장이다.

