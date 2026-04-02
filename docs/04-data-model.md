# Data Model

## 1. 개요

현재 데이터 모델은 PAT 기반 GitHub 연동 구조를 기준으로 한다.

- GitHub 저장소, 이슈, 댓글은 원본 데이터다.
- 내부 DB는 사용자 정보, PAT 연결 정보, 캐시, 동기화 상태를 저장한다.

## 2. 핵심 테이블

### `users`

- 내부 사용자 정보

주요 필드

- `id`
- `display_name`
- `email`
- `created_at`
- `updated_at`

### `github_accounts`

- GitHub 계정과 PAT 연결 정보

주요 필드

- `id`
- `user_id`
- `github_user_id`
- `login`
- `avatar_url`
- `access_token_encrypted`
- `token_scopes`
- `token_verified_at`
- `connected_at`
- `last_authenticated_at`

### `repository_caches`

- GitHub 저장소 캐시

주요 필드

- `id`
- `github_repository_id`
- `owner_login`
- `name`
- `full_name`
- `description`
- `private`
- `html_url`
- `default_branch`
- `last_pushed_at`
- `last_synced_at`

### `issue_caches`

- GitHub 이슈 캐시

주요 필드

- `id`
- `github_issue_id`
- `github_repository_id`
- `number`
- `title`
- `body`
- `state`
- `author_login`
- `created_at`
- `updated_at`
- `closed_at`
- `last_synced_at`

### `comment_caches`

- GitHub 댓글 캐시

주요 필드

- `id`
- `github_comment_id`
- `github_issue_id`
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

- `users` 1:1 `github_accounts`
- `repository_caches` 1:N `issue_caches`
- `issue_caches` 1:N `comment_caches`

## 4. 현재 제외된 모델

현재 구현 범위에서는 아래 구조를 사용하지 않는다.

- `github_installations`
- 라벨 관련 테이블
- 담당자 관련 테이블
- 우선순위 관련 테이블
- sub-issue 부모/자식 관계

## 5. 설계 원칙

- GitHub가 진실 원천이다.
- 캐시는 조회 성능과 단순한 UI 흐름을 위한 보조 저장소다.
- GitHub에 없는 기능은 추후 별도 테이블로 확장할 수 있다.
