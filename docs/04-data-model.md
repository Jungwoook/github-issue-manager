# Data Model

## 1. 개요

현재 데이터 모델은 플랫폼 공통 구조를 기준으로 한다.

- `platform + externalId` 조합을 외부 리소스 식별 기준으로 사용한다.
- GitHub와 GitLab 원본 데이터는 캐시에 저장한다.
- PAT는 플랫폼 연결 엔티티에 암호화해 저장한다.

## 2. 핵심 테이블

### `users`

- 내부 사용자 정보

주요 필드

- `id`
- `display_name`
- `email`
- `created_at`
- `updated_at`

### `platform_connections`

- 플랫폼 계정과 PAT 연결 정보

주요 필드

- `id`
- `user_id`
- `platform`
- `external_user_id`
- `account_login`
- `avatar_url`
- `access_token_encrypted`
- `base_url`
- `token_verified_at`
- `connected_at`
- `last_authenticated_at`

현재 제약

- `external_user_id`, `account_login`은 단일 unique 기준이다.
- self-managed GitLab까지 고려하면 `platform + baseUrl + externalUserId` 형태의 복합 기준 보강이 필요하다.

### `repository_caches`

- 플랫폼 저장소/프로젝트 캐시

주요 필드

- `id`
- `platform`
- `external_id`
- `owner_key`
- `name`
- `full_name`
- `description`
- `private`
- `web_url`
- `last_synced_at`

### `issue_caches`

- 플랫폼 이슈 캐시

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

### `comment_caches`

- 플랫폼 댓글 캐시

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

### `sync_states`

- 마지막 동기화 상태

주요 필드

- `id`
- `resource_type`
- `resource_key`
- `last_synced_at`
- `last_sync_status`
- `last_sync_message`

## 3. 관계

- `users` 1:N `platform_connections`
- `repository_caches` 1:N `issue_caches`
- `issue_caches` 1:N `comment_caches`
- 캐시 관계는 내부 FK보다 `platform + externalId` 기준 조회를 우선한다.

## 4. 현재 제외된 모델

- label 캐시/연결 테이블
- assignee 관리 테이블
- priority 관리 테이블
- milestone 관리 테이블
- sub-issue 부모/자식 관계
- GitHub App installation 모델
- GitLab merge request 모델

## 5. 설계 원칙

- 외부 플랫폼이 원본 데이터이다.
- 내부 DB는 조회 성능과 UI 흐름을 위한 보조 저장소이다.
- 플랫폼별 차이는 gateway와 mapper에서 흡수한다.
- 서비스와 API 응답은 공통 DTO 이름을 사용한다.
