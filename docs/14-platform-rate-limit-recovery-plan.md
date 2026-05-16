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
- 1차 구현의 알림은 별도 푸시/메일이 아니라 실패 API 응답과 조회 화면 상태로 제공한다.
- 동기화 실패 응답에는 사용자가 후속 조치를 판단할 수 있도록 `syncRunId`, `failureId`, `retryable`, `nextRetryAt`, `message`를 포함한다.

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

## 하위 문서

이 문서는 rate limit 복구 설계의 총괄 문서로 유지한다. 세부 설명은 `docs/guides/`의 하위 문서에서 독립적으로 확인한다.

| 문서 | 설명 |
| --- | --- |
| [14-1 SyncRun 실행 이력 설계](./guides/14-1-sync-run-state-flow.md) | 마지막 상태와 실행 이력을 분리하는 기준 |
| [14-2 플랫폼 Rate Limit 설계](./guides/14-2-platform-rate-limit-design.md) | 플랫폼 호출 제한 정보를 공통 모델로 다루는 방식 |
| [14-3 실패 기록과 재처리 설계](./guides/14-3-sync-failure-retry-design.md) | 실패 단위 저장과 재처리 판단 기준 |
| [14-4 수동 재동기화 설계](./guides/14-4-manual-resync-design.md) | 저장소/이슈 단위 cache 보정 흐름 |
| [14-5 복구 API 설계](./guides/14-5-recovery-api-design.md) | 복구 조회, retry, resync API 설계 방향 |
| [14-6 Rate Limit 복구 검증 계획](./guides/14-6-rate-limit-recovery-test-plan.md) | 기존 흐름 유지와 복구 흐름 검증 기준 |

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
- 신규 복구/운영 API에서 상세 이력과 실패 원인을 조회하는 기준이다.
- 기존 `sync-state` 조회 API의 직접 응답 기준으로 사용하지 않는다.

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

### 0. SyncState와 SyncRun 역할

1. `SyncState`는 기존 저장소/이슈 `sync-state` API의 마지막 상태 조회 기준으로 유지한다.
2. `SyncRun`은 rate limit, 수동 재처리, 수동 재동기화의 실행 이력과 장애 복구 조회 기준으로 사용한다.
3. 기존 `UC-09`, `UC-16`은 계속 `SyncState`를 반환한다.
4. refresh/resync 실행이 끝나면 application use case가 `SyncRun`을 먼저 마감하고, 마지막 상태 요약을 `SyncState`에 반영한다.
5. 두 값이 어긋나지 않도록 `SyncState`는 가장 최근에 마감된 관련 `SyncRun`의 요약 상태를 저장한다.

#### SyncRun 상태의 SyncState 반영 기준

| SyncRun 마감 상태 | SyncState 반영 | 메시지 기준 |
| --- | --- | --- |
| `SUCCESS` | `SUCCESS` | 성공 메시지 또는 `null` |
| `PARTIAL_SUCCESS` | `FAILED` | 부분 성공, 실패 건수, 실패 사유 요약 |
| `FAILED` | `FAILED` | 실패 사유 요약 |
| `RATE_LIMITED` | `FAILED` | GitHub rate limit 사유와 `nextRetryAt` 요약 |

- 이유: 1차 구현에서는 기존 `SyncState` 상태값을 확장하지 않는다.
- 결과: `sync-state` API는 성공/실패 요약만 제공하고, 부분 성공과 rate limit의 세부 원인은 `SyncRun`/`SyncFailure` 조회에서 확인한다.
- 주의: `PARTIAL_SUCCESS`는 일부 데이터가 반영됐더라도 운영 복구가 필요한 상태이므로 `SyncState`에는 `FAILED`로 요약한다.

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
5. 사용자 요청에서 발생한 실패라면 응답에 재처리 가능 여부와 재처리 가능 시각을 포함한다.

### 4. 실패 응답과 사용자 안내

