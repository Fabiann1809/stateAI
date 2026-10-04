package com.stateai.domain.voice

import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityCategory.COLLAB
import com.stateai.domain.activity.ActivityCategory.DEEP_WORK
import com.stateai.domain.activity.ActivityCategory.OTHER
import com.stateai.domain.activity.ActivityCategory.READING
import com.stateai.domain.activity.ActivityCategory.STUDY
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class IntentParserTest {
    private val parser = IntentParser(mascotName = "Lumi")

    @ParameterizedTest(name = "{0}")
    @MethodSource("phrases")
    fun `understands the category and the activity name`(sentence: String, category: ActivityCategory?, name: String?) {
        val intent = parser.parse(sentence)

        assertEquals(category, intent.category, "category of '$sentence'")
        assertEquals(name, intent.name, "name of '$sentence'")
    }

    @Test
    fun `a clear verb and name are more certain than a bare name`() {
        val clear = parser.parse("voy a estudiar bases de datos").confidence
        val verbOnly = parser.parse("voy a leer un rato").confidence
        val nameOnly = parser.parse("bases de datos").confidence

        assertTrue(clear > verbOnly)
        assertTrue(verbOnly > nameOnly)
        assertTrue(nameOnly > 0.0)
    }

    @Test
    fun `nothing said means nothing understood`() {
        assertEquals(VoiceIntent.EMPTY, parser.parse(""))
        assertEquals(VoiceIntent.EMPTY, parser.parse("hola Lumi"))
        assertEquals(0.0, parser.parse("voy a hacer algo").confidence)
    }

    @Test
    fun `without a mascot name its greeting is not removed`() {
        assertEquals("Lumi", IntentParser(mascotName = null).parse("Lumi").name)
    }

    companion object {
        @JvmStatic
        fun phrases(): List<Arguments> = listOf(
            Arguments.of("voy a estudiar bases de datos", STUDY, "bases de datos"),
            Arguments.of("Hola Lumi, voy a leer un rato", READING, null),
            Arguments.of("a trabajar en el informe", DEEP_WORK, "informe"),
            Arguments.of("Voy a estudiar Bases de Datos.", STUDY, "Bases de Datos"),
            Arguments.of("oye lumi, quiero repasar inglés", STUDY, "inglés"),
            Arguments.of("tengo que trabajar en la tesis", DEEP_WORK, "tesis"),
            Arguments.of("me voy a poner a leer la novela", READING, "novela"),
            Arguments.of("leer", READING, null),
            Arguments.of("voy a leer un poco de El Quijote", READING, "Quijote"),
            Arguments.of("buenos días, hoy toca programar la app", DEEP_WORK, "app"),
            Arguments.of("estudiar para el examen de física", STUDY, "examen de física"),
            Arguments.of("voy a escribir el capítulo dos", DEEP_WORK, "capítulo dos"),
            Arguments.of("reunión con el equipo", COLLAB, "reunión con el equipo"),
            Arguments.of("voy a hacer la tarea de cálculo", STUDY, "tarea de cálculo"),
            Arguments.of("voy a hacer ejercicio", OTHER, "ejercicio"),
            Arguments.of("bases de datos", null, "bases de datos"),
            Arguments.of("necesito reunirme con Ana", COLLAB, "Ana"),
            Arguments.of("¡Hola! Ahora a estudiar estadística por favor", STUDY, "estadística"),
            Arguments.of("practicar guitarra media hora", STUDY, "guitarra"),
        )
    }
}
