package de.tum.`in`.tumcampusapp.component.ui.chat.model

import com.google.gson.annotations.SerializedName

class ChatMember {

    var id: Int = 0
    @SerializedName("lrz_id")
    var lrzId: String? = null
    @SerializedName("display_name")
    var displayName: String? = null
    var signature: String? = null

    constructor(lrzId: String) : super() {
        this.lrzId = lrzId
    }

    constructor(id: Int, lrzId: String, displayName: String) : super() {
        this.id = id
        this.lrzId = lrzId
        this.displayName = displayName
    }

    override fun toString(): String {
        return displayName as String
    }
}
