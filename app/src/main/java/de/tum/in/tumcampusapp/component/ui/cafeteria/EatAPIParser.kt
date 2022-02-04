package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.*
import org.joda.time.DateTime

class EatAPIParser {

    companion object {

        /***
         * @param response CafeteriaResponse from which menu items should be extracted
         * @return a list of all extracted CafeteriaMenuItems
         */
        fun parseCafeteriaMenuFrom(response: CafeteriaResponse): List<CafeteriaMenu> {
            val menus: MutableList<CafeteriaMenu> = mutableListOf()

            var cafeteriaId: String
            var date: DateTime?
            var dishName: String
            var dishType: String
            var dishLabels: String

            val calendarWeek: Int = response.calendarWeek


            response.dishesForWeek.forEach { dailyMenu: DailyMenu ->
                date = dailyMenu.date

                dailyMenu.dishesForDay.forEach { dish: Dish ->
                    dishName = dish.name
                    dishType = dish.type
                    dishLabels = dish.labels.toString()

                    // Set id to 0 so that room will autogenerate the primary key
                    // TODO Id is not longer in any table, need to get it from the call
                    menus.add(CafeteriaMenu(
                            id = 0,
                            cafeteriaId = "mensa-garching",
                            date = date,
                            dishType = dishType,
                            name = dishName,
                            labels = dishLabels,
                            calendarWeek = calendarWeek
                    ))
                }
            }

            return menus
        }

        /***
         * @param response List of CafeteriaMetadata from the EatAPI /canteens.json endpoint
         * @return a list of all extracted Cafeterias
         */
        fun parseCafeteriaFrom(response: List<CafeteriaMetadata>): List<Cafeteria> {
            val cafeterias: MutableList<Cafeteria> = mutableListOf()

            var cafeteriaId: String
            var name: String
            var address: String
            var latitude: Double
            var longitude: Double

            response.forEach { cafeteriaMetadata: CafeteriaMetadata ->
                cafeteriaId = cafeteriaMetadata.cafeteriaId
                name = cafeteriaMetadata.name

                address = cafeteriaMetadata.geoMetadata.address
                latitude = cafeteriaMetadata.geoMetadata.latitude
                longitude = cafeteriaMetadata.geoMetadata.longitude

                cafeterias.add(Cafeteria(
                        id = 0,
                        cafeteriaId = cafeteriaId,
                        name = name,
                        address = address,
                        latitude = latitude,
                        longitude = longitude
                ))
            }

            return cafeterias
        }
    }
}