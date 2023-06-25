package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.text.SpannableString
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import java.util.regex.Pattern


class CafeteriaMenuFormatter(private val context: Context) {

    fun formatIngredientsInfo(stringId: Int): SpannableString {
        val ingredientsInfoString = context.getString(stringId)
        val processed = splitAnnotations(ingredientsInfoString)
        val replacedWithIcons = replaceWithIcons(processed)
        return SpannableString(replacedWithIcons)
    }

    private fun replaceWithIcons(string: String): String {
        return icons.entries.fold(string) { acc, entry -> acc.replace(entry.key, entry.value) }
    }

    fun format(menu: CafeteriaMenu, replaceWithImages: Boolean = false): String {
        val processed = splitAnnotations(menu.name)

        return if (replaceWithImages) {
            processed + addLabels(menu.labels)
        } else {
            // Remove all parentheses from the menu
            processed.replace("\\(.*?\\)".toRegex(), "").replace("\\s+".toRegex(), " ")
        }
    }

    private fun addLabels(labels: String): String {
        val processed = labels.replace("[", "").replace("]", "")
        val labelsList = processed.split(", ")
        val labelIcons: List<String> = labelsList.map { icons.getOrDefault(it, "") }

        return labelIcons.joinToString("")
    }

    /**
     * Splits annotations such as (2,3) into distinct annotations such as (2)(3). This makes
     * replacing annotations easier later on.
     *
     * @param menu A string containing comma-separated annotations
     * @return A string without comma-separated annotations
     */
    private fun splitAnnotations(menu: String): String {
        var len: Int
        var tmp = menu

        do {
            len = tmp.length
            tmp = SPLIT_ANNOTATIONS_PATTERN.matcher(tmp).replaceFirst("($1)(")
        } while (tmp.length > len)

        return tmp
    }

    companion object {

        private val SPLIT_ANNOTATIONS_PATTERN = Pattern.compile("\\(([A-Za-z0-9]+),")

        private val icons = mapOf(
                "VEGAN" to "\uD83E\uDED1", //🫑
                "VEGETARIAN" to "\uD83E\uDD55", //🥕
                "BEEF" to "\uD83D\uDC04", //🐄
                "PORK" to "\uD83D\uDC16", //🐖
                "ALCOHOL" to "\uD83C\uDF77" //🍷
        )
    }
}
