# GitHub vs GitLab Platform Comparison

## Summary

- 목적: GitHub와 GitLab의 적용 차이를 현재 공통 구조 기준으로 비교한다.
- 현재 상태: GitLab은 두 번째 플랫폼으로 연결되었고, 기본 프로젝트/이슈/댓글 흐름이 gateway 뒤에 붙어 있다.
- 결론: 공통 구조는 1차 검증되었지만, 접근 제어와 self-managed 식별자 충돌 가능성은 후속 보강이 필요하다.

## 1. 비교 목적

- 두 플랫폼 모두 PAT 기반 인증과 REST API를 사용할 수 있다.
- 저장소/프로젝트, 이슈, 댓글 모델은 공통 DTO로 1차 매핑 가능하다.
- 차이는 인증 헤더, project path, issue id/iid, base URL, 권한 모델에서 발생한다.

## 2. 공통점

- 개인 토큰 기반 인증 가능
- 현재 사용자 조회 API 제공
- 저장소/프로젝트 목록 조회 가능
- 이슈 목록 조회, 생성, 수정 가능
- 이슈 댓글 조회, 생성 가능
- private 리소스 접근에는 토큰 권한 필요

## 3. 핵심 차이

### 3.1 인증 방식

- GitHub:
  - `Authorization: Bearer <token>` 방식
  - fine-grained PAT 권장
- GitLab:
  - `PRIVATE-TOKEN` 헤더 사용
  - personal/project/group access token 선택지가 있음

정리:

- 공통 `PlatformConnection`은 유지 가능하다.
- 플랫폼별 토큰 안내 문구와 기본 scope 표기는 분리해야 한다.

### 3.2 사용자 확인

- GitHub:
  - authenticated user API로 login, id, avatar 등을 조회한다.
- GitLab:
  - `GET /user`로 username, name, email, avatar_url 등을 조회한다.

정리:

- `RemoteUserProfile`에 자연스럽게 매핑 가능하다.

### 3.3 저장소/프로젝트 모델

- GitHub:
  - `owner/repo` 구조가 기본이다.
  - API 호출은 owner와 repo name 조합을 사용한다.
- GitLab:
  - project id 또는 URL-encoded `path_with_namespace`를 사용할 수 있다.
  - group/subgroup 경로가 중요하다.
  - GitLab.com 외 self-managed base URL을 고려해야 한다.

정리:

- 현재 GitLab 구현은 `path_with_namespace`를 API 호출용 `name`으로 사용한다.
- 장기적으로 표시명과 API 경로용 식별자를 분리하는 것이 안전하다.

### 3.4 이슈 식별자

- GitHub:
  - 전역 `id`와 저장소 내 `number`가 있다.
- GitLab:
  - 전역 `id`와 프로젝트 내 `iid`가 있다.

정리:

- 현재 공통 모델의 `externalId + numberOrKey` 구조가 두 플랫폼 모두에 맞는다.
- GitLab에서는 `externalId = id`, `numberOrKey = iid`로 매핑한다.

### 3.5 댓글 모델

- GitHub:
  - issue comments API를 사용한다.
- GitLab:
  - notes API를 사용한다.

정리:

- 현재 범위에서는 `RemoteComment`로 매핑 가능하다.
- GitLab notes는 다른 리소스에도 쓰이는 개념이므로 gateway 내부에서 이슈 댓글로 한정한다.

### 3.6 상태 값

- GitHub:
  - 주로 `open`, `closed`
- GitLab:
  - 주로 `opened`, `closed`

정리:

- 서비스와 프론트는 공통 상태를 사용하고, 플랫폼별 변환은 mapper/gateway에서 처리하는 방향이 적절하다.

### 3.7 운영 환경

- GitHub:
  - 공식 SaaS 중심
  - API base URL 고정 성격이 강함
- GitLab:
  - GitLab.com, self-managed, dedicated 가능
  - base URL과 rate limit이 환경마다 다를 수 있음

정리:

- GitLab은 연결별 `baseUrl` 저장이 필요하다.
- 현재 구현은 HTTPS와 `/api/v4` 정규화를 반영했다.

## 4. 현재 구조에 미치는 영향

### 바로 재사용 가능한 부분

- `PlatformType`
- `PlatformGateway`, `PlatformGatewayResolver`
- `RemoteUserProfile`, `RemoteRepository`, `RemoteIssue`, `RemoteComment`
- `platform + externalId` 캐시 구조
- `/platforms/:platform/...` 프론트 라우트 구조
- `PlatformConnection` 중심 연결 화면 구조

### 보완이 필요한 부분

- 플랫폼별 API base URL 관리
- GitLab project path 표시/호출 분리
- issue `id`와 `iid` 변환 규칙
- owner 기준 접근 제어의 한계
- self-managed GitLab 연결 unique 제약
- 플랫폼별 오류 메시지와 토큰 안내

## 5. 구현 상태 비교

### GitHub 유지

- 장점:
  - 기존 구현과 테스트 기반이 있다.
  - 기본 사용자 흐름이 안정적이다.
- 리스크:
  - GitHub organization 저장소 접근 제어가 owner 기준에 묶여 있다.

### GitLab 추가

- 완료:
  - GitLab API client와 gateway 추가
  - 사용자 검증, 프로젝트 목록, 이슈, 댓글 흐름 연결
  - base URL 입력과 정규화
  - 프로젝트 경로 단일 인코딩 보강
- 남은 리스크:
  - self-managed 계정 식별자 충돌 가능성
  - group/subgroup 프로젝트 접근 제어
  - 프로젝트 표시명과 API 경로용 식별자 분리
  - 통합 테스트 보강

## 6. 권장 방향

- GitLab 추가를 이유로 서비스 레이어를 다시 플랫폼별 분기 구조로 되돌리지 않는다.
- 플랫폼 차이는 gateway, mapper, 설정 계층에서 처리한다.
- 접근 제어와 연결 unique 제약은 다음 백엔드 보강 작업으로 분리한다.
- 라벨, 담당자, 우선순위 같은 확장 기능은 GitHub/GitLab 공통 계약을 먼저 설계한 뒤 추가한다.

## Sources

- [GitHub REST API authentication](https://docs.github.com/en/rest/authentication/authenticating-to-the-rest-api)
- [GitHub repositories REST API](https://docs.github.com/en/rest/repos/repos)
- [GitHub issues REST API](https://docs.github.com/en/rest/issues)
- [GitHub issue comments REST API](https://docs.github.com/en/rest/issues/comments)
- [GitHub fine-grained PAT permissions](https://docs.github.com/en/rest/authentication/permissions-required-for-fine-grained-personal-access-tokens)
- [GitHub REST API rate limits](https://docs.github.com/rest/overview/rate-limits-for-the-rest-api)
- [GitLab REST API authentication](https://docs.gitlab.com/api/rest/authentication/)
- [GitLab users API](https://docs.gitlab.com/api/users/)
- [GitLab projects API](https://docs.gitlab.com/api/projects/)
- [GitLab issues API](https://docs.gitlab.com/api/issues/)
- [GitLab notes API](https://docs.gitlab.com/api/notes/)
- [GitLab personal access tokens API](https://docs.gitlab.com/api/personal_access_tokens/)
- [GitLab REST API overview](https://docs.gitlab.com/api/rest/)
