# GitHub Issue Manager Backend

이 백엔드는 GitHub App 기반으로
사용자별 GitHub 저장소와 GitHub 이슈를 조회, 캐시, 동기화하는 구조를 목표로 한다.

현재 코드베이스에는 내부 CRUD 중심 프로토타입 구현이 포함되어 있지만,
문서 기준 목표 구조는 GitHub 원본 데이터 + 캐시 + 부가 기능 저장 방식이다.

## 문서

- [PRD](../docs/01-prd.md)
- [Architecture](../docs/02-architecture.md)
- [API Specification](../docs/03-api-spec.md)
- [Data Model](../docs/04-data-model.md)
- [Implementation Summary](../docs/05-implementation-summary.md)
- [Core Use Cases](../docs/07-core-use-cases.md)
- [Domain Model Draft](../docs/08-domain-model-draft.md)
- [Data Model Draft](../docs/09-data-model-draft.md)
- [GitHub App Implementation Direction](../docs/10-github-app-implementation-direction.md)

## 목표 최소 구현 범위

- `users`
- `github_accounts`
- `repository_caches`
- `issue_caches`
- `comment_caches`
- `sync_states`

## 현재 로컬 실행

```powershell
./gradlew :app:bootRun
```

## 현재 테스트 실행

```powershell
./gradlew test
```
