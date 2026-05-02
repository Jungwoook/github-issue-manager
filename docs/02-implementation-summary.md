# 구현 요약

## 1. 현재 구현 범위

- 플랫폼 PAT 연결, 저장소 조회, 이슈 조회/생성/수정/닫기, 댓글 조회/작성까지 구현되어 있다.
- 기본 사용 플랫폼은 GitHub다.
- API는 `/api/platforms/{platform}/...` 경로를 사용한다.
- 조회 API는 로컬 캐시를 우선 읽고, refresh API가 원격 플랫폼과 캐시를 동기화한다.

## 2. 백엔드 구조

- Gradle 멀티 모듈 구조로 구성되어 있다.
- `app`은 HTTP 요청과 bootstrapping을 담당하고, application public API를 호출한다.
- `application`은 connection, platform, cache 모듈을 조립하는 use case orchestration을 담당한다.
- `platform`은 credential 검증, gateway, GitHub/GitLab adapter, remote DTO mapping을 담당한다.
- `connection`은 사용자, 플랫폼 연결, PAT 암호화, 세션 상태를 담당한다.
- `repository`, `issue`, `comment`는 각자 캐시와 cache 반영/조회 public API를 소유한다.
- `shared-kernel`은 `PlatformType`만 제공한다.

## 3. 반영 완료된 개선 사항

- GitHub 전용 경로와 식별자 중심 구조를 플랫폼 공통 API 기준으로 전환했다.
- PAT 검증과 저장 흐름을 platform / connection 책임으로 분리했다.
- 원격 API 호출 조립 책임을 application 계층으로 이동했다.
- repository / issue / comment 모듈의 connection / remote 호출 직접 의존을 제거했다.
- SyncState 클러스터를 application으로 이동했다.
- shared-kernel을 `PlatformType` only 기준으로 축소했다.
- 공통 not found 예외를 모듈별 예외로 분리했다.
- 모듈별 Spring/JPA configuration을 추가했다.
- Gradle 의존 방향과 금지 import 규칙을 테스트로 검증한다.

## 4. 현재 제약

- 기본 사용 흐름은 GitHub 기준이다.
- GitLab 실제 운영 사용성은 현재 포트폴리오 범위 밖이다.
- OAuth / GitHub App 기반 구조는 아직 미구현 상태다.
- 라벨, 담당자, 우선순위, 마일스톤, sub-issue는 현재 범위에서 제외한다.

## 5. 포트폴리오 관점의 핵심

- 단순 CRUD 기능보다 외부 플랫폼 의존성을 모듈 경계 뒤로 격리한 과정이 핵심이다.
- 현재 구조는 다중 플랫폼 완성이 아니라, 다중 플랫폼을 수용할 수 있는 백엔드 경계 설계를 보여준다.
- 변화 과정은 `04-architecture-transition-history.md`, 현재 구조는 `05-platform-module-service-structure.md`에 정리되어 있다.
