package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "labels")
data class Label(
        @PrimaryKey(autoGenerate = true)
        @SerializedName("labelId")
        var labelId: Int = 0,
        @SerializedName("enum_name")
        var labelName: String,
        @SerializedName("text")
        var labelText: LabelText,
        @SerializedName("abbreviation")
        var abbreviation: String
)
