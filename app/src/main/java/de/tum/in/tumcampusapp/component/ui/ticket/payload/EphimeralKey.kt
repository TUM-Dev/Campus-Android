package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName


@Entity
data class EphimeralKey(@PrimaryKey
                        @SerializedName("customer_mail")
                        var customerMail: String = "",
                        @SerializedName("api_version")
                        var apiVersion: String = "")