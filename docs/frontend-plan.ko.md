# Hello Eva — 프론트엔드 가이드

AI OPIc 모의고사 안드로이드 앱 (Kotlin + Jetpack Compose).
현재 상태: 모든 화면이 **Fake 데이터**로 처음부터 끝까지 동작한다. 음성(재생·녹음), 카카오 로그인, 실제 API 연동은 할 일(TODO)로 남아 있다.
백엔드 API 명세: https://mobileprogramming-team1.github.io/helloeva-backend/ (`openapi.yaml`, v0.1.0). 서버 주소는 아직 미정 ("배포 후 추가").

> 영어 원본: `docs/frontend-plan.md`. 두 문서 내용이 다르면 영어 원본을 기준으로 한다.

## 실행 방법

1. Android Studio(Windows)에서 프로젝트 폴더를 연다.
   **프로젝트는 Windows 드라이브에 둔다** (예: `C:\Users\<사용자>\AndroidStudioProjects\HelloEva`).
   Windows 의 Gradle 은 `\\wsl$\...` 안의 파일을 잠글 수 없어서, WSL 에 둔 프로젝트는
   `java.io.IOException: Incorrect function` 오류로 실패한다.
2. 에뮬레이터나 휴대폰에서 `app` 구성을 실행한다 (minSdk 26).
3. 흐름: 카카오 버튼(가짜 로그인) → 온보딩 → 홈 → 새 대화 시작 → 목표 등급 → 설문 1–8 → 시작 안내 → 채팅.

버전: AGP 9.4.1 (내장 Kotlin 2.2.10), Gradle 9.6.0, compileSdk/targetSdk 37, Compose BOM 2025.06.00.

## 프로젝트 구조

```
app/src/main/java/com/helloeva/app/
├── HelloEvaApp.kt            AppContainer (수동 DI): 여기서 Fake → 실제 저장소로 교체
├── MainActivity.kt
├── data/
│   ├── model/                Grade, Feedback (개별/전체 피드백), Conversation, Survey (8단계)
│   └── repository/           ConversationRepository (인터페이스) + FakeConversationRepository
└── ui/
    ├── theme/                Color.kt (색상 + 주요 버튼 색), Type.kt (Pretendard), Theme.kt
    ├── components/           공용 UI: 버튼, 칩, 말풍선, 피드백 카드, Eva 로봇, 아이콘
    ├── navigation/           HelloEvaNavHost.kt: 전체 화면 경로와 흐름
    └── screens/
        ├── login/            스플래시 + 카카오 로그인
        ├── onboarding/       온보딩 1–3
        ├── home/             대화 목록 (목록 / 빈 화면) + HomeViewModel
        ├── settings/         설정
        ├── create/           목표 등급 + 배경 설문 1–8 (CreateConversationViewModel 하나를 공유)
        ├── start/            모의고사 시작 안내
        └── chat/             대화 상세: ChatViewModel 상태 머신 + ChatScreen
```

모든 컴포넌트와 대부분의 화면에 `@Preview` 가 있어서 앱을 실행하지 않고도 확인할 수 있다.

## 디자인 토큰 (`ui/theme/`)

| 토큰 | 값 | 사용처 |
|---|---|---|
| Blue | `#1E45FB` | 주요 버튼, 사용자 말풍선, 선택된 항목·칩, 진행 바 |
| Lime | `#CDF22B` | 예상 등급 칩, 강조, 홈 배너 버튼 (항상 검은 글씨와 함께) |
| BlueSoft | `#E8EDFF` | 선택된 항목 배경, 키워드 칩, 설문 요약 바 |
| Ink | `#141414` | 글자 |
| Gray50 / Gray100 / Gray200 | `#F5F6F8` / `#F2F3F5` / `#E8EAEE` | 카드, Eva 말풍선, 구분선·트랙 |
| Record | `#E5383B` | 녹음 중 상태에만 사용 |
| Kakao | `#FEE500` + `#191919` | 카카오 버튼 (브랜드 규칙, 변경 금지) |

- **주요 버튼 색**: `Color.kt` 의 `EvaColors.CtaContainer` / `CtaContent` (현재 파랑 + 흰 글씨, 대안은 라임 + 검은 글씨).
- 폰트: Pretendard (OFL), `res/font`. 모서리: 항목 16dp, 카드 20dp, 버튼·칩은 알약형.

## 채팅 상태 머신 (`ChatViewModel`)

```
ASKING ──(음성 끝)──> READY ──(마이크)──> RECORDING ──(정지 / 2:00)──> ANALYZING ──> FEEDBACK
   ^                    │ 다시 듣기 (1회, 먼저 POST …/replay)                          │
   └────────────────────┘                                                            │
   다음 문제 <──────────────────────── "다음 문제" ──────────────────────────────────┤
              currentQuestionNumber == null: "전체 피드백 보기" → FINAL_LOADING → FINISHED
```

