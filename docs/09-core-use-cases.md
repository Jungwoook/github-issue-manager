# Core Use Cases

## 1. 개요

현재 구현은 플랫폼 공통 API를 기준으로 동작한다.

- 기본 플랫폼은 `github`이다.
- API 경로는 `/api/platforms/{platform}/...` 형식을 사용한다.
- 인증은 서버 세션과 등록된 플랫폼 PAT를 기준으로 한다.
- app controller는 application facade를 호출하고, application이 connection / platform / cache 모듈을 조립한다.
- 원격 호출은 application이 platform gateway를 호출하는 방식으로 수행한다.
- 조회 API는 로컬 캐시를 우선 읽고, `refresh` API가 원격 플랫폼과 캐시를 동기화한다.

## 2. 액터

- 방문자: 플랫폼 토큰을 아직 등록하지 않은 사용자
- 사용자: 플랫폼 토큰을 등록하고 세션이 연결된 사용자
- 플랫폼 API: GitHub 또는 GitLab API
- 로컬 캐시: 저장소, 이슈, 댓글, 동기화 상태 저장소

## 3. 구현 기준

- 플랫폼 연결 API는 `/api/platforms/{platform}/token` 경로를 사용함
- 저장소/이슈/댓글 API는 `/api/platforms/{platform}/repositories...` 경로를 사용함
- 외부 리소스는 `platform + repositoryId/issueId/numberOrKey` 표현으로 다룸
- 인증 검증은 `PlatformCredentialFacade`가 수행하고, 저장은 connection 모듈이 담당함
- 저장소/이슈/댓글 기능은 application facade를 통해 접근함
- 삭제 API는 실제 삭제가 아니라 이슈 상태를 `CLOSED`로 변경하는 닫기 처리임
- 라벨 API는 현재 백엔드 컨트롤러 구현 범위에 포함하지 않음

## 4. 유스케이스 목록

- UC-01 플랫폼 토큰 등록
- UC-02 토큰 상태 조회
- UC-03 현재 사용자 조회
- UC-04 플랫폼 연결 해제
- UC-05 로그아웃
- UC-06 저장소 새로고침
- UC-07 저장소 목록 조회
- UC-08 저장소 상세 조회
- UC-09 저장소 동기화 상태 조회
- UC-10 이슈 새로고침
- UC-11 이슈 목록 조회
- UC-12 이슈 생성
- UC-13 이슈 상세 조회
- UC-14 이슈 수정
- UC-15 이슈 닫기
- UC-16 이슈 동기화 상태 조회
- UC-17 댓글 새로고침
- UC-18 댓글 목록 조회
- UC-19 댓글 작성
- UC-20 GitHub Rate Limit 상태 수집
- UC-21 Rate Limit 동기화 중단 기록
- UC-22 동기화 실패 이력 조회
- UC-23 실패한 동기화 수동 재처리
- UC-24 저장소 단위 수동 재동기화
- UC-25 이슈 단위 수동 재동기화
- UC-26 자동 재처리 예약 TODO

## 5. 유스케이스 상세

### UC-01 플랫폼 토큰 등록

- 목적: 사용자가 플랫폼 PAT를 등록하고 세션을 연결한다.
- 액터: 방문자
- API: `POST /api/platforms/{platform}/token`
- 기본 흐름:
  1. 사용자가 플랫폼과 PAT를 입력한다.
  2. 백엔드가 `PlatformCredentialFacade`로 토큰을 검증한다.
  3. connection 모듈이 검증된 계정과 암호화된 토큰을 저장한다.
  4. 세션에 `currentUserId`, `currentPlatform`을 저장한다.
  5. 현재 사용자 정보를 반환한다.
- 결과: 사용자는 저장소/이슈/댓글 기능을 사용할 수 있다.

### UC-02 토큰 상태 조회

