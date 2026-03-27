# Screen Specification

## 1. 문서 목적

이 문서는 `frontend-guide.md`를 바탕으로 실제 프론트엔드 화면 단위 요구사항을 상세화한다.
화면별 목적, 주요 UI 구성, 사용자 액션, API 연동, 예외 상황을 정리해 구현 기준으로 사용한다.

현재 명세는 백엔드에 실제 구현된 API 범위만 다룬다.

---

## 2. 공통 화면 원칙

### 2.1 공통 레이아웃

권장 레이아웃은 다음과 같다.

* 상단: 프로젝트 제목, 주요 네비게이션
* 좌측 또는 상단 보조 영역: Repository 이동, User 화면 이동
* 메인 영역: 목록, 상세, 폼, 필터

### 2.2 공통 상태

모든 화면은 아래 상태를 고려한다.

* 초기 로딩
* 빈 데이터
* 정상 데이터
* 저장 중
* 요청 실패

### 2.3 공통 액션 패턴

* 생성/수정 완료 후 목록 또는 상세 화면 갱신
* 삭제 전 확인 다이얼로그 표시
* 실패 시 공통 에러 배너 또는 toast 표시

---

## 3. Repository 목록 화면

### 3.1 목적

* 전체 Repository를 조회한다.
* 새 Repository를 생성한다.
* 기존 Repository를 수정하거나 삭제한다.
* 선택한 Repository의 Issue 화면으로 이동한다.

### 3.2 라우트

* `/`
* `/repositories`

### 3.3 주요 UI 구성

* 페이지 제목
* Repository 생성 버튼
* Repository 리스트
* 리스트 아이템별 이름, 설명, 생성일 또는 수정일
* 수정 버튼
* 삭제 버튼
* Issue 화면 이동 버튼 또는 카드 클릭 영역

### 3.4 주요 사용자 액션

* Repository 생성
* Repository 수정
* Repository 삭제
* 특정 Repository 선택

### 3.5 연동 API

* `GET /api/repositories`
* `POST /api/repositories`
* `PUT /api/repositories/{repositoryId}`
* `DELETE /api/repositories/{repositoryId}`

### 3.6 폼 필드

생성/수정 공통

* `name`
* `description`

### 3.7 성공 후 처리

* 생성 성공 시 목록 갱신
* 수정 성공 시 목록 반영
* 삭제 성공 시 목록에서 제거

### 3.8 예외 처리

* 목록 조회 실패: 재시도 버튼 제공
* 생성/수정 검증 실패: 필드 메시지 표시
* 삭제 실패: 서버 메시지 노출

---

## 4. Repository별 Issue 목록 화면

### 4.1 목적

* 선택한 Repository의 Issue를 목록 형태로 조회한다.
* 조건 검색과 필터링을 수행한다.
* 새 Issue를 생성한다.
* 특정 Issue 상세로 이동한다.

### 4.2 라우트

* `/repositories/:repositoryId`
* `/repositories/:repositoryId/issues`

### 4.3 주요 UI 구성

* Repository 제목 및 설명
* Issue 생성 버튼
* 검색 입력
* 상태 필터
* 우선순위 필터
* 담당자 필터
* Label 필터
* Issue 리스트 테이블 또는 카드

### 4.4 목록 컬럼 또는 카드 정보

* 제목
* 상태
* 우선순위
* 담당자
* 생성일
* 수정일

### 4.5 주요 사용자 액션

* 키워드 검색
* 상태 필터 변경
* 우선순위 필터 변경
* 담당자 필터 변경
* Label 필터 변경
* Issue 생성 화면 이동
* Issue 상세 화면 이동

### 4.6 연동 API

* `GET /api/repositories/{repositoryId}`
* `GET /api/repositories/{repositoryId}/issues`
* `GET /api/users`
* `GET /api/repositories/{repositoryId}/labels`

### 4.7 쿼리 파라미터 매핑

* 검색어: `keyword`
* 상태: `status`
* 우선순위: `priority`
* 담당자: `assigneeId`
* 라벨: `labelId`

### 4.8 예외 처리

