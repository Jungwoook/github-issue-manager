# GitHub Issue Manager

GitHub Issue Manager는 사용자가 자신의 GitHub 저장소와 이슈를 간단한 흐름으로 조회하고 관리할 수 있도록 만든 애플리케이션입니다.

현재 서비스는 GitHub 저장소, GitHub 이슈, GitHub 댓글을 주요 데이터로 사용합니다. 사용자가 직접 등록한 GitHub PAT를 통해 데이터를 조회하고, 애플리케이션 내부 DB에는 사용자 정보, PAT 연결 정보, 캐시, 동기화 상태를 저장하도록 구성했습니다.

## 현재 구현된 기능

- GitHub PAT 등록, 연결 상태 조회, 연결 해제
- GitHub 저장소 목록 조회, 저장소 상세 조회, 저장소 동기화
- GitHub 이슈 목록 조회, 이슈 상세 조회, 이슈 동기화
- GitHub 이슈 생성, 수정, 닫기
- GitHub 이슈 제목/상태 기준 필터링
- GitHub 댓글 목록 조회, 댓글 작성, 댓글 동기화

## 현재 제외된 기능

- 릴리스 관리
- 담당자 선택
- 우선순위 관리
- Sub-issue
- GitHub App 기반 인증/설치 구조

## 프로젝트 구조

- `backend/`: Spring Boot 백엔드
- `frontend/`: React + Vite 프론트엔드
- Frontend URL: [https://github-issue-manager-beta.vercel.app/](https://github-issue-manager-beta.vercel.app/)
- `docs/`: 요구사항, 아키텍처, API, 데이터 모델, 유스케이스 문서

## 주요 문서

- [PRD](./docs/01-prd.md)
- [Architecture](./docs/02-architecture.md)
- [API Specification](./docs/03-api-spec.md)
- [Data Model](./docs/04-data-model.md)
- [Implementation Summary](./docs/05-implementation-summary.md)
- [Core Use Cases](./docs/06-core-use-cases.md)
- [Current Improvement Checklist](./docs/07-current-improvement-checklist.md)
- [ngrok HTTPS Setup](./docs/08-ngrok-https-setup.md)
- [Main Use Case Flow](./docs/11-main-use-case-flow.md)
