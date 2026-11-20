# Hello Eva — Frontend Guide

Android app (Kotlin + Jetpack Compose) for the AI OPIc mock test.
Status: every screen runs end-to-end on **fake data**. Audio, Kakao login and the real API are TODOs.
Backend API spec: https://mobileprogramming-team1.github.io/helloeva-backend/ (`openapi.yaml`, v0.1.0). The server address is not set yet ("배포 후 추가").

> Korean version: `docs/frontend-plan.ko.md`. If the two differ, this English file is the reference.

## Run it

1. Open the project folder in Android Studio (Windows).
   **Keep the project on the Windows drive** (e.g. `C:\Users\<you>\AndroidStudioProjects\HelloEva`).
   Gradle on Windows cannot lock files inside `\\wsl$\...`, so a project stored in WSL fails with
   `java.io.IOException: Incorrect function`.
2. Run the `app` configuration on an emulator or phone (minSdk 26).
3. Flow: Kakao button (fake login) → onboarding → home → 새 대화 시작 → grade → survey 1–8 → start → chat.

Versions: AGP 9.4.1 (built-in Kotlin 2.2.10), Gradle 9.6.0, compileSdk/targetSdk 37, Compose BOM 2025.06.00.

## Project layout

```
app/src/main/java/com/helloeva/app/
├── HelloEvaApp.kt            AppContainer (manual DI): swap Fake → real repository here
├── MainActivity.kt
├── data/
│   ├── model/                Grade, Feedback (개별/전체 피드백), Conversation, Survey (8 pages)
│   └── repository/           ConversationRepository (interface) + FakeConversationRepository
└── ui/
    ├── theme/                Color.kt (palette + CTA color), Type.kt (Pretendard), Theme.kt
    ├── components/           Shared UI: buttons, chips, bubbles, feedback cards, Eva robot, icons
    ├── navigation/           HelloEvaNavHost.kt: all routes and the flow
    └── screens/
        ├── login/            스플래시 + Kakao login
        ├── onboarding/       온보딩 1–3
        ├── home/             대화 목록 (list / empty) + HomeViewModel
        ├── settings/         설정
        ├── create/           목표 등급 + 배경 설문 1–8 (one shared CreateConversationViewModel)
        ├── start/            모의고사 시작 안내
        └── chat/             대화 상세: ChatViewModel state machine + ChatScreen
```

Every component and most screens have `@Preview`s, so you can check them without running the app.

## Design tokens (`ui/theme/`)

