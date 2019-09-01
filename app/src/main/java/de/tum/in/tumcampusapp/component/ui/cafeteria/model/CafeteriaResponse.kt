package de.tum.`in`.tumcampusapp.component.ui.cafeteria.model

import com.google.gson.annotations.SerializedName

data class CafeteriaResponse(
    @SerializedName("mensa_menu")
    val menus: List<CafeteriaMenu>,
    @SerializedName("mensa_beilagen")
    val sideDishes: List<CafeteriaMenu>
)