- 질문 텍스트는 FEEDBACK 단계 전까지 숨긴다 (답하는 동안은 음성만. API 도 답하기 전에는 `questionText: null` 을 준다).
- 다시 듣기는 문항당 1회이고 **서버가** 횟수를 센다: `POST …/replay` 호출 후 `questionAudioUrl` 을 다시 재생. 녹음 중에는 다시 듣기 불가.
- 녹음은 **2:00** 에 자동 정지: API 가 최대 2분, 10MB 까지만 받는다 (m4a / aac / wav).
- 다음 문항 번호는 항상 서버의 `currentQuestionNumber` 를 따른다 (앱에서 따로 세지 않음).
- 홈에서 대화를 열면 `GET /conversations/{id}` 후
  - 진행중 + `currentQuestionNumber` 있음 → 그 문항부터 이어서 (답한 문항은 텍스트 + 분석으로 표시)
  - 진행중 + `currentQuestionNumber == null` → "전체 피드백 보기" 표시 (`POST …/overall-feedback`)
  - 완료 → 읽기 전용, 종합 피드백은 `GET …/overall-feedback`

## API (실제 명세, v0.1.0)

출처: https://mobileprogramming-team1.github.io/helloeva-backend/ (`openapi.yaml` 을 보여 주는 Swagger UI).
로그인을 제외한 모든 요청에 `Authorization: Bearer <accessToken>` 이 필요하다. 등급은 enum 이름(`NL` … `AL`)으로 주고받는다.

| 메서드 | 경로 | 요청 → 응답 | 화면 |
|---|---|---|---|
| POST | `/auth/login` | `{ kakaoAccessToken }` → `{ accessToken, isNewUser }` (인증 불필요) | 로그인 |
| POST | `/auth/logout` | → 204 | 설정 |
| POST | `/auth/withdraw` | → 204 | 설정 |
| GET | `/users/me` | → `{ userId, nickname, provider: "KAKAO" }` | 설정 |
| GET | `/background-survey-steps` | → `{ minTotalSelections: 12, steps[] }` | 설문 |
| POST | `/conversations` | `{ targetGrade, backgroundSurvey }` → 201 `{ conversationId }` (**오래 걸림**: 15문항 + 음성 생성) | 대화 생성 |
| GET | `/conversations?status=` | → `{ totalCount, items[] }` (최신순, 페이지 나누기 없음) | 홈 |
| GET | `/conversations/{id}` | → `{ …, status, totalQuestionCount, currentQuestionNumber, questions[15] }` | 채팅 |
| POST | `/conversations/{id}/questions/{n}/replay` | → `{ questionNumber, replayRemaining, questionAudioUrl }` | 채팅 |
| POST | `/conversations/{id}/questions/{n}/answer` | multipart `audio` → 201 `{ questionNumber, questionText, answer, feedback, currentQuestionNumber }` (**오래 걸림**: 음성 인식 + 분석) | 채팅 |
| POST | `/conversations/{id}/overall-feedback` | → 201 생성 / 200 이미 있음 (**오래 걸림**), 이후 대화는 `COMPLETED` | 채팅 |
| GET | `/conversations/{id}/overall-feedback` | → 종합 피드백 (생성된 뒤에만) | 채팅 (완료) |

**데이터 형태**

```jsonc
// GET /conversations → items[]
{ "conversationId": 42, "round": 2, "targetGrade": "AL", "status": "IN_PROGRESS",
  "overallEstimatedGrade": null,               // COMPLETED 일 때 값이 있음
  "createdAt": "2026-11-18T10:00:00+09:00" }   // ISO 시간 → "2026년 11월 18일" 로 표시

// GET /conversations/{id} → questions[] (문항 하나)
{ "questionNumber": 1, "questionAudioUrl": "https://…/1.mp3", "questionAudioDurationSeconds": 9,
  "questionText": null, "replayRemaining": 1,  // 답하기 전에는 text/answer/feedback 이 null
  "answer": { "transcript": "…", "audioUrl": "…", "durationSeconds": 48, "answeredAt": "…" },
  "feedback": { "estimatedGrade": "IM2", "targetGrade": "AL", "improvementTips": ["…"],
                "recommendedScript": "…", "keyExpressions": ["…"] } }

// 종합 피드백
{ "conversationId": 42, "totalQuestionCount": 15, "overallEstimatedGrade": "IM3", "targetGrade": "AL",
  "areaScores": { "fluency": 60, "vocabulary": 55, "grammar": 65, "organization": 50 },
  "strengths": [], "weaknesses": [], "recommendedStudies": [], "frequentExpressions": [],
  "closingMessage": "…" }

// POST /conversations 요청 바디
{ "targetGrade": "IM3",
  "backgroundSurvey": { "job": "OFFICE_WORKER", "student": "NO", "recentCourse": "OVER_FIVE_YEARS_AGO",
                        "residence": "ALONE", "leisures": ["MOVIES", "PARKS"], "hobbies": ["READING"],
                        "sports": ["JOGGING"], "travels": ["DOMESTIC_TRAVEL"] } }

// 모든 에러
{ "status": 409, "code": "REPLAY_LIMIT_EXCEEDED", "message": "다시 듣기 횟수를 모두 사용했습니다.",
  "errors": [ { "field": "backgroundSurvey.leisures", "message": "최소 2개를 선택해야 합니다." } ] }
```

