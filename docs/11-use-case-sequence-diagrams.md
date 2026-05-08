# Use Case Sequence Diagrams

## 1. 개요

이 문서는 현재 구현 기준의 주요 유스케이스 흐름을 시퀀스 다이어그램으로 정리한다.

공통 기준은 다음과 같다.

- App: Spring MVC controller
- Application: use case orchestration 계층
- Connection: 세션, 플랫폼 연결, token access
- Platform: `PlatformCredentialFacade`, `PlatformGatewayResolver`, GitHub/GitLab gateway
- Repository / Issue / Comment: 로컬 cache 소유 모듈
- SyncState: 마지막 동기화 상태 요약 저장소
- SyncRun: 동기화 실행 상세 이력 저장소
- SyncFailure: 재처리 가능한 실패 저장소
- RateLimit: GitHub rate limit 상태 저장소

## UC-01 플랫폼 토큰 등록

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Platform
    participant Connection
    participant DB
    participant Remote

    User->>Frontend: 플랫폼/PAT 입력
    Frontend->>App: 토큰 등록 요청
    App->>Application: 토큰 등록 흐름 시작
    Application->>Platform: 토큰 유효성 확인 요청
    Platform->>Remote: 현재 사용자 조회
    Remote-->>Platform: 사용자 프로필
    Platform-->>Application: 검증 결과
    Application->>Connection: 검증된 연결 정보 저장 요청
    Connection->>DB: 사용자/플랫폼 연결 저장, PAT 암호화
    Connection->>Connection: 현재 세션 연결 설정
    Connection-->>Application: 현재 사용자 정보
    Application-->>App: 현재 사용자 정보
    App-->>Frontend: 현재 사용자 정보
```

## UC-02 토큰 상태 / 현재 사용자 / 연결 종료

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant DB

    User->>Frontend: 연결 상태 또는 현재 사용자 확인
    Frontend->>App: 연결 상태 또는 현재 사용자 조회 요청
    App->>Application: 상태 조회 흐름 시작
    Application->>Connection: 세션 기준 연결 조회
    Connection->>DB: 저장된 플랫폼 연결 조회
    DB-->>Connection: 연결 정보
    Connection-->>Application: 상태/현재 사용자 응답
    Application-->>App: 응답
    App-->>Frontend: 응답

    User->>Frontend: 연결 해제 또는 로그아웃
    Frontend->>App: 연결 해제 또는 로그아웃 요청
    App->>Application: 종료 흐름 시작
    Application->>Connection: 토큰 제거 또는 세션 종료 요청
    Connection-->>Application: 완료
    Application-->>App: 완료
    App-->>Frontend: 종료 완료 응답
```

## UC-06 저장소 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Platform
    participant Repository
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 저장소 새로고침 클릭
    Frontend->>App: 저장소 새로고침 요청
    App->>Application: 저장소 동기화 흐름 시작
    Application->>Connection: 현재 연결과 토큰 확인
    Connection-->>Application: 계정과 원격 호출 정보
    Application->>Platform: 접근 가능한 저장소 조회 요청
    Platform->>Remote: 저장소 목록 조회
    Remote-->>Platform: 원격 저장소 목록
    Platform-->>Application: 원격 저장소 목록
    Application->>Repository: 저장소 캐시 반영 요청
    Repository->>DB: 저장소 캐시 저장
    Repository-->>Application: 캐시 기준 저장소 목록
    Application->>SyncState: 저장소 동기화 성공 기록
    SyncState->>DB: 마지막 동기화 상태 저장
    Application-->>App: 저장소 목록
    App-->>Frontend: 저장소 목록
```

## UC-07 저장소 조회 / UC-09 저장소 동기화 상태 조회

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant SyncState
    participant DB

    User->>Frontend: 저장소 목록 또는 상세 조회
    Frontend->>App: 저장소 조회 요청
    App->>Application: 저장소 조회 흐름 시작
    Application->>Connection: 현재 연결 확인
    Connection-->>Application: 현재 계정 정보
    Application->>Repository: 저장소 캐시 조회 또는 접근 확인
    Repository->>DB: 저장소 캐시 조회
    DB-->>Repository: 저장소 캐시
    Repository-->>Application: 저장소 정보
    Application-->>App: 응답
    App-->>Frontend: 응답

    User->>Frontend: 저장소 동기화 상태 조회
    Frontend->>App: 저장소 동기화 상태 조회 요청
    App->>Application: 저장소 동기화 상태 조회 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Note over Application,SyncState: sync-state API는 SyncState 요약을 반환
    Application->>SyncState: 마지막 동기화 상태 조회
    SyncState->>DB: 동기화 상태 조회
    DB-->>SyncState: 동기화 상태
    SyncState-->>Application: 동기화 상태
    Application-->>App: 동기화 상태
    App-->>Frontend: 동기화 상태
```

