# GitHub Issue Manager

GitHub Issue Manager는 사용자가 자신의 GitHub 저장소와 이슈를 더 간단한 흐름으로 조회하고 관리할 수 있도록 만든 웹 애플리케이션입니다.

현재 서비스는 GitHub 저장소, GitHub 이슈, GitHub 댓글을 원본 데이터로 사용합니다. 사용자가 직접 등록한 GitHub PAT를 통해 데이터를 조회하며, 애플리케이션 내부 DB는 사용자 정보, PAT 연결 정보, 캐시, 동기화 상태를 저장하는 용도로 사용합니다.

## 현재 구현된 기능

- GitHub PAT 등록, 연결 상태 조회, 연결 해제
- GitHub 저장소 목록 조회 및 저장소 새로고침
- GitHub 이슈 목록 조회, 이슈 새로고침, 검색 및 상태 필터링
- GitHub 이슈 생성, 상세 조회, 수정, 닫기
- GitHub 댓글 조회, 댓글 작성, 댓글 새로고침

## 현재 제외된 기능

- 라벨 관리
- 담당자 선택
- 우선순위 관리
- sub-issue
- GitHub App 기반 인증/설치 구조

## 프로젝트 구조

- `backend/`: Spring Boot 백엔드
- `frontend/`: React + Vite 프론트엔드
- `docs/`: 요구사항, 아키텍처, API, 데이터 모델, 유스케이스 문서

## 주요 문서

- [PRD](./docs/01-prd.md)
- [Architecture](./docs/02-architecture.md)
- [API Specification](./docs/03-api-spec.md)
- [Data Model](./docs/04-data-model.md)
- [Implementation Summary](./docs/05-implementation-summary.md)
- [Core Use Cases](./docs/07-core-use-cases.md)
- [Current Improvement Checklist](./docs/14-current-improvement-checklist.md)
