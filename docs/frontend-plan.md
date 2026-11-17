# Hello Eva — Frontend Guide

Android app (Kotlin + Jetpack Compose) for the AI OPIc mock test.
Status: every screen runs end-to-end on **fake data**. Audio, Kakao login and the real API are TODOs.

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
ASKING ──(audio ends)──> READY ──(mic)──> RECORDING ──(stop / 3:00)──> ANALYZING ──> FEEDBACK
   ^                       │ replay (1×)                                                │
   └───────────────────────┘                                                            │
   next question <──────────────────────── "다음 문제" ────────────────────────────────┤
                                     Q15: "전체 피드백 보기" → FINAL_LOADING → FINISHED
```

- The question text stays hidden until FEEDBACK (audio only while answering).
- One replay per question; replay is disabled while recording.
- Recording auto-stops at 3:00 (OPIc has no official per-answer limit; the cap keeps STT cost/upload size down).
- Opening a 진행중 conversation resumes at the next question; a 완료 one opens read-only at the final feedback.

## API contract (draft, to agree with the backend)

All grades are enum names (`"AL"`, `"IM2"`…). Scores are integers 0–100.

| Method | Path | Body → Response |
|---|---|---|
| POST | `/auth/kakao` | `{ kakaoAccessToken }` → `{ accessToken, nickname }` |
| GET | `/conversations` | → `Conversation[]` |
| POST | `/conversations` | `{ targetGrade, survey: { pageId: [options] } }` → `Conversation` |
| GET | `/conversations/{id}` | → `{ conversation, turns[], finalFeedback? }` |
| GET | `/conversations/{id}/questions/{n}` | → `{ index, text, audioUrl, durationSeconds }` |
| POST | `/conversations/{id}/questions/{n}/answer` | multipart `audio` (m4a) → `{ questionText, answerText, feedback }` |
| GET | `/conversations/{id}/final-feedback` | → `FinalFeedback` |

```json
// feedback (개별 피드백)
{ "expectedGrade": "IM2", "targetGrade": "AL",
  "developTips": ["..."], "recommendedScript": "...", "keywords": ["lined with"] }

// FinalFeedback (전체 피드백)
{ "overallGrade": "IM3", "targetGrade": "AL",
  "scores": { "fluency": 60, "vocabulary": 55, "grammar": 65, "structure": 50 },
  "strengths": [], "improvements": [], "recommendations": [],
  "frequentExpressions": [], "closingMessage": "..." }
```

Notes for the backend:
- STT + GPT can take 5–10 s: keep the answer request timeout ≥ 30 s.
- OpenAI is called **only from the backend** (never put the API key in the app).
- Survey page ids: `job, student, course, housing, leisure, hobby, sport, travel`.

## TODO (next steps)

| Area | Where | What |
|---|---|---|
| Kakao login | `LoginScreen`, new `AuthRepository` | Kakao SDK → backend `/auth/kakao` → save JWT (DataStore) |
| Persistence | `AppContainer` | DataStore for `onboardingDone` + token (auto-login) |
| TTS playback | `ChatViewModel.playQuestion()` | Media3 ExoPlayer with `audioUrl`, progress from the player |
| Recording | `ChatViewModel.startRecording()/stopRecording()` | MediaRecorder → m4a in cache dir, pass the file to `submitAnswer` |
| Real API | `data/repository/` | Retrofit `RemoteConversationRepository`, swap in `AppContainer` |
| Errors | all ViewModels | error state + retry (network, upload failure) |
| Settings | `SettingsScreen` | notice/terms/privacy pages, logout clears token, withdraw dialog + API |
| Mic denied | `ChatScreen` | explain why the mic is needed |
