package de.tum.`in`.tumcampusapp.models.tumcabe

import com.google.gson.annotations.SerializedName

class ChatRegistrationId(@SerializedName("registration_id")
                         var regId: String?, var signature: String?) {
    var status: String? = null
}