1. 사용자가 실행한 refresh/resync 요청에서 실패가 발생한다.
2. application 계층이 `SyncRun`과 `SyncFailure`를 먼저 저장한다.
3. app 계층은 실패 응답에 `syncRunId`, `failureId`, `retryable`, `nextRetryAt`, `message`를 포함한다.
4. 프론트는 응답 정보를 바탕으로 실패 사유와 재처리 가능 시각을 표시한다.
5. 사용자는 즉시 재처리 가능한 경우 재처리 액션을 실행하거나, 실패 이력 화면에서 나중에 다시 실행한다.
6. 1차 구현에서는 시스템이 별도 푸시/메일/자동 알림을 보내지 않는다.

### 5. 수동 재동기화

1. 운영자가 특정 플랫폼/저장소/이슈 단위로 재동기화를 요청한다.
2. application 계층이 새로운 `SyncRun`을 생성한다.
3. 기존 cache와 원격 데이터를 비교해 필요한 변경만 반영한다.
4. 결과와 실패 건수를 이력으로 남긴다.

### 6. 장애 복구

1. 장애 발생 시점 이후의 `SyncRun`과 `SyncFailure`를 조회한다.
2. `FAILED`, `PARTIAL_SUCCESS`, `RATE_LIMITED` 상태를 우선 확인한다.
3. 재시도 가능한 실패만 선택 재처리한다.
4. 누락 가능성이 큰 저장소는 수동 재동기화로 보정한다.
5. 복구 결과를 새 `SyncRun`으로 남겨 추적 가능하게 한다.

## API 초안

OpenAPI YAML 파일은 Swagger Editor에서 확인할 수 있도록 별도 문서로 분리한다.

- [Rate Limit / Recovery API OpenAPI](./openapi/rate-limit-recovery-api.yaml)

아래 목록은 총괄 문서에서 빠르게 훑어보기 위한 요약이다.

| Method | Path | 설명 | 주요 파라미터 | 응답 |
| --- | --- | --- | --- | --- |
| <span style="color:#0969da"><strong>GET</strong></span> | `/api/platforms/{platform}/rate-limit` | 현재 플랫폼 연결 기준 최신 rate limit 상태 조회 | Path: `platform` | 최근 `RateLimitSnapshot`, 없으면 `204 No Content` |
| <span style="color:#0969da"><strong>GET</strong></span> | `/api/sync-runs` | 최근 동기화 실행 이력 조회 | Query: `platform`, `resourceType`, `status`, `from`, `to` | `SyncRun` 목록 |
| <span style="color:#0969da"><strong>GET</strong></span> | `/api/sync-runs/{syncRunId}` | 단일 동기화 실행 상세 조회 | Path: `syncRunId` | `SyncRun` 상세, 처리 건수, 실패 메시지 |
| <span style="color:#0969da"><strong>GET</strong></span> | `/api/sync-failures` | 해결되지 않은 실패와 재처리 가능 여부 조회 | Query: `platform`, `retryable`, `resolved`, `resourceType` | `SyncFailure` 목록 |
| <span style="color:#1a7f37"><strong>POST</strong></span> | `/api/sync-failures/{failureId}/retry` | 재처리 가능한 실패를 선택해 다시 실행 | Path: `failureId` | 새 `SyncRun` 결과 |
| <span style="color:#1a7f37"><strong>POST</strong></span> | `/api/platforms/{platform}/repositories/{repositoryId}/resync` | 특정 저장소 범위의 cache를 원격 상태와 다시 맞춤 | Path: `platform`, `repositoryId`; Query: `scope` | 새 `SyncRun` 결과 |
| <span style="color:#1a7f37"><strong>POST</strong></span> | `/api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync` | 특정 이슈와 필요 시 댓글 cache를 원격 상태와 다시 맞춤 | Path: `platform`, `repositoryId`, `issueNumberOrKey`; Query: `includeComments` | 새 `SyncRun` 결과 |

### refresh/resync 실패 응답

