# EC2 배포용 환경변수 목록

## 1. 개요

이 문서는 EC2에서 `ec2` 프로파일로 백엔드를 실행할 때 필요한 환경변수를 정리한 문서다.

EC2 전용 설정 파일은 `backend/src/main/resources/application-ec2.yml`이며, 실행 시 아래처럼 프로파일을 활성화하면 된다.

```bash
java -jar app.jar --spring.profiles.active=ec2
```

## 2. 필수 환경변수

### `APP_CORS_ALLOWED_ORIGINS`

- 설명: 백엔드 API에 접근할 수 있는 프론트엔드 Origin 목록
- 사용 위치: `app.cors.allowed-origins`
- 예시:

```bash
APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-*.vercel.app
```

여러 Origin을 허용해야 하면 쉼표로 구분해 전달하면 된다.

```bash
APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-*.vercel.app,https://admin.example.com
```

현재 프로젝트는 `allowCredentials(true)`를 사용하므로, 프로젝트 코드도 `allowedOriginPatterns` 기준으로 동작해야 한다. 이 설정이면 Vercel의 배포 URL이 바뀌어도 `github-issue-manager-` 접두사를 가진 프로젝트 배포 주소는 계속 허용할 수 있다.

### `GITHUB_PAT_ENCRYPTION_KEY`

- 설명: 사용자가 등록한 GitHub PAT를 암호화/복호화할 때 사용하는 운영 비밀키
- 사용 위치: `app.github.pat-encryption-key`
- 필수 여부: 필수
- 주의사항:

`local-dev-pat-key` 같은 개발용 값을 사용하면 안 된다. 충분히 길고 예측하기 어려운 문자열을 사용해야 하며, 운영 중 키를 바꾸면 기존에 저장된 암호화 토큰을 복호화하지 못할 수 있다.

예시:

```bash
GITHUB_PAT_ENCRYPTION_KEY=change-this-to-a-long-random-production-secret
```

## 3. 선택 환경변수

### `GITHUB_API_BASE_URL`

- 설명: GitHub API 기본 주소
- 사용 위치: `app.github.api-base-url`
- 기본값: `https://api.github.com`

기본 GitHub.com을 쓴다면 보통 따로 설정하지 않아도 된다.

예시:

```bash
GITHUB_API_BASE_URL=https://api.github.com
```

## 4. 권장 EC2 환경변수 예시

```bash
export APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-*.vercel.app
export GITHUB_PAT_ENCRYPTION_KEY=change-this-to-a-long-random-production-secret
export GITHUB_API_BASE_URL=https://api.github.com
```

## 5. 배포 체크리스트

- `SPRING_PROFILES_ACTIVE=ec2` 또는 `--spring.profiles.active=ec2`로 실행하기
- `APP_CORS_ALLOWED_ORIGINS`를 실제 프론트 도메인으로 설정하기
- `GITHUB_PAT_ENCRYPTION_KEY`를 운영 전용 비밀값으로 설정하기
- 기존 운영 데이터가 있다면 암호화 키 변경 영향 확인하기
