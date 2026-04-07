# PAT 입력, 관리, 사용 흐름 정리

## 1. 목적

이 문서는 현재 프로젝트에서 GitHub PAT(Personal Access Token)를

- 어디서 입력받는지
- 백엔드에서 어떻게 검증하고 저장하는지
- 이후 어떤 방식으로 GitHub API 호출에 사용하는지
- 연결 해제 시 무엇이 사라지고 무엇이 남는지

를 구현 기준으로 정리한다.

현재 기준 대상 코드는 `frontend`의 PAT 설정 화면과 `backend`의 인증, 저장소, 이슈, 댓글 서비스다.

## 2. 전체 흐름 한눈에 보기

1. 사용자가 프론트엔드 `GitHub PAT 설정` 화면에서 PAT를 입력한다.
2. 프론트엔드는 `POST /api/github/token`으로 `accessToken`을 백엔드에 전달한다.
3. 백엔드는 전달받은 PAT로 GitHub `/user` API를 호출해 토큰이 유효한지 확인한다.
4. 검증에 성공하면 PAT를 암호화해서 `github_accounts.access_token_encrypted`에 저장한다.
5. 동시에 세션에 현재 사용자 ID를 저장해서 이후 요청에서 현재 GitHub 계정을 식별한다.
6. 프론트엔드는 등록 성공 직후 저장소 새로고침을 한 번 시도한다.
7. 이후 저장소/이슈/댓글 관련 서비스는 세션으로 현재 계정을 찾고, 저장된 암호화 PAT를 복호화해 GitHub API 호출에 사용한다.
8. 사용자가 연결 해제를 하면 암호화된 PAT와 토큰 범위 정보가 제거되고 세션도 해제된다.

## 3. PAT 입력

### 3.1 프론트엔드 입력 화면

- 화면 위치: `frontend/src/pages/settings/GitHubTokenPage.tsx`
- 실제 입력 폼: `frontend/src/widgets/github-token/GitHubTokenForm.tsx`

동작은 다음과 같다.

- 사용자가 `Personal Access Token` 입력란에 PAT를 입력한다.
- 등록 버튼 클릭 시 `registerGitHubToken({ accessToken })`를 호출한다.
- 요청은 `frontend/src/entities/github/api/githubTokenApi.ts`를 통해 `POST /github/token`으로 전송된다.
- 공통 API 클라이언트는 `credentials: 'include'`를 사용하므로 세션 쿠키 기반으로 백엔드와 상태를 유지한다.

요청 바디 형식은 다음과 같다.

```json
{
  "accessToken": "github_pat_xxx"
}
```

### 3.2 등록 직후 프론트 동작

PAT 등록이 성공하면 프론트는 다음을 수행한다.

- 입력창 값을 비운다.
- 저장소 새로고침 API를 한 번 시도한다.
- 토큰 상태, 저장소 목록 쿼리를 무효화한다.
- `/repositories`로 이동한다.

즉, 현재 UX는 "PAT 등록 후 바로 저장소를 볼 수 있게 하는 흐름"으로 구현되어 있다.

## 4. PAT 검증과 연결

### 4.1 진입 API

- 컨트롤러: `backend/src/main/java/com/jw/github_issue_manager/controller/AuthController.java`
- 엔드포인트: `POST /api/github/token`

이 API는 `RegisterGitHubTokenRequest`를 받아 `AuthService.registerGitHubToken(...)`으로 위임한다.

### 4.2 검증 방식

`AuthService.registerGitHubToken(...)`의 핵심 순서는 다음과 같다.

1. 전달받은 PAT로 `gitHubApiClient.getAuthenticatedUser(request.accessToken())` 호출
2. GitHub `/user` 응답으로 토큰 유효성 확인
3. 사용자 프로필 정보(login, id, avatar, email, name) 확보
4. PAT 암호화
5. 기존 GitHub 계정 레코드 갱신 또는 신규 생성
6. 세션에 현재 사용자 ID 저장

여기서 검증 실패는 곧 GitHub API 호출 실패이므로, 유효하지 않거나 권한이 맞지 않는 PAT는 등록 단계에서 막힌다.

## 5. PAT 저장과 관리

### 5.1 저장 위치

PAT 자체는 DB 평문 저장이 아니라 `github_accounts` 테이블의 아래 컬럼에 암호화된 값으로 저장된다.

- `access_token_encrypted`
- `token_scopes`
- `token_verified_at`
- `connected_at`
- `last_authenticated_at`

도메인 모델은 `backend/src/main/java/com/jw/github_issue_manager/domain/GitHubAccount.java`에 있다.

