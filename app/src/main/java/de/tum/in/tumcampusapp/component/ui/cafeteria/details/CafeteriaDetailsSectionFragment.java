package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.FavoriteDishDao;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaPrices;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.FavoriteDish;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateUtils;
import de.tum.in.tumcampusapp.utils.Utils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {
    private static final Pattern SPLIT_ANNOTATIONS_PATTERN = Pattern.compile("\\(([A-Za-z0-9]+),");
    private static final Pattern NUMERICAL_ANNOTATIONS_PATTERN = Pattern.compile("\\(([1-9]|10|11)\\)");

    private CafeteriaViewModel cafeteriaViewModel;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CafeteriaRemoteRepository remoteRepository = CafeteriaRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(getContext()));
        CafeteriaLocalRepository localRepository = CafeteriaLocalRepository.INSTANCE;
        localRepository.setDb(TcaDb.getInstance(getContext()));
        cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository, mDisposable);
        JodaTimeAndroid.init(getContext());
    }

    /**
     * Inflates the cafeteria menu layout.
     * This is put into an extra static method to be able to
     * reuse it in {@link CafeteriaMenuCard}
     *
     * @param rootView    Parent layout
     * @param cafeteriaId Cafeteria id
     * @param dateStr     Date in yyyy-mm-dd format
     * @param big         True to show big lines
     */
    @SuppressLint("ShowToast")
    public static List<View> showMenu(LinearLayout rootView, int cafeteriaId, String dateStr, boolean big, List<CafeteriaMenu> cafeteriaMenus) {
        // initialize a few often used things
        final Context context = rootView.getContext();
        final Map<String, String> rolePrices = CafeteriaPrices.INSTANCE.getRolePrices(context);
        final int padding = (int) context.getResources()
                                         .getDimension(R.dimen.card_text_padding);
        List<View> addedViews = new ArrayList<>(32);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TcaDb db = TcaDb.getInstance(context);
        final FavoriteDishDao favoriteDishDao = db.favoriteDishDao();

        TextView textview;
        if (!big) {
            // Show opening hours
            OpenHoursHelper lm = new OpenHoursHelper(context);
            textview = new TextView(context);
            textview.setText(lm.getHoursByIdAsString(context, cafeteriaId, DateUtils.getDate(dateStr)));
            textview.setTextColor(ContextCompat.getColor(context, R.color.sections_green));
            rootView.addView(textview);
            addedViews.add(textview);
        }

        // Show cafeteria menu
        String curShort = "";

        for (CafeteriaMenu cafeteriaMenu : cafeteriaMenus) {
            String typeShort = cafeteriaMenu.getTypeShort();
            String typeLong = cafeteriaMenu.getTypeLong();
            // Skip unchecked categories if showing card
            boolean shouldShow = Utils.getSettingBool(context, "card_cafeteria_" + typeShort,
                                                      "tg".equals(typeShort) || "ae".equals(typeShort));
            if (!big && !shouldShow) {
                continue;
            }

            // Add header if we start with a new category
            if (!typeShort.equals(curShort)) {
                curShort = typeShort;
                View view = inflater.inflate(big ? R.layout.list_header_big : R.layout.card_list_header, rootView, false);
                textview = view.findViewById(R.id.list_header);
                textview.setText(typeLong.replaceAll("[0-9]", "")
                                         .trim());
                rootView.addView(view);
                addedViews.add(view);
            }

            // Show menu item
            String menuName = cafeteriaMenu.getName();
            final SpannableString text = menuToSpan(context, big ? menuName : prepare(menuName));
            if (rolePrices.containsKey(typeLong)) {
                // If price is available
                View view = inflater.inflate(big ? R.layout.price_line_big : R.layout.card_price_line, rootView, false);
                textview = view.findViewById(R.id.line_name);
                TextView priceView = view.findViewById(R.id.line_price);
                final View favDish = view.findViewById(R.id.favoriteDish);
                favDish.setTag(menuName + "__" + cafeteriaId);
                        /*
                         * saved dish id in the favoriteDishButton tag.
                         * onButton checked getTag->DishID and mark it as favorite locally (favorite=1)
                         */
                textview.setText(text);
                priceView.setText(String.format("%s €", rolePrices.get(typeLong)));
                rootView.addView(view);
                addedViews.add(view);

                Object tag = favDish.getTag();
                List<FavoriteDish> isFavourite = favoriteDishDao.checkIfFavoriteDish(tag.toString());
                favDish.setSelected(!isFavourite.isEmpty());

                favDish.setOnClickListener(view1 -> {
                    String id = view1.getTag()
                                     .toString();
                    String[] data = id.split("__");
                    String dishName = data[0];
                    int mensaId = Integer.parseInt(data[1]);

                    if (!view1.isSelected()) {
                        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
                        String currentDate = DateTime.now()
                                                     .toString(formatter);
                        favoriteDishDao.insertFavouriteDish(FavoriteDish.Companion.create(mensaId, dishName, currentDate, tag.toString()));
                        view1.setSelected(true);
                    } else {
                        favoriteDishDao.deleteFavoriteDish(mensaId, dishName);
                        view1.setSelected(false);
                    }
                });
            } else {
                // Without price
                textview = new TextView(context);
                textview.setText(text);
                textview.setPadding(padding, padding, padding, padding);
                rootView.addView(textview);
                addedViews.add(textview);
            }
        }
        return addedViews;
    }

    /**
     * Converts menu text to {@link SpannableString}.
     * Replaces all (v), ... annotations with images
     *
     * @param context Context
     * @param menu    Text with annotations
     * @return Spannable text with images
     */
    public static SpannableString menuToSpan(Context context, String menu) {
        final String processedMenu = splitAnnotations(menu);
        final SpannableString text = new SpannableString(processedMenu);
        replaceWithImg(context, processedMenu, text, "(v)", R.drawable.meal_vegan);
        replaceWithImg(context, processedMenu, text, "(f)", R.drawable.meal_veggie);
        replaceWithImg(context, processedMenu, text, "(R)", R.drawable.meal_beef);
        replaceWithImg(context, processedMenu, text, "(S)", R.drawable.meal_pork);
        replaceWithImg(context, processedMenu, text, "(GQB)", R.drawable.ic_gqb);
        replaceWithImg(context, processedMenu, text, "(99)", R.drawable.meal_alcohol);
        /* TODO Somday replace all of them:
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
        return text;
    }

    private static void replaceWithImg(Context context, String menu, Spannable text, String sym, int drawable) {
        int ind = menu.indexOf(sym);
        while (ind >= 0) {
            ImageSpan is = new ImageSpan(context, drawable);
            text.setSpan(is, ind, ind + sym.length(), 0);
            ind = menu.indexOf(sym, ind + sym.length());
        }
    }

    /**
     * Replaces all annotations that cannot be replaces with images such as (1), ...
     *
     * @param menu Text to delete annotations from
     * @return Text without un-replaceable annotations
     */
    private static String prepare(String menu) {
        final String tmp = splitAnnotations(menu);
        return NUMERICAL_ANNOTATIONS_PATTERN.matcher(tmp)
                                            .replaceAll("");
    }

    @NonNull
    private static String splitAnnotations(String menu) {
        int len;
        String tmp = menu;
        do {
            len = tmp.length();
            tmp = SPLIT_ANNOTATIONS_PATTERN.matcher(tmp)
                                           .replaceFirst("($1)(");
        } while (tmp.length() > len);
        return tmp;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_cafeteriadetails_section, container, false);
        LinearLayout root = rootView.findViewById(R.id.layout);
        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);
        String date = getArguments().getString(Const.DATE);
        cafeteriaViewModel.getCafeteriaMenus(cafeteriaId, date)
                          .subscribe(menu -> showMenu(root, cafeteriaId, date, true, menu));
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }
}
