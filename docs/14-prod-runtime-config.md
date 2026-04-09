# Prod Runtime Config Alignment

## 배경

EC2 서비스가 `--spring.config.location=file:/home/ec2-user/app/config/application-prod.yml` 로 외부 설정 파일을 직접 읽는 구조여서,
리포 안 설정과 실제 런타임 설정이 서로 어긋나도 바로 파악하기 어려웠다.

특히 CORS 같은 런타임 동작은 코드와 설정을 함께 봐야 하는데,
외부 파일 우선 구조에서는 "리포 설정은 맞는데 실제 서비스 응답은 다름" 같은 상황이 반복될 수 있다.

## 이번 변경

- 리포에 `backend/src/main/resources/application-prod.yml` 추가
- `prod` 프로필에서 사용할 CORS 허용 Origin을 내부 설정으로 관리
- Vercel 프론트와 HTTPS 백엔드 간 세션 유지를 위해 쿠키 속성 설정 추가
- GitHub API 기본 주소와 PAT 암호화 키는 계속 환경변수로 주입

## prod 설정 값

```yaml
server:
  servlet:
    session:
      cookie:
        same-site: none
        secure: true

app:
  cors:
    allowed-origins:
      - https://github-issue-manager-beta.vercel.app
  github:
    api-base-url: ${GITHUB_API_BASE_URL:https://api.github.com}
    pat-encryption-key: ${GITHUB_PAT_ENCRYPTION_KEY}
```

## EC2 서비스 정리 방향

EC2에서는 외부 `application-prod.yml` 대신 jar 내부 `application-prod.yml` 을 사용하도록 정리하는 것을 권장한다.

예시:

```bash
/usr/bin/java -jar /home/ec2-user/app/backend/build/libs/app.jar --spring.profiles.active=prod
```

즉, 다음 항목만 운영 환경에서 별도로 주입한다.

- `GITHUB_PAT_ENCRYPTION_KEY`
- 필요 시 `GITHUB_API_BASE_URL`

### 권장 systemd 예시

```ini
[Unit]
Description=GitHub Issue Manager Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user/app
EnvironmentFile=/home/ec2-user/app/config/github-issue-manager.env
ExecStart=/usr/bin/java -jar /home/ec2-user/app/backend/build/libs/app.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

### 권장 env 파일 예시

```bash
GITHUB_PAT_ENCRYPTION_KEY=change-this-to-a-long-random-production-secret
GITHUB_API_BASE_URL=https://api.github.com
```

## 세션 메모

현재 프론트는 Vercel, 백엔드는 HTTPS 주소로 분리되어 있으므로 세션 쿠키가 교차 출처 요청에서도 유지되어야 한다.

- `same-site: none`
- `secure: true`
- 프론트 요청은 `credentials: 'include'`
- 백엔드 CORS는 `allowCredentials(true)` 와 허용 Origin 설정이 함께 맞아야 한다

브라우저에서 정상 동작을 확인할 때는 아래 순서로 보면 된다.

1. `/api/github/token` 응답 헤더에 `Set-Cookie` 가 내려오는지 확인
2. `Set-Cookie` 에 `SameSite=None`, `Secure` 가 포함되는지 확인
3. 이후 `/api/repositories/refresh` 요청에 세션 쿠키가 실려 `200` 이 되는지 확인

## 문제 해결 기록

이번 이슈는 처음에는 단순 CORS 문제처럼 보였지만, 실제로는 CORS와 세션 쿠키 설정이 함께 맞아야 해결되는 문제였다.

### 1. 초기 증상

- 브라우저에서 `OPTIONS` preflight 요청이 `403 Invalid CORS request` 로 실패
- `/api/github/token` 호출 이후에도 후속 요청이 정상적으로 이어지지 않음

### 2. 1차 원인

- EC2 서비스가 외부 `application-prod.yml` 을 직접 읽는 구조여서 실제 런타임 설정 추적이 어려웠다
- 허용 Origin 설정이 기대대로 반영되지 않아 preflight 요청이 차단되었다

### 3. 1차 해결 방향

- EC2 서비스에서 외부 설정 파일 의존을 줄이고, 리포 내부 `application-prod.yml` 을 기준 설정으로 두는 방향으로 정리
- `prod` 프로필에 허용 Origin을 명시하고, systemd는 `--spring.profiles.active=prod` 중심으로 단순화

### 4. 2차 증상

- CORS preflight는 `200` 으로 통과했지만
- `/api/github/token` 은 `200` 이고 이후 `refresh`, `status`, `repositories` 요청은 `401` 이 발생

### 5. 2차 원인

- 세션 쿠키는 내려오고 있었지만 `SameSite=None; Secure` 가 없어 cross-site 요청에서 후속 전송이 되지 않았다
- 결과적으로 브라우저는 토큰 등록 직후 생성된 세션을 다음 요청에 사용하지 못했다

### 6. 최종 해결 방향

- `prod` 설정에 세션 쿠키 속성 추가
- `same-site: none`
- `secure: true`
- 프론트는 `credentials: 'include'` 유지
- 백엔드는 `allowCredentials(true)` 와 허용 Origin을 함께 맞춤

### 7. 최종 확인 기준

- preflight `OPTIONS` 가 `200`
- 응답 헤더에 `Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials: true` 포함
- `/api/github/token` 응답의 `Set-Cookie` 에 `SameSite=None`, `Secure` 포함
- 이후 `/api/repositories/refresh` 가 `200` 으로 응답

## 기대 효과

- 코드와 prod 설정을 같은 PR 범위에서 함께 검토할 수 있다.
- EC2에서 실제로 어떤 CORS 설정이 적용되는지 추적하기 쉬워진다.
- 교차 출처 요청에서도 세션 쿠키가 유지되어 후속 API 호출이 401로 끊기지 않도록 돕는다.
- 외부 설정 파일과 리포 설정 파일의 불일치로 생기는 배포 혼선을 줄일 수 있다.
