**기능**

- 로그인
- 온보딩 (시간 남으면)
- 대화 목록
- 서베이 선택 → 모의고사 (15 문제별 피드백) → 최종 피드백
- 설정

**필요한 기술요소**

- 프론트 : Kotlin, Jetpack Compose
- 백엔드 : Kotlin, Spring Boot
- Open AI API
    - STT: 사용자 음성 → 텍스트
    - TTS: Eva 질문 음성 생성
    - LLM(GPT): 질문 생성, 답변 분석, 종합 피드백
- 인프라 : GCP
- 데이터베이스 : MySQL

