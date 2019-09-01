package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import de.tum.`in`.tumcampusapp.R
import java.util.regex.Pattern

class CafeteriaMenuFormatter(private val context: Context) {

    fun format(menuResId: Int, replaceWithImages: Boolean = false): SpannableString {
        return format(context.getString(menuResId), replaceWithImages)
    }

    fun format(menu: String, replaceWithImages: Boolean = false): SpannableString {
        val processed = splitAnnotations(menu)

        return if (replaceWithImages) {
            // Replace all annotations with images
            SpannableString(processed).apply { replaceWithImages(processed, this) }
        } else {
            // Remove all parentheses from the menu
            val result = processed.replace("\\(.*?\\)".toRegex(), "").replace("\\s+".toRegex(), " ")
            SpannableString(result)
        }
    }

    private fun replaceWithImages(menu: String, spannable: SpannableString) {
        images.entries.forEach { replaceWithImage(menu, spannable, it.key, it.value) }
    }

    private fun replaceWithImage(menu: String, text: Spannable, symbol: String, imageResId: Int) {
        var index = menu.indexOf(symbol)
        while (index >= 0) {
            val imageSpan = ImageSpan(context, imageResId)
            text.setSpan(imageSpan, index, index + symbol.length, 0)
            index = menu.indexOf(symbol, index + symbol.length)
        }
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

        private val images = mapOf(
                "(v)" to R.drawable.meal_vegan,
                "(f)" to R.drawable.meal_veggie,
                "(R)" to R.drawable.meal_beef,
                "(S)" to R.drawable.meal_pork,
                "(GQB)" to R.drawable.ic_gqb,
                "(99)" to R.drawable.meal_alcohol
        )

        /* TODO Someday replace all of them:
        '2':'mit Konservierungsstoff',
        '3':'mit Antioxidationsmittel',
        '4':'mit Geschmacksverstärker',
        '5':'geschwefelt',
        '6':'geschwärzt (Oliven)',
        '7':'unbekannt',
        '8':'mit Phosphat',
        '9':'mit Süßungsmitteln',
        '10':'enthält eine Phenylalaninquelle',
        '11':'mit einer Zuckerart und Süßungsmitteln',
        '99':'mit Alkohol',
        'f':'fleischloses Gericht',
        'v':'veganes Gericht',
        'GQB':'Geprüfte Qualität - Bayern',
        'S':'mit Schweinefleisch',
        'R':'mit Rindfleisch',
        'K':'mit Kalbfleisch',
        'MSC':'Marine Stewardship Council',
        'Kn':'Knoblauch',
        '13':'kakaohaltige Fettglasur',
        '14':'Gelatine',
        'Ei':'Hühnerei',
        'En':'Erdnuss',
        'Fi':'Fisch',
        'Gl':'Glutenhaltiges Getreide',
        'GlW':'Weizen',
        'GlR':'Roggen',
        'GlG':'Gerste',
        'GlH':'Hafer',
        'GlD':'Dinkel',
        'Kr':'Krebstiere',
        'Lu':'Lupinen',
        'Mi':'Milch und Laktose',
        'Sc':'Schalenfrüchte',
        'ScM':'Mandeln',
        'ScH':'Haselnüsse',
        'ScW':'Walnüsse',
        'ScC':'Cashewnüssen',
        'ScP':'Pistazien',
        'Se':'Sesamsamen',
        'Sf':'Senf',
        'Sl':'Sellerie',
        'So':'Soja',
        'Sw':'Schwefeloxid und Sulfite',
        'Wt':'Weichtiere'
        */
    }
}