* Repository 없음: 404 화면 또는 목록으로 복귀
* Issue 없음: 빈 상태 UI 표시
* 필터 조회 실패: 마지막 성공 목록 유지 후 오류 표시

---

## 5. Issue 생성 화면

### 5.1 목적

* 특정 Repository에 새 Issue를 생성한다.

### 5.2 라우트

* `/repositories/:repositoryId/issues/new`

### 5.3 주요 UI 구성

* 페이지 제목 또는 모달 제목
* 제목 입력
* 내용 입력
* 우선순위 선택
* 담당자 선택
* 저장 버튼
* 취소 버튼

### 5.4 폼 필드

* `title`
* `content`
* `priority`
* `assigneeId`

### 5.5 데이터 로딩

* 담당자 선택용 User 목록 조회

### 5.6 연동 API

* `GET /api/users`
* `POST /api/repositories/{repositoryId}/issues`

### 5.7 성공 후 처리

* 생성 성공 시 Issue 상세 화면 또는 Issue 목록 화면으로 이동

### 5.8 예외 처리

* 필수값 누락 시 즉시 검증
* 생성 실패 시 폼 유지

---

## 6. Issue 상세 화면

### 6.1 목적

* Issue의 전체 정보를 조회한다.
* 수정 가능한 값들을 변경한다.
* Label과 Comment를 함께 관리한다.

### 6.2 라우트

* `/repositories/:repositoryId/issues/:issueId`

### 6.3 주요 UI 구성

* 제목
* 본문
* 상태 표시 및 변경 컨트롤
* 우선순위 표시 및 변경 컨트롤
* 담당자 표시 및 변경 컨트롤
* Label 목록
* Label 추가 UI
* Comment 목록
* Comment 입력 영역
* 수정 버튼
* 삭제 버튼

### 6.4 주요 사용자 액션

* Issue 기본 정보 수정
* 상태 변경
* 우선순위 변경
* 담당자 지정 또는 해제
* Label 연결
* Label 해제
* Comment 작성
* Comment 삭제
* Issue 삭제

### 6.5 초기 로딩 데이터

* Issue 상세
* Repository별 Label 목록
* User 목록
* Comment 목록

### 6.6 연동 API

