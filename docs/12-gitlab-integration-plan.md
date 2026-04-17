# GitLab Integration Design Plan

## Summary

- 목적: 현재 `공통 코어 + GitHub 어댑터` 구조를 유지하면서 GitLab을 두 번째 플랫폼으로 추가하기 위한 설계 계획을 정리한다.
- 범위: 인증, 플랫폼 설정, GitLab 게이트웨이, Remote DTO 매핑, API 계약, 프론트 연결 흐름, 테스트 전략.
- 원칙: 서비스 레이어는 공통 포트에만 의존하고, 플랫폼별 차이는 어댑터와 설정 계층에서 흡수한다.

## 1. Goal

- GitHub 동작을 유지한 채 GitLab을 추가한다.
- 첫 단계에서는 GitLab.com 기준으로 동작하는 최소 기능 세트를 구현한다.
- self-managed GitLab 지원은 구조를 열어 두되 1차 범위에서는 선택 설정으로 제한한다.

## 2. Scope

### 2.1 1차 구현 범위

- GitLab 연결 생성 및 사용자 검증
- GitLab 프로젝트 목록 조회
- GitLab 이슈 목록 조회
- GitLab 이슈 생성/수정
- GitLab 이슈 댓글 조회/생성

### 2.2 1차 제외 범위

- GitLab merge request 연동
- group access token 별도 관리 UI
- self-managed GitLab 전용 운영 가이드
- GitLab 전용 고급 필터와 검색 조건

## 3. Design Principles

- 서비스 레이어는 `PlatformGateway`만 사용한다.
- 플랫폼별 base URL, 인증 헤더, 식별자 규칙은 어댑터 내부로 한정한다.
- 공통 DTO는 유지하고, GitLab의 `id`, `iid`, `path_with_namespace`를 공통 필드로 변환한다.
- GitHub와 GitLab 분기 로직은 서비스가 아니라 resolver, config, mapper에 둔다.
- 프론트는 플랫폼 공통 화면을 유지하고, GitLab 전용 안내만 선택적으로 노출한다.

## 4. Backend Plan

### 4.1 Platform config 확장

- `PlatformConnection`에 GitLab용 설정을 담을 수 있는 구조를 준비한다.
- 1차 기본값:
  - `platform = GITLAB`
  - `baseUrl = https://gitlab.com`
- 확장 포인트:
  - 추후 self-managed 지원을 위해 플랫폼별 base URL 저장 가능 구조 유지

### 4.2 API client 계층 추가

- GitLab 전용 API client를 분리한다.
- 책임:
  - 인증 헤더 구성
  - API base URL 조합
  - GitLab REST 응답 수신
  - 공통 mapper 입력용 원본 DTO 반환

권장 구성:
- `GitlabApiClient`
- `GitlabProperties` 또는 플랫폼 설정 객체
- `GitlabRequestFactory` 수준의 경량 보조 계층

### 4.3 Gateway 구현

- `GitlabPlatformGateway`를 추가한다.
- 구현 책임:
  - 현재 사용자 조회
  - 프로젝트 목록 조회
  - 이슈 목록 조회
  - 이슈 생성/수정
  - 댓글 목록 조회/생성
- `PlatformGatewayResolver`는 `GITLAB` 선택을 지원한다.

### 4.4 Remote DTO 매핑 규칙

- `RemoteUserProfile`
  - `externalId = GitLab user id`
  - `login = username`
  - `displayName = name`
- `RemoteRepository`
  - `externalId = project id`
  - `ownerKey = namespace 또는 상위 그룹 식별값`
  - `name = project name`
  - `fullName = path_with_namespace`
- `RemoteIssue`
  - `externalId = issue id`
  - `numberOrKey = iid`
  - `title`, `body`, `state`, `author`, `webUrl` 공통 필드로 변환
- `RemoteComment`
  - `externalId = note id`
  - `body`, `author`, `createdAt`, `updatedAt` 공통 필드로 변환

### 4.5 상태값 변환 규칙

- GitHub `open/closed`와 GitLab `opened/closed`를 공통 상태로 매핑한다.
- 서비스와 프론트는 공통 상태만 사용한다.
- 플랫폼별 상태 문자열은 gateway 내부 mapper에서만 처리한다.

### 4.6 식별자 처리 원칙

- 내부 캐시와 도메인 식별자는 계속 `platform + externalId`를 사용한다.
- 사용자 노출 번호는 계속 `numberOrKey`를 사용한다.
- GitLab 구현 시:
  - `externalId = id`
  - `numberOrKey = iid`
- API 호출에서 GitLab 이슈 수정/댓글 경로는 `issue_iid`를 우선 사용한다.

## 5. API Contract Plan

### 5.1 유지할 공통 계약

- 프론트 API 경로는 계속 `/platforms/:platform/...` 구조를 사용한다.
- 응답 DTO는 GitHub/GitLab 구분 없이 공통 응답 스키마를 유지한다.
- 에러 응답은 플랫폼 공통 코드 중심으로 유지한다.

