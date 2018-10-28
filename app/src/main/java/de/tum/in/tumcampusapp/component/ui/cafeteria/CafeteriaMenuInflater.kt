package de.tum.`in`.tumcampusapp.component.ui.cafeteria

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import de.tum.`in`.tumcampusapp.R
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices
import de.tum.`in`.tumcampusapp.component.ui.cafeteria.model.FavoriteDish
import de.tum.`in`.tumcampusapp.database.TcaDb
import de.tum.`in`.tumcampusapp.utils.Utils
import kotlinx.android.synthetic.main.card_list_header.view.*
import kotlinx.android.synthetic.main.card_price_line.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.regex.Pattern

class CafeteriaMenuInflater(
        private val context: Context,
        private val rootView: ViewGroup,
        private val isBigLayout: Boolean
) {

    private val inflater = LayoutInflater.from(context)

    private val rolePrices: Map<String, String> by lazy {
        CafeteriaPrices.getRolePrices(context)
    }

    private val dao: FavoriteDishDao by lazy {
        TcaDb.getInstance(context).favoriteDishDao()
    }

    fun inflate(menu: CafeteriaMenu, isFirstInSection: Boolean): View? {
        val typeShort = menu.typeShort
        val shouldShow = Utils.getSettingBool(
                context,
                "card_cafeteria_$typeShort",
                "tg" == typeShort || "ae" == typeShort
        )

        if (!isBigLayout && !shouldShow) {
            return null
        }

        if (isFirstInSection) {
            val header = inflater.inflate(R.layout.card_list_header, rootView, false)
            header.list_header.text = menu.typeLong.replace("[0-9]", "").trim()
            rootView.addView(header)
        }

        val menuView = inflater.inflate(R.layout.card_price_line, rootView, false)

        if (!isBigLayout) {
            menu.name = prepare(menu.name)
        }

        val menuSpan = menuToSpan(context, menu.name)
        menuView.line_name.text = menuSpan

        val isPriceAvailable = rolePrices.containsKey(menu.typeLong)

        if (isPriceAvailable) {
            inflateWithPrice(menuView, menu)
        } else {
            inflateWithoutPrice(menuView)
        }

        return menuView
    }

    private fun inflateWithPrice(view: View, menu: CafeteriaMenu) = with(view) {
        val price = rolePrices[menu.typeLong]
        price?.let {
            line_price.text = String.format("%s €", it)
        }

        val tag = "${menu.name}__${menu.cafeteriaId}"
        val isFavorite = dao.checkIfFavoriteDish(tag).isNotEmpty()

        favoriteDish.isSelected = isFavorite
        favoriteDish.setOnClickListener { view ->
            if (!view.isSelected) {
                val formatter = DateTimeFormat.forPattern("dd-MM-yyyy")
                val date = formatter.print(DateTime.now())
                dao.insertFavouriteDish(
                        FavoriteDish.create(menu.cafeteriaId, menu.name, date, tag)
                )
                view.isSelected = true
            } else {
                dao.deleteFavoriteDish(menu.cafeteriaId, menu.name)
                view.isSelected = false
            }
        }
    }

    private fun inflateWithoutPrice(view: View) = with(view) {
        line_price.visibility = View.GONE
        favoriteDish.visibility = View.GONE
    }

    companion object {

        private val SPLIT_ANNOTATIONS_PATTERN = Pattern.compile("\\(([A-Za-z0-9]+),")
        private val NUMERICAL_ANNOTATIONS_PATTERN = Pattern.compile("\\(([1-9]|10|11)\\)")

        /**
         * Converts menu text to {@link SpannableString}.
         * Replaces all (v), ... annotations with images
         *
         * @param context Context
         * @param menu    Text with annotations
         * @return Spannable text with images
         */
        @JvmStatic
        fun menuToSpan(context: Context, menu: String): SpannableString {
            val processedMenu = splitAnnotations(menu)
            val text = SpannableString(processedMenu)
            replaceWithImg(context, processedMenu, text, "(v)", R.drawable.meal_vegan)
            replaceWithImg(context, processedMenu, text, "(f)", R.drawable.meal_veggie)
            replaceWithImg(context, processedMenu, text, "(R)", R.drawable.meal_beef)
            replaceWithImg(context, processedMenu, text, "(S)", R.drawable.meal_pork)
            replaceWithImg(context, processedMenu, text, "(GQB)", R.drawable.ic_gqb)
            replaceWithImg(context, processedMenu, text, "(99)", R.drawable.meal_alcohol)
            return text

            // TODO: Move to CafeteriaMenu

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

        private fun replaceWithImg(context: Context, menu: String,
                                   text: Spannable, sym: String, drawable: Int) {
            var index = menu.indexOf(sym)
            while (index >= 0) {
                val imageSpan = ImageSpan(context, drawable)
                text.setSpan(imageSpan, index, index + sym.length, 0)
                index = menu.indexOf(sym, index + sym.length)
            }
        }

        /**
         * Replaces all annotations that cannot be replaces with images such as (1), ...
         *
         * @param menu Text to delete annotations from
         * @return Text without un-replaceable annotations
         */
        private fun prepare(menu: String): String {
            val tmp = splitAnnotations(menu)
            return NUMERICAL_ANNOTATIONS_PATTERN.matcher(tmp)
                    .replaceAll("")
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

    }

}
