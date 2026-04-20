# Architecture

## 1. 개요

현재 시스템은 `공통 코어 + 플랫폼 어댑터`를 지향하는 1차 추상화 구조를 사용한다.

- 프론트엔드는 플랫폼 파라미터를 포함한 공통 라우트와 API 모듈을 사용한다.
- 백엔드는 서비스 레이어에서 `PlatformGateway`를 호출한다.
- GitHub와 GitLab은 각각 어댑터로 분리되어 외부 API 차이를 변환한다.
- 내부 DB는 플랫폼 연결 정보, 저장소/이슈/댓글 캐시, 동기화 상태를 저장한다.

다만 현재 구조는 완전한 모듈 구조는 아니다. 새 플랫폼을 추가하면 `PlatformType`, gateway resolver, 프론트 플랫폼 메타데이터처럼 공통 코드에 등록성 수정이 발생한다. 따라서 현재 단계는 "플랫폼별 구현체 분리"이고, 다음 단계 목표는 "플랫폼 모듈이 스스로 등록되는 구조"이다.

## 2. 구성 요소

### 프론트엔드

- React + Vite
- React Query 기반 데이터 조회
- 플랫폼 연결 화면
- 저장소 목록, 이슈 목록, 이슈 생성/수정/상세, 댓글 화면
- `/platforms/:platform/...` 라우트와 legacy GitHub 라우트 redirect

### 백엔드

- Spring Boot
- 세션 기반 현재 사용자 및 현재 플랫폼 관리
- PAT 암호화 저장
- `PlatformGatewayResolver`로 플랫폼 어댑터 선택
- 캐시 및 동기화 상태 관리

### 플랫폼 어댑터

- GitHub: GitHub REST API 호출
- GitLab: GitLab REST API 호출, `baseUrl` 지원, project path와 issue `iid` 처리

### 플랫폼 모듈화 목표

- 플랫폼 목록을 공통 enum에 고정하지 않는다.
- 플랫폼 모듈이 id, 표시명, 연결 입력값, 지원 capability를 선언한다.
- resolver는 플랫폼 구현체를 수동 분기하지 않고 registry에서 조회한다.
- 프론트엔드는 하드코딩된 플랫폼 목록 대신 서버 metadata를 기준으로 화면을 구성한다.

## 3. 백엔드 계층

- `controller`: `/api/platforms/{platform}/...` HTTP 엔드포인트 제공
- `service`: 비즈니스 흐름, 캐시 갱신, 공통 플랫폼 포트 호출
- `core.platform`: `PlatformType`, `PlatformGateway`, resolver
- `core.remote`: `RemoteRepository`, `RemoteIssue`, `RemoteComment`, `RemoteUserProfile`
- `github`: GitHub 전용 API client와 gateway
- `gitlab`: GitLab 전용 API client와 gateway
- `repository`: JPA 기반 데이터 접근
- `domain`: 사용자, 플랫폼 연결, 캐시, 동기화 상태 엔티티
- `exception`: 공통 오류 응답

## 4. 주요 처리 흐름

### 플랫폼 PAT 등록

1. 사용자가 플랫폼과 PAT를 입력한다.
2. GitLab은 선택적으로 `baseUrl`을 입력한다.
3. 백엔드는 해당 플랫폼 gateway로 현재 사용자 API를 호출한다.
4. 검증 성공 시 `platform_connections`에 연결 정보와 암호화된 PAT를 저장한다.
5. 세션에 현재 사용자와 플랫폼을 기록한다.

### 저장소 조회

1. 프론트엔드가 `/api/platforms/{platform}/repositories`를 호출한다.
2. 백엔드는 현재 세션의 플랫폼 연결을 확인한다.
3. 캐시된 저장소 목록을 반환한다.

### 저장소 새로고침

1. 프론트엔드가 `/api/platforms/{platform}/repositories/refresh`를 호출한다.
2. 백엔드는 gateway로 외부 플랫폼 저장소/프로젝트 목록을 조회한다.
3. 결과를 `repository_caches`에 upsert한다.
4. 동기화 상태를 기록한다.

### 이슈 조회와 새로고침

1. 이슈 목록은 캐시에서 조회한다.
2. 새로고침 요청 시 gateway로 외부 플랫폼 이슈 목록을 조회한다.
3. 결과를 `issue_caches`에 upsert한다.

### 댓글 조회와 새로고침

1. 댓글 목록은 캐시에서 조회한다.
2. 새로고침 요청 시 gateway로 외부 플랫폼 댓글 목록을 조회한다.
3. 결과를 `comment_caches`에 upsert한다.

## 5. 현재 제약

- 현재 구조는 독립 모듈/플러그인 구조가 아니라 어댑터 분리 구조이다.
- 새 플랫폼 추가 시 공통 코드와 프론트 메타데이터 수정이 발생한다.
- 세션은 현재 사용자와 현재 플랫폼 1개를 중심으로 동작한다.
- 저장소 접근 검증은 현재 `ownerKey == accountLogin` 전제를 사용한다.
- GitLab group/subgroup 프로젝트와 조직 저장소 접근 제어는 후속 보강 대상이다.
- 라벨/담당자/우선순위 관련 프론트 스캐폴드는 있으나 정식 백엔드 계약에는 포함되지 않는다.

## 6. 모듈화 전환 단계

### 6.1 1단계: 등록 구조 분리

- `PlatformType` enum 의존 축소
- `PlatformModule` 또는 동등한 registry 계약 도입
- 각 플랫폼이 `platformId`, 표시명, gateway, 지원 기능을 자체 제공

### 6.2 2단계: capability 분리

- 저장소, 이슈, 댓글, 라벨 같은 기능을 capability 단위로 선언
- 모든 플랫폼이 모든 기능을 지원한다는 전제를 제거
- 서비스는 capability 지원 여부를 확인한 뒤 기능 실행

### 6.3 3단계: 프론트 metadata 연동

- `/api/platforms` 같은 metadata API 제공
- 프론트의 `SUPPORTED_PLATFORMS`, platform 문구, 입력 필드 하드코딩 축소
- 플랫폼 추가 시 프론트 코드 수정 범위 최소화

### 6.4 4단계: 패키지/빌드 모듈 분리

- 초기에는 패키지 경계부터 정리
- 이후 필요 시 Gradle 멀티 모듈 또는 플러그인 구조로 분리
- 목표 구조: `platform-core`, `platform-github`, `platform-gitlab`, `platform-{new}`
