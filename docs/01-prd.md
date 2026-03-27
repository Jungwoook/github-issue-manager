<!-- 무엇을 만들지 정의하는 요구사항 정의서 -->

## 1. 프로젝트 개요

이 프로젝트는 GitHub 이슈 관리 방식을 참고하여, Repository 단위로 이슈를 생성하고 관리할 수 있는 백엔드 시스템을 구축하는 것을 목표로 한다.

현재 1차 단계에서는 내부 데이터베이스 기반으로 동작하는 이슈 관리 시스템을 구현하며,
향후 GitHub API 연동을 통해 실제 GitHub repository와 연결 가능한 구조로 확장하는 것을 목표로 한다.

---

## 2. 목표

### 2.1 1차 목표

* Repository 중심의 이슈 관리 시스템 구현
* REST API 기반 백엔드 설계 및 구현
* Issue, Comment, Label 기능 제공
* 검색 및 필터 기능 제공

### 2.2 2차 목표 (확장)

* GitHub repository 연동
* GitHub issue 동기화
* GitHub comment 및 label 연동

---

## 3. 주요 사용자 문제

현재 문제 상황

* 프로젝트 내 작업을 체계적으로 관리하기 어려움
* 이슈 단위로 작업을 추적하기 어려움
* 협업 시 진행 상황 공유가 어려움

해결 방향

* Repository 단위로 작업을 구조화
* Issue를 통해 작업 단위 관리
* Comment로 진행 상황 기록
* Label로 이슈 분류

---

## 4. 핵심 기능

### 4.1 Repository 관리

* Repository 생성
* Repository 조회
* Repository 수정
* Repository 삭제

### 4.2 Issue 관리

* Issue 생성
* Issue 조회 (목록/단건)
* Issue 수정
* Issue 삭제
* Issue 상태 변경 (OPEN/CLOSED)
* Issue 우선순위 변경 (LOW/MEDIUM/HIGH)

### 4.3 Comment 관리

* Comment 작성
* Comment 조회
* Comment 삭제

### 4.4 Label 관리

* Label 생성
* Label 조회
* Issue에 Label 추가
* Issue에서 Label 제거

### 4.5 검색 및 필터

* 제목 기반 검색
* 상태 기반 필터
* 우선순위 기반 필터
* Label 기반 필터

---

## 5. 사용자 시나리오

### 시나리오 1: 프로젝트 시작

사용자는 새로운 Repository를 생성한다.

### 시나리오 2: 작업 등록

사용자는 특정 Repository에 Issue를 생성한다.

### 시나리오 3: 작업 진행

사용자는 Issue 상태를 변경하거나 댓글을 추가하여 진행 상황을 기록한다.

### 시나리오 4: 작업 완료

사용자는 Issue 상태를 CLOSED로 변경한다.

### 시나리오 5: 이슈 관리

사용자는 Label을 통해 이슈를 분류하고, 검색 및 필터 기능으로 필요한 이슈를 빠르게 찾는다.

---

## 6. 기능 요구사항

### 6.1 Repository

* 이름은 필수 입력값이다
* 여러 Repository 생성 가능
* 삭제 시 하위 데이터 함께 삭제

### 6.2 Issue

* Repository에 종속된다
* 제목은 필수 입력값
* 생성 시 기본 상태는 OPEN
* 수정 및 삭제 가능

### 6.3 Comment

* Issue에 종속된다
* 내용은 필수 입력값

### 6.4 Label

* Repository 단위로 관리된다
* 동일 Repository 내 이름 중복 불가
* Issue와 다대다 관계

### 6.5 검색

* 제목 부분 검색 가능
* 상태, 우선순위, Label 필터 지원

---

## 7. 비기능 요구사항

* RESTful API 설계
* 계층형 아키텍처 적용
* DTO 기반 데이터 전달
* Global Exception Handling 적용
* 로깅 적용
* 확장 가능한 구조 설계

---

## 8. 제약 사항

* 사용자 인증 기능은 1차 범위에서 제외
* 외부 GitHub API 연동은 포함하지 않음
* 내부 DB 기반으로만 동작

---

## 9. 성공 기준

* Repository 기반으로 Issue 관리 가능
* Issue 상태 및 우선순위 변경 가능
* Comment 및 Label 기능 정상 동작
* 검색 및 필터 기능 정상 동작
* API가 일관된 구조로 동작

---

## 10. 향후 확장 계획

* GitHub API 연동
* 사용자 인증 및 권한 관리
* 담당자 기능 추가
* 알림 기능 추가
* 대시보드 기능 추가

---

## 11. 요약

이 프로젝트는 GitHub 이슈 관리 방식을 참고한 내부 이슈 관리 시스템이다.

1차 단계에서는 내부 도메인과 API 설계에 집중하며,
향후 GitHub API 연동을 통해 실서비스형 백엔드로 확장 가능한 구조를 만드는 것을 목표로 한다.

