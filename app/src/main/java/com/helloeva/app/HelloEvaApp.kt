package com.helloeva.app

import android.app.Application
import com.helloeva.app.data.repository.ConversationRepository
import com.helloeva.app.data.repository.FakeConversationRepository

/** Manual dependency injection: one place that builds the app's objects. */
class AppContainer {
    // TODO: replace with the Retrofit-backed repository once the backend is ready
    val conversationRepository: ConversationRepository = FakeConversationRepository()

    // TODO: persist with DataStore (onboarding flag, auth token)
    var onboardingDone: Boolean = false
}

class HelloEvaApp : Application() {
    val container = AppContainer()
}