## UC-10 이슈 새로고침

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Platform
    participant Issue
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 이슈 새로고침 클릭
    Frontend->>App: 이슈 새로고침 요청
    App->>Application: 이슈 동기화 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Repository-->>Application: 저장소 접근 정보
    Application->>Connection: 원격 호출 토큰 확인
    Connection-->>Application: 원격 호출 정보
    Application->>Platform: 원격 이슈 목록 조회 요청
    Platform->>Remote: 이슈 목록 조회
    Remote-->>Platform: 원격 이슈 목록
    Platform-->>Application: 원격 이슈 목록
    Application->>Issue: 이슈 캐시 반영 요청
    Issue->>DB: 이슈 캐시 저장
    Issue-->>Application: 캐시 기준 이슈 목록
    Application->>SyncState: 이슈 동기화 성공 기록
    SyncState->>DB: 마지막 동기화 상태 저장
    Application-->>App: 이슈 목록
    App-->>Frontend: 이슈 목록
```

## UC-11~16 이슈 조회 / 생성 / 수정 / 닫기 / 동기화 상태

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Platform
    participant Issue
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 이슈 조회
    Frontend->>App: 이슈 조회 요청
    App->>Application: 이슈 조회 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Application->>Issue: 이슈 캐시 조회
    Issue->>DB: 이슈 캐시 조회
    Issue-->>Application: 이슈 응답
    Application-->>App: 이슈 응답
    App-->>Frontend: 이슈 응답

    User->>Frontend: 이슈 생성/수정/닫기
    Frontend->>App: 이슈 생성/수정/닫기 요청
    App->>Application: 이슈 변경 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Application->>Issue: 필요 시 현재 이슈 캐시 조회
    Application->>Connection: 원격 호출 토큰 확인
    Connection-->>Application: 원격 호출 정보
    Application->>Platform: 원격 이슈 변경 요청
    Platform->>Remote: 원격 이슈 변경
    Remote-->>Platform: 변경된 이슈
    Platform-->>Application: 변경된 원격 이슈
    Application->>Issue: 이슈 캐시 갱신 요청
    Issue->>DB: 이슈 캐시 저장
    Issue-->>Application: 변경된 이슈 정보
    Application->>SyncState: 이슈 변경 성공 기록
    SyncState->>DB: 마지막 동기화 상태 저장
    Application-->>App: 변경 완료 결과
    App-->>Frontend: 응답

    User->>Frontend: 이슈 동기화 상태 조회
    Frontend->>App: 이슈 동기화 상태 조회 요청
    App->>Application: 이슈 동기화 상태 조회 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Application->>Issue: 이슈 존재 여부 확인
    Note over Application,SyncState: sync-state API는 SyncState 요약을 반환
    Application->>SyncState: 마지막 동기화 상태 조회
    SyncState-->>Application: 동기화 상태
    Application-->>App: 동기화 상태
    App-->>Frontend: 동기화 상태
```

## UC-17~19 댓글 새로고침 / 조회 / 작성

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Issue
    participant Platform
    participant Comment
    participant SyncState
    participant DB
    participant Remote

    User->>Frontend: 댓글 조회
    Frontend->>App: 댓글 조회 요청
    App->>Application: 댓글 조회 흐름 시작
    Application->>Issue: 이슈 존재 여부 확인
    Application->>Comment: 댓글 캐시 조회
    Comment->>DB: 댓글 캐시 조회
    Comment-->>Application: 댓글 목록
    Application-->>App: 댓글 목록
    App-->>Frontend: 댓글 목록

    User->>Frontend: 댓글 새로고침 또는 작성
    Frontend->>App: 댓글 새로고침 또는 작성 요청
    App->>Application: 댓글 동기화/작성 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Application->>Issue: 이슈 존재 여부 확인
    Application->>Connection: 원격 호출 토큰 확인
    Connection-->>Application: 원격 호출 정보
    Application->>Platform: 원격 댓글 조회 또는 작성 요청
    Platform->>Remote: 원격 댓글 조회 또는 작성
    Remote-->>Platform: 원격 댓글 결과
    Platform-->>Application: 원격 댓글 결과
    Application->>Comment: 댓글 캐시 반영 요청
    Comment->>DB: 댓글 캐시 저장
    Comment-->>Application: 댓글 정보
    Application->>SyncState: 댓글 동기화 성공 기록
    SyncState->>DB: 마지막 동기화 상태 저장
    Application-->>App: 응답
    App-->>Frontend: 응답
