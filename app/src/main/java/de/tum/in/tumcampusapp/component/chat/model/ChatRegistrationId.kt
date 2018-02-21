package de.tum.`in`.tumcampusapp.component.chat.model

import com.google.gson.annotations.SerializedName

data class ChatRegistrationId(@field:SerializedName("registration_id")
                              var regId: String = "",
                              var status: String = "",
                              var signature: String = "")
