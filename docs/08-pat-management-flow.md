# 플랫폼 PAT 관리 흐름

## 목적

- 사용자가 입력한 플랫폼 PAT가 어디서 검증되고 저장되며 이후 원격 호출에 어떻게 사용되는지 정리한다.

## 현재 흐름

1. 프론트에서 플랫폼과 PAT를 입력한다.
2. app controller가 `PlatformCredentialFacade`에 토큰 검증을 요청한다.
3. platform 모듈이 선택된 플랫폼 gateway로 원격 사용자 프로필을 조회한다.
4. app controller가 검증 결과와 원문 토큰을 connection 모듈에 전달한다.
5. connection 모듈이 사용자와 `platform_connections` 정보를 저장한다.
6. connection 모듈이 PAT를 암호화해 저장하고 세션에 현재 사용자와 플랫폼을 기록한다.
7. repository / issue / comment 모듈은 원격 호출이 필요할 때 platform 모듈을 호출한다.
8. platform 모듈은 connection 모듈에서 token access를 얻어 원격 API를 호출한다.

## 책임 분리

- app: HTTP 요청 수신, 검증 결과와 저장 명령 조립
- platform: 토큰 검증, 원격 API 호출, adapter 선택
- connection: PAT 암호화 저장, 연결 상태 조회, 세션 관리, token access 제공
- repository / issue / comment: token과 baseUrl을 직접 다루지 않음

## 관리 기준

- PAT는 영구 저장소에 평문으로 보관하지 않는다.
- 운영 환경에서는 HTTPS를 기본 전제로 유지한다.
- 요청 본문, 예외 로그, 프록시 로그에 PAT가 남지 않도록 계속 점검한다.
- 암호화 키는 운영 전용 값으로 별도 관리한다.
- 업무 모듈은 PAT 복호화 방식과 플랫폼별 인증 헤더 구성을 알지 않는다.
