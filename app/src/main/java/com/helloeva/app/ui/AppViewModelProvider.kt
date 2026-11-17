package com.helloeva.app.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.helloeva.app.AppContainer
import com.helloeva.app.HelloEvaApp

/** Gives ViewModel factories access to the app's [AppContainer]. */
fun CreationExtras.appContainer(): AppContainer =
    (this[APPLICATION_KEY] as HelloEvaApp).container
