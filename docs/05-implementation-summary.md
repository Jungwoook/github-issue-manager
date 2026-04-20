# 구현 요약

## 현재 구현 범위

- GitHub PAT 연결, 저장소 조회, 이슈 조회/생성/수정/닫기, 댓글 조회/작성이 구현되어 있다.
- GitLab PAT 연결, 프로젝트 조회, 이슈 조회/생성/수정, 댓글 조회/작성이 공통 gateway 구조에 연결되어 있다.
- 백엔드는 GitHub/GitLab API 연동과 캐시 저장을 담당한다.
- 프론트엔드는 플랫폼 파라미터 기반 라우트와 API 호출을 사용한다.
- 현재 구조는 플랫폼 어댑터를 분리한 1차 추상화 단계이며, 완전한 독립 모듈 구조는 아니다.

## 완료된 구조 개선

- `PlatformType`, `PlatformGateway`, `PlatformGatewayResolver` 도입
- `RemoteRepository`, `RemoteIssue`, `RemoteComment`, `RemoteUserProfile` 도입
- `PlatformConnection` 기반 PAT 연결 모델 도입
- 캐시 식별자 `platform + externalId` 기준으로 일반화
- API 경로 `/api/platforms/{platform}/...`로 전환
- 프론트 API 모듈, query key, 라우트에 platform 반영
- GitLab `baseUrl` 저장과 HTTPS/API path 정규화 반영
- GitLab 프로젝트 경로 인코딩 보강

## 현재 제약

- 새 플랫폼 추가 시 `PlatformType`, resolver, 프론트 platform metadata 등 공통 코드 수정이 발생한다.
- 플랫폼 목록과 지원 기능이 registry/capability 기반으로 분리되어 있지 않다.
- 세션은 현재 플랫폼 1개 중심으로 동작한다.
- 저장소 접근 검증은 `ownerKey == accountLogin` 기준이다.
- GitLab group/subgroup, GitHub organization 저장소 접근 제어는 후속 보강 대상이다.
- 라벨/담당자/우선순위 UI 일부는 있으나 정식 백엔드 구현 범위가 아니다.

## 다음 주요 과제

- 플랫폼 모듈 registry 도입
- `PlatformType` enum 의존 축소
- capability 기반 기능 지원 범위 분리
- 서버 제공 platform metadata로 프론트 플랫폼 목록 구성
- 플랫폼 연결 unique 제약을 self-managed GitLab까지 견딜 수 있게 보강
- 저장소 접근 제어를 owner 기준에서 사용자 접근 권한 기준으로 전환
- GitLab 프로젝트 표시명, path, slug 역할 분리
- GitLab 통합 시나리오 테스트 보강
- 라벨/담당자/우선순위는 별도 기능 범위로 재정의
