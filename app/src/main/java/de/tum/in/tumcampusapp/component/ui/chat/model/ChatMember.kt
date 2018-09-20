package de.tum.`in`.tumcampusapp.component.ui.chat.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class ChatMember() : Parcelable {

    var id: Int = 0
    @SerializedName("lrz_id")
    var lrzId: String? = null
    @SerializedName("display_name")
    var displayName: String? = null
    var signature: String? = null

    constructor(lrzId: String) : this() {
        this.lrzId = lrzId
    }

    constructor(id: Int, lrzId: String, displayName: String) : this() {
        this.id = id
        this.lrzId = lrzId
        this.displayName = displayName
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        lrzId = parcel.readString()
        displayName = parcel.readString()
        signature = parcel.readString()
    }

    override fun toString(): String {
        return displayName as String
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(lrzId)
        parcel.writeString(displayName)
        parcel.writeString(signature)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<ChatMember> {
        override fun createFromParcel(parcel: Parcel): ChatMember {
            return ChatMember(parcel)
        }

        override fun newArray(size: Int): Array<ChatMember?> {
            return arrayOfNulls(size)
        }
    }
}
