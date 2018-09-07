package de.tum.`in`.tumcampusapp.component.ui.ticket.payload

import com.google.gson.annotations.SerializedName

data class EphimeralKey(@SerializedName("api_version")
                        var apiVersion: String = "")