- 목적: 현재 세션의 플랫폼 연결 상태를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/token/status`
- 기본 흐름:
  1. 프론트가 선택된 플랫폼의 토큰 상태를 요청한다.
  2. connection 모듈이 세션 사용자와 플랫폼 연결을 확인한다.
  3. 연결 여부, 계정명, baseUrl, 검증 시각을 반환한다.
- 결과: 화면은 연결됨/연결 필요 상태를 표시한다.

### UC-03 현재 사용자 조회

- 목적: 세션에 연결된 현재 사용자를 확인한다.
- 액터: 사용자
- API: `GET /api/me`
- 기본 흐름:
  1. 프론트가 현재 사용자 정보를 요청한다.
  2. connection 모듈이 세션의 현재 플랫폼을 확인한다.
  3. 연결된 사용자 정보를 반환한다.
- 결과: 화면은 사용자명, 플랫폼, 계정명을 사용할 수 있다.

### UC-04 플랫폼 연결 해제

- 목적: 현재 플랫폼의 저장된 PAT 연결을 해제한다.
- 액터: 사용자
- API: `DELETE /api/platforms/{platform}/token`
- 기본 흐름:
  1. 사용자가 연결 해제를 실행한다.
  2. connection 모듈이 저장된 암호화 토큰과 scope를 제거한다.
  3. 현재 세션 플랫폼이면 세션 연결 정보를 제거한다.
- 결과: 이후 보호 API 요청은 인증 실패가 된다.

### UC-05 로그아웃

- 목적: 서버 세션을 종료한다.
- 액터: 사용자
- API: `POST /api/auth/logout`
- 기본 흐름:
  1. 사용자가 로그아웃을 실행한다.
  2. connection 모듈이 세션을 무효화한다.
- 결과: 현재 브라우저 세션의 로그인 상태가 종료된다.

### UC-06 저장소 새로고침

- 목적: 접근 가능한 원격 저장소 목록을 로컬 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/refresh`
- 기본 흐름:
  1. 사용자가 저장소 새로고침을 실행한다.
  2. application 모듈이 현재 플랫폼 연결과 token access를 확인한다.
  3. application 모듈이 platform gateway로 원격 저장소 목록을 조회한다.
  4. repository 모듈이 `repository_caches`를 upsert한다.
  5. application 모듈이 저장소 목록 동기화 성공 상태를 기록한다.
  6. 캐시 기준 저장소 목록을 반환한다.
- 결과: 저장소 목록 화면이 최신 캐시를 표시한다.

### UC-07 저장소 목록 조회

- 목적: 현재 사용자 계정의 저장소 캐시 목록을 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories`
- 기본 흐름:
  1. 프론트가 저장소 목록을 요청한다.
  2. application 모듈이 현재 플랫폼 연결의 accountLogin을 확인한다.
  3. repository 모듈이 해당 ownerKey의 저장소 캐시를 이름순으로 조회한다.
- 결과: 저장소 카드 목록을 표시한다.

### UC-08 저장소 상세 조회

- 목적: 선택된 저장소의 캐시 정보를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}`
- 기본 흐름:
  1. 사용자가 저장소를 선택한다.
  2. application 모듈이 현재 플랫폼 연결의 accountLogin을 확인한다.
  3. repository 모듈이 세션 계정과 저장소 ownerKey를 비교한다.
  4. 접근 가능한 저장소면 상세 정보를 반환한다.
- 결과: 이슈 목록 화면에서 저장소명을 표시할 수 있다.

### UC-09 저장소 동기화 상태 조회

- 목적: 저장소 단위 동기화 상태를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/sync-state`
- 기본 흐름:
  1. 프론트 또는 운영 확인 흐름이 동기화 상태를 요청한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. application의 `SyncStateService`가 상태를 조회한다.
- 결과: 마지막 성공/실패 상태를 표시할 수 있다.
- 기준: 응답 기준은 `SyncState`이며, `SyncRun` 상세 이력을 직접 반환하지 않는다.
- 비고: rate limit/recovery 도입 후 `SyncState`는 마지막으로 마감된 관련 `SyncRun`의 요약 상태를 반영한다.

### UC-10 이슈 새로고침

- 목적: 원격 이슈 목록을 로컬 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/refresh`
- 기본 흐름:
  1. 사용자가 이슈 새로고침을 실행한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. application 모듈이 platform gateway로 원격 이슈 목록을 조회한다.
  4. issue 모듈이 `issue_caches`를 upsert한다.
  5. application 모듈이 저장소 이슈 동기화 성공 상태를 기록한다.
  6. 캐시 기준 이슈 목록을 반환한다.
