# PAT 관리 흐름

## 목적

- 사용자가 입력한 플랫폼 PAT가 어디서 검증되고 저장되며, 이후 요청에서 어떻게 사용되는지 정리한다.

## 현재 흐름

1. 프론트엔드에서 플랫폼과 PAT를 입력한다.
2. GitLab은 선택적으로 base URL을 입력한다.
3. 백엔드가 `PlatformType`에 맞는 gateway를 선택한다.
4. gateway가 외부 플랫폼의 현재 사용자 API로 토큰을 검증한다.
5. 검증된 PAT는 암호화되어 `platform_connections`에 저장된다.
6. 세션에는 `currentUserId`, `currentPlatform`이 저장된다.
7. 이후 저장소, 이슈, 댓글 요청은 세션과 저장된 암호화 PAT를 사용한다.

## 플랫폼별 처리

### GitHub

- base URL은 별도로 저장하지 않는다.
- 기본 GitHub REST API를 사용한다.
- 기본 token scope 표기는 `fine-grained`로 저장한다.

### GitLab

- base URL을 저장한다.
- 입력이 없으면 `https://gitlab.com/api/v4`를 사용한다.
- 입력 URL은 HTTPS만 허용한다.
- `/api/v4` 누락 시 자동 보정한다.
- query, fragment가 포함된 URL은 거부한다.
- 기본 token scope 표기는 `api`로 저장한다.

## 관리 기준

- PAT는 영구 저장소에 평문으로 보관하지 않는다.
- 운영 환경에서는 HTTPS를 기본 전제로 사용한다.
- 요청 본문, 예외 로그, 프론트 로그에 PAT가 남지 않도록 관리한다.
- 암호화 키는 운영 전용 환경 변수로 별도 관리한다.

## 남은 보강

- `platform + baseUrl + externalUserId` 기준의 연결 unique 제약 검토
- self-managed GitLab 환경별 오류 메시지 정리
- 다중 플랫폼을 동시에 연결한 사용자 흐름 점검
