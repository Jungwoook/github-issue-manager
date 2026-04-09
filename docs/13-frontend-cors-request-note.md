# 프론트 CORS 요청 변경 메모

## 배경

프론트엔드에서 백엔드 API를 호출할 때 `fetch` 요청에 `credentials: 'include'` 가 포함되어 있었다.
테스트 시점의 백엔드 CORS 설정은 모든 Origin을 허용하되 `allowCredentials(false)` 로 동작하고 있어,
브라우저가 credential 포함 교차 출처 요청을 CORS 정책 위반으로 차단했다.

## 변경 내용

- 대상 파일: `frontend/src/shared/api/client.ts`
- `fetch` 기본 옵션에서 `credentials: 'include'` 제거

## 기대 효과

- Vercel 또는 로컬 프론트엔드에서 EC2 백엔드로 보내는 일반 API 요청이 현재 테스트용 CORS 설정과 충돌하지 않는다.

## 이후 정리 방향

- 테스트가 끝난 뒤 인증 방식이 쿠키/세션 기반으로 다시 필요해지면,
  프론트의 credential 포함 여부와 백엔드의 `allowCredentials(true)` 및 허용 Origin 제한을 함께 재조정해야 한다.