### 5.2 보완할 계약

- 플랫폼 연결 생성/수정 요청에 `baseUrl` 필드 확장 가능성을 열어 둔다.
- GitLab 연결 시 base URL 미입력은 `https://gitlab.com`으로 처리한다.
- 플랫폼 연결 검증 실패 메시지는 플랫폼별 가이드를 포함할 수 있게 한다.

## 6. Frontend Plan

### 6.1 공통 화면 유지

- 기존 플랫폼 공통 라우트와 페이지 구조를 유지한다.
- GitLab 추가 시 새 페이지를 복제하지 않고 플랫폼 선택 값만 확장한다.

### 6.2 추가할 UI 요소

- 플랫폼 연결 화면:
  - GitLab 선택지 추가
  - GitLab PAT 입력 안내 문구 추가
  - 선택적 base URL 입력 필드 검토
- 프로젝트/이슈 화면:
  - GitLab `fullName(path_with_namespace)` 표시 대응
  - 이슈 번호는 `numberOrKey` 기준 유지

### 6.3 UX 원칙

- GitHub와 GitLab의 인증 차이는 연결 화면 안내 문구에서 설명한다.
- 목록, 상세, 생성, 수정 UI는 공통 구조를 유지한다.
- self-managed 설정은 1차에서 접어 두거나 고급 옵션으로 제한한다.

## 7. Test Plan

### 7.1 Backend

- GitLab gateway 단위 테스트
- GitLab 응답 → `Remote* DTO` 매핑 테스트
- resolver 분기 테스트
- 연결 검증 실패/성공 테스트
- 기존 GitHub 회귀 테스트 유지

### 7.2 Frontend

- 플랫폼 연결 폼 렌더링 테스트
- 플랫폼별 안내 문구 분기 테스트
- 라우트 파라미터 기반 화면 동작 확인
- 기존 GitHub 흐름 회귀 확인

## 8. Step Plan

1. `PlatformType`와 resolver에 GitLab 추가
2. GitLab API client와 설정 객체 추가
3. `GitlabPlatformGateway` 구현
4. GitLab 응답 mapper와 테스트 추가
5. 플랫폼 연결 API 계약 보완
6. 프론트 플랫폼 연결 화면에 GitLab 옵션 추가
7. 프로젝트/이슈 흐름의 GitLab 실제 조회 검증
8. 필요 시 base URL 고급 설정 추가

## 9. Risks

- GitLab self-managed 지원을 1차에 함께 넣으면 설정 복잡도가 급증할 수 있다.
- `project id`와 `path_with_namespace`를 혼용하면 조회/표시 기준이 흔들릴 수 있다.
- `issue id`와 `iid`를 혼동하면 수정/댓글 API에서 런타임 오류가 날 수 있다.
- 플랫폼별 인증 안내가 부족하면 연결 실패 원인 파악이 어려울 수 있다.

## 10. Recommendation

- 첫 구현은 GitLab.com 기준 최소 기능으로 제한한다.
- 서비스 계층은 그대로 두고 GitLab 차이는 gateway와 config로 흡수한다.
- 1차 목표는 "GitLab 지원 완료"보다 "공통 구조가 두 번째 플랫폼까지 버티는지 검증"에 둔다.
- self-managed GitLab은 2차 확장 과제로 미룬다.

## 11. Phase 2 Scope

### 11.1 목표

- 1차에서 만든 GitLab 백엔드 연동을 실제 사용자 흐름에서 사용할 수 있게 확장한다.
- GitLab.com 기준 최소 연동에서 멈추지 않고, 설정/표시/운영 차이를 공통 구조 안에서 안정화한다.
- GitLab 추가가 일회성 예외 구현이 아니라 다음 플랫폼 확장의 기준이 되도록 만든다.

### 11.2 2차 구현 범위

- 플랫폼 연결 계약 확장
  - 연결 요청/응답에 `baseUrl` 확장
  - GitLab 미입력 시 기본값 `https://gitlab.com/api/v4` 처리
  - 추후 self-managed 입력 가능 구조 확보
- 프로젝트 식별자/표시 모델 정리
  - 조회용 식별자와 표시용 이름 분리
  - `project id`, `path_with_namespace`, 사용자 표시 문자열 역할 고정
  - 현재 `ownerKey`와 접근 제어 전제 재검토
- 프론트 플랫폼 연결 화면 확장
  - GitLab 선택지 표시
  - GitLab PAT 안내 문구 분리
  - 필요 시 고급 설정으로 base URL 입력 추가
- 프론트 프로젝트/이슈 흐름 연결
  - GitLab 프로젝트 목록 조회
  - GitLab 이슈 목록/상세/생성/수정/댓글 흐름 연결
  - `numberOrKey = iid` 기준 라우트와 화면 표시 확인
- 에러/운영 메시지 보강
  - GitLab 인증 실패, 권한 부족, 잘못된 base URL 상황 메시지 정리
  - GitHub/GitLab별 연결 가이드 분리