**앱에서 바꿔야 하는 규칙**

- **토큰:** 유효 기간 30일, 재발급 없음. 401 `UNAUTHORIZED` / `EXPIRED_TOKEN` / `INVALID_TOKEN` 이면 토큰을 지우고 로그인 화면으로.
- **신규 사용자:** `isNewUser == true` → 온보딩. 지금의 로컬 `onboardingDone` 값을 대신할 수 있다.
- **설문:** `GET /background-survey-steps` 로 설문 화면을 만들고, 한글 문구가 아니라 각 선택지의 `code` 를 보낸다.
  단계 키: `job, student, recentCourse, residence, leisures, hobbies, sports, travels`. `SINGLE` → 문자열 하나, `MULTIPLE` → 배열.
  최소 개수: 여가 2개, 취미·운동·휴가 각 1개, 이 네 단계 합계 12개.
  **"운동을 전혀 하지 않음"(`NONE`) 은 다른 운동과 함께 고를 수 있으므로** 지금의 `exclusiveOption` 규칙을 없앤다.
- **오래 걸리는 요청** (대화 생성, 답변 제출, 종합 피드백): 타임아웃을 길게 하고 로딩 화면을 보여 준다. 몇 초 걸리는지 백엔드에 확인.
- 에러 바디의 **`message` 를 사용자에게 그대로 보여 준다** (사용자용 문구). 400 `INVALID_INPUT_VALUE` 에는 필드별 `errors` 가 있다.

**필드 이름: API → 우리 모델** (DTO 로 받아서 지금 모델로 바꾸면 화면 코드는 그대로 둘 수 있다)

| 우리 모델 | API |
|---|---|
| `Conversation.id / resultGrade / currentQuestion` | `conversationId / overallEstimatedGrade / currentQuestionNumber` (상세 조회에만 있음) |
| `AnswerFeedback.expectedGrade / developTips / keywords` | `estimatedGrade / improvementTips / keyExpressions` |
| `AnswerResult.answerText` | `answer.transcript` |
| `FinalFeedback.overallGrade / scores / improvements / recommendations` | `overallEstimatedGrade / areaScores / weaknesses / recommendedStudies` |
| `AreaScores.structure` | `organization` |

**`ConversationRepository` 변경안 (역할을 나누기 전에 함께 정할 것)**

```kotlin
interface ConversationRepository {
    suspend fun getConversations(status: ConversationStatus? = null): List<Conversation>
    suspend fun getSurveySteps(): SurveySteps
    suspend fun createConversation(targetGrade: Grade, survey: BackgroundSurvey): Long      // conversationId
    suspend fun getConversationDetail(conversationId: Long): ConversationDetail             // 15문항 + currentQuestionNumber
    suspend fun replayQuestion(conversationId: Long, questionNumber: Int): ReplayResult
    suspend fun submitAnswer(conversationId: Long, questionNumber: Int, audio: File): AnswerResult // + 다음 currentQuestionNumber
    suspend fun createOverallFeedback(conversationId: Long): FinalFeedback
    suspend fun getOverallFeedback(conversationId: Long): FinalFeedback
}
// 별도 AuthRepository: login(kakaoAccessToken), logout(), withdraw(), getMe()
```

백엔드에 물어볼 것: 서버 주소와 배포 날짜, 오래 걸리는 요청 3개의 소요 시간 (타임아웃 설정용).
OpenAI 는 **백엔드에서만** 호출한다. 앱에는 절대 API 키를 넣지 않는다.

## 할 일 (TODO)