```

## UC-20~21 GitHub Rate Limit 수집 / 동기화 중단 기록

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Platform
    participant SyncRun
    participant SyncFailure
    participant RateLimit
    participant DB
    participant Remote as GitHub API

    User->>Frontend: 저장소/이슈/댓글 새로고침 실행
    Frontend->>App: 새로고침 요청
    App->>Application: 동기화 흐름 시작
    Application->>SyncRun: 동기화 실행 시작 기록
    SyncRun->>DB: 실행 중 상태 저장
    Application->>Connection: 원격 호출 토큰 확인
    Connection-->>Application: 원격 호출 정보
    Application->>Platform: GitHub 원격 호출 요청
    Platform->>Remote: 원격 데이터 요청
    Remote-->>Platform: 응답과 호출 제한 정보
    Platform->>Platform: 호출 제한 상태 정리
    Platform-->>Application: 원격 결과와 호출 제한 상태
    Application->>RateLimit: 최근 호출 제한 상태 저장 요청
    RateLimit->>DB: 호출 제한 상태 저장

    alt 정상 응답
        Application->>SyncRun: 성공으로 실행 마감
        SyncRun->>DB: 성공 상태 저장
        Application-->>App: 새로고침 결과
        App-->>Frontend: 새로고침 결과
    else 호출 제한 응답
        Platform-->>Application: 호출 제한 실패 전달
        Application->>SyncFailure: 재처리 가능한 실패 저장 요청
        SyncFailure->>DB: 실패 이력 저장
        Application->>SyncRun: 호출 제한으로 실행 마감
        SyncRun->>DB: 호출 제한 또는 부분 성공 상태 저장
        Application-->>App: 호출 제한 실패 응답
        App-->>Frontend: 재처리 가능 시각 포함 오류
    end
```

## UC-22~23 실패 이력 조회 / 수동 재처리

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant SyncFailure
    participant SyncRun
    participant Connection
    participant Platform
    participant Repository
    participant Issue
    participant Comment
    participant DB
    participant Remote as GitHub API

    User->>Frontend: 실패 이력 조회
    Frontend->>App: 실패 이력 조회 요청
    App->>Application: 실패 이력 조회 흐름 시작
    Application->>SyncFailure: 조건에 맞는 실패 목록 조회
    SyncFailure->>DB: 실패 이력 조회
    DB-->>SyncFailure: 실패 목록
    SyncFailure-->>Application: 실패 목록
    Application-->>App: 실패 목록 응답
    App-->>Frontend: 실패 유형과 재처리 가능 시각 표시

    User->>Frontend: 실패 건 수동 재처리
    Frontend->>App: 실패 건 재처리 요청
    App->>Application: 수동 재처리 흐름 시작
    Application->>SyncFailure: 재처리 대상 실패 조회
    SyncFailure->>DB: 실패 이력 조회
    DB-->>SyncFailure: 실패 정보
    SyncFailure-->>Application: 실패 정보

    alt 재처리 불가 또는 아직 재처리 시각 전
        Application-->>App: 재처리 거부/보류 응답
        App-->>Frontend: 재처리 불가 사유 표시
    else 재처리 가능
        Application->>SyncRun: 재처리 실행 시작 기록
        SyncRun->>DB: 실행 중 상태 저장
        Application->>Connection: 원격 호출 토큰 확인
        Connection-->>Application: 원격 호출 정보
        Application->>Platform: 실패한 원격 작업 다시 요청
        Platform->>Remote: 원격 데이터 다시 요청
        Remote-->>Platform: 원격 결과 또는 실패

        alt 재처리 성공
            Application->>Repository: 필요 시 저장소 캐시 갱신
            Application->>Issue: 필요 시 이슈 캐시 갱신
            Application->>Comment: 필요 시 댓글 캐시 갱신
            Application->>SyncFailure: 실패 해결 처리
            SyncFailure->>DB: 해결 시각 저장
            Application->>SyncRun: 성공으로 실행 마감
            SyncRun->>DB: 성공 상태 저장
            Application-->>App: 재처리 성공 응답
            App-->>Frontend: 성공 표시
        else 재처리 실패
            Application->>SyncFailure: 재시도 횟수와 다음 가능 시각 갱신
            SyncFailure->>DB: 실패 상태 갱신
            Application->>SyncRun: 실패로 실행 마감
            SyncRun->>DB: 실패 또는 호출 제한 상태 저장
            Application-->>App: 재처리 실패 응답
            App-->>Frontend: 실패 사유 표시
        end
    end