- 결과: 이슈 목록 화면이 최신 캐시를 표시한다.

### UC-11 이슈 목록 조회

- 목적: 선택된 저장소의 이슈 캐시를 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues`
- 기본 흐름:
  1. 프론트가 이슈 목록을 요청한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. issue 모듈이 `keyword`, `state` 조건으로 캐시 결과를 필터링한다.
  4. `numberOrKey` 역순으로 이슈 요약 목록을 반환한다.
- 결과: 이슈 목록 화면을 표시한다.

### UC-12 이슈 생성

- 목적: 원격 플랫폼에 이슈를 생성하고 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues`
- 기본 흐름:
  1. 사용자가 제목과 본문을 입력한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. application 모듈이 platform gateway로 원격 이슈 생성 API를 호출한다.
  4. issue 모듈이 생성 결과를 `issue_caches`에 upsert한다.
  5. application 모듈이 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: 생성된 이슈 상세 정보를 반환한다.

### UC-13 이슈 상세 조회

- 목적: 선택된 이슈의 캐시 상세 정보를 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 이슈 상세 화면에 진입한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. issue 모듈이 `repositoryId + numberOrKey` 기준으로 이슈 캐시를 조회한다.
- 결과: 제목, 본문, 상태, 작성자, 일시 정보를 표시한다.

### UC-14 이슈 수정

- 목적: 이슈 제목, 본문, 상태를 원격 플랫폼에 반영하고 캐시를 갱신한다.
- 액터: 사용자
- API: `PATCH /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 수정 내용을 저장한다.
  2. application 모듈이 저장소 접근을 확인하고 issue 모듈에서 현재 이슈 캐시를 조회한다.
  3. application 모듈이 요청에 없는 필드는 기존 캐시 값으로 보완한다.
  4. application 모듈이 platform gateway로 원격 이슈 수정 API를 호출한다.
  5. issue 모듈이 수정 결과를 `issue_caches`에 upsert한다.
  6. application 모듈이 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: 수정된 이슈 상세 정보를 반환한다.

### UC-15 이슈 닫기

- 목적: 이슈를 삭제 대신 닫힘 상태로 변경한다.
- 액터: 사용자
- API: `DELETE /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 이슈 닫기를 실행한다.
  2. application 모듈이 repository 모듈로 저장소 접근 권한을 확인한다.
  3. application 모듈이 platform gateway로 원격 이슈 상태를 `CLOSED`로 수정한다.
  4. application 모듈이 저장소 이슈 목록을 다시 새로고침한다.
  5. application 모듈이 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: API는 `204 No Content`를 반환하고 캐시는 닫힘 상태를 반영한다.

### UC-16 이슈 동기화 상태 조회

- 목적: 이슈 단위 동기화 상태를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/sync-state`
- 기본 흐름:
  1. 프론트 또는 운영 확인 흐름이 동기화 상태를 요청한다.
  2. application 모듈이 repository / issue 모듈로 접근 가능 여부를 확인한다.
  3. application의 `SyncStateService`가 상태를 조회한다.
- 결과: 마지막 이슈 생성/수정/닫기 동기화 상태를 확인할 수 있다.
- 기준: 응답 기준은 `SyncState`이며, `SyncRun` 상세 이력을 직접 반환하지 않는다.
- 비고: rate limit/recovery 도입 후 `SyncState`는 마지막으로 마감된 관련 `SyncRun`의 요약 상태를 반영한다.

### UC-17 댓글 새로고침

- 목적: 원격 댓글 목록을 로컬 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments/refresh`
- 기본 흐름:
  1. 사용자가 댓글 새로고침을 실행한다.
  2. application 모듈이 repository / issue 모듈로 접근 권한을 확인한다.
  3. application 모듈이 platform gateway로 원격 댓글 목록을 조회한다.
  4. comment 모듈이 `comment_caches`를 upsert한다.
  5. application 모듈이 댓글 목록 동기화 성공 상태를 기록한다.
