# GitHub vs GitLab Platform Comparison

## Summary

- 목적: 이 프로젝트에 GitLab을 추가하기 전에 GitHub와 GitLab 적용 차이를 비교한다.
- 기준: 현재 프로젝트 범위인 토큰 인증, 사용자 확인, 저장소/프로젝트 조회, 이슈 조회/생성/수정, 댓글 조회/생성, 플랫폼 공통화 적합성.
- 결론: GitLab은 현재 공통 구조에 붙이기 좋은 다음 플랫폼이지만, `프로젝트 식별자`, `issue id vs iid`, `자체 호스트 지원`, `토큰/권한 모델` 차이를 먼저 반영해야 한다.

## 1. 비교 목적

- 현재 프로젝트는 GitHub 기준으로 `PlatformGateway`, `Remote* DTO`, `platform + externalId` 구조를 갖췄다.
- 다음 플랫폼으로 GitLab을 붙일 수 있는지 판단하려면 기능 유사성만이 아니라 API 계약 차이도 같이 봐야 한다.
- 이 문서는 구현 전에 어떤 항목을 공통화해야 하는지 정리하기 위한 비교 문서다.

## 2. 공통점

- 두 플랫폼 모두 REST API 기반으로 사용자, 저장소/프로젝트, 이슈, 댓글 조회 및 생성이 가능하다.
- 두 플랫폼 모두 개인 토큰 기반 인증이 가능하다.
- 두 플랫폼 모두 이슈 댓글을 별도 엔드포인트로 관리한다.
- 두 플랫폼 모두 private 리소스 접근 시 인증 토큰이 필요하다.
- 현재 프로젝트의 `RemoteUserProfile`, `RemoteRepository`, `RemoteIssue`, `RemoteComment` 모델로 1차 매핑은 가능하다.

## 3. 핵심 차이

### 3.1 인증 방식

- GitHub:
  - REST API 인증은 `Authorization: Bearer <token>` 방식.
  - 공식 문서는 개인 사용 시 fine-grained PAT를 권장.
  - fine-grained PAT는 엔드포인트별 권한이 세분화된다.
- GitLab:
  - REST API 인증은 `PRIVATE-TOKEN` 헤더가 권장.
  - `Authorization: Bearer`도 사용할 수 있다.
  - Personal, project, group access token 등 선택지가 더 많다.

정리:
- GitHub는 "권한 세분화"가 더 강하다.
- GitLab은 "토큰 종류"와 "배포 환경" 선택지가 더 많다.
- 공통 `PlatformConnection` 구조는 유지할 수 있지만, 토큰 검증/안내 문구는 플랫폼별 분기가 필요하다.

### 3.2 사용자 확인

- GitHub:
  - 인증 후 현재 사용자 확인은 `Get the authenticated user` 흐름으로 해결 가능하다.
  - login 중심 식별이 자연스럽다.
- GitLab:
  - 현재 사용자 확인은 `GET /user`.
  - username, name, email, avatar_url, web_url 등을 바로 받을 수 있다.

정리:
- `RemoteUserProfile` 공통 DTO는 유지 가능하다.
- GitLab도 `externalUserId + login(username)` 구조에 잘 맞는다.

### 3.3 저장소/프로젝트 모델

- GitHub:
  - 저장소는 `owner/repo` 구조가 기본이다.
  - 현재 사용자 기준 저장소 목록 조회 엔드포인트가 명확하다.
  - fine-grained PAT로 저장소 목록 조회 시 `Metadata` read 권한이 필요하다.
- GitLab:
  - 프로젝트는 숫자 ID뿐 아니라 `URL-encoded path`로도 접근 가능하다.
  - 그룹/하위 그룹까지 포함한 path 기반 식별이 중요하다.
  - API 루트도 `https://<host>/api/v4`라서 GitLab.com 외 self-managed를 고려해야 한다.

정리:
- 현재 `RemoteRepository.ownerKey + name + fullName` 구조는 GitLab에도 대응 가능하다.
- 다만 GitLab 추가 시 `baseUrl`을 플랫폼 설정에 포함해야 한다.
- `externalId`는 GitHub처럼 단순 숫자 ID 문자열로 저장할 수 있지만, 사람이 보는 key는 path가 더 중요하다.

### 3.4 이슈 식별자

- GitHub:
  - 이슈는 저장소 내부 `number`와 전역 `id`가 함께 존재한다.
  - 현재 프로젝트는 `externalId`와 `numberOrKey`로 이미 분리해 두었다.
- GitLab:
  - 이슈는 전역 `id`와 프로젝트 내부 `iid`가 분리된다.
  - 프로젝트 이슈 조회/수정/댓글 API는 `issue_iid`를 기준으로 많이 동작한다.
  - 응답에는 `references.short`, `references.relative`, `references.full`도 제공된다.

정리:
- 현재 `externalId + numberOrKey` 구조는 GitLab의 `id + iid`에 잘 맞는다.
- GitLab을 붙일 때 `numberOrKey = iid`, `externalId = id`로 두는 것이 가장 자연스럽다.
- 이 부분은 현재 설계가 GitLab 추가를 잘 버티는 가장 큰 장점이다.

### 3.5 댓글 모델

- GitHub:
  - 이슈 댓글은 Issues Comments API로 관리한다.
  - 저장소 기준 `owner`, `repo`, `issue_number` 조합으로 접근한다.
- GitLab:
  - 이슈 댓글은 Notes API로 관리한다.
  - `project id/path + issue_iid + note_id` 조합을 쓴다.
  - note는 이슈 외에도 merge request, snippet 등 여러 대상에 공통으로 쓰인다.