- 대상: 저장소/이슈/댓글 refresh, 저장소/이슈 resync
- 목적: 실패 직후 사용자에게 재처리 판단 정보를 전달
- 포함 필드:
  - `syncRunId`
  - `failureId`
  - `status`
  - `message`
  - `retryable`
  - `nextRetryAt`
- 비고: 1차 구현에서는 이 응답이 사용자 안내의 기준이며 별도 알림 발송은 제외한다.

## 유즈케이스

### RL-01 GitHub Rate Limit 상태 수집

- 목적: GitHub API 응답에서 호출 제한 상태를 추출해 운영 상태로 저장한다.
- 액터: 시스템
- 트리거: GitHub API 호출 완료
- 기본 흐름:
  1. application 계층이 platform gateway로 GitHub API를 호출한다.
  2. platform adapter가 응답 헤더를 확인한다.
  3. platform adapter가 `RateLimitSnapshot`을 생성한다.
  4. application 계층이 최근 rate limit 상태를 저장한다.
  5. 남은 호출 수가 임계값 이하이면 동기화 흐름에 주의 상태를 전달한다.
- 결과: 운영자는 현재 GitHub API 호출 여유를 확인할 수 있다.
- 1차 구현: GitHub만 대상

### RL-02 Rate Limit으로 동기화 중단

- 목적: 호출 제한에 도달한 동기화를 무한 재시도하지 않고 복구 가능한 실패로 남긴다.
- 액터: 시스템
- 트리거: GitHub API가 rate limit 응답을 반환
- 기본 흐름:
  1. platform adapter가 `X-RateLimit-Remaining=0`, `X-RateLimit-Reset`, `Retry-After`를 확인한다.
  2. platform adapter가 `PlatformApiFailure`를 `RATE_LIMITED`로 변환한다.
  3. application 계층이 현재 `SyncRun`을 `RATE_LIMITED` 또는 `PARTIAL_SUCCESS`로 마감한다.
  4. application 계층이 `SyncFailure.retryable=true`로 실패를 저장한다.
  5. application 계층이 `resetAt` 또는 `Retry-After` 기준으로 `nextRetryAt`을 저장한다.
  6. 사용자 요청에서 발생한 실패라면 `failureId`, `syncRunId`, `retryable`, `nextRetryAt`을 응답에 포함한다.
- 결과: 호출 제한이 풀린 뒤 수동 재처리할 수 있는 실패 단위가 남고, 사용자는 응답으로 재처리 가능 여부를 확인한다.
- 1차 구현: 자동 재처리 없음
- 1차 알림 방식: 별도 푸시/메일 없음, 실패 응답과 실패 이력 조회 화면으로 안내

### RL-03 동기화 실패 이력 조회

- 목적: 운영자가 실패한 동기화와 재처리 가능 여부를 확인한다.
- 액터: 사용자
- API: `GET /api/sync-failures`
- 기본 흐름:
  1. 사용자가 실패 목록을 조회한다.
  2. app 계층이 조회 조건을 application 계층에 전달한다.
  3. application 계층이 `SyncFailure` 목록을 조회한다.
  4. 응답에 `failureId`, `syncRunId`, 실패 유형, 재처리 가능 여부, 다음 재처리 가능 시각을 포함한다.
- 결과: 운영자는 rate limit으로 멈춘 동기화와 재처리 가능 시점을 확인할 수 있다.
- 비고: 사용자가 실패 직후 응답을 놓쳤거나 나중에 복구할 때 진입하는 조회 흐름이다.

### RL-04 실패한 동기화 수동 재처리

- 목적: 운영자가 재처리 가능한 실패를 선택해 다시 실행한다.
- 액터: 사용자
- API: `POST /api/sync-failures/{failureId}/retry`
- 기본 흐름:
  1. 사용자가 실패 건 재처리를 요청한다.
  2. application 계층이 `SyncFailure`를 조회한다.
  3. `retryable=false`이면 재처리를 거부한다.
  4. `nextRetryAt`이 현재 시각보다 이후이면 재처리를 보류한다.
  5. application 계층이 새 `SyncRun`을 생성한다.
  6. 실패에 저장된 `resourceType`, `resourceKey`, `operation` 기준으로 동기화를 다시 실행한다.
  7. 성공하면 기존 실패에 `resolvedAt`을 기록한다.
  8. 다시 실패하면 `retryCount`와 `nextRetryAt`을 갱신한다.
