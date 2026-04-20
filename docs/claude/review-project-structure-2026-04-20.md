# 프로젝트 구조 적절성 리뷰
날짜: 2026-04-20  
참조: docs/01-prd.md, docs/02-architecture.md, docs/10-platform-abstraction-interface-draft.md

---

## 1. [구조 비일관성] `config/GitHubClientConfig` — 플랫폼 어댑터 분리 원칙 위반

**파일**: `config/GitHubClientConfig.java`

**문제**  
doc 02는 플랫폼 어댑터 계층을 `github/`, `gitlab/`으로 분리하도록 정의한다.  
- GitLab 설정(`GitLabIntegrationProperties`)은 `gitlab/` 패키지에 위치  
- GitHub WebClient 설정(`GitHubClientConfig`)은 공통 `config/` 패키지에 위치

GitHub 전용 WebClient 빈이 공통 config에 노출되어 있어 새 플랫폼 추가 시 `config/`에 플랫폼별 설정이 누적된다. 어댑터 분리 원칙을 일관성 있게 적용하려면 `GitHubClientConfig`는 `github/` 패키지에 있어야 한다.

---

## 2. [인프라 누락] DB 마이그레이션 전략 없음

**관련**: PRD 4.1, architecture 전반

**문제**  
PRD는 플랫폼 연결 정보, 암호화된 PAT, 캐시, 동기화 상태의 DB 저장을 명시한다.  
현재 프로젝트에 Flyway/Liquibase 등 DB 마이그레이션 도구가 없다.

`ddl-auto: create` 또는 `update` 환경에서는 스키마가 자동 반영되지만, 스테이징·운영에서 `validate` 또는 `none`으로 전환 시 스키마 변경 이력 추적이 불가하다.  
이전 리뷰(2026-04-18)에서 `base_url` 컬럼 추가에 마이그레이션 스크립트가 없어 동일 위험이 이미 확인된 상태다. 반복될 구조적 문제다.

---

## 3. [계약 불일치] `PlatformConnection` unique constraint — self-managed GitLab 충돌 가능

**파일**: `domain/PlatformConnection.java`  
**참조**: doc 10 section 9.2

**문제**  
현재 `externalUserId` 단일 unique constraint는 GitLab 사용자 ID를 플랫폼 전체에서 유일하다고 가정한다.  
self-managed GitLab 인스턴스와 gitlab.com에서 동일한 `externalUserId`를 가진 사용자가 각각 PAT를 등록하면 unique constraint 위반이 발생한다.

PRD 2.2는 GitLab `baseUrl` 저장을 명시하고 doc 10은 `platform + baseUrl + externalUserId` 기준이 필요하다고 지적했으나, 실제 엔티티 제약에 반영되지 않았다.

---

## 4. [런타임 실패] `ownerKey == accountLogin` 접근 제어 — GitLab group 프로젝트 불가

**파일**: `service/RepositoryService.java`  
**참조**: doc 02 section 5, doc 10 section 9.1

**문제**  
저장소 접근 검증이 `ownerKey == accountLogin` 전제를 사용한다.  
GitLab group/subgroup 프로젝트의 경우 `ownerKey`(= `path_with_namespace`의 namespace)가 개인 계정 login과 다르다.

PRD 2.2는 GitLab을 지원 플랫폼으로 명시하며 조직/그룹 단위 관리는 제외 범위(PRD 5)로 선언했으나, group 프로젝트 접근은 개인 계정에서도 발생 가능하다. 현재 구조에서는 사용자가 소속된 group의 프로젝트를 조회해도 접근 검증 실패로 런타임 오류가 발생한다.

---

## 종합

| 항목 | 심각도 | 상태 |
|---|---|---|
| `config/GitHubClientConfig` 위치 비일관성 | 낮음 | 즉시 이동 가능 |
| DB 마이그레이션 도구 없음 | 높음 | 운영 배포 전 반드시 도입 필요 |
| `PlatformConnection` unique constraint 오류 | 중간 | self-managed GitLab 시나리오 진입 전 수정 필요 |
| `ownerKey == accountLogin` GitLab 접근 제어 실패 | 중간 | GitLab group 프로젝트 사용 시 런타임 오류 발생 |
