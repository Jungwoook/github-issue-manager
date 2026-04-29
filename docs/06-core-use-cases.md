# Core Use Cases

## 1. 개요

현재 구현은 플랫폼 공통 API를 기준으로 동작한다.

- 기본 플랫폼은 `github`이다.
- API 경로는 `/api/platforms/{platform}/...` 형식을 사용한다.
- 인증은 서버 세션과 등록된 플랫폼 PAT를 기준으로 한다.
- 원격 호출은 `PlatformRemoteFacade` / `PlatformCredentialFacade` 뒤의 플랫폼 gateway가 담당한다.
- 조회 API는 로컬 캐시를 우선 읽고, `refresh` API가 원격 플랫폼과 캐시를 동기화한다.

## 2. 액터

- 방문자: 플랫폼 토큰을 아직 등록하지 않은 사용자
- 사용자: 플랫폼 토큰을 등록하고 세션이 연결된 사용자
- 플랫폼 API: GitHub 또는 GitLab API
- 로컬 캐시: 저장소, 이슈, 댓글, 동기화 상태 저장소

## 3. 구현 기준 수정 사항

- 기존 `/api/github/token` 경로는 현재 `/api/platforms/{platform}/token`으로 변경됨
- 기존 `/api/repositories...` 경로는 현재 `/api/platforms/{platform}/repositories...`로 변경됨
- `githubRepositoryId`, `githubIssueId` 중심 표현은 `platform + repositoryId/issueId/numberOrKey` 표현으로 정리함
- 인증 검증은 컨트롤러에서 `PlatformCredentialFacade`가 수행하고, 저장은 connection 모듈이 담당함
- 저장소/이슈/댓글 기능은 각 모듈 facade를 통해 접근함
- 삭제 API는 실제 삭제가 아니라 이슈 상태를 `CLOSED`로 변경하는 닫기 처리임
- 라벨 API는 프론트 호출 코드가 남아 있지만 현재 백엔드 컨트롤러 구현 범위에는 포함하지 않음

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
  2. repository 모듈이 현재 플랫폼 연결을 확인한다.
  3. platform 모듈이 원격 저장소 목록을 조회한다.
  4. repository 모듈이 `repository_caches`를 upsert한다.
  5. 저장소 목록 동기화 성공 상태를 기록한다.
  6. 캐시 기준 저장소 목록을 반환한다.
- 결과: 저장소 목록 화면이 최신 캐시를 표시한다.

### UC-07 저장소 목록 조회

- 목적: 현재 사용자 계정의 저장소 캐시 목록을 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories`
- 기본 흐름:
  1. 프론트가 저장소 목록을 요청한다.
  2. repository 모듈이 현재 플랫폼 연결의 accountLogin을 확인한다.
  3. 해당 ownerKey의 저장소 캐시를 이름순으로 조회한다.
- 결과: 저장소 카드 목록을 표시한다.

### UC-08 저장소 상세 조회

- 목적: 선택된 저장소의 캐시 정보를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}`
- 기본 흐름:
  1. 사용자가 저장소를 선택한다.
  2. repository 모듈이 세션 계정과 저장소 ownerKey를 비교한다.
  3. 접근 가능한 저장소면 상세 정보를 반환한다.
- 결과: 이슈 목록 화면에서 저장소명을 표시할 수 있다.

### UC-09 저장소 동기화 상태 조회

- 목적: 저장소 단위 동기화 상태를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/sync-state`
- 기본 흐름:
  1. 프론트 또는 운영 확인 흐름이 동기화 상태를 요청한다.
  2. repository 모듈이 저장소 접근 권한을 확인한다.
  3. shared-kernel의 `SyncStateService`가 상태를 조회한다.
- 결과: 마지막 성공/실패 상태를 표시할 수 있다.

### UC-10 이슈 새로고침

- 목적: 원격 이슈 목록을 로컬 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/refresh`
- 기본 흐름:
  1. 사용자가 이슈 새로고침을 실행한다.
  2. issue 모듈이 저장소 접근 권한을 확인한다.
  3. platform 모듈이 원격 이슈 목록을 조회한다.
  4. issue 모듈이 `issue_caches`를 upsert한다.
  5. 저장소 이슈 동기화 성공 상태를 기록한다.
  6. 캐시 기준 이슈 목록을 반환한다.
- 결과: 이슈 목록 화면이 최신 캐시를 표시한다.

### UC-11 이슈 목록 조회

