[변경 목적]
platform/connection 중심 모듈 구조와 유스케이스별 동작 흐름을 문서로 고정해, 이후 GitHub/GitLab 공통화 작업에서 구현 기준과 리뷰 기준을 명확히 한다.

[핵심 변경]
- 15번 문서에 platform/connection/repository/issue/comment/shared-kernel의 책임과 의존 방향 정리
- 15번 문서에 PAT 등록 흐름과 원격 호출 흐름 시퀀스 추가
- 15번 문서에 모듈 경계 검증 기준 추가
- 16번 문서 신규 작성
- 16번 문서에 UC-01~UC-19 유스케이스별 시퀀스 다이어그램 추가
- 16번 문서의 backend controller 참여자명을 `API`가 아니라 `App`으로 정리
- 16번 문서 인코딩 깨짐 복구 및 UTF-8 기준 재저장
- Claude 리뷰 반영: 저장소 시퀀스의 connection 조회 표현, `PlatformType` 소유권, DELETE 닫기 의미 보강

[리뷰 대상 - 중요]
"이 파일들의 diff를 중심으로 리뷰"
- `docs/15-platform-module-service-structure.md`
  - 모듈 책임, 의존 방향, 금지 의존성, 검증 기준이 현재 구현과 맞는지 확인하는 기준 파일
- `docs/16-use-case-sequence-diagrams.md`
  - 각 유스케이스별 시퀀스가 현재 controller/facade/service/cache/remote 호출 흐름과 맞는지 확인하는 기준 파일

[참고 파일]
"필요할 때만 참고하고, 기본적으로는 리뷰 대상 파일에 집중"
- `docs/06-core-use-cases.md`
  - 16번 문서의 UC-01~UC-19 목록과 상세 설명의 원본 기준
- `docs/08-main-use-case-flow.md`
  - 전체 사용자 흐름, API 호출 순서, 코드 추적 기준 확인용 문서
- `backend/app/src/main/java/com/jw/github_issue_manager/controller/AuthController.java`
  - 16번 문서의 `App -> Platform -> Connection` 토큰 등록 흐름 확인용
- `backend/app/src/main/java/com/jw/github_issue_manager/controller/RepositoryController.java`
  - 저장소 유스케이스 경로와 facade 호출 확인용
- `backend/app/src/main/java/com/jw/github_issue_manager/controller/IssueController.java`
  - 이슈 유스케이스 경로와 생성/수정/닫기 흐름 확인용
- `backend/app/src/main/java/com/jw/github_issue_manager/controller/CommentController.java`
  - 댓글 유스케이스 경로와 refresh/create 흐름 확인용
- `backend/platform/src/main/java/com/jw/github_issue_manager/platform/api/PlatformRemoteFacade.java`
  - 원격 호출 단일 관문 표현이 현재 구현과 맞는지 확인용
- `backend/connection/src/main/java/com/jw/github_issue_manager/connection/internal/service/AuthService.java`
  - 세션과 token access 책임이 connection에 머무는지 확인용
- `backend/app/src/test/java/com/jw/github_issue_manager/ModuleBoundaryTest.java`
  - 문서화된 모듈 의존 방향과 테스트 기대값 일치 여부 확인용

[리뷰 포인트]
1. 모듈 의존 방향 정확성
   - 15번 문서의 의존 그래프가 Gradle 모듈 의존과 실제 import 금지 규칙을 정확히 반영하는지
   - repository / issue / comment가 connection을 직접 모른다는 설명이 현재 구현과 맞는지
   - platform이 connection token access를 통해 원격 호출에 필요한 credential을 얻는다는 설명이 빠짐없는지
2. 책임 경계 표현
   - app은 HTTP 조립과 public facade 호출만 담당한다는 표현이 과하거나 부족하지 않은지
   - connection이 token 저장/암호화/세션을 소유하고, platform이 credential 검증/원격 호출을 소유한다는 구분이 명확한지
   - shared-kernel에 업무 규칙을 넣지 않는다는 제한이 이후 작업 기준으로 충분한지
3. 유스케이스별 시퀀스 정확성
   - 16번 문서의 UC-01~UC-19가 06번 문서의 유스케이스 목록과 1:1로 대응하는지
   - 각 시퀀스의 API 경로가 `/api/platforms/{platform}/...` 현재 계약을 반영하는지
   - 이슈 닫기가 실제 삭제가 아니라 `CLOSED` 상태 변경 후 refresh하는 흐름으로 표현됐는지
   - `DELETE` 경로가 현재 계약임을 쓰더라도 실제 의미가 닫기라는 점이 명시됐는지
   - 댓글 작성/새로고침이 comment cache와 sync-state 기록까지 반영하는지
4. 용어 일관성
   - backend controller 참여자명이 `App`으로 통일되어 외부 `Remote` API와 혼동되지 않는지
   - `Platform`, `Connection`, `Repository`, `Issue`, `Comment`, `DB`, `Remote` 의미가 문서 전반에서 흔들리지 않는지
   - GitHub 전용 용어가 플랫폼 공통 문맥에서 불필요하게 남아 있지 않은지
5. 문서 유지보수성
   - 16번 문서가 유스케이스별로 충분히 분리되어 특정 흐름만 리뷰하기 쉬운지
   - 15번 문서의 검증 기준이 실제 테스트 명령과 연결되는지
   - 이후 label/milestone/assignee 같은 신규 유스케이스 추가 시 확장 위치가 분명한지

