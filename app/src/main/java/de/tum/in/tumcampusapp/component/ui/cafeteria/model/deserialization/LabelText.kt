package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import com.google.gson.annotations.SerializedName

data class LabelText(
        @SerializedName("DE")
        var textDe: String,
        @SerializedName("EN")
        var textEn: String
)