| Token | Value | Used for |
|---|---|---|
| Blue | `#1E45FB` | Primary buttons, user bubble, selected rows/chips, progress |
| Lime | `#CDF22B` | Expected-grade chip, highlights, home banner button (always with black text) |
| BlueSoft | `#E8EDFF` | Selected row background, keyword chips, survey summary bar |
| Ink | `#141414` | Text |
| Gray50 / Gray100 / Gray200 | `#F5F6F8` / `#F2F3F5` / `#E8EAEE` | Cards, Eva bubble, dividers/tracks |
| Record | `#E5383B` | Recording state only |
| Kakao | `#FEE500` + `#191919` | Kakao button (brand rule, don't change) |

- **Main button color**: `EvaColors.CtaContainer` / `CtaContent` in `Color.kt` (blue + white now; lime + ink is the alternative).
- Font: Pretendard (OFL) in `res/font`. Radius: rows 16dp, cards 20dp, buttons/chips pill.

## Chat state machine (`ChatViewModel`)

```
ASKING ──(audio ends)──> READY ──(mic)──> RECORDING ──(stop / 2:00)──> ANALYZING ──> FEEDBACK
   ^                       │ replay (1×, POST …/replay first)                           │
   └───────────────────────┘                                                            │
   next question <──────────────────────── "다음 문제" ────────────────────────────────┤
              currentQuestionNumber == null: "전체 피드백 보기" → FINAL_LOADING → FINISHED
```

- The question text stays hidden until FEEDBACK (audio only while answering; the API sends `questionText: null` until the question is answered).
- One replay per question, counted by the **server**: call `POST …/replay`, then replay `questionAudioUrl`. Replay is disabled while recording.
- Recording auto-stops at **2:00**: the API accepts at most 2 minutes and 10 MB (m4a / aac / wav).
- The next question always comes from the server's `currentQuestionNumber`, not from a local counter.
- Opening a conversation from Home: `GET /conversations/{id}`, then
  - 진행중 + `currentQuestionNumber` set → resume at that question (answered questions shown as text + analysis)
  - 진행중 + `currentQuestionNumber == null` → show "전체 피드백 보기" (`POST …/overall-feedback`)
  - 완료 → read-only, final feedback from `GET …/overall-feedback`

## API (real spec, v0.1.0)

Source: https://mobileprogramming-team1.github.io/helloeva-backend/ (Swagger UI over `openapi.yaml`).
Every call except login needs `Authorization: Bearer <accessToken>`. Grades are enum names (`NL` … `AL`).

| Method | Path | Request → Response | Screen |
|---|---|---|---|
| POST | `/auth/login` | `{ kakaoAccessToken }` → `{ accessToken, isNewUser }` (no auth) | Login |
| POST | `/auth/logout` | → 204 | Settings |
| POST | `/auth/withdraw` | → 204 | Settings |
| GET | `/users/me` | → `{ userId, nickname, provider: "KAKAO" }` | Settings |
| GET | `/background-survey-steps` | → `{ minTotalSelections: 12, steps[] }` | Survey |
| POST | `/conversations` | `{ targetGrade, backgroundSurvey }` → 201 `{ conversationId }` (**slow**: builds 15 questions + audio) | Create |
| GET | `/conversations?status=` | → `{ totalCount, items[] }` (newest first, no paging) | Home |
| GET | `/conversations/{id}` | → `{ …, status, totalQuestionCount, currentQuestionNumber, questions[15] }` | Chat |
| POST | `/conversations/{id}/questions/{n}/replay` | → `{ questionNumber, replayRemaining, questionAudioUrl }` | Chat |
| POST | `/conversations/{id}/questions/{n}/answer` | multipart `audio` → 201 `{ questionNumber, questionText, answer, feedback, currentQuestionNumber }` (**slow**: STT + analysis) | Chat |
| POST | `/conversations/{id}/overall-feedback` | → 201 created / 200 already existed (**slow**); conversation becomes `COMPLETED` | Chat |
| GET | `/conversations/{id}/overall-feedback` | → overall feedback (only after it was created) | Chat (완료) |

**Shapes**

```jsonc
// GET /conversations → items[]
{ "conversationId": 42, "round": 2, "targetGrade": "AL", "status": "IN_PROGRESS",
  "overallEstimatedGrade": null,               // set when COMPLETED
  "createdAt": "2026-11-18T10:00:00+09:00" }   // ISO → format as "2026년 11월 18일"

// GET /conversations/{id} → questions[] (one item)
{ "questionNumber": 1, "questionAudioUrl": "https://…/1.mp3", "questionAudioDurationSeconds": 9,
  "questionText": null, "replayRemaining": 1,  // text/answer/feedback are null until answered
  "answer": { "transcript": "…", "audioUrl": "…", "durationSeconds": 48, "answeredAt": "…" },
  "feedback": { "estimatedGrade": "IM2", "targetGrade": "AL", "improvementTips": ["…"],
                "recommendedScript": "…", "keyExpressions": ["…"] } }

// overall feedback
{ "conversationId": 42, "totalQuestionCount": 15, "overallEstimatedGrade": "IM3", "targetGrade": "AL",
  "areaScores": { "fluency": 60, "vocabulary": 55, "grammar": 65, "organization": 50 },
  "strengths": [], "weaknesses": [], "recommendedStudies": [], "frequentExpressions": [],
  "closingMessage": "…" }

// POST /conversations body
{ "targetGrade": "IM3",
  "backgroundSurvey": { "job": "OFFICE_WORKER", "student": "NO", "recentCourse": "OVER_FIVE_YEARS_AGO",
                        "residence": "ALONE", "leisures": ["MOVIES", "PARKS"], "hobbies": ["READING"],
                        "sports": ["JOGGING"], "travels": ["DOMESTIC_TRAVEL"] } }

// every error
{ "status": 409, "code": "REPLAY_LIMIT_EXCEEDED", "message": "다시 듣기 횟수를 모두 사용했습니다.",
  "errors": [ { "field": "backgroundSurvey.leisures", "message": "최소 2개를 선택해야 합니다." } ] }
```

**Rules that change the app**

- **Token:** valid 30 days, no refresh. On 401 `UNAUTHORIZED` / `EXPIRED_TOKEN` / `INVALID_TOKEN` → clear the token and go to login.
- **New user:** `isNewUser == true` → onboarding. This can replace the local `onboardingDone` flag.
- **Survey:** build the pages from `GET /background-survey-steps` and send each option's `code`, not the Korean label.
  Step keys: `job, student, recentCourse, residence, leisures, hobbies, sports, travels`. `SINGLE` → one string, `MULTIPLE` → array.
  Minimums: leisures 2, hobbies/sports/travels 1, those four together 12.
  **"운동을 전혀 하지 않음" (`NONE`) can be picked together with other sports**, so remove our `exclusiveOption` rule.
- **Slow calls** (create conversation, answer, overall feedback): long timeouts and a loading state. Ask the backend how long they take.
- **Show `message`** from the error body to the user (it's written for users); 400 `INVALID_INPUT_VALUE` has per-field `errors`.

**Field names: API → our models** (map DTOs to the existing models so screens don't change)

| Our model | API |
|---|---|
| `Conversation.id / resultGrade / currentQuestion` | `conversationId / overallEstimatedGrade / currentQuestionNumber` (only in the detail) |
| `AnswerFeedback.expectedGrade / developTips / keywords` | `estimatedGrade / improvementTips / keyExpressions` |
| `AnswerResult.answerText` | `answer.transcript` |
| `FinalFeedback.overallGrade / scores / improvements / recommendations` | `overallEstimatedGrade / areaScores / weaknesses / recommendedStudies` |
| `AreaScores.structure` | `organization` |

**Proposed `ConversationRepository` (to agree on before splitting)**

```kotlin
interface ConversationRepository {
    suspend fun getConversations(status: ConversationStatus? = null): List<Conversation>
    suspend fun getSurveySteps(): SurveySteps
    suspend fun createConversation(targetGrade: Grade, survey: BackgroundSurvey): Long      // conversationId
    suspend fun getConversationDetail(conversationId: Long): ConversationDetail             // 15 questions + currentQuestionNumber
    suspend fun replayQuestion(conversationId: Long, questionNumber: Int): ReplayResult
    suspend fun submitAnswer(conversationId: Long, questionNumber: Int, audio: File): AnswerResult // + next currentQuestionNumber
    suspend fun createOverallFeedback(conversationId: Long): FinalFeedback
    suspend fun getOverallFeedback(conversationId: Long): FinalFeedback
}
// plus AuthRepository: login(kakaoAccessToken), logout(), withdraw(), getMe()
```

Open questions for the backend: server address and deploy date; how long the three slow calls take (for timeouts).
OpenAI is called **only from the backend**; never put an API key in the app.

## TODO (next steps)

| Area | Where | What |
|---|---|---|
| Repository interface | `data/repository/` | Change it to the proposal above, update `FakeConversationRepository` to API-shaped data |
| Kakao login | `LoginScreen`, new `AuthRepository` | Kakao SDK → `POST /auth/login` → save token (DataStore), `isNewUser` → onboarding |
| Persistence | `AppContainer` | DataStore for the token (auto-login) |
| Survey from API | `CreateConversationViewModel`, `SurveyScreen` | Load steps, send codes, drop the exclusive "none" rule |
| TTS playback | `ChatViewModel.playQuestion()` | Media3 ExoPlayer with `questionAudioUrl`, progress from the player |
| Replay | `ChatViewModel.replay()` | `POST …/replay`, then play again; handle 409 |
| Recording | `ChatViewModel.startRecording()/stopRecording()` | MediaRecorder → m4a in cache dir, **max 2:00**, pass the file to `submitAnswer` |
| Real API | `data/repository/` | Retrofit `RemoteConversationRepository` + DTO mappers, swap in `AppContainer` |
| Errors | all ViewModels | error state + retry; 401 → login; show the server `message` |
| Settings | `SettingsScreen` | `GET /users/me` nickname, terms pages, logout / withdraw (204) clear the token |
| Mic denied | `ChatScreen` | explain why the mic is needed |

## Team split (2 frontend devs)

Already done (shared base): project setup, theme, shared components, every screen, navigation and the
chat state machine, all on `FakeConversationRepository`. The work below replaces the fakes with real features.
First step together: agree on the `ConversationRepository` proposal above; then the two parts can be built separately.

### Part A: Account, data, API (owner: TBD)

| Task | Where |
|---|---|
| Kakao login: Kakao SDK → `POST /auth/login` → save token, `isNewUser` → onboarding, auto-login | `LoginScreen`, new `AuthRepository`, `AppContainer` |
| Real API: Retrofit `RemoteConversationRepository` (all endpoints) + DTO → model mappers, swap it in for the fake | `data/repository/`, `AppContainer` |
| Auth header on every call; 401 → clear token, back to login | Retrofit interceptor |
| Survey from `GET /background-survey-steps`, send codes, remove the exclusive "none" rule | `CreateConversationViewModel`, `SurveyScreen` |
| Create conversation (`POST /conversations`, slow): loading state, then open the chat | `CreateConversationViewModel`, `StartScreen` |
| Home list from `GET /conversations` (ISO date → "2026년 11월 18일", status filter) | `HomeViewModel` |
| Settings: `GET /users/me`, terms pages, logout / withdraw | `SettingsScreen`, `HelloEvaNavHost` |
| Error state + retry on home / create / settings | `HomeViewModel`, `CreateConversationViewModel` |

### Part B: Mock test (chat) and audio (owner: TBD)

| Task | Where |
|---|---|
| Chat flow from `GET /conversations/{id}`: resume at `currentQuestionNumber`, read-only 완료 | `ChatViewModel` |
| TTS playback: Media3 ExoPlayer plays `questionAudioUrl`, progress from the player | `ChatViewModel.playQuestion()` |
| Replay: `POST …/replay` (server allows 1), then play again; handle 409 | `ChatViewModel.replay()` |
| Recording: MediaRecorder → m4a in cache dir, auto-stop at **2:00**, pass the file to `submitAnswer` | `ChatViewModel.startRecording()/stopRecording()` |
| Mic permission: request it, explain why when denied | `ChatScreen` |
| Answer upload (slow) → analyzing state → per-question feedback; next question from the response | `ChatScreen`, `FeedbackCards` |
| Overall feedback: `POST …/overall-feedback` after Q15, `GET` for 완료 conversations | `ChatViewModel` |
| Error state + retry for upload and network failures in the chat | `ChatViewModel` |

**Shared:** `ui/theme`, `ui/components`, keep `@Preview`s working. Onboarding polish if time allows.
Part B can work on the fake repository until Part A's real repository is swapped in.