- 결과: 댓글 목록 화면이 최신 캐시를 표시한다.

### UC-18 댓글 목록 조회

- 목적: 선택된 이슈의 댓글 캐시 목록을 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`
- 기본 흐름:
  1. 프론트가 댓글 목록을 요청한다.
  2. application 모듈이 issue 모듈로 이슈 접근 가능 여부를 확인한다.
  3. comment 모듈이 `issueExternalId` 기준 댓글 캐시를 작성일 오름차순으로 조회한다.
- 결과: 이슈 상세 화면에 댓글 목록을 표시한다.

### UC-19 댓글 작성

- 목적: 원격 플랫폼에 댓글을 작성하고 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`
- 기본 흐름:
  1. 사용자가 댓글 본문을 입력한다.
  2. application 모듈이 repository / issue 모듈로 접근 권한을 확인한다.
  3. application 모듈이 platform gateway로 원격 댓글 생성 API를 호출한다.
  4. comment 모듈이 생성 결과를 `comment_caches`에 저장한다.
  5. application 모듈이 댓글 목록 동기화 성공 상태를 기록한다.
- 결과: 생성된 댓글 정보를 반환한다.

### UC-20 GitHub Rate Limit 상태 수집

- 목적: GitHub API 호출 제한 상태를 수집해 동기화 안정성 판단에 사용한다.
- 액터: 시스템
- API: 내부 platform gateway 호출 흐름
- 기본 흐름:
  1. application 모듈이 GitHub 대상 platform gateway를 호출한다.
  2. platform 모듈이 GitHub 응답 헤더에서 rate limit 값을 추출한다.
  3. platform 모듈이 GitHub 헤더를 `RateLimitSnapshot`으로 변환한다.
  4. application 모듈이 최근 rate limit 상태를 저장한다.
  5. 남은 호출 수가 임계값 이하이면 이후 동기화 실행에 주의 상태를 반영한다.
- 결과: 운영자는 GitHub API 호출 여유와 초기화 시각을 확인할 수 있다.
- 비고: 1차 구현 대상은 GitHub만 포함한다.

### UC-21 Rate Limit 동기화 중단 기록

- 목적: rate limit 발생 시 동기화를 무한 재시도하지 않고 복구 가능한 실패로 기록한다.
- 액터: 시스템
- API: 저장소/이슈/댓글 refresh 또는 resync 흐름
- 기본 흐름:
  1. GitHub API 호출 중 rate limit 응답이 발생한다.
  2. platform 모듈이 실패를 `PlatformApiFailure`로 변환한다.
  3. application 모듈이 현재 `SyncRun`을 `RATE_LIMITED` 또는 `PARTIAL_SUCCESS`로 마감한다.
  4. application 모듈이 `SyncFailure.retryable=true`로 실패 단위를 저장한다.
  5. application 모듈이 `resetAt` 또는 `Retry-After` 기준으로 `nextRetryAt`을 기록한다.
  6. 사용자 요청에서 발생한 실패라면 실패 응답에 `syncRunId`, `failureId`, `retryable`, `nextRetryAt`, `message`를 포함한다.
- 결과: 호출 제한 해제 이후 수동 재처리할 수 있는 실패 이력이 남고, 사용자는 실패 응답에서 재처리 가능 여부를 확인한다.
- 비고: 1차 구현에서는 자동 재처리와 별도 푸시/메일 알림을 수행하지 않는다.
- 비고: 사용자 안내는 실패 응답과 실패 이력 조회 화면으로 제공한다.

### UC-22 동기화 실패 이력 조회

- 목적: 운영자가 실패한 동기화와 재처리 가능 여부를 확인한다.
- 액터: 사용자
- API: `GET /api/sync-failures`
- 기본 흐름:
  1. 사용자가 실패 이력 조회를 요청한다.
  2. app 계층이 조회 조건을 application 모듈에 전달한다.
  3. application 모듈이 `SyncFailure` 목록을 조회한다.
  4. application 모듈이 `failureId`, `syncRunId`, 실패 유형, 재처리 가능 여부, 재처리 가능 시각을 응답한다.
