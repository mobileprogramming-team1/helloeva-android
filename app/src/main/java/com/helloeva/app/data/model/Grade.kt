package com.helloeva.app.data.model

import kotlinx.serialization.Serializable

/** OPIc grades, highest first. The API sends/receives the enum name ("IM2"). */
@Serializable
enum class Grade(val label: String) {
    AL("Advanced Low"),
    IH("Intermediate High"),
    IM3("Intermediate Mid 3"),
    IM2("Intermediate Mid 2"),
    IM1("Intermediate Mid 1"),
    IL("Intermediate Low"),
    NH("Novice High"),
    NM("Novice Mid"),
    NL("Novice Low"),
}
