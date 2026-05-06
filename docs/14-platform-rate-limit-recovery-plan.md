# 플랫폼별 API Rate Limit 관리 + 장애 복구 시스템 설계 계획

## 목적

- 외부 플랫폼 API 제약과 장애 상황을 전제로 동기화 안정성을 높인다.
- 1차 구현은 GitHub만 대상으로 하되, GitLab 등 다른 플랫폼 확장은 TODO로 남긴다.
- 장애 발생 후 누락 범위 확인, 실패 원인 추적, 선택 재처리가 가능한 백엔드 기반을 만든다.

## 배경

- 현재 원격 플랫폼 호출은 application 계층이 connection, platform, cache 모듈을 조합한다.
- 현재 동기화 상태는 마지막 결과 중심으로 관리된다.
- 운영 단계에서는 API rate limit, 일시적 외부 장애, 서버 장애, Webhook 처리 실패가 발생할 수 있다.
- 단순 재시도만으로는 어느 데이터가 누락됐는지, 어떤 작업을 다시 실행해야 하는지 추적하기 어렵다.

## 설계 원칙

- 플랫폼별 API 제약은 `platform` 모듈 안에서 수집하고 중립 모델로 변환한다.
- 동기화 실행 이력과 실패 이벤트는 application 계층의 운영 데이터로 관리한다.
- repository / issue / comment 모듈은 동기화 복구 정책을 직접 알지 않는다.
- GitHub 전용 헤더와 오류 코드는 공통 `RateLimitSnapshot`, `PlatformApiFailure` 형태로 감싼다.
- 실패 처리는 재시도 가능 여부를 명시하고, 운영자가 수동 복구할 수 있는 단위를 남긴다.

## 목표 범위

- 플랫폼별 rate limit 상태 수집
- 외부 API 호출 실패 유형 분류
- 동기화 실행 이력 저장
- 실패한 동기화 작업 재처리
- 수동 재동기화 API 설계
- 장애 복구용 조회 API 설계
- 1차 구현 대상은 GitHub로 제한

## 제외 범위

- 대규모 메시지 브로커 도입
- 별도 관리자 프론트 화면 구현
- GitHub App 전환
- GitLab rate limit 구현
- OAuth 기반 인증 전환
- 다중 사용자 조직 권한 모델 고도화

## 주요 개념

### RateLimitSnapshot

- 플랫폼 API 응답에서 추출한 호출 제한 상태
- 1차 구현에서는 GitHub 기준 헤더를 공통 모델로 변환
- GitLab 등 다른 플랫폼 매핑은 TODO로 분리

주요 필드

- `platform`
- `limit`
- `remaining`
- `resetAt`
- `retryAfterSeconds`
- `resource`
- `capturedAt`

### PlatformApiFailure

- 외부 플랫폼 API 호출 실패를 공통 형식으로 표현
- application 계층은 이 값으로 재시도 가능 여부와 복구 방식을 판단한다.

주요 필드

- `platform`
- `operation`
- `statusCode`
- `errorType`
- `retryable`
- `rateLimited`
- `message`
- `externalRequestId`
- `occurredAt`

### SyncRun

- 한 번의 동기화 실행 단위
- 수동 실행, 예약 실행, Webhook 기반 실행을 모두 같은 테이블에 기록한다.

주요 필드

- `id`
- `platform`
- `syncType`
- `resourceType`
- `resourceKey`
- `status`
- `triggeredBy`
- `startedAt`
- `finishedAt`
- `createdCount`
- `updatedCount`
- `skippedCount`
- `failedCount`
- `failureMessage`

### SyncFailure

- 재처리 가능한 실패 단위
- SyncRun 전체 실패뿐 아니라 일부 리소스 실패도 별도로 남긴다.

주요 필드

- `id`
- `syncRunId`
- `platform`
- `resourceType`
- `resourceKey`
- `operation`
- `errorType`
- `retryable`
- `retryCount`
- `nextRetryAt`
- `lastErrorMessage`
- `resolvedAt`

## 처리 흐름

### 1. 원격 API 호출

1. application 계층이 connection에서 token/baseUrl을 조회한다.
2. application 계층이 platform gateway를 호출한다.
3. platform adapter가 원격 API 응답 헤더와 오류를 수집한다.
4. platform adapter가 `RateLimitSnapshot`과 `PlatformApiFailure`로 변환한다.
5. application 계층이 sync 이력과 실패 정보를 기록한다.

### 2. Rate Limit 감지

1. platform adapter가 API 응답마다 남은 호출 수를 확인한다.
2. `remaining`이 임계값 이하이면 application 계층에 제한 위험 상태를 전달한다.
3. application 계층은 새 동기화 실행을 지연하거나 부분 성공으로 종료한다.
4. `resetAt` 이후 재시도할 수 있도록 `SyncFailure.nextRetryAt`을 기록한다.

### 3. 동기화 실패 기록

1. 동기화 시작 시 `SyncRun`을 `RUNNING`으로 생성한다.
2. 원격 호출이나 cache 반영 실패가 발생하면 `SyncFailure`를 생성한다.
3. 재시도 가능한 실패는 `retryable=true`로 저장한다.
4. 전체 실행 결과는 `SUCCESS`, `PARTIAL_SUCCESS`, `FAILED`, `RATE_LIMITED` 중 하나로 마감한다.

### 4. 수동 재동기화

1. 운영자가 특정 플랫폼/저장소/이슈 단위로 재동기화를 요청한다.
2. application 계층이 새로운 `SyncRun`을 생성한다.
3. 기존 cache와 원격 데이터를 비교해 필요한 변경만 반영한다.
4. 결과와 실패 건수를 이력으로 남긴다.

### 5. 장애 복구

