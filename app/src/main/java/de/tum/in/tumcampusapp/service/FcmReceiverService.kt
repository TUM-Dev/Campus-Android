/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.tum.`in`.tumcampusapp.service

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import de.tum.`in`.tumcampusapp.api.app.TUMCabeClient
import de.tum.`in`.tumcampusapp.component.other.general.Update
import de.tum.`in`.tumcampusapp.component.other.generic.GenericNotification
import de.tum.`in`.tumcampusapp.component.ui.alarm.AlarmNotification
import de.tum.`in`.tumcampusapp.component.ui.chat.ChatNotification
import de.tum.`in`.tumcampusapp.utils.Const
import de.tum.`in`.tumcampusapp.utils.Utils
import java.io.IOException

/**
 * This `IntentService` does the actual handling of the FCM message.
 * `FcmBroadcastReceiver` (a `WakefulBroadcastReceiver`) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls `completeWakefulIntent()` to release the
 * wake lock.
 */
class FcmReceiverService : FirebaseMessagingService() {

    /**
     * Called when message is received.
     */
    // [START receive_message]
    override fun onMessageReceived(message: RemoteMessage?) {
        Utils.log("Notification received...")
        val data = message?.data ?: return

        //Legacy messages need to be handled - maybe some data is missing?
        if (data.containsKey(PAYLOAD) && data.containsKey("type")) {
            //Get some important values
            val notification = data["notification"]?.toInt() ?: return
            val type = data["type"]?.toInt() ?: return
            val payload = data[PAYLOAD]

            //Initialize our outputs
            var genericNotification: GenericNotification? = null
            Utils.log("Notification received: $data")

            // Switch on the type as both the type and payload must be present
            when (type) {
                //https://github.com/TCA-Team/TumCampusApp/wiki/GCM-Message-format
                CHAT_NOTIFICATION -> genericNotification = ChatNotification(payload, this, notification)
                UPDATE -> genericNotification = Update(payload, this, notification)
                ALERT -> genericNotification = AlarmNotification(payload, this, notification)
                else -> {
                    // Nothing to do, just confirm the retrieved notification
                    try {
                        TUMCabeClient
                                .getInstance(this)
                                .confirm(notification)
                    } catch (e: IOException) {
                        Utils.log(e)
                    }
                }
            }

            genericNotification?.let {
                postNotification(it)
                try {
                    it.sendConfirmation()
                } catch (e: IOException) {
                    Utils.log(e)
                }

                //TODO
                //de.tum.in.tumcampusapp.managers.NotificationManager notificationManager
                //        = new de.tum.in.tumcampusapp.managers.NotificationManager(this);
                //notificationManager.replaceInto(n);
            }
        } else {
            //Try to match it as a legacy chat notification
            try {
                val bundle = Bundle().apply {
                    data.entries.forEach { entry -> putString(entry.key, entry.value) }
                }
                postNotification(ChatNotification(bundle, this, -1))
            } catch (e: Exception) {
                Utils.log(e)
            }

        }
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        Utils.log("new FCM token received")
        Utils.setSetting(this, Const.FCM_INSTANCE_ID, FirebaseInstanceId.getInstance().id)
        Utils.setSetting(this, Const.FCM_TOKEN_ID, token ?: "")
    }

    /**
     * @param genericNotification the Notification to post
     */
    private fun postNotification(genericNotification: GenericNotification?) {
        genericNotification?.let {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val note = it.notification
            if (note != null) {
                notificationManager.notify(it.notificationIdentification, note)
            }
        }
    }

    companion object {

        private const val CHAT_NOTIFICATION = 1
        private const val UPDATE = 2
        private const val ALERT = 3

        private const val PAYLOAD = "payload"

    }

}