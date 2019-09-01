package de.tum.`in`.tumcampusapp.component.tumui.lectures.activity

import android.os.Bundle
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.component.other.generic.activity.ActivityForAccessingTumOnline
import de.tum.`in`.tumcampusapp.component.tumui.lectures.adapter.LectureAppointmentsListAdapter
import de.tum.`in`.tumcampusapp.component.tumui.lectures.model.LectureAppointmentsResponse
import de.tum.`in`.tumcampusapp.utils.Const
import kotlinx.android.synthetic.main.activity_lecturesappointments.*

/**
 * This activity provides the appointment dates to a given lecture using the
 * TUMOnline web service.
 *
 *
 * HINT: a valid TUM Online token is needed
 *
 *
 * NEEDS: stp_sp_nr and title set in incoming bundle (lecture id, title)
 */
class LecturesAppointmentsActivity : ActivityForAccessingTumOnline<LectureAppointmentsResponse>(R.layout.activity_lecturesappointments) {

    private var lectureId: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvTermineLectureName.text = intent.getStringExtra(Const.TITLE_EXTRA)
        lectureId = intent.getStringExtra(Const.LECTURE_ID_EXTRA)

        if (lectureId == null) {
            finish()
            return
        }

        loadLectureAppointments(lectureId, CacheControl.USE_CACHE)
    }

    override fun onRefresh() {
        loadLectureAppointments(lectureId, CacheControl.BYPASS_CACHE)
    }

    private fun loadLectureAppointments(lectureId: String?, cacheControl: CacheControl) {
        lectureId?.let {
            fetch(apiClient.getLectureAppointments(lectureId, cacheControl))
        }
    }

    override fun onDownloadSuccessful(response: LectureAppointmentsResponse) {
        val appointments = response.lectureAppointments
        if (appointments == null || appointments.isEmpty()) {
            showError(R.string.no_appointments)
            return
        }

        lvTerminList.adapter = LectureAppointmentsListAdapter(this, appointments)
    }
}
