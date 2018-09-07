package de.tum.`in`.tumcampusapp.api.app.model

import com.google.gson.annotations.SerializedName

/**
 * @param fcmToken can be null/uploaded/invalid
 * @param publicKey can be null/uploaded/verified
 * @param studentId true if uploaded
 * @param employeeId true if uploaded
 * @param externalId true if uploaded
 */
data class UploadStatus(
        @SerializedName("fcm_token") var fcmToken: String = "",
        @SerializedName("public_key") var publicKey: String = "",
        @SerializedName("student_id") var studentId: Boolean = false,
        @SerializedName("employee_id") var employeeId: Boolean = false,
        @SerializedName("external_id") var externalId: Boolean = false
) {

    companion object {
        const val UPLOADED = "uploaded"
        const val VERIFIED = "verified"
    }

}
