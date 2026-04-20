# GitLab Integration Design Plan

## Summary

- 목적: 플랫폼 어댑터 분리 구조를 유지하면서 GitLab을 두 번째 플랫폼으로 붙이는 계획과 현재 상태를 정리한다.
- 현재 상태: GitLab 1차/2차 핵심 구현은 완료되었다.
- 남은 범위: 플랫폼 모듈화 전환, self-managed 안정화, 접근 제어, 식별자 모델, 통합 테스트 보강이다.

## 1. Goal

- GitHub 동작을 유지한 채 GitLab을 추가한다.
- GitLab.com 기준 최소 기능 흐름을 먼저 완성한다.
- self-managed GitLab은 연결별 `baseUrl` 구조를 열어두되, 운영 안정화는 후속 범위로 분리한다.
- GitLab 추가 이후에는 새 플랫폼 추가가 공통 코드 수정으로 번지지 않도록 모듈화 단계로 전환한다.

## 2. Scope

### 2.1 완료된 구현 범위

- GitLab 연결 생성 및 사용자 검증
- GitLab 프로젝트 목록 조회
- GitLab 이슈 목록 조회
- GitLab 이슈 생성/수정
- GitLab 이슈 댓글 조회/작성
- 연결별 `baseUrl` 저장
- GitLab.com 기본 base URL 처리
- HTTPS 검증과 `/api/v4` 경로 정규화
- GitLab 프로젝트 path 인코딩 보강
- 프론트 플랫폼 탭과 GitLab 연결 입력 흐름

### 2.2 아직 제외된 범위

- GitLab merge request 연동
- group/project access token 별도 관리 UI
- self-managed GitLab 운영 가이드
- GitLab 전용 고급 필터와 검색 조건
- labels, milestones, assignees 같은 확장 기능

## 3. Design Principles

- 서비스 레이어는 `PlatformGateway`만 사용한다.
- 플랫폼별 base URL, 인증 헤더, 식별자 규칙은 어댑터 내부에서 처리한다.
- 공통 DTO를 유지하고, GitLab의 `id`, `iid`, `path_with_namespace`를 공통 필드로 변환한다.
- GitHub/GitLab 분기 로직은 resolver, config, mapper에 둔다.
- 프론트엔드는 플랫폼 공통 화면을 유지하고, 플랫폼별 안내 문구만 분기한다.
- 현재 구조는 독립 모듈 구조가 아니므로, 다음 단계에서는 platform registry와 capability 선언을 도입한다.

## 4. Backend Status

### 4.1 Platform config 확장

- 완료: `PlatformConnection.baseUrl` 추가
- 완료: GitLab 미입력 시 `https://gitlab.com/api/v4` 사용
- 완료: HTTPS 검증과 `/api/v4` 자동 보정
- 남음: self-managed 환경별 unique 제약 보강

### 4.2 API client 추가

- 완료: `GitLabApiClient`
- 완료: `DefaultGitLabApiClient`
- 완료: GitLab 사용자, 프로젝트, 이슈, 댓글 API 호출
- 완료: project path 단일 인코딩 테스트

### 4.3 Gateway 구현

- 완료: `GitLabPlatformGateway`
- 완료: `PlatformGatewayResolver`의 GITLAB 선택
- 완료: GitLab 응답을 `Remote*` DTO로 변환

### 4.4 Remote DTO 매핑 규칙

- `RemoteUserProfile.externalUserId`: GitLab user id
- `RemoteUserProfile.login`: GitLab username
- `RemoteRepository.externalId`: project id
- `RemoteRepository.name`: `path_with_namespace`
- `RemoteRepository.fullName`: `path_with_namespace`
- `RemoteIssue.externalId`: issue id
- `RemoteIssue.numberOrKey`: issue iid
- `RemoteComment.externalId`: note id

### 4.5 상태 값 변환

- 현재 구현은 플랫폼 gateway 결과를 공통 캐시에 저장한다.
- GitLab 상태 값과 GitHub 상태 값의 완전한 공통화는 후속 점검 대상이다.

### 4.6 접근 제어

- 현재 `RepositoryService.requireAccessibleRepository`는 `ownerKey == accountLogin` 기준이다.
- GitLab group/subgroup 프로젝트와 GitHub organization 저장소를 고려하면 접근 가능 저장소 캐시 기준으로 바꾸는 보강이 필요하다.

## 5. API Contract Status

### 5.1 완료된 공통 계약

- `/api/platforms/{platform}/token`
- `/api/platforms/{platform}/token/status`
- `/api/platforms/{platform}/repositories`
- `/api/platforms/{platform}/repositories/refresh`
- `/api/platforms/{platform}/repositories/{repositoryId}/issues`
- `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`

### 5.2 완료된 응답 필드 전환

- `githubLogin` -> `accountLogin`
- `githubRepositoryId` -> `repositoryId`
- `ownerLogin` -> `ownerKey`
- `htmlUrl` -> `webUrl`
- `githubIssueId` -> `issueId`
- `number` -> `numberOrKey`
- `githubCommentId` -> `commentId`

### 5.3 남은 계약 보강

- 플랫폼별 오류 메시지 상세화
- GitLab base URL 검증 실패 메시지 UX 개선
- 라벨/담당자/우선순위 API 계약 별도 설계

## 6. Frontend Status

### 6.1 완료된 공통 화면

- `/settings/platforms/:platform`
- `/platforms/:platform/repositories`
- `/platforms/:platform/repositories/:repositoryId/issues`
- `/platforms/:platform/repositories/:repositoryId/issues/:issueId`
- legacy GitHub 경로 redirect

