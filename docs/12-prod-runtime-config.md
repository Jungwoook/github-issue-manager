# Prod Runtime Config

## 목적

- 운영 환경에서 사용하는 런타임 설정과 EC2 환경 변수 기준을 한 문서로 정리한다.

## 기본 원칙

- 운영 실행은 `prod` 프로필 기준으로 맞춘다.
- 리포 설정과 실제 서버 설정이 서로 어긋나지 않도록 한 곳에서 관리한다.
- 민감 정보는 리포에 저장하지 않고 환경 변수로 주입한다.

## 주요 설정

### 필수 환경 변수

- `APP_CORS_ALLOWED_ORIGINS`
- `GITHUB_PAT_ENCRYPTION_KEY`

### 선택 환경 변수

- `GITHUB_API_BASE_URL`
- `GITLAB_API_BASE_URL`

### 예시

```bash
APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-*.vercel.app
GITHUB_PAT_ENCRYPTION_KEY=change-this-to-a-long-random-production-secret
GITHUB_API_BASE_URL=https://api.github.com
GITLAB_API_BASE_URL=https://gitlab.com/api/v4
```

## 운영 체크 포인트

- CORS 허용 Origin은 실제 프론트 주소 기준으로 유지한다.
- 세션 쿠키와 CORS 설정은 함께 맞춰서 관리한다.
- 암호화 키는 개발용 기본값이 아닌 운영 전용 값으로 관리한다.
- 플랫폼별 base URL은 운영 환경에서 검증된 값만 사용한다.
- PAT 원문은 로그, 예외 메시지, 배포 산출물에 남기지 않는다.
