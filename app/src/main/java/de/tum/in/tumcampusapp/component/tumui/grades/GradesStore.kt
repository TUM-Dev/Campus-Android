package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.SharedPreferences
import javax.inject.Inject

class GradesStore @Inject constructor(
    private val sharedPrefs: SharedPreferences
) {

    val gradedCourses: List<String>
        get() = sharedPrefs.getStringSet(KEY_GRADED_COURSES, emptySet())!!.toList().sorted()

    fun store(courses: List<String>) {
        sharedPrefs.edit().putStringSet(KEY_GRADED_COURSES, courses.toSet()).apply()
    }

    companion object {
        private const val KEY_GRADED_COURSES = "KEY_GRADED_COURSES"
    }
}
