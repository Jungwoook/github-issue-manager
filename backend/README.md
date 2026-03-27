# GitHub Issue Manager Backend

## 1. 프로젝트 소개

GitHub 이슈 관리 방식에서 아이디어를 가져와, Repository 단위로 이슈를 생성하고 관리하는 백엔드 프로젝트입니다.

현재 구현은 외부 GitHub API 연동 없이 애플리케이션 내부 데이터 모델과 REST API에 집중되어 있습니다. Repository, User, Issue, Comment, Label을 중심으로 이슈 관리 흐름을 구성했고, 향후 GitHub 연동이나 인증 기능을 붙일 수 있도록 계층 구조를 분리한 형태로 작성되어 있습니다.

관련 설계 문서는 루트의 `docs/` 아래에 정리되어 있습니다.

- `../docs/01-prd.md`
- `../docs/02-architecture.md`
- `../docs/03-api-spec.md`
- `../docs/04-data-model.md`
- `../docs/05-implementation-summary.md`

## 2. 개발 배경 / 문제 정의

작업 항목을 저장소 단위로 나누어 관리하고, 각 작업의 상태와 우선순위, 담당자, 논의 이력을 구조적으로 추적하기 위한 백엔드 서비스가 필요하다는 가정에서 시작한 프로젝트입니다.

이 프로젝트는 다음 문제를 다룹니다.

- Repository 단위로 작업 범위를 구분하고 싶다.
- Issue 단위로 작업 상태를 관리하고 싶다.
- Comment와 Label을 통해 진행 이력과 분류 정보를 남기고 싶다.
- 검색 및 필터링으로 필요한 이슈를 빠르게 찾고 싶다.

현재 단계에서는 위 요구를 내부 DB 기반 CRUD/API로 구현하는 데 초점을 두고 있습니다.

## 3. 주요 기능

현재 코드 기준으로 구현된 기능은 다음과 같습니다.

### Repository 관리

- Repository 생성, 목록 조회, 단건 조회, 수정, 삭제

### User 관리

- User 생성, 목록 조회, 단건 조회, 수정, 삭제
- `keyword`, `role` 조건 기반 사용자 조회
- username, email 중복 검증
- 담당 중인 Issue 또는 작성한 Comment가 있으면 삭제 제한

### Issue 관리

- Repository 하위 Issue 생성, 목록 조회, 단건 조회, 수정, 삭제
- Issue 상태 변경: `OPEN`, `CLOSED`
- Issue 우선순위 변경: `LOW`, `MEDIUM`, `HIGH`
- Issue 담당자 지정 및 해제
- `keyword`, `status`, `priority`, `labelId`, `assigneeId` 조건 기반 조회

### Comment 관리

- Issue 하위 Comment 생성, 목록 조회, 삭제

### Label 관리

- Repository 하위 Label 생성, 목록 조회
- Issue에 Label 연결 및 해제
- 동일 Repository 내 Label 이름 중복 방지

### 예외 처리

- 글로벌 예외 처리기 기반 공통 에러 응답 제공
- 조회 실패, 중복 데이터, 삭제 충돌, 중복 라벨 연결 등의 상황 처리

## 4. 기술 스택

- Java 17
- Spring Boot 4.0.4
- Spring Web MVC
- Spring Data JPA
- Spring Validation
- H2 Database
- Lombok
- Gradle
- JUnit 5 / Spring Boot Test

## 5. 프로젝트 구조

백엔드 애플리케이션은 Controller, Service, Repository, Domain, DTO, Exception 계층으로 나뉘어 있습니다.

```text
src/main/java/com/jw/github_issue_manager
├─ controller   : REST API 엔드포인트
├─ service      : 비즈니스 로직
├─ repository   : JPA Repository
├─ domain       : 엔티티 및 enum
├─ dto          : 요청/응답 DTO
└─ exception    : 예외 및 공통 에러 응답
```

