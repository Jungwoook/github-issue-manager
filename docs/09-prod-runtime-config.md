# Prod Runtime Config

## 목적

- 운영 환경에서 사용하는 핵심 설정과 EC2/프론트 배포 환경 변수를 정리한다.

## 기본 원칙

- 운영 실행은 `prod` 프로필 기준으로 맞춘다.
- CORS, PAT 암호화 키, 외부 API base URL은 코드 고정값이 아니라 환경 설정으로 관리한다.
- GitLab self-managed 대응은 연결별 `baseUrl`을 우선 사용한다.

## 주요 설정

### 필수 환경 변수

- `APP_CORS_ALLOWED_ORIGINS`
- `GITHUB_PAT_ENCRYPTION_KEY`

### 선택 환경 변수

- `GITHUB_API_BASE_URL`

### GitLab base URL

- GitLab 연결 요청의 `baseUrl`로 저장한다.
- 미입력 시 `https://gitlab.com/api/v4`를 사용한다.
- 서버는 HTTPS와 `/api/v4` 경로를 검증/정규화한다.

### 예시

```bash
APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-*.vercel.app
GITHUB_PAT_ENCRYPTION_KEY=change-this-to-a-long-random-production-secret
GITHUB_API_BASE_URL=https://api.github.com
```

## 운영 체크 포인트

- CORS 허용 Origin은 실제 프론트 주소 기준으로 설정한다.
- 세션 쿠키 설정은 CORS 설정과 함께 검증한다.
- 암호화 키는 개발 기본값이 아닌 운영 전용 값으로 관리한다.
- GitLab self-managed URL은 HTTPS만 허용한다.
- PAT와 base URL 입력값이 로그에 남지 않는지 확인한다.
