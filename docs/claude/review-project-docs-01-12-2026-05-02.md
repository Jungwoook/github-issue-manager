# Code Review: 문서 01-12 전체 vs 현재 구현 정합성 점검
날짜: 2026-05-02
대상: docs/01-prd.md ~ docs/12-prod-runtime-config.md

---

## 1. [문서 오류] Doc 05 sections 3, 8 — PlatformType 소유 모듈 오기재

**파일**: `docs/05-platform-module-service-structure.md:78, 135`

```
# section 3 (platform 설명)
소유: `PlatformType`, remote DTO, gateway 계약

# section 8 (shared-kernel 설명)
참고: `PlatformType`은 현재 platform 모듈 소유이며 shared-kernel 소유가 아니다.
```

실제 파일 위치:

```
backend/shared-kernel/src/main/java/com/jw/github_issue_manager/core/platform/PlatformType.java
```

`backend/platform/src/main/java/...` 경로에는 `PlatformType.java`가 없다. `IssueController.java`, `RepositoryCache.java`, `IssueCache.java` 모두 `com.jw.github_issue_manager.core.platform.PlatformType`을 import하며 이 패키지는 shared-kernel 소유다.

doc 05 section 8이 명시적으로 "platform 모듈 소유이며 shared-kernel 소유가 아니다"라고 기술하고 있어, 모듈 소유권 판단 기준 문서로서 오류를 유발한다.

---

## 2. [문서 오류] Doc 03, 04, 05 — shared-kernel 소유 목록에서 PlatformType 누락

**파일**:
- `docs/03-architecture.md:76`
- `docs/04-architecture-transition-history.md:110`
- `docs/05-platform-module-service-structure.md:134`

세 문서 모두 shared-kernel 소유 목록을 아래처럼 기술한다.

```
shared-kernel: 동기화 상태, 공통 예외, 공통 응답 DTO
```

실제 `shared-kernel` 소스 트리:

```
core/platform/PlatformType.java          ← 누락
domain/SyncState.java
domain/SyncResourceType.java
domain/SyncStatus.java
exception/ResourceNotFoundException.java
service/SyncStateService.java
shared/api/dto/SyncStateResponse.java
```

`PlatformType`이 shared-kernel에 포함되어 있으나 세 문서 모두 이를 반영하지 않았다. 이 문서들을 신뢰하고 패키지 배치 결정을 내릴 경우 오판을 유발한다.

---

## 3. [문서 오류] Doc 12 — APP_CORS_ALLOWED_ORIGINS 환경변수 실제 미사용

**파일**: `docs/12-prod-runtime-config.md:16-18`

```
### 필수 환경 변수

- `APP_CORS_ALLOWED_ORIGINS`
- `GITHUB_PAT_ENCRYPTION_KEY`
```

실제 설정 파일:

```yaml
# application-prod.yml
app:
  cors:
    allowed-origins:
      - https://github-issue-manager-beta.vercel.app   ← 하드코딩

# application-ec2.yml
app:
  cors:
    allowed-origins:
      - https://github-issue-manager-*.vercel.app      ← 하드코딩
```

두 yml 파일 어디에도 `${APP_CORS_ALLOWED_ORIGINS}`를 읽는 항목이 없다. CORS 허용 Origin은 이미 yml에 고정되어 있으므로 운영 환경에서 이 환경변수를 설정해도 아무 영향을 주지 않는다.

`GITHUB_PAT_ENCRYPTION_KEY`는 두 yml에서 `${GITHUB_PAT_ENCRYPTION_KEY}`로 정상 참조된다.

---

## 4. [이전 리뷰 해소 확인] ModuleBoundaryTest 기대값 — connection 제거 완료

**파일**: `backend/app/src/test/java/com/jw/github_issue_manager/ModuleBoundaryTest.java:39-47`

`review-platform-docs-15-16-2026-04-28.md` 항목 1에서 아래 기대값이 build.gradle과 불일치해 테스트가 실패한다고 보고했다.

```java
// 보고 당시 코드 (현재는 변경됨)
"comment", Set.of("connection", "issue", "platform", "repository", "shared-kernel"),
"issue", Set.of("connection", "platform", "repository", "shared-kernel"),
"repository", Set.of("connection", "platform", "shared-kernel"),
```

현재 테스트 코드는 아래와 같이 수정된 상태다.

```java
"comment", Set.of("issue", "platform", "repository", "shared-kernel"),
"issue", Set.of("platform", "repository", "shared-kernel"),
"repository", Set.of("platform", "shared-kernel"),
```

build.gradle과 일치하며 이 테스트는 현재 통과한다. 이전 리뷰 지적사항이 해소되었음을 확인한다.
