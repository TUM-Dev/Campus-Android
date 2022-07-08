package de.tum.`in`.tumcampusapp.api.navigatum.domain

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import de.tum.`in`.tumcampusapp.component.other.general.model.Recent
import java.io.Serializable

data class NavigationEntity(
    @SerializedName("id")
    var id: String = "",
    @SerializedName("type")
    var type: String = "",
    @SerializedName("name")
    var name: String = "",
    @SerializedName("subtext")
    var subtext: String = ""
) : Serializable {

    companion object {
        @JvmStatic fun toRecent(navigationEntity: NavigationEntity, type: Int): Recent {
            val gson = Gson()
            val jsonString = gson.toJson(navigationEntity)
            return Recent(name = jsonString, type = type)
        }

        @JvmStatic
        fun fromRecent(recent: Recent): NavigationEntity {
            val gson = Gson()
            return gson.fromJson(recent.name, NavigationEntity::class.java)
        }
    }
}
