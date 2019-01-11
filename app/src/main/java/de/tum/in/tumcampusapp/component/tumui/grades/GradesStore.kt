package de.tum.`in`.tumcampusapp.component.tumui.grades

import android.content.SharedPreferences
import javax.inject.Inject

data class GradesStoreState(
        val isFirstRefresh: Boolean,
        val existingGrades: List<String>
)

class GradesStore @Inject constructor(
        private val sharedPrefs: SharedPreferences
) {

    private val isFirstRefresh: Boolean
        get() = sharedPrefs.contains(KEY_GRADED_COURSES).not()

    private val gradedCourseNames: List<String>
        get() = sharedPrefs.getStringSet(KEY_GRADED_COURSES, emptySet()).toList()

    fun getGradedCourses(): GradesStoreState {
        return GradesStoreState(isFirstRefresh, gradedCourseNames)
    }

    fun storeGradedCourses(courses: List<String>) {
        sharedPrefs.edit().putStringSet(KEY_GRADED_COURSES, courses.toSet()).apply()
    }

    companion object {
        private const val KEY_GRADED_COURSES = "KEY_GRADED_COURSES"
    }

}