- 결과: 자동 작업 큐 없이도 운영자가 실패한 동기화를 복구할 수 있다.
- 1차 구현: 수동 재처리만 제공
- TODO: scheduler 기반 자동 재처리

### RL-05 저장소 단위 수동 재동기화

- 목적: rate limit이나 장애 이후 특정 저장소의 캐시를 원격 상태와 다시 맞춘다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/resync`
- 기본 흐름:
  1. 사용자가 저장소 재동기화를 요청한다.
  2. application 계층이 저장소 접근 권한을 확인한다.
  3. application 계층이 새 `SyncRun`을 생성한다.
  4. 요청 scope에 따라 저장소, 이슈, 댓글 동기화를 실행한다.
  5. cache 반영 결과와 실패 건수를 기록한다.
  6. 실패가 남으면 `SyncFailure`로 저장한다.
- 결과: 누락 가능성이 있는 저장소를 운영자가 직접 보정할 수 있다.

### RL-06 이슈 단위 수동 재동기화

- 목적: 특정 이슈와 댓글만 좁은 범위로 복구한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync`
- 기본 흐름:
  1. 사용자가 이슈 재동기화를 요청한다.
  2. application 계층이 저장소와 이슈 접근 가능 여부를 확인한다.
  3. application 계층이 원격 이슈 단건을 조회한다.
  4. issue 모듈이 `issue_caches`를 갱신한다.
  5. `includeComments=true`이면 댓글 목록도 다시 조회한다.
  6. 결과를 `SyncRun`에 기록한다.
- 결과: 전체 저장소 재동기화 없이 특정 이슈 불일치를 복구할 수 있다.

### RL-07 자동 재처리 예약

- 목적: 1차 수동 재처리 이후 scheduler 기반 자동 복구로 확장한다.
- 액터: 시스템
- 상태: TODO
- 기본 흐름:
  1. scheduler가 `retryable=true`, `resolvedAt=null`, `nextRetryAt<=now` 실패를 조회한다.
  2. 실패 건별로 새 `SyncRun`을 생성한다.
  3. 수동 재처리와 같은 경로로 동기화를 다시 실행한다.
  4. 성공/실패 결과를 기존 `SyncFailure`에 반영한다.
- 결과: 운영자 개입 없이 일시적 장애와 rate limit 실패를 순차 복구한다.
- TODO 사유: 1차에서는 실패 기록과 수동 복구 경로를 먼저 안정화한다.

## 데이터 모델 초안

### `platform_rate_limit_snapshots`

- 목적: 플랫폼별 API 제한 상태 추적
- 소유: application
- 수집: platform
- 저장: application
- 원칙: platform은 GitHub 응답에서 rate limit 값을 추출해 반환값에 포함하고, application은 전달받은 snapshot을 저장한다.
- 금지: platform이 application 저장소나 service를 호출하지 않는다.

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

## 모듈 추가/변경 정리

### 신규 application 하위 영역

#### `application.sync.run`

- 목적: 동기화 실행 단위 관리
- 추가 대상: `SyncRun`, `SyncRunStatus`, `SyncRunRepository`, `SyncRunService`
- 책임:
  - 동기화 시작/종료 기록
  - `SUCCESS`, `PARTIAL_SUCCESS`, `FAILED`, `RATE_LIMITED` 상태 관리
  - 처리 건수와 실패 메시지 저장
- 의존:
  - shared-kernel의 `PlatformType`
  - application 내부 persistence 설정

#### `application.sync.failure`

- 목적: 재처리 가능한 실패 단위 관리
- 추가 대상: `SyncFailure`, `SyncFailureType`, `SyncFailureRepository`, `SyncFailureService`
- 책임:
  - 실패 유형 기록
  - `retryable`, `retryCount`, `nextRetryAt`, `resolvedAt` 관리
  - 수동 재처리 대상 조회