### 6.2 완료된 UI 요소

- GitHub/GitLab 플랫폼 탭
- GitLab PAT 안내
- GitLab base URL 입력
- platform 기반 query key
- platform 기반 API client 호출

### 6.3 남은 UI 보강

- GitLab self-managed 오류 안내
- 플랫폼별 권한 부족 메시지
- GitLab 프로젝트 표시명과 경로 표시 분리

## 7. Test Status

### 완료

- GitLab API client 단위 테스트
- GitLab gateway 매핑 테스트
- 플랫폼 스키마 통합 테스트
- PAT 암호화 테스트
- base URL 정규화 테스트
- project path 인코딩 테스트

### 남음

- GitLab 전체 사용자 흐름 통합 테스트
- group/subgroup 프로젝트 접근 제어 테스트
- self-managed base URL 케이스 추가
- 프론트 플랫폼 라우트 렌더링 테스트

## 8. Step Plan

### 완료된 단계

1. `PlatformType`과 resolver에 GitLab 추가
2. GitLab API client와 설정 객체 추가
3. `GitLabPlatformGateway` 구현
4. GitLab 응답 mapper와 테스트 추가
5. 플랫폼 연결 API 계약 확장
6. 프론트 플랫폼 연결 화면에 GitLab 옵션 추가
7. GitLab 프로젝트/이슈/댓글 기본 흐름 연결
8. base URL 정규화와 프로젝트 path 인코딩 보강

### 다음 단계

1. 플랫폼 모듈 registry 도입
2. capability 기반 기능 지원 범위 분리
3. 서버 제공 platform metadata API 추가
4. 연결 unique 제약 재설계
5. 저장소 접근 제어 기준 재설계
6. GitLab 프로젝트 표시/호출 식별자 분리
7. GitLab 전체 흐름 통합 테스트 추가
8. 플랫폼별 오류 메시지 보강

## 9. Risks

- self-managed GitLab까지 포함하면 계정 식별자 충돌 가능성이 있다.
- `project id`와 `path_with_namespace`를 혼용하면 조회/표시 기준이 흔들릴 수 있다.
- `issue id`와 `iid`를 혼동하면 수정/댓글 API에서 오류가 날 수 있다.
- owner 기준 접근 제어는 group/subgroup 프로젝트에 부적합할 수 있다.
- 현재 구조는 새 플랫폼 추가 시 공통 enum, resolver, 프론트 metadata 수정이 필요하다.

## 10. Recommendation

- 현재 구현은 GitLab 흐름 검증용으로는 충분하다.
- 다음 단계에서는 모듈화 전환을 먼저 문서/구조 기준으로 잡고, 접근 제어와 unique 제약을 별도 백엔드 작업으로 분리한다.
- GitLab self-managed 운영 안정화는 문서와 테스트를 보강한 뒤 확장한다.
- 확장 기능은 GitLab 추가와 섞지 않고 별도 기능 단위로 진행한다.

## 11. Modularization Follow-up

### 11.1 Platform registry 도입

- 문제: `PlatformType` enum과 resolver 등록이 공통 코드에 고정
- 방향: 각 플랫폼 모듈이 `platformId`, 표시명, gateway, capability를 제공

### 11.2 Capability 분리

- 문제: `PlatformGateway` 하나가 모든 기능을 지원한다고 가정
- 방향: 저장소, 이슈, 댓글, 라벨 등 기능별 capability를 선언하고 지원 여부 기준으로 실행

### 11.3 Platform metadata API

- 문제: 프론트의 플랫폼 목록과 입력 필드가 하드코딩
- 방향: 백엔드가 등록 플랫폼 metadata를 제공하고 프론트가 이를 기준으로 탭/폼 구성

### 11.4 패키지 또는 빌드 모듈 분리

- 문제: 현재는 패키지 분리만 되어 있고 독립 모듈 경계가 약함
- 방향: `platform-core`, `platform-github`, `platform-gitlab` 구조로 단계적 분리

## 12. Backend Follow-up

### 12.1 연결 식별자 제약 재설계

- 문제: `externalUserId`, `accountLogin` 단일 unique 기준은 self-managed GitLab에서 충돌 가능
- 방향: `platform + baseUrl + externalUserId` 또는 동등한 복합 식별 기준 검토

### 12.2 접근 제어 재설계

- 문제: `ownerKey == accountLogin` 전제는 organization/group 리소스에 부적합
- 방향: 연결 사용자가 refresh한 접근 가능 저장소 목록을 기준으로 검증

### 12.3 저장소 캐시 모델 정리

- 문제: GitLab `path_with_namespace`가 표시명과 API 호출 식별자를 겸함
- 방향: `displayName`, `pathWithNamespace`, `ownerKey`, `repositorySlug` 역할 분리

### 12.4 base URL 정규화 보강

- 완료: HTTPS 검증, `/api/v4` 보정
- 남음: 운영 문서와 오류 메시지 보강

### 12.5 통합 테스트 보강

- 문제: gateway 단위 테스트는 있으나 실제 사용자 흐름 통합 검증이 부족
- 방향: `token 등록 -> 저장소 refresh -> 이슈/댓글 조회` GitLab 시나리오 추가

## 13. 완료 기준

- 사용자가 프론트엔드에서 GitLab 연결을 생성하고 프로젝트 목록을 조회할 수 있다.
- GitLab 프로젝트에서 이슈 목록, 상세, 생성, 수정, 댓글 흐름이 동작한다.
- GitHub 기존 흐름에 회귀가 없다.
- GitLab 특성인 `baseUrl`, `path_with_namespace`, `iid` 처리가 문서와 코드에서 일관된다.
