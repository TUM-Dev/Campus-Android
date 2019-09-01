package de.tum.`in`.tumcampusapp.component.tumui.tutionfees

import android.content.Context
import de.tum.`in`.tumcampusapp.api.tumonline.CacheControl
import de.tum.`in`.tumcampusapp.api.tumonline.TUMOnlineClient
import de.tum.`in`.tumcampusapp.component.notifications.NotificationScheduler
import de.tum.`in`.tumcampusapp.component.notifications.ProvidesNotifications
import de.tum.`in`.tumcampusapp.component.notifications.persistence.NotificationType
import de.tum.`in`.tumcampusapp.component.tumui.tutionfees.model.Tuition
import de.tum.`in`.tumcampusapp.component.ui.overview.card.Card
import de.tum.`in`.tumcampusapp.component.ui.overview.card.ProvidesCard
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException
import java.util.*

/**
 * Tuition manager, handles tuition card
 */
class TuitionFeeManager(private val context: Context) : ProvidesCard, ProvidesNotifications {

    override fun getCards(cacheControl: CacheControl): List<Card> {
        val results = ArrayList<Card>()
        val tuition = loadTuition(cacheControl) ?: return results

        val card = TuitionFeesCard(context, tuition)
        card.getIfShowOnStart()?.let { results.add(it) }
        return results
    }

    override fun hasNotificationsEnabled(): Boolean {
        return Utils.getSettingBool(context, "card_tuition_fee_phone", true)
    }

    fun loadTuition(cacheControl: CacheControl): Tuition? {
        try {
            val response = TUMOnlineClient
                    .getInstance(context)
                    .getTuitionFeesStatus(cacheControl)
                    .execute()
            if (!response.isSuccessful) {
                return null
            }

            val tuitionList = response.body()
            if (tuitionList == null || tuitionList.tuitions.isEmpty()) {
                return null
            }

            val tuition = tuitionList.tuitions[0]
            if (!tuition.isPaid && hasNotificationsEnabled()) {
                scheduleNotificationAlarm(tuition)
            }
            return tuition
        } catch (e: IOException) {
            Utils.log(e)
            return null
        }
    }

    private fun scheduleNotificationAlarm(tuition: Tuition) {
        val notificationTime = TuitionNotificationScheduler.getNextNotificationTime(tuition)
        val scheduler = NotificationScheduler(context)
        scheduler.scheduleAlarm(NotificationType.TUITION_FEES, notificationTime)
    }
}