* `GET /api/repositories/{repositoryId}/issues/{issueId}`
* `PUT /api/repositories/{repositoryId}/issues/{issueId}`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/status`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/priority`
* `PATCH /api/repositories/{repositoryId}/issues/{issueId}/assignee`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}`
* `GET /api/repositories/{repositoryId}/labels`
* `POST /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}/labels/{labelId}`
* `GET /api/repositories/{repositoryId}/issues/{issueId}/comments`
* `POST /api/repositories/{repositoryId}/issues/{issueId}/comments`
* `DELETE /api/repositories/{repositoryId}/issues/{issueId}/comments/{commentId}`
* `GET /api/users`

### 6.7 상태 변경 UX

권장 방식

* 드롭다운 또는 segmented control 사용
* 변경 요청 중 해당 컨트롤 비활성화
* 성공 후 상세 데이터 갱신 또는 로컬 상태 업데이트

### 6.8 Label 관리 UX

권장 방식

* 현재 연결된 Label은 badge로 표시
* 추가 가능한 Label은 드롭다운 또는 멀티 선택 UI로 표시
* 이미 연결된 Label은 다시 선택하지 않도록 처리

### 6.9 Comment 관리 UX

권장 방식

* 작성자 선택 후 댓글 입력
* 작성 성공 시 목록 하단에 바로 반영
* 삭제는 확인 후 수행

### 6.10 예외 처리

* `LABEL_ALREADY_ATTACHED`: 중복 연결 메시지 표시
* `USER_NOT_FOUND`: 담당자 선택 목록 갱신 유도
* `ISSUE_NOT_FOUND`: 상세 화면 이탈 처리

---

## 7. Issue 수정 화면

### 7.1 목적

* Issue 기본 정보인 제목, 내용, 담당자를 수정한다.

### 7.2 라우트

* `/repositories/:repositoryId/issues/:issueId/edit`

### 7.3 주요 UI 구성

* 기존 값이 채워진 폼
* 제목 입력
* 내용 입력
* 담당자 선택
* 저장 버튼
* 취소 버튼

### 7.4 연동 API

* `GET /api/repositories/{repositoryId}/issues/{issueId}`
* `GET /api/users`
* `PUT /api/repositories/{repositoryId}/issues/{issueId}`

### 7.5 비고

Issue 상세 화면 내부 인라인 수정으로 대체할 수 있다.

---

## 8. User 목록 화면

### 8.1 목적

* 시스템에서 사용하는 User를 관리한다.

### 8.2 라우트

* `/users`

### 8.3 주요 UI 구성

* 페이지 제목
* User 생성 버튼
* 검색 입력
* role 필터
* User 리스트
* 수정 버튼
* 삭제 버튼

### 8.4 목록 컬럼

* username
* displayName
* email
* role
* 생성일
* 수정일

### 8.5 주요 사용자 액션

* 사용자 검색
* role 필터
* 생성
* 수정
* 삭제

### 8.6 연동 API

* `GET /api/users`
* `POST /api/users`
* `PUT /api/users/{userId}`
* `DELETE /api/users/{userId}`

### 8.7 예외 처리

* `DUPLICATE_USER_USERNAME`
* `DUPLICATE_USER_EMAIL`
* `USER_DELETE_CONFLICT`

각 오류는 일반 실패 메시지보다 구체적으로 보여주는 것이 좋다.

---

## 9. User 생성/수정 화면

### 9.1 라우트

* `/users/new`
* `/users/:userId/edit`

### 9.2 폼 필드

생성

* `username`
* `displayName`
* `email`
* `role`

수정

* `displayName`
* `email`
* `role`

주의

* 수정 화면에서는 `username` 변경을 지원하지 않는다.

### 9.3 연동 API

* `POST /api/users`
* `GET /api/users/{userId}`
* `PUT /api/users/{userId}`

---

## 10. Label 관리 화면

### 10.1 목적

* 특정 Repository의 Label을 생성하고 조회한다.

### 10.2 라우트

* `/repositories/:repositoryId/labels`

### 10.3 주요 UI 구성

* Repository 정보
* Label 생성 폼
* Label 리스트
* 색상 미리보기

### 10.4 폼 필드

* `name`
* `color`

### 10.5 연동 API

* `GET /api/repositories/{repositoryId}/labels`
* `POST /api/repositories/{repositoryId}/labels`

### 10.6 예외 처리

* `DUPLICATE_LABEL_NAME`
* `REPOSITORY_NOT_FOUND`

---

## 11. 공통 다이얼로그 명세

### 11.1 삭제 확인 다이얼로그

대상

* Repository 삭제
* Issue 삭제
* Comment 삭제
* User 삭제

포함 요소

* 대상 이름 또는 요약 정보
* 삭제 경고 문구
* 취소 버튼
* 삭제 버튼

### 11.2 공통 오류 다이얼로그 또는 배너

포함 요소

* 오류 메시지
* 재시도 버튼
* 닫기 버튼

---

## 12. 빈 상태 메시지 예시

* Repository 없음: 아직 생성된 Repository가 없습니다.
* Issue 없음: 조건에 맞는 Issue가 없습니다.
* Comment 없음: 아직 Comment가 없습니다.
* Label 없음: 아직 Label이 없습니다.
* User 없음: 먼저 User를 생성해야 담당자를 지정할 수 있습니다.

---

## 13. 화면 구현 순서 제안

1. Repository 목록 화면
2. Issue 목록 화면
3. Issue 생성 화면
4. Issue 상세 화면
5. User 목록 및 생성/수정 화면
6. Label 관리 화면
7. 공통 다이얼로그와 오류 처리

---

## 14. 요약

초기 프론트엔드 구현은 Repository와 Issue 중심 화면을 먼저 완성하고, User와 Label 관리 화면을 보조 관리 기능으로 붙이는 방식이 효율적이다.
Issue 상세 화면이 가장 많은 API를 사용하므로, 이 화면을 기준으로 상태 관리와 공통 컴포넌트를 정리하면 전체 구조를 안정적으로 가져갈 수 있다.