1. 장애 발생 시점 이후의 `SyncRun`과 `SyncFailure`를 조회한다.
2. `FAILED`, `PARTIAL_SUCCESS`, `RATE_LIMITED` 상태를 우선 확인한다.
3. 재시도 가능한 실패만 선택 재처리한다.
4. 누락 가능성이 큰 저장소는 수동 재동기화로 보정한다.
5. 복구 결과를 새 `SyncRun`으로 남겨 추적 가능하게 한다.

## API 초안

### GET `/api/platforms/{platform}/rate-limit`

- 현재 플랫폼 연결 기준 rate limit 상태 조회
- 응답: 최근 `RateLimitSnapshot`

### GET `/api/sync-runs`

- 동기화 실행 이력 조회
- 쿼리: `platform`, `resourceType`, `status`, `from`, `to`

### GET `/api/sync-runs/{syncRunId}`

- 동기화 실행 상세 조회
- 실패 목록과 처리 건수 포함

### GET `/api/sync-failures`

- 재처리 대상 실패 목록 조회
- 쿼리: `platform`, `retryable`, `resolved`, `resourceType`

### POST `/api/sync-failures/{failureId}/retry`

- 단일 실패 재처리
- 응답: 새 `SyncRun` 결과

### POST `/api/platforms/{platform}/repositories/{repositoryId}/resync`

- 저장소 단위 수동 재동기화
- 요청 옵션: `scope=REPOSITORY|ISSUES|COMMENTS|ALL`

### POST `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync`

- 이슈 단위 수동 재동기화
- 요청 옵션: `includeComments`

## 데이터 모델 초안

### `platform_rate_limit_snapshots`

- 목적: 플랫폼별 API 제한 상태 추적
- 소유: platform 또는 application 중 구현 단계에서 결정
- 권장: 수집은 platform, 저장은 application

주요 필드

- `id`
- `platform`
- `connection_id`
- `resource`
- `limit_count`
- `remaining_count`
- `reset_at`
- `retry_after_seconds`
- `captured_at`

### `sync_runs`

- 목적: 동기화 실행 단위 기록
- 소유: application

주요 필드

- `id`
- `platform`
- `sync_type`
- `resource_type`
- `resource_key`
- `status`
- `triggered_by`
- `started_at`
- `finished_at`
- `created_count`
- `updated_count`
- `skipped_count`
- `failed_count`
- `failure_message`

### `sync_failures`

- 목적: 실패 원인과 재처리 단위 기록
- 소유: application

주요 필드

- `id`
- `sync_run_id`
- `platform`
- `resource_type`
- `resource_key`
- `operation`
- `error_type`
- `retryable`
- `retry_count`
- `next_retry_at`
- `last_error_message`
- `resolved_at`

## 모듈 책임

### app

- 복구/조회용 HTTP API 제공
- 요청 DTO를 application command로 변환
- 플랫폼별 세부 오류를 직접 해석하지 않는다.

### application

- 동기화 실행 이력 생성/마감
- 실패 기록과 재처리 정책 관리
- 수동 재동기화 use case 조합
- rate limit 상태에 따른 실행 지연/중단 판단

### platform

- GitHub API 호출
- 응답 헤더 기반 rate limit 정보 추출
- 외부 API 오류를 공통 실패 모델로 변환
- 재시도 정책 자체는 소유하지 않는다.

### connection

- 현재 사용자 플랫폼 연결과 token/baseUrl 제공
- rate limit 정책과 sync 이력을 직접 알지 않는다.

### repository / issue / comment

- cache 반영 API 제공
- 복구 정책, 재시도 횟수, 외부 API 제한 상태를 직접 알지 않는다.

## 구현 단계

### 구현 대상

- 1차 구현: GitHub
- TODO: GitLab rate limit 헤더/응답 매핑
- TODO: GitLab 장애 유형 분류
- TODO: GitLab 수동 재동기화 검증

### 1단계: 실패 모델과 이력 기반 추가

- `SyncRun`, `SyncFailure` 모델 추가
- 기존 `SyncState`는 마지막 상태 조회용으로 유지
- repository refresh / issue refresh 흐름부터 이력 기록 적용

### 2단계: Rate Limit 수집

- GitHub 응답 헤더 수집
- `RateLimitSnapshot` 공통 모델 도입
- `RATE_LIMITED` 상태와 `nextRetryAt` 기록

### 3단계: 수동 재동기화 API

- 저장소 단위 재동기화 API 추가
- 이슈 단위 재동기화 API 추가
- 실패 이력 조회 API 추가

### 4단계: 실패 재처리

- `SyncFailure.retryable=true` 대상 재처리
- 재처리 성공 시 `resolvedAt` 기록
- 실패 반복 시 `retryCount`, `nextRetryAt` 갱신

### 5단계: Webhook 복구 기반 확장

- Webhook 이벤트 저장 구조와 연결
- 이벤트 처리 실패를 `SyncFailure`로 연결
- Webhook 누락 의심 구간을 수동 재동기화로 보정

## 검증 기준

- rate limit 응답을 받으면 동기화가 무한 재시도되지 않는다.
- 실패한 동기화 실행은 조회 API에서 확인할 수 있다.
- 재시도 가능한 실패와 불가능한 실패가 구분된다.
- 저장소 단위 수동 재동기화 후 cache 상태가 원격 상태와 맞춰진다.
- 기존 GitHub 저장소/이슈/댓글 흐름은 유지된다.

## 포트폴리오 설명 포인트

- 외부 API 제약을 플랫폼 어댑터 경계에서 중립 모델로 추상화
- 동기화 실행 이력과 실패 단위를 분리해 장애 복구 가능성 확보
- 단순 CRUD를 넘어 운영 가능한 백엔드 흐름 설계
- GitHub를 먼저 안정화하고 GitLab 확장 지점을 TODO로 보존
