package com.stateai.domain.voice

import com.stateai.domain.activity.Activity
import com.stateai.domain.activity.ActivityCategory
import com.stateai.domain.activity.ActivityId
import com.stateai.domain.activity.ActivityName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ActivityMatcherTest {
    private val databases = activity("1", ActivityCategory.STUDY, "Bases de datos")
    private val thesis = activity("2", ActivityCategory.DEEP_WORK, "Tesis")
    private val reading = activity("3", ActivityCategory.READING, name = null)
    private val activities = listOf(databases, thesis, reading)
    private val matcher = ActivityMatcher()
    private val parser = IntentParser(mascotName = "Lumi")

    @Test
    fun `an existing activity is found by its name`() {
        assertEquals(VoiceMatch.Existing(databases), match("voy a estudiar bases de datos"))
    }

    @Test
    fun `a similar name finds the same activity`() {
        assertEquals(VoiceMatch.Existing(databases), match("voy a estudiar baces de datos"))
    }

    @Test
    fun `a bare name finds the activity in any category`() {
        assertEquals(VoiceMatch.Existing(thesis), match("tesis"))
    }

    @Test
    fun `the same name in another category is a different activity`() {
        assertEquals(VoiceMatch.New(ActivityCategory.READING, "tesis"), match("voy a leer la tesis"))
    }

    @Test
    fun `a category without name uses its unnamed activity`() {
        assertEquals(VoiceMatch.Existing(reading), match("voy a leer un rato"))
    }

    @Test
    fun `a clear request for something new creates it`() {
        assertEquals(VoiceMatch.New(ActivityCategory.STUDY, "inglés"), match("quiero repasar inglés"))
        assertEquals(VoiceMatch.New(ActivityCategory.DEEP_WORK, null), match("a trabajar"))
    }

    @Test
    fun `an unsure request goes to review instead of being created`() {
        assertEquals(VoiceMatch.Review(category = null, name = "jardinería"), match("jardinería"))
    }

    @Test
    fun `nothing understood falls back to the list`() {
        assertEquals(VoiceMatch.NotUnderstood, match("hola Lumi"))
        assertEquals(VoiceMatch.NotUnderstood, match("voy a hacer algo"))
    }

    private fun match(sentence: String) = matcher.match(parser.parse(sentence), activities)

    private fun activity(id: String, category: ActivityCategory, name: String?) =
        Activity(ActivityId(id), category, name?.let(ActivityName::of))
}
