# Data Model

## 1. 개요

현재 데이터 모델은 플랫폼 공통 구조를 기준으로 한다.

- 외부 플랫폼 저장소, 이슈, 댓글은 원본 데이터다.
- 내부 DB는 사용자 정보, 플랫폼 연결 정보, 캐시, 동기화 상태를 저장한다.
- 외부 리소스는 `platform + externalId` 기준으로 식별한다.
- GitHub 전용 ID 이름은 캐시 모델 밖으로 노출하지 않는다.

## 2. 핵심 테이블

### `users`

- 내부 사용자 정보
- connection 모듈 소유

주요 필드

- `id`
- `display_name`
- `email`
- `created_at`
- `updated_at`

### `platform_connections`

- 플랫폼 계정과 PAT 연결 정보
- connection 모듈 소유

주요 필드

- `id`
- `user_id`
- `platform`
- `external_user_id`
- `account_login`
- `avatar_url`
- `access_token_encrypted`
- `token_scopes`
- `base_url`
- `token_verified_at`
- `connected_at`
- `last_authenticated_at`

### `repository_caches`

- 외부 저장소/프로젝트 캐시
- repository 모듈 소유

주요 필드

- `id`
- `platform`
- `external_id`
- `owner_key`
- `name`
- `full_name`
- `description`
- `is_private`
- `web_url`
- `default_branch`
- `last_pushed_at`
- `last_synced_at`

### `issue_caches`

- 외부 이슈 캐시
- issue 모듈 소유

주요 필드

- `id`
- `platform`
- `external_id`
- `repository_external_id`
- `number_or_key`
- `title`
- `body`
- `state`
- `author_login`
- `created_at`
- `updated_at`
- `closed_at`
- `last_synced_at`

주요 인덱스

- `platform, repository_external_id`
- `platform, repository_external_id, number_or_key`

### `comment_caches`

- 외부 댓글 캐시
- comment 모듈 소유

주요 필드

- `id`
- `platform`
- `external_id`
- `issue_external_id`
- `author_login`
- `body`
- `created_at`
- `updated_at`
- `last_synced_at`

주요 인덱스

- `platform, issue_external_id`

### `sync_states`

- 마지막 동기화 상태
- shared-kernel 모듈 소유

주요 필드

- `id`
- `resource_type`
- `resource_key`
- `last_synced_at`
- `last_sync_status`
- `last_sync_message`

## 3. 관계

- `users` 1:N `platform_connections`
- `repository_caches`는 `platform + external_id`로 외부 저장소를 식별
- `issue_caches`는 `platform + repository_external_id + number_or_key`로 저장소 내부 이슈를 식별
- `comment_caches`는 `platform + issue_external_id`로 상위 이슈를 연결
- 모듈 간 직접 entity 참조 대신 public API result와 외부 식별자를 사용한다.

## 4. 현재 제외된 모델

현재 구현 범위에서는 아래 구조를 사용하지 않는다.

- OAuth / GitHub App installation
- 라벨 관련 테이블
- 담당자 관련 테이블
- 우선순위 관련 테이블
- 마일스톤 관련 테이블
- sub-issue 부모/자식 관계

## 5. 설계 원칙

- 외부 플랫폼이 진실 원천이다.
- 내부 캐시는 조회 성능과 단순한 UI 흐름을 위한 보조 저장소다.
- 플랫폼별 ID 차이는 `platform`, `external_id`, `number_or_key`로 흡수한다.
- credential 저장 방식은 connection 모듈 내부에 둔다.
- 원격 API 응답은 platform 모듈에서 플랫폼 중립 모델로 변환한다.
