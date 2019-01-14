package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import de.tum.`in`.tumcampusapp.R
import java.util.regex.Pattern

class CafeteriaMenuFormatter(private val context: Context) {

    /**
     * Converts menu text to {@link SpannableString}.
     * Replaces all (v), ... annotations with images
     *
     * @param menuResId Resource ID of the text with annotations
     * @return Spannable text with images
     */
    fun menuToSpan(menuResId: Int): SpannableString {
        return menuToSpan(context.getString(menuResId))
    }

    /**
     * Converts menu text to {@link SpannableString}.
     * Replaces all (v), ... annotations with images
     *
     * @param menu Text with annotations
     * @return Spannable text with images
     */
    fun menuToSpan(menu: String): SpannableString {
        val processedMenu = splitAnnotations(menu)
        val text = SpannableString(processedMenu)
        replaceWithImg(processedMenu, text, "(v)", R.drawable.meal_vegan)
        replaceWithImg(processedMenu, text, "(f)", R.drawable.meal_veggie)
        replaceWithImg(processedMenu, text, "(R)", R.drawable.meal_beef)
        replaceWithImg(processedMenu, text, "(S)", R.drawable.meal_pork)
        replaceWithImg(processedMenu, text, "(GQB)", R.drawable.ic_gqb)
        replaceWithImg(processedMenu, text, "(99)", R.drawable.meal_alcohol)
        return text

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

    /**
     * Replaces all annotations that cannot be replaces with images such as (1), ...
     *
     * @param menu Text to delete annotations from
     * @return Text without un-replaceable annotations
     */
    fun prepare(menu: String): String {
        val tmp = splitAnnotations(menu)
        return NUMERICAL_ANNOTATIONS_PATTERN.matcher(tmp).replaceAll("")
    }

    private fun replaceWithImg(menu: String, text: Spannable, sym: String, drawable: Int) {
        var index = menu.indexOf(sym)
        while (index >= 0) {
            val imageSpan = ImageSpan(context, drawable)
            text.setSpan(imageSpan, index, index + sym.length, 0)
            index = menu.indexOf(sym, index + sym.length)
        }
    }

    private fun splitAnnotations(menu: String): String {
        var len: Int
        var tmp = menu
        do {
            len = tmp.length
            tmp = SPLIT_ANNOTATIONS_PATTERN.matcher(tmp)
                    .replaceFirst("($1)(")
        } while (tmp.length > len)
        return tmp
    }

    companion object {
        private val SPLIT_ANNOTATIONS_PATTERN = Pattern.compile("\\(([A-Za-z0-9]+),")
        private val NUMERICAL_ANNOTATIONS_PATTERN = Pattern.compile("\\(([1-9]|10|11)\\)")
    }

}