- 결과: 운영자는 rate limit 또는 외부 장애로 멈춘 동기화 대상을 확인할 수 있다.
- 비고: 실패 직후 응답으로 안내받은 사용자가 상세 확인을 위해 진입하거나, 나중에 복구 대상을 찾을 때 사용한다.

### UC-23 실패한 동기화 수동 재처리

- 목적: 운영자가 재처리 가능한 실패를 선택해 다시 실행한다.
- 액터: 사용자
- API: `POST /api/sync-failures/{failureId}/retry`
- 기본 흐름:
  1. 사용자가 특정 실패 건의 재처리를 요청한다.
  2. application 모듈이 `SyncFailure`를 조회한다.
  3. `retryable=false`이면 재처리를 거부한다.
  4. `nextRetryAt`이 현재 시각보다 이후이면 재처리를 보류한다.
  5. application 모듈이 새 `SyncRun`을 생성한다.
  6. 저장된 `resourceType`, `resourceKey`, `operation` 기준으로 동기화를 다시 실행한다.
  7. 성공하면 기존 실패에 `resolvedAt`을 기록한다.
  8. 다시 실패하면 `retryCount`와 `nextRetryAt`을 갱신한다.
- 결과: 자동 작업 없이도 운영자가 실패한 동기화를 복구할 수 있다.
- 비고: 1차 구현은 수동 재처리만 제공한다.

### UC-24 저장소 단위 수동 재동기화

- 목적: 장애 또는 rate limit 이후 특정 저장소의 캐시를 원격 상태와 다시 맞춘다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/resync`
- 기본 흐름:
  1. 사용자가 저장소 재동기화를 요청한다.
  2. application 모듈이 현재 플랫폼 연결과 저장소 접근 권한을 확인한다.
  3. application 모듈이 새 `SyncRun`을 생성한다.
  4. 요청 scope에 따라 저장소, 이슈, 댓글 동기화를 실행한다.
  5. repository / issue / comment 모듈이 원격 결과를 cache에 반영한다.
  6. application 모듈이 처리 건수와 실패 건수를 `SyncRun`에 기록한다.
  7. 실패가 발생하면 `SyncFailure`로 저장한다.
- 결과: 누락 가능성이 있는 저장소 데이터를 운영자가 직접 보정할 수 있다.

### UC-25 이슈 단위 수동 재동기화

- 목적: 특정 이슈와 댓글만 좁은 범위로 복구한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/resync`
- 기본 흐름:
  1. 사용자가 이슈 재동기화를 요청한다.
  2. application 모듈이 저장소와 이슈 접근 가능 여부를 확인한다.
  3. application 모듈이 새 `SyncRun`을 생성한다.
  4. application 모듈이 platform gateway로 원격 이슈 단건을 조회한다.
  5. issue 모듈이 `issue_caches`를 갱신한다.
  6. `includeComments=true`이면 comment 모듈이 댓글 cache도 갱신한다.
  7. application 모듈이 결과를 `SyncRun`에 기록한다.
- 결과: 전체 저장소 재동기화 없이 특정 이슈 불일치를 복구할 수 있다.

### UC-26 자동 재처리 예약 TODO

- 목적: 1차 수동 재처리 이후 scheduler 기반 자동 복구로 확장한다.
- 액터: 시스템
- API: 내부 scheduler 흐름
- 기본 흐름:
  1. scheduler가 `retryable=true`, `resolvedAt=null`, `nextRetryAt<=now` 실패를 조회한다.
  2. 실패 건별로 새 `SyncRun`을 생성한다.
  3. 수동 재처리와 같은 application use case로 동기화를 다시 실행한다.
  4. 성공하면 기존 실패에 `resolvedAt`을 기록한다.
  5. 실패하면 `retryCount`와 `nextRetryAt`을 갱신한다.
- 결과: 일시적 장애와 rate limit 실패를 운영자 개입 없이 순차 복구한다.
- 비고: 1차 구현에서는 TODO로 남긴다.
