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

## 기대 효과

- 코드와 prod 설정을 같은 PR 범위에서 함께 검토할 수 있다.
- EC2에서 실제로 어떤 CORS 설정이 적용되는지 추적하기 쉬워진다.
- 교차 출처 요청에서도 세션 쿠키가 유지되어 후속 API 호출이 401로 끊기지 않도록 돕는다.
- 외부 설정 파일과 리포 설정 파일의 불일치로 생기는 배포 혼선을 줄일 수 있다.
