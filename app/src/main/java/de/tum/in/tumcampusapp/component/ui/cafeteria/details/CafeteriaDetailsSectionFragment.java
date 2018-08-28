package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.api.app.TUMCabeClient;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuInflater;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaRemoteRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.DateTimeUtils;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {

    private CafeteriaViewModel cafeteriaViewModel;
    private final CompositeDisposable mDisposable = new CompositeDisposable();

    /**
     * Inflates the cafeteria menu layout.
     * This is put into an extra static method to be able to
     * reuse it in {@link CafeteriaMenuCard}
     *
     * @param rootView    Parent layout
     * @param cafeteriaId Cafeteria id
     * @param date        Date
     * @param isBigLayout True to show big lines in the Activity, false to show small lines in Card
     */
    public static void showMenu(LinearLayout rootView, int cafeteriaId, DateTime date,
                                boolean isBigLayout, List<CafeteriaMenu> cafeteriaMenus) {
        final Context context = rootView.getContext();

        if (!isBigLayout) {
            // Show opening hours
            OpenHoursHelper lm = new OpenHoursHelper(context);

            TextView textview;
            textview = new TextView(context, null, R.style.CardBody);
            textview.setText(lm.getHoursByIdAsString(context, cafeteriaId, date));
            textview.setTextColor(ContextCompat.getColor(context, R.color.sections_green));

            int bottomPadding = context.getResources()
                    .getDimensionPixelOffset(R.dimen.material_default_padding);
            textview.setPadding(0, 0, 0, bottomPadding);

            rootView.addView(textview);
        }

        // Show cafeteria menu
        String curShort = "";
        CafeteriaMenuInflater menuInflater =
                new CafeteriaMenuInflater(context, rootView, isBigLayout);

        for (CafeteriaMenu cafeteriaMenu : cafeteriaMenus) {
            boolean isFirstInSection = !cafeteriaMenu.getTypeShort().equals(curShort);
            View view = menuInflater.inflate(cafeteriaMenu, isFirstInSection);

            if (view != null) {
                rootView.addView(view);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CafeteriaRemoteRepository remoteRepository = CafeteriaRemoteRepository.INSTANCE;
        remoteRepository.setTumCabeClient(TUMCabeClient.getInstance(getContext()));

        CafeteriaLocalRepository localRepository = CafeteriaLocalRepository.INSTANCE;
        localRepository.setDb(TcaDb.getInstance(getContext()));

        cafeteriaViewModel = new CafeteriaViewModel(localRepository, remoteRepository, mDisposable);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(
                R.layout.fragment_cafeteriadetails_section, container, false);

        if (getArguments() == null) {
            return rootView;
        }

        String dateString = getArguments().getString(Const.DATE);
        if (dateString == null) {
            return rootView;
        }

        DateTimeFormatter formatter = DateTimeFormat.fullDate();
        DateTime menuDate = DateTimeUtils.INSTANCE.getDate(dateString);
        String menuDateString = formatter.print(menuDate);

        TextView dateTextView = rootView.findViewById(R.id.menuDateTextView);
        dateTextView.setText(menuDateString);

        LinearLayout root = rootView.findViewById(R.id.layout);
        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);
        DateTime date = DateTimeUtils.INSTANCE.getDate(dateString);

        mDisposable.add(
                cafeteriaViewModel
                        .getCafeteriaMenus(cafeteriaId, date)
                        .subscribe(menu -> showMenu(root, cafeteriaId, date, true, menu))
        );

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
    }

}