### 5.2 암호화 방식

- 서비스: `backend/src/main/java/com/jw/github_issue_manager/service/PatCryptoService.java`
- 설정 키: `app.github.pat-encryption-key`
- 환경 변수: `GITHUB_PAT_ENCRYPTION_KEY`

현재 구현은 다음 순서로 동작한다.

1. `GITHUB_PAT_ENCRYPTION_KEY` 문자열을 읽는다.
2. SHA-256 해시를 구한다.
3. 앞 16바이트를 AES 키로 사용한다.
4. PAT를 암호화한 뒤 Base64 문자열로 저장한다.

즉, 운영 환경에서는 `GITHUB_PAT_ENCRYPTION_KEY`를 반드시 별도 값으로 설정해야 한다.

주의:

- `application.yaml`에는 기본값 `local-dev-pat-key`가 들어 있다.
- 이 값은 로컬 개발용으로만 봐야 하며 운영에서는 그대로 쓰면 안 된다.

### 5.3 계정 연결 정보

PAT 등록에 성공하면 현재 구현은 GitHub 계정 단위로 아래 정보를 함께 관리한다.

- GitHub 사용자 ID
- GitHub login
- avatar URL
- 내부 사용자(`users`)와의 연결
- 마지막 토큰 검증 시각
- 마지막 인증 시각

또한 `tokenScopes`는 현재 코드상 실제 GitHub 응답에서 읽지 않고 `"fine-grained"` 문자열로 저장한다.

즉, 현재는 "실제 scope를 정밀 저장"하는 구조가 아니라 "이 앱이 기대하는 토큰 종류"를 기록하는 수준이다.

## 6. 세션으로 현재 사용자 유지

PAT를 등록하면 백엔드는 세션에 `currentUserId`를 저장한다.

- 상수 위치: `AuthService.CURRENT_USER_ID`
- 저장 값: 내부 `users.id`

이후 백엔드는 모든 보호된 GitHub 연동 요청에서 다음 순서를 따른다.

1. 세션에서 `currentUserId` 조회
2. 해당 사용자에 연결된 `GitHubAccount` 조회
3. `accessTokenEncrypted` 존재 여부 확인
4. 저장된 값을 복호화해 실제 PAT 획득
5. GitHub API 호출에 사용

즉, 프론트엔드는 등록 이후 매번 PAT를 다시 보내지 않고, 세션과 서버 저장 토큰을 이용해 연동을 유지한다.

## 7. PAT 사용 지점

### 7.1 저장소 조회/동기화

- 서비스: `RepositoryService`

PAT 사용 메서드:

- `refreshRepositories(HttpSession session)`

흐름:

1. `authService.requirePersonalAccessToken(session)` 호출
2. 저장된 암호화 PAT 복호화
3. `gitHubApiClient.getAccessibleRepositories(token)` 호출
4. GitHub 저장소 목록을 `repository_caches`에 upsert

`getRepositories(...)`는 GitHub를 직접 다시 호출하지 않고 캐시된 저장소를 반환한다.

### 7.2 이슈 조회/생성/수정/닫기

- 서비스: `IssueService`

PAT 사용 메서드:

- `refreshIssues(...)`
- `createIssue(...)`
- `updateIssue(...)`
- `deleteIssue(...)`

흐름:

1. 세션으로 현재 사용자 확인
2. 저장소가 현재 사용자 소유/접근 범위인지 확인
3. 저장된 PAT 복호화
4. GitHub Issues API 호출
5. 결과를 `issue_caches`에 반영

주의:

- `getIssues(...)`, `getIssue(...)`는 캐시 조회 중심이다.
- 실제 GitHub 최신 상태 반영은 `refreshIssues(...)` 또는 생성/수정/닫기 후 캐시 반영 시점에 일어난다.

### 7.3 댓글 조회/작성

- 서비스: `CommentService`

PAT 사용 메서드:

- `refreshComments(...)`
- `createComment(...)`

흐름은 이슈와 동일하다.

1. 세션으로 현재 사용자 확인
2. 저장소/이슈 접근 가능 여부 확인
3. 저장된 PAT 복호화
4. GitHub Issue Comments API 호출
5. 결과를 `comment_caches`에 반영

## 8. 실제 GitHub API 호출 방식

GitHub API 호출 구현은 `DefaultGitHubApiClient`에 있다.

현재 PAT는 모든 GitHub REST 호출에서 아래 형식으로 사용된다.

- `Authorization: Bearer {PAT}`
- `Accept: application/vnd.github+json`

