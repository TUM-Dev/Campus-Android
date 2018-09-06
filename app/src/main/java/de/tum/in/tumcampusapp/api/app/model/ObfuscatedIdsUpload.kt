package de.tum.`in`.tumcampusapp.api.app.model

import com.google.gson.annotations.SerializedName

/**
 * @param fcmToken can be null/uploaded/invalid
 * @param publicKey can be null/uploaded/verified
 * @param studentId true if uploaded
 * @param employeeId true if uploaded
 * @param externalId true if uploaded
 */
data class ObfuscatedIdsUpload(
        @SerializedName("student_id") var studentId: String = "",
        @SerializedName("employee_id") var employeeId: String = "",
        @SerializedName("external_id") var externalId: String = "",
        var verification: TUMCabeVerification)