- 목적: 선택된 저장소의 이슈 캐시를 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues`
- 기본 흐름:
  1. 프론트가 이슈 목록을 요청한다.
  2. issue 모듈이 저장소 접근 권한을 확인한다.
  3. `keyword`, `state` 조건이 있으면 캐시 결과를 필터링한다.
  4. `numberOrKey` 역순으로 이슈 요약 목록을 반환한다.
- 결과: 이슈 목록 화면을 표시한다.

### UC-12 이슈 생성

- 목적: 원격 플랫폼에 이슈를 생성하고 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues`
- 기본 흐름:
  1. 사용자가 제목과 본문을 입력한다.
  2. issue 모듈이 저장소 접근 권한을 확인한다.
  3. platform 모듈이 원격 이슈 생성 API를 호출한다.
  4. issue 모듈이 생성 결과를 `issue_caches`에 upsert한다.
  5. 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: 생성된 이슈 상세 정보를 반환한다.

### UC-13 이슈 상세 조회

- 목적: 선택된 이슈의 캐시 상세 정보를 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 이슈 상세 화면에 진입한다.
  2. issue 모듈이 저장소 접근 권한을 확인한다.
  3. `repositoryId + numberOrKey` 기준으로 이슈 캐시를 조회한다.
- 결과: 제목, 본문, 상태, 작성자, 일시 정보를 표시한다.

### UC-14 이슈 수정

- 목적: 이슈 제목, 본문, 상태를 원격 플랫폼에 반영하고 캐시를 갱신한다.
- 액터: 사용자
- API: `PATCH /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 수정 내용을 저장한다.
  2. issue 모듈이 저장소와 이슈 캐시를 확인한다.
  3. 요청에 없는 필드는 기존 캐시 값을 사용한다.
  4. platform 모듈이 원격 이슈 수정 API를 호출한다.
  5. issue 모듈이 수정 결과를 `issue_caches`에 upsert한다.
  6. 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: 수정된 이슈 상세 정보를 반환한다.

### UC-15 이슈 닫기

- 목적: 이슈를 삭제 대신 닫힘 상태로 변경한다.
- 액터: 사용자
- API: `DELETE /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}`
- 기본 흐름:
  1. 사용자가 이슈 닫기를 실행한다.
  2. issue 모듈이 저장소 접근 권한을 확인한다.
  3. platform 모듈이 원격 이슈 상태를 `CLOSED`로 수정한다.
  4. issue 모듈이 저장소 이슈 목록을 다시 새로고침한다.
  5. 이슈 단위 동기화 성공 상태를 기록한다.
- 결과: API는 `204 No Content`를 반환하고 캐시는 닫힘 상태를 반영한다.

### UC-16 이슈 동기화 상태 조회

- 목적: 이슈 단위 동기화 상태를 확인한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/sync-state`
- 기본 흐름:
  1. 프론트 또는 운영 확인 흐름이 동기화 상태를 요청한다.
  2. issue 모듈이 이슈 접근 가능 여부를 확인한다.
  3. shared-kernel의 `SyncStateService`가 상태를 조회한다.
- 결과: 마지막 이슈 생성/수정/닫기 동기화 상태를 확인할 수 있다.

### UC-17 댓글 새로고침

- 목적: 원격 댓글 목록을 로컬 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments/refresh`
- 기본 흐름:
  1. 사용자가 댓글 새로고침을 실행한다.
  2. comment 모듈이 저장소와 이슈 접근 권한을 확인한다.
  3. platform 모듈이 원격 댓글 목록을 조회한다.
  4. comment 모듈이 `comment_caches`를 upsert한다.
  5. 댓글 목록 동기화 성공 상태를 기록한다.
- 결과: 댓글 목록 화면이 최신 캐시를 표시한다.

### UC-18 댓글 목록 조회

- 목적: 선택된 이슈의 댓글 캐시 목록을 조회한다.
- 액터: 사용자
- API: `GET /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`
- 기본 흐름:
  1. 프론트가 댓글 목록을 요청한다.
  2. comment 모듈이 이슈 접근 가능 여부를 확인한다.
  3. `issueExternalId` 기준 댓글 캐시를 작성일 오름차순으로 조회한다.
- 결과: 이슈 상세 화면에 댓글 목록을 표시한다.

### UC-19 댓글 작성

- 목적: 원격 플랫폼에 댓글을 작성하고 캐시에 반영한다.
- 액터: 사용자
- API: `POST /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}/comments`
- 기본 흐름:
  1. 사용자가 댓글 본문을 입력한다.
  2. comment 모듈이 저장소와 이슈 접근 권한을 확인한다.
  3. platform 모듈이 원격 댓글 생성 API를 호출한다.
  4. comment 모듈이 생성 결과를 `comment_caches`에 저장한다.
  5. 댓글 목록 동기화 성공 상태를 기록한다.
- 결과: 생성된 댓글 정보를 반환한다.
