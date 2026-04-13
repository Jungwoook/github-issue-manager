# GitHub Issue Manager

GitHub 저장소와 이슈를 더 간단하게 조회하고 관리하기 위한 애플리케이션이다.

현재 서비스는 GitHub 저장소, GitHub 이슈, GitHub 댓글을 주요 데이터로 사용하며, 사용자가 직접 등록한 GitHub PAT를 통해 데이터를 조회한다.

## 현재 구현 기능

- GitHub PAT 등록, 연결 상태 조회, 연결 해제
- GitHub 저장소 목록 조회, 저장소 상세 조회, 저장소 새로고침
- GitHub 이슈 목록 조회, 이슈 상세 조회, 이슈 새로고침
- GitHub 이슈 생성, 수정, 닫기
- GitHub 댓글 목록 조회, 댓글 작성, 댓글 새로고침

## 현재 제외 기능

- 라벨 관리
- 담당자 선택
- 우선순위 관리
- Sub-issue
- GitHub App 기반 인증/설치 구조

## 프로젝트 구조

- `backend/`: Spring Boot 백엔드
- `frontend/`: React + Vite 프론트엔드
- `docs/`: 요구사항, 구조, 운영, 작업 기록 문서

## 주요 문서

- [PRD](./docs/01-prd.md)
- [Architecture](./docs/02-architecture.md)
- [API Specification](./docs/03-api-spec.md)
- [Data Model](./docs/04-data-model.md)
- [Implementation Summary](./docs/05-implementation-summary.md)
- [Core Use Cases](./docs/06-core-use-cases.md)
- [PAT Management Flow](./docs/07-pat-management-flow.md)
- [Main Use Case Flow](./docs/08-main-use-case-flow.md)
- [Prod Runtime Config](./docs/09-prod-runtime-config.md)
- [Platform Abstraction Interface Draft](./docs/10-platform-abstraction-interface-draft.md)
- [Task Notes](./docs/task)