정리:
- 현재 `RemoteComment` 공통 DTO는 두 플랫폼 모두 대응 가능하다.
- GitLab은 댓글 리소스가 더 일반화되어 있어서 `issue comments`가 아니라 `notes` 개념이라는 점만 어댑터에서 흡수하면 된다.

### 3.6 이슈 상태와 동작

- GitHub:
  - 상태는 보통 `open`, `closed` 축이 중심이다.
- GitLab:
  - 상태는 `opened`, `closed` 축을 기본으로 사용한다.
  - 이슈 수정은 `state_event`로 close/reopen을 처리하는 방식이 포함된다.

정리:
- 서비스 레이어에서는 `OPEN`, `CLOSED` 공통 상태로 유지하고, 플랫폼 어댑터에서 변환하는 구조가 적절하다.

### 3.7 권한과 운영 환경

- GitHub:
  - 공식 SaaS 중심이고, fine-grained PAT 권한 매핑이 명확하다.
  - REST API rate limit이 문서화돼 있고, 인증 사용자 기준 5,000 req/hour가 기본이다.
- GitLab:
  - GitLab.com뿐 아니라 self-managed, dedicated까지 공식 지원 범위에 포함된다.
  - REST API 요청은 인스턴스별 rate limit 설정 영향을 받을 수 있다.
  - 같은 GitLab API라도 배포 환경에 따라 정책이 달라질 수 있다.

정리:
- GitHub는 API 사용 조건이 더 균일하다.
- GitLab은 멀티 플랫폼이라는 목표에는 잘 맞지만, 운영 설정 차이를 고려한 `platform config` 구조가 더 중요하다.

## 4. 현재 프로젝트 구조에 미치는 영향

### 4.1 바로 재사용 가능한 부분

- `PlatformType`
- `PlatformGateway`, `PlatformGatewayResolver`
- `RemoteUserProfile`, `RemoteRepository`, `RemoteIssue`, `RemoteComment`
- `platform + externalId` 캐시 구조
- 프론트의 `/platforms/:platform/...` 라우트 구조
- `PlatformConnection` 중심 연결 화면 구조

### 4.2 GitLab 추가 전에 보완할 부분

- 플랫폼별 API base URL 관리
  - GitHub는 고정 base URL 성격이 강하지만 GitLab은 self-managed 가능성이 높다.
- 플랫폼별 토큰 안내/검증 정책
  - GitHub fine-grained PAT와 GitLab PAT의 권한 안내가 다르다.
- 프로젝트 path 처리
  - GitLab은 숫자 ID 외 URL-encoded path 사용이 빈번하다.
- issue `id` vs `iid` 변환 규칙
  - 공통 DTO에는 잘 들어가지만 어댑터 구현에서 실수하기 쉬운 지점이다.
- 에러 메시지와 문구
  - 현재 공통화는 진행됐지만, 플랫폼별 사용 안내 문구는 더 분리해야 한다.

## 5. 구현 난이도 비교

### GitHub 유지/확장

- 장점:
  - 현재 구현이 이미 존재한다.
  - 계약과 테스트 기반이 있다.
- 리스크:
  - GitHub 전용 가정이 남아 있으면 새 플랫폼 추가 시 다시 드러난다.

### GitLab 추가

- 장점:
  - 프로젝트, 이슈, 댓글 모델이 현재 프로젝트 범위와 잘 맞는다.
  - `id + iid` 구조가 현재 `externalId + numberOrKey` 설계와 잘 맞는다.
  - 첫 번째 추가 플랫폼으로 구조 검증 효과가 크다.
- 리스크:
  - self-managed base URL 지원 필요
  - 프로젝트 path / URL encoding 처리 필요
  - 토큰/권한/배포 환경 차이로 운영 복잡도 증가

## 6. 권장 적용 방향

### 1차 목표

- GitLab을 "GitHub 다음 플랫폼"으로 붙여 공통 구조가 실제로 동작하는지 검증한다.
- 범위는 현재 GitHub와 같은 최소 기능으로 맞춘다:
  - 현재 사용자 확인
  - 프로젝트 목록 조회
  - 프로젝트 이슈 목록 조회
  - 이슈 생성/수정
  - 이슈 댓글 조회/생성

### 구현 순서

1. GitLab API 클라이언트와 설정 객체 추가
2. `GitlabPlatformGateway` 구현
3. GitLab 응답을 `Remote* DTO`로 매핑
4. GitLab 토큰 연결/검증 흐름 추가
5. 테스트 fixture에 GitLab 시나리오 추가
6. 마지막에 프론트 플랫폼 선택/연결 UX 보완

### 설계 원칙

- GitLab 추가 때문에 서비스 레이어를 다시 GitHub/GitLab 분기 로직으로 되돌리지 않는다.
- 플랫폼 차이는 어댑터에서 흡수하고, 서비스는 공통 DTO와 공통 상태만 사용한다.
- GitLab 1차는 GitLab.com만 기준으로 시작하고, self-managed는 설정 확장 포인트로 먼저 열어둔다.

## 7. 결론

- GitLab은 현재 프로젝트에 추가하기 가장 적절한 다음 플랫폼이다.
- 현재 공통화 구조는 GitLab을 붙일 수 있을 정도로 준비되어 있다.
- 가장 중요한 차이는 기능 자체보다 `프로젝트 식별자`, `issue iid`, `토큰/권한`, `base URL`이다.
- 따라서 다음 실제 구현은 "GitLab 기능 추가"보다 "GitLab 차이를 어댑터와 설정으로 흡수"하는 방식으로 진행하는 것이 맞다.

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