- 의존:
  - `SyncRun`
- 금지:
  - 재동기화 use case 의존
  - platform gateway 호출
  - repository / issue / comment cache 갱신
- 흐름:
  - recovery use case가 `SyncFailureService`를 조회/갱신한다.
  - `SyncFailureService`는 실패 레코드 저장과 상태 변경만 담당한다.

#### `application.ratelimit`

- 목적: platform에서 수집한 rate limit 상태 저장/조회
- 추가 대상: `RateLimitSnapshot`, `RateLimitSnapshotRepository`, `RateLimitService`
- 책임:
  - 최근 GitHub rate limit 상태 저장
  - 조회 API 응답용 상태 제공
  - 임계값 이하 여부 판단
- 의존:
  - platform에서 전달한 공통 rate limit 모델

#### `application.sync.recovery`

- 목적: 수동 재처리와 수동 재동기화 use case 조립
- 추가 대상: `GetSyncFailuresUseCase`, `RetrySyncFailureUseCase`, `ResyncRepositoryUseCase`, `ResyncIssueUseCase`
- 책임:
  - 실패 이력 조회
  - 실패 건 수동 재처리
  - 저장소/이슈 단위 수동 재동기화
  - 재처리 가능 여부와 `nextRetryAt` 검증
- 의존:
  - connection token access
  - platform gateway
  - repository / issue / comment cache public API
  - sync run / failure / rate limit service
- 진입점:
  - app controller는 넓은 facade가 아니라 개별 use case public API를 호출한다.
  - 공통 세션/토큰 조회는 use case 내부의 package-private collaborator로만 분리한다.
- 금지:
  - `SyncRecoveryApplicationFacade` 같은 복구 통합 facade 추가
  - 여러 복구 흐름을 하나의 넓은 service/facade에 누적

### 기존 application 변경

- 변경 대상: 기존 refresh/create/update/comment 작성 흐름을 담당하는 개별 use case 또는 현재 전환 전 facade 메서드
- 변경 내용:
  - refresh 시작 시 `SyncRun` 생성
  - 성공 시 `SyncRun` 성공 마감
  - 일부 실패 시 `PARTIAL_SUCCESS` 기록
  - rate limit 실패 시 `RATE_LIMITED` 기록
  - 재처리 가능한 실패를 `SyncFailure`로 저장
- 유지 기준:
  - 기존 `SyncState`는 `UC-09`, `UC-16`의 마지막 상태 조회용으로 유지
  - `SyncState`는 마지막으로 마감된 관련 `SyncRun`의 요약을 반영한다.
  - 기존 refresh API 응답 형태는 가능한 한 유지
- 전환 기준:
  - 신규 rate limit/recovery 구현은 개별 use case 클래스를 기본 진입점으로 둔다.
  - 기존 `*ApplicationFacade`가 남아 있는 흐름은 당장 제거하지 않되, 새 복구 흐름을 facade에 추가하지 않는다.

### platform 변경

- 변경 대상: `PlatformGateway`, GitHub gateway/client
- 추가 대상: `RateLimitSnapshot` 전달 모델, `PlatformApiFailure` 실패 모델
- 변경 내용:
  - GitHub 응답 헤더에서 rate limit 정보 추출
  - GitHub 오류 응답을 공통 실패 유형으로 변환
  - rate limit 여부, 재시도 가능 여부, reset 시각 전달
- 유지 기준:
  - platform은 실패를 기록하지 않는다.
  - platform은 rate limit snapshot을 저장하지 않는다.
  - platform은 재처리 정책을 판단하지 않는다.
  - GitLab 구현은 TODO로 남긴다.

### app 변경