대표 호출은 다음과 같다.

- `GET /user`
- `GET /user/repos`
- `GET /repos/{owner}/{repo}/issues`
- `POST /repos/{owner}/{repo}/issues`
- `PATCH /repos/{owner}/{repo}/issues/{issueNumber}`
- `GET /repos/{owner}/{repo}/issues/{issueNumber}/comments`
- `POST /repos/{owner}/{repo}/issues/{issueNumber}/comments`

즉, 현재 프로젝트는 OAuth나 GitHub App 설치 토큰이 아니라 "사용자가 직접 넣은 PAT"를 GitHub REST API에 그대로 사용하는 구조다.

## 9. 상태 조회와 연결 해제

### 9.1 상태 조회

- API: `GET /api/github/token/status`

반환 정보:

- `connected`
- `githubLogin`
- `tokenScopes`
- `tokenVerifiedAt`

현재 화면에서는 이 정보를 이용해 연결 상태, GitHub 계정, 최근 확인 시각을 보여준다.

### 9.2 연결 해제

- API: `DELETE /api/github/token`

현재 구현의 해제 동작:

- `accessTokenEncrypted = null`
- `tokenScopes = null`
- 세션의 `currentUserId` 제거

중요:

- 연결 해제 시 캐시 테이블(`repository_caches`, `issue_caches`, `comment_caches`)은 삭제하지 않는다.
- 다만 세션도 사라지고 PAT도 제거되므로 이후 GitHub 연동 API는 다시 인증이 필요하다.

즉, "토큰 연결은 끊기지만 과거 동기화 데이터는 DB에 남을 수 있는 구조"다.

## 10. 운영 설정 포인트

### 10.1 필수 환경 변수

`backend/src/main/resources/application.yaml` 기준:

- `GITHUB_API_BASE_URL`
- `GITHUB_PAT_ENCRYPTION_KEY`
- `APP_CORS_ALLOWED_ORIGINS`

가장 중요한 것은 `GITHUB_PAT_ENCRYPTION_KEY`다.

- 운영 환경에서는 충분히 긴 랜덤 문자열로 설정해야 한다.
- 이 값이 바뀌면 기존에 암호화되어 저장된 PAT는 복호화할 수 없게 된다.

즉, 운영 중 키 변경은 "기존 연결 토큰 무효화"와 같은 효과를 낸다.

### 10.2 프론트엔드와 세션

프론트 공통 API 클라이언트는 `credentials: 'include'`를 사용한다.

따라서 다음 조건이 맞아야 세션 기반 인증이 유지된다.

- 브라우저가 쿠키를 포함해서 요청해야 한다.
- 백엔드 CORS 허용 출처가 프론트 주소와 일치해야 한다.

## 11. 현재 구현 기준의 제약과 해석

### 11.1 PAT 범위 관리가 정교하지 않다

현재 `tokenScopes`는 실제 GitHub 응답 기반이 아니라 `"fine-grained"` 고정값이다.

즉, 문서상 "어떤 권한으로 등록되었는지"를 정밀 추적하는 구조는 아직 아니다.

### 11.2 토큰 재검증은 등록 시점 중심이다

현재 `tokenVerifiedAt`는 등록 시점에 갱신된다.

이후 모든 GitHub 호출 때마다 검증 시각을 다시 갱신하는 구조는 아니다.

즉, 등록 이후 토큰이 만료되거나 권한이 바뀌면 실제 문제는 후속 GitHub API 호출 실패로 드러난다.

### 11.3 연결 해제와 데이터 정리는 분리되어 있다

현재 연결 해제는 "토큰 제거 + 세션 종료"까지만 수행한다.

즉, 캐시 정리까지 포함하는 완전한 계정 정리 기능은 아직 아니다.

## 12. 권장 문서 해석

현재 프로젝트의 PAT 관리 방식은 다음 한 문장으로 요약할 수 있다.

"사용자가 프론트에서 PAT를 한 번 등록하면, 백엔드는 이를 검증 후 암호화 저장하고 세션과 연결한 뒤, 이후 저장소/이슈/댓글 GitHub API 호출 시 복호화해서 재사용한다."

운영/개선 관점에서 특히 중요한 포인트는 아래 세 가지다.

- 운영 환경의 `GITHUB_PAT_ENCRYPTION_KEY`를 안전하게 관리할 것
- 연결 해제 시 캐시 데이터는 남는다는 점을 이해할 것
- 실제 토큰 권한과 만료 상태를 더 정교하게 추적하려면 추가 개선이 필요하다는 점을 전제로 볼 것
