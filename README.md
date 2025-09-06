# 2025_SEASONTHON_TEAM_2_BE
[2025 kakao X groom 시즌톤] 2팀 [ Everflow ] 백엔드 레포지토리

<img width="900" height="532" alt="스크린샷 2025-09-07 오전 5 25 34" src="https://github.com/user-attachments/assets/c869b0de-ef20-404f-9ef0-c930b8468319" />

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
블록 아래 한 칸 띄우고 작성
<br>
else 사용 지양
stream 사용 시 .stream() 뒤에 줄바꿈
<br>
