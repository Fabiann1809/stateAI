package com.stateai.domain.voice

import com.stateai.domain.activity.ActivityCategory

/** Spanish words the intent parser knows, already normalized (lowercase, no accents). */
internal object IntentVocabulary {
    /** Said before the request and ignored ("hola Lumi, ..."). The mascot's name is added at run time. */
    val greetings: List<List<String>> =
        phrases("buenos dias", "buenas tardes", "buenas noches", "buenas", "hola", "oye", "hey")

    /** How people start the request; they carry no meaning for the activity. */
    val leadIns: List<List<String>> = phrases(
        "me voy a poner a", "me pongo a", "ponerme a", "voy a", "vamos a", "empezar a", "empiezo a", "comenzar a",
        "tengo que", "necesito", "quiero", "me toca", "toca", "por favor", "ahorita", "ahora", "hoy", "a",
    )

    /** First verb of the request and the category it means. */
    val verbs: Map<String, ActivityCategory> = mapOf(
        "estudiar" to ActivityCategory.STUDY,
        "repasar" to ActivityCategory.STUDY,
        "aprender" to ActivityCategory.STUDY,
        "practicar" to ActivityCategory.STUDY,
        "leer" to ActivityCategory.READING,
        "trabajar" to ActivityCategory.DEEP_WORK,
        "escribir" to ActivityCategory.DEEP_WORK,
        "programar" to ActivityCategory.DEEP_WORK,
        "redactar" to ActivityCategory.DEEP_WORK,
        "disenar" to ActivityCategory.DEEP_WORK,
        "reunirme" to ActivityCategory.COLLAB,
        "hacer" to ActivityCategory.OTHER,
    )

    /** Nouns that tell the category but are part of the activity's name ("reunión con el equipo"). */
    val nouns: Map<String, ActivityCategory> = mapOf(
        "reunion" to ActivityCategory.COLLAB,
        "junta" to ActivityCategory.COLLAB,
        "llamada" to ActivityCategory.COLLAB,
        "lectura" to ActivityCategory.READING,
        "tarea" to ActivityCategory.STUDY,
        "tareas" to ActivityCategory.STUDY,
        "deberes" to ActivityCategory.STUDY,
    )

    /** Linking words between the verb and the name ("trabajar en el informe"). */
    val connectors: Set<String> = setOf(
        "en", "el", "la", "los", "las", "lo", "un", "una", "unos", "unas", "mi", "mis", "del", "de", "al", "para",
        "sobre", "con",
    )

    /** Openings that say how much, not what ("leer un poco de El Quijote"). */
    val quantities: List<List<String>> = phrases("un ratito de", "un rato de", "un poco de", "un poco del")

    /** Endings that say how long or when, not what ("leer un rato"). */
    val fillers: List<List<String>> = phrases(
        "un ratito", "un rato", "un poco", "un momento", "media hora", "una hora", "otra vez", "por favor", "ahora",
        "hoy", "algo",
    )

    private fun phrases(vararg texts: String): List<List<String>> = texts.map(::phrase)
}
