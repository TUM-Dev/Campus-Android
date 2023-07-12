package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import androidx.room.Entity
import com.google.gson.annotations.SerializedName

@Entity(tableName = "labels")
data class Label(
        @SerializedName("enum_name")
        var labelName: String,
        @SerializedName("text")
        var labelText: LabelText,
        @SerializedName("abbreviation")
        var abbreviation: String
)