[계약 변경]
- 문서 계약
  - platform 공통 경로 기준: `/api/platforms/{platform}/...`
  - backend controller 참여자명: `App`
  - 외부 플랫폼 API 참여자명: `Remote`
  - 원격 호출 관문: `PlatformRemoteFacade`
  - credential 검증 관문: `PlatformCredentialFacade`
- 구현 계약
  - 이번 변경은 문서 변경이며 런타임 API 또는 Java/TypeScript 코드 계약을 직접 변경하지 않음

[잠재 리스크]
- 문서 정확성
  - 16번 문서의 일부 시퀀스가 서비스 내부 세부 구현을 단순화해 표현하므로, 코드 레벨 리뷰 기준으로 사용할 때 세부 호출 순서 오해 가능
  - `Repository -> Platform -> Connection` 표현은 facade 내부 호출을 문서화한 것이므로, 실제 클래스 메서드명을 그대로 나타내지 않는 구간이 있음
  - GitLab 구현이 추가된 상태에서 일부 흐름이 GitHub 테스트 중심으로 검증되어, 플랫폼별 차이가 문서에 충분히 드러나지 않을 수 있음
- 누락 가능성
  - label API는 프론트 호출 코드가 있으나 백엔드 구현 범위에 없어 16번 유스케이스에서 제외됨
  - token status, logout, sync-state 등 보조 유스케이스는 화면 중심 흐름보다 구현 API 기준으로 정리되어 프론트 화면 명칭과 다를 수 있음
  - 실패 흐름, 인증 실패, 권한 없음, 원격 API 오류 복구 시퀀스는 아직 별도 문서화되지 않음
- 회귀 가능성
  - 이후 app/controller 또는 facade 이름이 바뀌면 15, 16번 문서의 참여자명이 빠르게 낡을 수 있음
  - 시퀀스 다이어그램이 많아 한 API 변경 시 여러 UC를 동시에 갱신해야 함
  - PowerShell 기본 인코딩으로 문서를 저장하면 한글 깨짐이 재발할 수 있음

[검증 기준]
- `docs/16-use-case-sequence-diagrams.md`에서 `## UC-` 항목이 19개인지 확인
- `docs/16-use-case-sequence-diagrams.md`에 `participant API` 또는 `->>API`가 남아 있지 않은지 확인
- `docs/16-use-case-sequence-diagrams.md`가 UTF-8로 정상 출력되는지 확인
- `git diff --check`로 문서 공백 오류가 없는지 확인
- `ModuleBoundaryTest.gradleModuleDependenciesFollowDocumentedDirection` 기대값이 15번 문서의 의존 방향과 일치하는지 확인

[diff]
```markdown
// docs/15-platform-module-service-structure.md
// 설명: 모듈 구조를 platform/connection 중심으로 정리하고, 원격 호출과 token access 책임을 분리해 문서화
+ ## 1. 전체 의존 방향
+ App --> Platform
+ Repository --> Platform
+ Issue --> Repository
+ Comment --> Issue
+ Platform --> Connection
+ Connection --> Shared
+
+ - app: HTTP 조립, 등록 흐름 조립
+ - platform: credential 검증, 원격 API 호출, adapter 선택
+ - connection: token 저장, 암호화, 현재 연결 조회, token access 제공
+ - repository / issue / comment: 자기 cache와 업무 유스케이스 소유
```

```markdown
// docs/15-platform-module-service-structure.md
// 설명: PAT 등록 흐름은 app이 platform 검증 후 connection 저장을 조립하는 방식으로 명시
+ sequenceDiagram
+     participant App as app
+     participant Platform as platform
+     participant Connection as connection
+
+     App->>Platform: validateCredential(platform, token, baseUrl)
+     Platform-->>App: validation result
+     App->>Connection: registerPlatformToken(validated command)
+     Connection-->>App: MeResponse
```

```markdown
// docs/16-use-case-sequence-diagrams.md
// 설명: 유스케이스별 시퀀스 다이어그램 신규 문서 작성
+ ## UC-01 플랫폼 토큰 등록
+ ## UC-02 토큰 상태 조회
+ ## UC-03 현재 사용자 조회
+ ...
+ ## UC-19 댓글 작성
```

```markdown
// docs/16-use-case-sequence-diagrams.md
// 설명: controller 계층 참여자명을 API 대신 App으로 사용해 외부 API와 혼동을 줄임
- participant API
- Frontend->>API: POST /api/platforms/{platform}/token
- API->>Platform: validateCredential(platform, token, baseUrl)
+ participant App
+ Frontend->>App: POST /api/platforms/{platform}/token
+ App->>Platform: validateCredential(platform, token, baseUrl)
```

```markdown
// docs/16-use-case-sequence-diagrams.md
// 설명: 이슈 닫기는 삭제가 아니라 CLOSED 상태 변경 후 refresh하는 구현 흐름으로 문서화
+ Frontend->>App: DELETE /api/platforms/{platform}/repositories/{repositoryId}/issues/{issueNumberOrKey}
+ App->>Issue: deleteIssue(platform, repositoryId, issueNumberOrKey, session)
+ Issue->>Platform: updateIssue(state=CLOSED)
+ Platform->>Remote: 원격 이슈 상태 CLOSED 변경
+ Issue->>Issue: refreshIssues(platform, repositoryId, session)
+ App-->>Frontend: 204 No Content
```