| 영역 | 위치 | 내용 |
|---|---|---|
| 저장소 인터페이스 | `data/repository/` | 위 변경안으로 바꾸고, `FakeConversationRepository` 를 API 형태 데이터로 수정 |
| 카카오 로그인 | `LoginScreen`, 새 `AuthRepository` | 카카오 SDK → `POST /auth/login` → 토큰 저장 (DataStore), `isNewUser` → 온보딩 |
| 저장 | `AppContainer` | 토큰을 DataStore 에 저장 (자동 로그인) |
| API 기반 설문 | `CreateConversationViewModel`, `SurveyScreen` | 단계 불러오기, 코드로 전송, "운동 안 함" 배타 규칙 제거 |
| 질문 음성 재생 | `ChatViewModel.playQuestion()` | Media3 ExoPlayer 로 `questionAudioUrl` 재생, 진행률은 플레이어 기준 |
| 다시 듣기 | `ChatViewModel.replay()` | `POST …/replay` 후 다시 재생, 409 처리 |
| 녹음 | `ChatViewModel.startRecording()/stopRecording()` | MediaRecorder → 캐시 폴더에 m4a, **최대 2:00**, 파일을 `submitAnswer` 로 전달 |
| 실제 API | `data/repository/` | Retrofit `RemoteConversationRepository` + DTO 변환, `AppContainer` 에서 교체 |
| 에러 처리 | 모든 ViewModel | 에러 상태 + 재시도, 401 → 로그인, 서버 `message` 표시 |
| 설정 | `SettingsScreen` | `GET /users/me` 닉네임, 약관 화면, 로그아웃·탈퇴 (204) 시 토큰 삭제 |
| 마이크 권한 거부 | `ChatScreen` | 마이크가 왜 필요한지 안내 |

## 역할 분담 (프론트엔드 2명)

이미 완료 (공통 기반): 프로젝트 설정, 테마, 공용 컴포넌트, 모든 화면, 내비게이션, 채팅 상태 머신.
모두 `FakeConversationRepository` 로 동작한다. 아래 작업은 Fake 를 실제 기능으로 바꾸는 일이다.
먼저 함께 할 일: 위 `ConversationRepository` 변경안을 합의한다. 그 뒤에는 두 파트를 따로 작업할 수 있다.

### 파트 A: 계정 · 데이터 · API (담당: 미정)

| 작업 | 위치 |
|---|---|
| 카카오 로그인: 카카오 SDK → `POST /auth/login` → 토큰 저장, `isNewUser` → 온보딩, 자동 로그인 | `LoginScreen`, 새 `AuthRepository`, `AppContainer` |
| 실제 API: Retrofit `RemoteConversationRepository` (모든 엔드포인트) + DTO → 모델 변환, Fake 대신 교체 | `data/repository/`, `AppContainer` |
| 모든 요청에 인증 헤더, 401 → 토큰 삭제 후 로그인 화면 | Retrofit 인터셉터 |
| 설문을 `GET /background-survey-steps` 로 표시, 코드로 전송, "운동 안 함" 배타 규칙 제거 | `CreateConversationViewModel`, `SurveyScreen` |
| 대화 생성 (`POST /conversations`, 오래 걸림): 로딩 화면 후 채팅 열기 | `CreateConversationViewModel`, `StartScreen` |
| 홈 목록 `GET /conversations` (ISO 날짜 → "2026년 11월 18일", 상태 필터) | `HomeViewModel` |
| 설정: `GET /users/me`, 약관 화면, 로그아웃 / 탈퇴 | `SettingsScreen`, `HelloEvaNavHost` |
| 홈 / 대화 생성 / 설정 화면의 에러 상태 + 재시도 | `HomeViewModel`, `CreateConversationViewModel` |

### 파트 B: 모의고사(채팅) · 음성 (담당: 미정)

| 작업 | 위치 |
|---|---|
| `GET /conversations/{id}` 기반 채팅 흐름: `currentQuestionNumber` 부터 이어하기, 완료 대화는 읽기 전용 | `ChatViewModel` |
| 질문 음성 재생: Media3 ExoPlayer 로 `questionAudioUrl` 재생, 진행률은 플레이어 기준 | `ChatViewModel.playQuestion()` |
| 다시 듣기: `POST …/replay` (서버 기준 1회) 후 다시 재생, 409 처리 | `ChatViewModel.replay()` |
| 녹음: MediaRecorder → 캐시 폴더에 m4a, **2:00** 자동 정지, 파일을 `submitAnswer` 로 전달 | `ChatViewModel.startRecording()/stopRecording()` |
| 마이크 권한: 요청하고, 거부하면 이유 안내 | `ChatScreen` |
| 답변 제출 (오래 걸림) → 분석 중 화면 → 문항별 피드백, 다음 문항은 응답 기준 | `ChatScreen`, `FeedbackCards` |
| 종합 피드백: 15번 후 `POST …/overall-feedback`, 완료 대화는 `GET` | `ChatViewModel` |
| 채팅 화면의 업로드·네트워크 실패 에러 상태 + 재시도 | `ChatViewModel` |

**공통:** `ui/theme`, `ui/components`, `@Preview` 가 계속 동작하도록 유지. 시간이 되면 온보딩 다듬기.
파트 A 의 실제 저장소로 교체되기 전까지 파트 B 는 Fake 저장소로 작업할 수 있다.