```

## UC-24~25 저장소 / 이슈 수동 재동기화

```mermaid
sequenceDiagram
    actor User
    participant Frontend
    participant App
    participant Application
    participant Connection
    participant Repository
    participant Issue
    participant Comment
    participant Platform
    participant SyncRun
    participant SyncFailure
    participant DB
    participant Remote as GitHub API

    User->>Frontend: 저장소 또는 이슈 재동기화 실행
    Frontend->>App: 저장소 또는 이슈 재동기화 요청
    App->>Application: 수동 재동기화 흐름 시작
    Application->>Repository: 저장소 접근 가능 여부 확인
    Repository-->>Application: 저장소 접근 정보
    Application->>Issue: 이슈 단위면 이슈 존재 여부 확인
    Issue-->>Application: 이슈 접근 정보
    Application->>SyncRun: 수동 재동기화 실행 시작 기록
    SyncRun->>DB: 실행 중 상태 저장
    Application->>Connection: 원격 호출 토큰 확인
    Connection-->>Application: 원격 호출 정보

    alt 저장소 단위 재동기화
        Application->>Platform: 저장소 또는 이슈 목록 조회 요청
        Platform->>Remote: 저장소/이슈 목록 조회
        Remote-->>Platform: 원격 결과
        Platform-->>Application: 원격 결과
        Application->>Repository: 저장소 캐시 갱신
        Application->>Issue: 이슈 캐시 갱신
    else 이슈 단위 재동기화
        Application->>Platform: 이슈 단건 조회 요청
        Platform->>Remote: 이슈 단건 조회
        Remote-->>Platform: 원격 이슈
        Platform-->>Application: 원격 이슈
        Application->>Issue: 이슈 캐시 갱신

        opt 댓글 포함 요청
            Application->>Platform: 댓글 목록 조회 요청
            Platform->>Remote: 댓글 목록 조회
            Remote-->>Platform: 원격 댓글 목록
            Platform-->>Application: 원격 댓글 목록
            Application->>Comment: 댓글 캐시 갱신
        end
    end

    alt 재동기화 성공
        Application->>SyncRun: 성공으로 실행 마감
        SyncRun->>DB: 성공 상태 저장
        Application-->>App: 재동기화 결과
        App-->>Frontend: 재동기화 결과
    else 일부 또는 전체 실패
        Application->>SyncFailure: 실패 이력 저장 요청
        SyncFailure->>DB: 실패 이력 저장
        Application->>SyncRun: 부분 성공 또는 실패로 실행 마감
        SyncRun->>DB: 부분 성공 또는 실패 상태 저장
        Application-->>App: 실패 포함 재동기화 결과
        App-->>Frontend: 실패 사유 표시
    end
```

## UC-26 자동 재처리 예약 TODO

```mermaid
sequenceDiagram
    participant Scheduler
    participant Application
    participant SyncFailure
    participant SyncRun
    participant Connection
    participant Platform
    participant Repository
    participant Issue
    participant Comment
    participant DB
    participant Remote as GitHub API

    Scheduler->>Application: 재처리 가능 시각이 된 실패 확인
    Application->>SyncFailure: 재처리 대상 실패 조회
    SyncFailure->>DB: 재처리 가능하고 미해결인 실패 조회
    DB-->>SyncFailure: 재처리 대상
    SyncFailure-->>Application: 재처리 대상 목록

    loop 각 실패 건
        Application->>SyncRun: 자동 재처리 실행 시작 기록
        SyncRun->>DB: 실행 중 상태 저장
        Application->>Connection: 원격 호출 정보 조회
        Connection-->>Application: 원격 호출 정보
        Application->>Platform: 실패한 원격 작업 다시 요청
        Platform->>Remote: 원격 데이터 다시 요청
        Remote-->>Platform: 원격 결과 또는 실패

        alt 성공
            Application->>Repository: 필요 시 저장소 캐시 갱신
            Application->>Issue: 필요 시 이슈 캐시 갱신
            Application->>Comment: 필요 시 댓글 캐시 갱신
            Application->>SyncFailure: 실패 해결 처리
            SyncFailure->>DB: 해결 시각 저장
            Application->>SyncRun: 성공으로 실행 마감
            SyncRun->>DB: 성공 상태 저장
        else 실패
            Application->>SyncFailure: 재시도 횟수와 다음 가능 시각 갱신
            SyncFailure->>DB: 실패 상태 갱신
            Application->>SyncRun: 실패로 실행 마감
            SyncRun->>DB: 실패 또는 호출 제한 상태 저장
        end
    end
```
