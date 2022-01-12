package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.deserialization.*
import org.joda.time.DateTime

class EatAPIParser {

    companion object {

        fun parse(response: CafeteriaResponse): List<CafeteriaMenu> {
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
    }
}