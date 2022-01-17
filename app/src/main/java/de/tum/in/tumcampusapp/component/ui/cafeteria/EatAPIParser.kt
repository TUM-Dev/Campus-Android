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
            var calendarWeek: Short
            var date: DateTime?
            var dishName: String
            var dishType: String
            var dishLabels: String

            response.cafeterias.forEach { cafeteria: CafeteriaData ->
                cafeteriaId = cafeteria.cafeteriaId

                cafeteria.menusByWeeks.forEach { weeklyMenu: WeeklyMenu ->
                    calendarWeek = weeklyMenu.weekOfYear

                    weeklyMenu.dishesForWeek.forEach { dailyMenu: DailyMenu ->
                        date = dailyMenu.date

                        dailyMenu.dishesForDay.forEach { dish: Dish ->
                            dishName = dish.name
                            dishType = dish.type
                            dishLabels = dish.labels.toString()

                            // Set id to 0 so that room will autogenerate the primary key
                            menus.add(CafeteriaMenu(
                                    id = 0,
                                    cafeteriaId = cafeteriaId,
                                    date = date,
                                    dishType = dishType,
                                    name = dishName,
                                    labels = dishLabels,
                                    calendarWeek = calendarWeek
                            ))
                        }
                    }
                }
            }

            return menus
        }

        /***
         * @param response List of CafeteriaMetadata from the EatAPI /canteens.json endpoint
         * @return a list of all extracted Cafeterias
         */
        fun parseCafeteriaFrom(response: List<CafeteriaMetadata>) : List<Cafeteria> {
            val cafeterias: MutableList<Cafeteria> = mutableListOf()

            var id: String
            var name: String
            var address: String
            var latitude: Double
            var longitude: Double

            response.forEach { cafeteriaMetadata: CafeteriaMetadata ->
                id = cafeteriaMetadata.id
                name = cafeteriaMetadata.name

                address = cafeteriaMetadata.geoMetadata.address
                latitude = cafeteriaMetadata.geoMetadata.latitude
                longitude = cafeteriaMetadata.geoMetadata.longitude

                cafeterias.add(Cafeteria(
                        id = id,
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