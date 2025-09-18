<h1> <p align="center"> ☁️[2025 kakao X 9oorm 시즌톤]2팀 Everflow(에버플로우)☁️ </p> </h1>
<p align="center">$\huge{\rm{\color{#5ad7b7}세대를\ 잇는\ 소통의\ 흐름}}$</p>
<h2> <p align="center">🌊 Everflow 와 함께해보세요.</p> </h2>
<img width="7680" height="4320" alt="에버플로우_아키텍쳐맵" src="https://github.com/user-attachments/assets/b16beb8a-a993-4d4b-b40b-ed8805f8fb0d" />

<h2> <p align="center">“여러분은 부모님이나 자녀에 대해 얼마나 알고 계신가요?”</p></h2>  
<p align="center">
    $\rm{\color{#5ad7b7}{매일\ 함께\ 지내고\ 대화를\ 나누더라도,\ 가족\ 간의\ 생각이나\ 취향,\ 감정은\ 종종\ 놓치기\ 마련입니다.}}$<br>
    $\rm{\color{#5ad7b7}{에버플로우는\ 이러한\ 간격을\ 좁히고,\ 세대\ 간\ 이해를\ 돕는\ 따뜻한\ 연결의\ 공간입니다.}}$
</p>

<br>

## 📌 프로젝트 목표
- 세대 간 대화의 시작: 3일 간격으로 AI가 생성하는 질문으로 자연스럽게 이어지는 "대화"
- 깊어지는 유대감: 나는 몰랐던 내 가족의 자기소개부터 가치관까지 공유할 수 있는 소중한 "기회"
- 확장되는 소통: 가족내에서 약속을 편지처럼 주고받으며 오프라인으로 이어지는 "만남"

<br>


## 🚀 주요 기능
카카오 OAuth2 OIDC 기반 회원가입/로그인 <br>
JWT 기반 토큰 관리, 재발급, 로그아웃, 토큰 블랙리스트 <br>
유저 프로필 이미지 수정, 닉네임 수정 <br><br>
가족 생성 / 가족 참여를 통한 구성원 소속 / 가족 코드, 가입 승인 대기 가족 관련 기능<br>
가족 책자 답변 / 수정, 커스텀 질문 추가, 답변 / 수정 <br>
가족 메모 (구성원 모두에게 보이며, 모두가 수정 가능, 즉시반영) <br>
가족 구성원간 약속 생성, 수락 / 거절, 캘린더 월별, 일별 조회 <br>
액션 알림 (수락, 거절), 일반 알림 <br><br>
모바일 반응형 UI - 큰글씨 기능 <br>
관리자 API (Gemini 미작동 상황을 미연에 방지, 수동 토픽(오늘의 질문) 등록) <br>
Gemini 기반 생성형 "오늘의 질문" 제공 (자정 크론잡)
SSE 기반 실시간 알림, 알림 연동 토스트 메시지 제공 <br>

<br>

## 🌱 BE 기술 요약
Java 17 / MySQL 8.4.3 / Gradle <br>
openjdk version "17.0.15" (2025-04-15 LTS) <br>
springframework.boot version '3.5.5' <br>
Spring Security + OAuth2 <br>
Spring Data JPA / Spring JDBC <br>
<br>
💬 OAuth2 KAKAO OIDC Login <br>
🎮 Discord Webhooks <br>
🤖 AI-Powered "Gemini" <br>

<br>

## 🔧 DevOps 기술 요약
AWS VPC (Public, Private) <br>
AWS EC2 free-tier t3.micro(ram-swapped) <br>
AWS RDS MySQL <br>
AWS S3, ECR, IAM <br>
Docker, Docker-compose <br>
<br>
Github Actions (CI/CD pipeline) <br>
Grafana monitoring system <br>
Prometheus / Node-exportor <br>
Alert-manager / Prometheus Blackbox<br>
<img width="441" height="349" alt="스크린샷 2025-09-18 오후 2 09 23" src="https://github.com/user-attachments/assets/4b5fa577-7922-4cc2-97aa-3d1c63ee8443" />

<br>


<details>
<summary><h2>🤝 개발자 간 협업 방법(코드컨벤션, 브랜치 전략 등)</h2></summary>

## 🌿 Git 브랜치 전략
기능 개발 - feat 브랜치에서 작업 후, main 브랜치로 Pull Request.
<br>
버그 수정 - hotfix 브랜치에서 작업 후, main 브랜치로 Pull Request.
<br>
머지 전략 - 2명 이상의 BE 개발자 코드 리뷰 시 main 브랜치로 머지 가능.
<br>
간단한 기능은 1명 이상 승인 시 머지 가능.
<br>
PR올린지 24시간 경과시 승인이 없어도 CI가 안깨지면 머지 가능
<br>
CI가 깨지면, 머지 무조건 불가능
<br>

## 📝 커밋 메시지 컨벤션 (브랜치 전략 네이밍도 동일)
타입	설명
<br>
feat#이슈번호	새로운 기능 추가
<br>
hotfix#이슈번호	버그 수정
<br>
chore#이슈번호	빌드 작업, 환경 설정
<br>
refactor#이슈번호	코드 리팩토링 (기능 변경 없음)
<br>
docs#이슈번호	문서 수정
<br>

## 🔍 메서드 네이밍 컨벤션
생성 : create
<br>
수정 : update
<br>
삭제 : delete
<br>
조회 : find
<br>

## 💡 코드 컨벤션
클래스 선언부 아래 필드 작성 시 한 칸 띄우기
<br>
메서드 길이는 15줄 이하 (SRP 원칙 준수)
<br>
의미 없는 개행 제거, 개행 규칙 준수
<br>
블록 들여쓰기는 1단계로 제한
<br>
블록 띄어쓰기는 4칸, LF(Line Feed) 사용
<br>
블록 아래는 한 칸 띄우고 작성
<br>
else 사용 지양
stream 사용 시 .stream() 뒤에 줄바꿈
<br>
</details>

## 🌟 ERD
<img width="1013" height="834" alt="에버플로우_ERD" src="https://github.com/user-attachments/assets/b0e37d30-f862-424a-939d-968d5db7a9d8" />

<br>

## 📁 프로젝트 파일 구조
<img width="361" height="408" alt="스크린샷 2025-09-18 오후 1 27 29" src="https://github.com/user-attachments/assets/4d54c867-5e88-466b-99ef-aabf4d259d3c" />
[요약 버전, DDD 계층구조 기반]
<br>

<details>
<summary><h2>📁 파일 구조 상세보기(펼침)</h2></summary>
<img width="361" height="767" alt="스크린샷 2025-09-18 오후 1 27 53" src="https://github.com/user-attachments/assets/6c3e4380-fc29-455a-9b13-50456f8a948f" />
<img width="356" height="743" alt="스크린샷 2025-09-18 오후 1 28 09" src="https://github.com/user-attachments/assets/8e201189-3093-4ce6-9ce2-ede4a08620d0" />
</details>
