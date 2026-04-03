# ngrok HTTPS 설정 가이드

## 1. 목적

도메인 없이도 EC2에서 실행 중인 백엔드를 HTTPS 주소로 노출해 프론트와 연동할 수 있도록 한다.

현재 기준:

- 백엔드: EC2에서 `8080` 포트로 실행 중
- 프론트: HTTPS 환경에서 실행
- 목적: `http://EC2:8080` 대신 `https://*.ngrok-free.app` 주소 사용

## 2. 사전 조건

- EC2에서 백엔드가 정상 실행 중이어야 한다.
- `curl http://localhost:8080/api/health` 응답이 정상이어야 한다.
- ngrok 계정이 있어야 한다.

## 3. ngrok 설치

### 3.1 EC2에 접속

```bash
ssh -i 키파일.pem ec2-user@EC2_PUBLIC_IP
```

### 3.2 ngrok 다운로드

```bash
curl -s https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-linux-amd64.tgz -o ngrok.tgz
```

### 3.3 압축 해제

```bash
tar -xvzf ngrok.tgz
```

### 3.4 실행 파일 이동

```bash
sudo mv ngrok /usr/local/bin/ngrok
```

### 3.5 설치 확인

```bash
ngrok version
```

## 4. ngrok 계정 연결

### 4.1 ngrok 대시보드에서 인증 토큰 확인

- ngrok 사이트 로그인
- `Your Authtoken` 확인

### 4.2 EC2에 토큰 등록

```bash
ngrok config add-authtoken YOUR_NGROK_AUTHTOKEN
```

## 5. 백엔드 HTTPS 터널 열기

### 5.1 8080 포트를 ngrok으로 공개

```bash
ngrok http 8080
```

### 5.2 ngrok 주소 확인

실행 후 아래와 비슷한 주소가 출력된다.

```text
https://abcde12345.ngrok-free.app
```

이 주소가 외부에서 접근할 백엔드 HTTPS 주소다.

## 6. 동작 확인

### 6.1 EC2에서 헬스 체크

```bash
curl -i https://abcde12345.ngrok-free.app/api/health
```

### 6.2 브라우저에서 확인

```text
https://abcde12345.ngrok-free.app/api/health
```

정상이면 JSON 응답이 보여야 한다.

## 7. 백엔드 CORS 설정

EC2 환경변수 파일 수정:

```bash
sudo vi /etc/github-issue-manager.env
```

예시:

```env
APP_CORS_ALLOWED_ORIGINS=https://github-issue-manager-f0h23adqb-okjungwoo-4903s-projects.vercel.app
GITHUB_PAT_ENCRYPTION_KEY=운영용_충분히_긴_랜덤_문자열
GITHUB_API_BASE_URL=https://api.github.com
```

재시작:

```bash
sudo systemctl daemon-reload
sudo systemctl restart github-issue-manager
sudo systemctl status github-issue-manager
```

## 8. 프론트 환경변수 설정

프론트 배포 환경변수:

```env
VITE_API_BASE_URL=https://abcde12345.ngrok-free.app/api
```

주의:

- ngrok 주소가 바뀌면 프론트 환경변수도 다시 바꿔야 한다.
- 프론트는 다시 배포해야 반영된다.

## 9. 운영 시 주의사항

- ngrok 주소는 고정되지 않을 수 있다.
- 테스트나 임시 연동에는 적합하지만 장기 운영용으로는 부적합하다.
- 운영 환경으로 넘어갈 때는 도메인 + 정식 HTTPS 구성을 고려하는 것이 좋다.

## 10. 전체 실행 순서 요약

1. EC2에서 백엔드 실행 확인
2. ngrok 설치
3. ngrok 인증 토큰 등록
4. `ngrok http 8080` 실행
5. 발급된 HTTPS 주소 확인
6. 프론트 `VITE_API_BASE_URL`에 ngrok 주소 반영
7. 브라우저에서 저장소/이슈/댓글 흐름 점검