- 테스트 보강
  - GitLab 연결 API 통합 테스트
  - 프로젝트 path / `iid` / base URL 관련 회귀 테스트
  - 프론트 플랫폼별 렌더링과 라우트 테스트

### 11.3 2차 선행 확인 사항

- 현재 `RepositoryService.requireAccessibleRepository`가 `ownerKey == accountLogin` 전제를 갖고 있는지 점검
- GitLab group/subgroup 프로젝트를 현재 캐시 구조에서 안전하게 다룰 수 있는지 확인
- `PlatformConnection`에 base URL을 저장할지, 별도 설정 객체로 분리할지 결정
- 프론트에서 GitHub 전용 문구와 query key가 완전히 플랫폼 중립 구조인지 확인

### 11.4 2차에서 해결할 핵심 문제

- 접근 가능한 프로젝트의 소유자와 로그인 사용자가 다를 때도 조회/상세가 동작해야 한다.
- GitLab `path_with_namespace`를 API 호출용으로 쓰면서도 UI에는 읽기 좋은 이름을 유지해야 한다.
- self-managed GitLab을 열어둘 수 있는 설정 구조가 필요하지만, 2차에서도 기본 기준은 GitLab.com이어야 한다.
- GitLab 전용 예외 상황이 서비스 계층이 아니라 연결/설정/어댑터 계층에서 처리되어야 한다.

### 11.5 2차 제외 범위

- merge request 연동
- labels, milestones, assignees 같은 GitLab 전용 확장 기능
- project/group access token 전용 관리 UI
- 여러 GitLab 연결 계정을 동시에 관리하는 기능
- 운영 문서 수준의 self-managed 설치/배포 가이드

### 11.6 권장 구현 순서

1. `baseUrl` 포함 플랫폼 연결 계약 확정
2. 프로젝트 식별자/접근 제어 규칙 정리
3. GitLab 연결 화면과 안내 문구 반영
4. GitLab 프로젝트/이슈/댓글 프론트 연결
5. GitLab 전용 오류/예외 메시지 보강
6. 통합 테스트와 회귀 테스트 보강

### 11.7 완료 기준

- 사용자가 프론트에서 GitLab 연결을 생성하고 프로젝트 목록을 조회할 수 있다.
- GitLab 프로젝트에서 이슈 목록, 상세, 생성, 수정, 댓글 흐름이 동작한다.
- GitHub 기존 흐름에 회귀가 없다.
- GitLab 특성인 `baseUrl`, `path_with_namespace`, `iid` 처리가 문서와 코드에서 일관된다.

## 12. Backend Follow-up

### 12.1 추가로 정리할 필요가 있는 항목

- 연결 식별자 유니크 제약 재설계
  - 현재 `PlatformConnection.externalUserId`, `accountLogin`은 단일 컬럼 unique 기준이다.
  - GitLab self-managed까지 열어두면 같은 `externalUserId`나 `accountLogin`이 다른 인스턴스에서 충돌할 수 있다.
  - 장기적으로는 `platform + baseUrl + externalUserId` 또는 이에 준하는 복합 식별 기준이 필요하다.
- 접근 제어 기준 재설계
  - 현재 `RepositoryService.requireAccessibleRepository`는 `ownerKey == accountLogin` 전제를 사용한다.
  - GitLab group/subgroup 프로젝트나 GitHub organization 저장소처럼 "접근 가능하지만 owner가 다른 리소스"를 안정적으로 처리하기 어렵다.
  - 사용자 연결과 저장소 접근 권한을 분리한 모델이 필요하다.
- 저장소 캐시 모델 정리
  - 현재 GitLab은 API 호출용 `path_with_namespace`를 `name/fullName`에 싣는 방식으로 동작한다.
  - 장기적으로는 `displayName`, `pathWithNamespace`, `ownerKey`, `repositorySlug` 역할을 분리하는 것이 안전하다.
- base URL 정규화 보강
  - 현재는 입력값을 그대로 base URL로 저장한다.
  - self-managed 사용 시 `/api/v4`를 포함하지 않은 URL, trailing slash, 잘못된 scheme 등을 정규화하거나 검증할 필요가 있다.
- 연결/조회 통합 테스트 보강
  - 현재는 gateway 단위와 일부 서비스 테스트 중심이다.
  - `token 등록 -> 저장소 refresh -> 이슈/댓글 조회`까지 GitLab 시나리오를 통합 테스트로 보강하면 회귀를 줄일 수 있다.

### 12.2 현재 기준 판단

- 지금 상태는 "GitLab 2차 흐름 검증용"으로는 충분하다.
- 다만 위 항목들은 실제 사용자/운영 환경에서 리소스 소유 구조와 self-managed 케이스를 다루기 위해 후속 백엔드 작업으로 남겨두는 것이 좋다.
- 따라서 PR을 올리는 것은 가능하지만, 병합 전 또는 다음 단계에서 이 후속 백엔드 항목을 별도 이슈처럼 관리하는 것을 권장한다.
