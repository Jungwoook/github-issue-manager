# Architecture

## 1. 개요

현재 시스템은 PAT 기반 GitHub 연동 구조를 사용한다.

- GitHub 저장소, 이슈, 댓글이 원본 데이터다.
- 백엔드는 GitHub API를 호출하고 결과를 캐시에 저장한다.
- 프론트는 백엔드 API를 통해 캐시 데이터를 조회하고, 필요 시 수동 새로고침을 요청한다.
- 내부 DB는 사용자, PAT 연결 정보, 캐시, 동기화 상태를 저장한다.

## 2. 구성 요소

### 프론트엔드

- React + Vite
- 저장소 목록, 이슈 목록, 이슈 상세, PAT 설정 화면 제공

### 백엔드

- Spring Boot
- 세션 기반 현재 사용자 식별
- PAT를 사용한 GitHub REST API 호출
- 캐시 및 동기화 상태 관리

### GitHub

- `/user`
- `/user/repos`
- `/repos/{owner}/{repo}/issues`
- `/repos/{owner}/{repo}/issues/{issueNumber}`
- `/repos/{owner}/{repo}/issues/{issueNumber}/comments`

## 3. 백엔드 계층

- `controller`: HTTP 엔드포인트 제공
- `service`: 비즈니스 로직, 캐시 갱신, GitHub 연동 처리
- `repository`: JPA 기반 데이터 접근
- `domain`: 사용자, 계정, 캐시, 동기화 상태 엔티티
- `github`: GitHub API 클라이언트
- `exception`: 예외 처리 및 공통 오류 응답

## 4. 주요 처리 흐름

### PAT 등록

1. 사용자가 PAT를 입력한다.
2. 백엔드는 GitHub `/user`를 호출해 토큰을 검증한다.
3. 사용자와 GitHub 계정 정보를 저장한다.
4. 세션에 현재 사용자 정보를 기록한다.

### 저장소 조회

1. 프론트가 `/api/repositories`를 호출한다.
2. 백엔드는 현재 세션 사용자 기준 저장소 캐시를 조회한다.
3. 프론트는 캐시된 저장소 목록을 표시한다.

### 저장소 새로고침

1. 프론트가 `/api/repositories/refresh`를 호출한다.
2. 백엔드는 PAT로 GitHub 저장소 목록을 조회한다.
3. 결과를 `repository_caches`에 반영한다.

### 이슈 조회 및 새로고침

1. 프론트가 `/api/repositories/{repositoryId}/issues`를 호출한다.
2. 백엔드는 이슈 캐시를 조회한다.
3. 필요 시 `/issues/refresh`로 GitHub 이슈 목록을 다시 동기화한다.

### 댓글 조회 및 새로고침

1. 프론트가 `/comments`를 호출한다.
2. 백엔드는 댓글 캐시를 조회한다.
3. 필요 시 `/comments/refresh`로 GitHub 댓글 목록을 다시 동기화한다.

## 5. 현재 제약

- 사용자 수는 사실상 1명 기준이다.
- 저장소 접근은 PAT에 포함된 범위에 의존한다.
- 라벨, 담당자, 우선순위, sub-issue는 현재 구조에서 제외한다.
