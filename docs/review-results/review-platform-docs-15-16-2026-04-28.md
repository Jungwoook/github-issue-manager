# Code Review: Platform 구조 문서화 (docs 15, 16)
날짜: 2026-04-28
참조: docs/reviews/platform-docs-15-16-review.md

---

## 1. [테스트 실패] `ModuleBoundaryTest.gradleModuleDependenciesFollowDocumentedDirection` — 기대값과 build.gradle 불일치

**파일**: `backend/app/src/test/java/com/jw/github_issue_manager/ModuleBoundaryTest.java:39-47`

```java
"comment", Set.of("connection", "issue", "platform", "repository", "shared-kernel"),
"issue", Set.of("connection", "platform", "repository", "shared-kernel"),
"repository", Set.of("connection", "platform", "shared-kernel"),
```

테스트 기대값에 `connection`이 포함되어 있다. 그러나 현재 build.gradle 실제 상태:

```gradle
// repository/build.gradle — connection 없음
api project(':platform')
api project(':shared-kernel')

// issue/build.gradle — connection 없음
api project(':platform')
api project(':shared-kernel')
implementation project(':repository')

// comment/build.gradle — connection 없음
api project(':platform')
implementation project(':issue')
implementation project(':repository')
implementation project(':shared-kernel')
```

build.gradle에서 connection 의존이 제거됐지만 ModuleBoundaryTest의 expected 값은 갱신되지 않았다. `projectDependencies("repository")`는 `{"platform", "shared-kernel"}`을 반환하지만 기대값은 `{"connection", "platform", "shared-kernel"}`이므로 이 테스트는 현재 **실패한다**.

Doc 15 section 9 검증 기준("repository / issue / comment는 connection 모듈에 Gradle 의존을 갖지 않는다")과 build.gradle은 일치하지만 테스트 코드가 낡은 상태로 남아 있다.

---

## 2. [문서-구현 불일치] Doc 16 UC-06, UC-07, UC-08 — `requireCurrentConnection` 별도 호출로 중복 connection 조회

**파일**: `docs/16-use-case-sequence-diagrams.md:133-138` (UC-06)

```
Repository->>Platform: requireCurrentConnection(platform, session)
Repository->>Platform: getAccessibleRepositories(platform, session)
```

`PlatformRemoteFacade.getAccessibleRepositories()` 실제 구현:

```java
public List<RemoteRepository> getAccessibleRepositories(PlatformType platform, HttpSession session) {
    TokenAccess tokenAccess = platformConnectionFacade.requireTokenAccess(platform, session);
    ...
}
```

`getAccessibleRepositories` 내부에서 이미 `requireTokenAccess`를 통해 connection DB를 조회한다. 시퀀스에서 `requireCurrentConnection`을 별도로 먼저 호출하면 동일 세션에 대해 connection DB 조회가 최소 2번 발생한다. UC-07, UC-08도 같은 패턴이다.

두 가지 중 하나다:
- 실제 구현에서 `requireCurrentConnection`은 캐시가 없어 매번 DB를 조회 → 런타임에 중복 조회 발생
- 시퀀스가 실제 호출 흐름을 압축해서 표현한 것 → 문서가 구현을 정확히 반영하지 않음

어느 쪽이든 문서의 시퀀스가 실제 동작을 오해하게 만든다.

---

## 3. [문서 오류] Doc 15 section 8 — `PlatformType` shared-kernel 소유권 오류

**파일**: `docs/15-platform-module-service-structure.md:132`

```
소유: sync 상태, 공통 exception, 공통 response DTO, `PlatformType`
```

`PlatformType`은 platform 모듈의 `com.jw.github_issue_manager.core.platform.PlatformType`에 있다. shared-kernel 모듈 소스 트리에는 `PlatformType` 파일이 없다.

```
backend/platform/src/main/java/com/jw/github_issue_manager/core/platform/PlatformType.java
```

shared-kernel이 `platform`에 의존하므로 transitively 접근 가능하지만, 소유권은 platform에 있다. Doc 15가 `PlatformType`을 shared-kernel의 소유로 기술하면 패키지 이동 시 기준 문서 역할을 못하고, doc 15 section 8 마지막 줄("후속 후보: PlatformType 패키지명을 shared-kernel 성격에 맞게 정리")의 '후속 작업'인지 '현재 상태'인지 판단이 어렵다.

---

## 4. [API 계약 혼동] Doc 16 UC-15 — HTTP `DELETE` 메서드로 이슈 닫기 표현

**파일**: `docs/16-use-case-sequence-diagrams.md:353`

```
Frontend->>App: DELETE /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}
```

실제 동작은 CLOSED 상태 변경 후 refresh이다:

```
Issue->>Platform: updateIssue(state=CLOSED)
```

HTTP `DELETE`는 REST 시맨틱상 리소스 삭제를 의미한다. GitHub API는 이슈 삭제를 지원하지 않고 `PATCH`로 상태를 변경하는 방식을 사용한다. `DELETE /...issues/{issueNumberOrKey}`는 API 소비자(프론트엔드, 외부 클라이언트)가 이슈가 실제로 삭제된다고 오해할 수 있다.

또한 `IssueFacade`의 메서드명이 `deleteIssue`이지만 실제 동작은 "이슈 닫기"이다. 메서드명, HTTP 메서드, 실제 동작이 서로 다른 의미를 가리키고 있어 문서가 이 불일치를 명시하지 않으면 이후 구현자가 혼동한다.
