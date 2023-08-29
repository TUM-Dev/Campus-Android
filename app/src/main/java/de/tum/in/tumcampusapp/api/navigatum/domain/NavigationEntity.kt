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
    var subtext: String = "",
    @SerializedName("parsed_id")
    var parsedId: String? = null
) : Serializable {

    fun getFormattedName(): String {
        return if (parsedId == null) {
            removeHighlight(name)
        } else {
            removeHighlight(parsedId!!) + " ➤ " + removeHighlight(name)
        }
    }

    fun getFormattedSubtext(): String {
        return removeHighlight(subtext)
    }

    private fun removeHighlight(field: String): String {
        /***
         * Info from NavigaTum swagger: https://editor.swagger.io/?url=https://raw.githubusercontent.com/TUM-Dev/navigatum/main/openapi.yaml
         * In future maybe there will be query parameter for this
         * "Some fields support highlighting the query terms and it uses DC3 (\x19 or \u{0019})
         * and DC1 (\x17 or \u{0017}) to mark the beginning/end of a highlighted sequence"
         */
        return field
            .replace("\u0019", "")
            .replace("\u0017", "")
            .replace("\\x19", "")
            .replace("\\x17", "")
    }

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