- 변경 대상: 신규 controller 또는 기존 sync 관련 controller
- 추가 API:
  - `GET /api/platforms/{platform}/rate-limit`
  - `GET /api/sync-runs`
  - `GET /api/sync-runs/{syncRunId}`
  - `GET /api/sync-failures`
  - `POST /api/sync-failures/{failureId}/retry`
  - `POST /api/platforms/{platform}/repositories/{repositoryId}/resync`
  - `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync`
- 책임:
  - HTTP 요청/응답 경계 제공
  - 세션 사용자 기준 application use case 호출
  - 재처리 불가, 재처리 보류, rate limit 상태를 명확한 HTTP 응답으로 변환

### connection 변경

- 변경 없음 또는 최소 변경
- 유지 책임:
  - 현재 사용자 platform 연결 조회
  - token/baseUrl 제공
- 금지:
  - rate limit 상태 저장
  - 동기화 실패 정책 판단

### repository / issue / comment 변경

- 변경 없음 또는 public API 소폭 확장
- 필요한 경우:
  - 저장소/이슈 단위 resync에서 재사용 가능한 cache upsert API 보강
  - 접근 권한 확인 API 재사용
- 금지:
  - rate limit 판단
  - `SyncRun`, `SyncFailure` 직접 저장
  - platform gateway 직접 호출

### shared-kernel 변경

- 원칙: 변경 없음
- 유지 기준:
  - `PlatformType`만 공유
  - rate limit / sync failure / recovery 모델은 shared-kernel로 올리지 않는다.

### 1차 구현 모듈 영향 요약

| 모듈 | 영향 | 내용 |
| --- | --- | --- |
| app | 추가 | 복구/조회 API controller |
| application | 추가/변경 | sync run, failure, rate limit, recovery use case |
| platform | 변경 | GitHub rate limit 추출, 실패 모델 변환 |
| connection | 유지 | token/baseUrl 제공만 유지 |
| repository | 유지/소폭 확장 | cache upsert와 접근 확인 API 재사용 |
| issue | 유지/소폭 확장 | issue cache upsert와 접근 확인 API 재사용 |
| comment | 유지/소폭 확장 | comment cache upsert API 재사용 |
| shared-kernel | 유지 | `PlatformType` only |

## 구현 단계

### 구현 대상

- 1차 구현: GitHub
- TODO: GitLab rate limit 헤더/응답 매핑
- TODO: GitLab 장애 유형 분류
- TODO: GitLab 수동 재동기화 검증

### 1단계: 동기화 실행/실패 이력 기반 추가

- `SyncRun`, `SyncFailure` 모델 추가
- 기존 `SyncState`는 `UC-09`, `UC-16` 응답 기준으로 유지
- `SyncRun` 마감 결과를 `SyncState` 요약 상태에 반영
- `PARTIAL_SUCCESS`, `RATE_LIMITED`는 `SyncState`에 `FAILED`로 요약하고 메시지에 상세 사유 기록
- repository refresh / issue refresh 흐름부터 이력 기록 적용

### 2단계: GitHub Rate Limit 감지와 실패 응답

- GitHub 응답 헤더 수집
- `RateLimitSnapshot` 공통 모델 도입
- `RATE_LIMITED` 상태와 `nextRetryAt` 기록
- refresh/resync 실패 응답에 `syncRunId`, `failureId`, `retryable`, `nextRetryAt`, `message` 포함
- 별도 푸시/메일 알림 없이 실패 응답과 조회 화면으로 사용자 안내

### 3단계: 수동 복구 API

- 실패 이력 조회 API 추가
- `SyncFailure.retryable=true` 대상 수동 재처리 API 추가
- 저장소 단위 재동기화 API 추가
- 이슈 단위 재동기화 API 추가
- 재처리 성공 시 `resolvedAt` 기록
- 실패 반복 시 `retryCount`, `nextRetryAt` 갱신

### 후속 TODO

- scheduler 기반 자동 재처리
- Webhook 이벤트 저장 구조와 `SyncFailure` 연결
- Webhook 누락 의심 구간 수동 재동기화 보정
- GitLab rate limit / 장애 유형 / 수동 재동기화 검증

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
