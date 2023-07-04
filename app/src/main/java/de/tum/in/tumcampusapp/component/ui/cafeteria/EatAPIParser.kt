package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.Cafeteria
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaLocation
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.DishPrices
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.*
import org.joda.time.DateTime

class EatAPIParser {

    companion object {

        /***
         * @param response CafeteriaResponse from which menu items should be extracted
         * @return a list of all extracted CafeteriaMenuItems
         */
        fun parseCafeteriaMenuFrom(response: CafeteriaResponse, cafeteriaId: Int, cafeteriaLocation: CafeteriaLocation): List<CafeteriaMenu> {
            val menus: MutableList<CafeteriaMenu> = mutableListOf()

            val slug: String = cafeteriaLocation.toSlug()
            var date: DateTime?
            var dishName: String
            var dishType: String
            var dishLabels: String
            var dishPrices: DishPrices

            val calendarWeek: Int = response.calendarWeek


            response.dishesForWeek.forEach { dailyMenu: DailyMenu ->
                date = dailyMenu.date

                dailyMenu.dishesForDay.forEach { dish: Dish ->
                    dishName = dish.name
                    dishType = dish.type
                    dishLabels = dish.labels.toString()
                    dishPrices = dish.prices

                    // Set id to 0 so that room will autogenerate the primary key
                    menus.add(CafeteriaMenu(
                            menuId = 0,
                            cafeteriaId = cafeteriaId,
                            slug = slug,
                            date = date,
                            dishType = dishType,
                            name = dishName,
                            labels = dishLabels,
                            calendarWeek = calendarWeek,
                            dishPrices = dishPrices
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

            var slug: String
            var name: String
            var address: String
            var latitude: Double
            var longitude: Double

            // Manually set id here, because auto-generating causes the data to be inserted multiple times
            // The PK is simply increased and thus their is no conflict as the rows differ and they are inserted
            // By setting ids here a conflict is forced
            var id = 0

            response.forEach { cafeteriaMetadata: CafeteriaMetadata ->
                slug = cafeteriaMetadata.cafeteriaSlug
                name = cafeteriaMetadata.name

                address = cafeteriaMetadata.geoMetadata.address
                latitude = cafeteriaMetadata.geoMetadata.latitude
                longitude = cafeteriaMetadata.geoMetadata.longitude

                cafeterias.add(Cafeteria(
                        id = id,
                        slug = slug,
                        name = name,
                        address = address,
                        latitude = latitude,
                        longitude = longitude
                ))

                id++
            }

            return cafeterias
        }
    }
}