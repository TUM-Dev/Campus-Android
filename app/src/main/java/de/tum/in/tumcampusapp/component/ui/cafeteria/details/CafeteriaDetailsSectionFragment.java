package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuCard;
import de.tum.in.tumcampusapp.component.ui.cafeteria.CafeteriaMenuInflater;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.component.ui.cafeteria.repository.CafeteriaLocalRepository;
import de.tum.in.tumcampusapp.database.TcaDb;
import de.tum.in.tumcampusapp.utils.Const;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {

    private CafeteriaViewModel cafeteriaViewModel;

    public static CafeteriaDetailsSectionFragment newInstance(int cafeteriaId, DateTime dateTime) {
        CafeteriaDetailsSectionFragment fragment = new CafeteriaDetailsSectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(Const.DATE, dateTime);
        args.putInt(Const.CAFETERIA_ID, cafeteriaId);
        fragment.setArguments(args);
        return fragment;
    }

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
            TextView textView = createOpeningHoursTextView(context, cafeteriaId, date);
            rootView.addView(textView);
        }

        // Show cafeteria menu
        String currentCafeteriaMenuType = "";
        CafeteriaMenuInflater menuInflater = new CafeteriaMenuInflater(context, rootView, isBigLayout);

        for (CafeteriaMenu cafeteriaMenu : cafeteriaMenus) {
            boolean isFirstInSection = !cafeteriaMenu.getTypeShort().equals(currentCafeteriaMenuType);
            View view = menuInflater.inflate(cafeteriaMenu, isFirstInSection);

            if (view != null) {
                rootView.addView(view);
            }
        }
    }

    private static TextView createOpeningHoursTextView(Context context, int cafeteriaId, DateTime date) {
        OpenHoursHelper lm = new OpenHoursHelper(context);

        TextView textview;
        textview = new TextView(context, null, R.style.CardBody);
        textview.setText(lm.getHoursByIdAsString(context, cafeteriaId, date));
        textview.setTextColor(ContextCompat.getColor(context, R.color.sections_green));

        int bottomPadding = context.getResources()
                .getDimensionPixelOffset(R.dimen.material_default_padding);
        textview.setPadding(0, 0, 0, bottomPadding);
        return textview;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TcaDb database = TcaDb.getInstance(requireContext());
        CafeteriaLocalRepository localRepository = new CafeteriaLocalRepository(database);

        cafeteriaViewModel = new CafeteriaViewModel(localRepository);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cafeteriadetails_section, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        DateTime menuDate = (DateTime) getArguments().getSerializable(Const.DATE);
        String menuDateString = DateTimeFormat.fullDate().print(menuDate);

        TextView dateTextView = view.findViewById(R.id.menuDateTextView);
        dateTextView.setText(menuDateString);

        LinearLayout root = view.findViewById(R.id.layout);
        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);

        cafeteriaViewModel.getCafeteriaMenus().observe(getViewLifecycleOwner(), cafeteriaMenus -> {
            showMenu(root, cafeteriaId, menuDate, true, cafeteriaMenus);
        });

        cafeteriaViewModel.fetchCafeteriaMenus(cafeteriaId, menuDate);
    }
}