핵심 도메인 관계는 다음과 같습니다.

- Repository 1:N Issue
- Repository 1:N Label
- User 1:N Issue(assignee)
- User 1:N Comment(author)
- Issue 1:N Comment
- Issue N:M Label

## 6. API 개요

기본 경로는 `/api` 입니다.

### Repository API

- `POST /api/repositories`
- `GET /api/repositories`
- `GET /api/repositories/{repositoryId}`
- `PUT /api/repositories/{repositoryId}`
- `DELETE /api/repositories/{repositoryId}`

### User API

- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{userId}`
- `PUT /api/users/{userId}`
- `DELETE /api/users/{userId}`

### Issue API

- `POST /api/repositories/{repositoryId}/issues`
- `GET /api/repositories/{repositoryId}/issues`
- `GET /api/repositories/{repositoryId}/issues/{issueId}`
- `PUT /api/repositories/{repositoryId}/issues/{issueId}`
- `DELETE /api/repositories/{repositoryId}/issues/{issueId}`
- `PATCH /api/repositories/{repositoryId}/issues/{issueId}/status`
- `PATCH /api/repositories/{repositoryId}/issues/{issueId}/priority`
- `PATCH /api/repositories/{repositoryId}/issues/{issueId}/assignee`

### Comment API

- `POST /api/repositories/{repositoryId}/issues/{issueId}/comments`
- `GET /api/repositories/{repositoryId}/issues/{issueId}/comments`
- `DELETE /api/repositories/{repositoryId}/issues/{issueId}/comments/{commentId}`

### Label API

- `POST /api/repositories/{repositoryId}/labels`
- `GET /api/repositories/{repositoryId}/labels`
- `POST /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`
- `DELETE /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`

세부 요청/응답 예시는 `../docs/03-api-spec.md`를 참고하면 됩니다.

## 7. 현재 구현 범위와 제외 범위

현재 구현된 범위는 내부 DB 기반 이슈 관리 API입니다.

구현된 내용:

- Repository, User, Issue, Comment, Label 도메인 모델
- REST API와 DTO 기반 요청/응답 구조
- 검색/필터링이 포함된 기본 조회 기능
- 서비스 계층 중심의 비즈니스 규칙 처리
- 통합 테스트 기반 주요 도메인 흐름 검증

아직 구현되지 않은 내용:

- GitHub API 연동
- 로그인/인증/인가
- 페이징, 정렬, 고급 검색
- 알림, 웹훅, 동기화 기능
- 운영 환경 배포/관측 관련 설정

## 8. 실행 방법

이 프로젝트는 Gradle Wrapper를 포함하고 있어 별도 Gradle 설치 없이 실행할 수 있습니다.

### 실행 환경

- Java 17

### 애플리케이션 실행

Windows PowerShell 기준:

```powershell
./gradlew bootRun
```

기본적으로 Spring Boot 내장 서버로 실행되며, 별도 포트를 설정하지 않았기 때문에 기본 포트 `8080`을 사용합니다.

### 테스트 실행

전체 테스트 실행:

```powershell
./gradlew test
```

### 참고

- 런타임 데이터베이스는 H2를 사용합니다.
- 현재 `application.yaml`에는 최소 설정만 포함되어 있어, 로컬 개발용 상세 설정은 추후 확장될 수 있습니다.

## 9. 향후 개선 계획

설계 문서와 현재 코드 구조를 기준으로 예상되는 다음 단계는 아래와 같습니다.

- GitHub Repository 및 Issue와의 외부 연동 추가
- 사용자 인증 및 권한 관리 도입
- Issue 검색 조건 확장 및 조회 성능 개선
- 페이징, 정렬, 고급 필터링 지원
- 테스트 범위 확장 및 운영 설정 정리

## TODO

- API 예시 요청/응답을 README에 얼마나 포함할지 결정 필요
- 배포 방식과 운영 환경 설정이 정리되면 실행 섹션 보강 필요
