package de.tum.in.tumcampusapp.component.ui.cafeteria.details;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.component.other.generic.activity.BaseActivity;
import de.tum.in.tumcampusapp.component.ui.cafeteria.di.CafeteriaModule;
import de.tum.in.tumcampusapp.component.ui.cafeteria.model.CafeteriaMenu;
import de.tum.in.tumcampusapp.di.ViewModelFactory;
import de.tum.in.tumcampusapp.utils.Const;
import de.tum.in.tumcampusapp.utils.Utils;

/**
 * Fragment for each cafeteria-page.
 */
public class CafeteriaDetailsSectionFragment extends Fragment {

    @Inject
    Provider<CafeteriaViewModel> viewModelProvider;

    private CafeteriaViewModel cafeteriaViewModel;
    private CafeteriaMenusAdapter adapter;

    public static CafeteriaDetailsSectionFragment newInstance(int cafeteriaId, DateTime dateTime) {
        CafeteriaDetailsSectionFragment fragment = new CafeteriaDetailsSectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(Const.DATE, dateTime);
        args.putInt(Const.CAFETERIA_ID, cafeteriaId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((BaseActivity) requireActivity()).getInjector()
                .cafeteriaComponent()
                .cafeteriaModule(new CafeteriaModule())
                .build()
                .inject(this);

        ViewModelFactory<CafeteriaViewModel> factory = new ViewModelFactory<>(viewModelProvider);
        cafeteriaViewModel = ViewModelProviders.of(this, factory).get(CafeteriaViewModel.class);
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

        RecyclerView recyclerView = view.findViewById(R.id.menusRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new CafeteriaMenusAdapter(requireContext(), true);
        recyclerView.setAdapter(adapter);

        int cafeteriaId = getArguments().getInt(Const.CAFETERIA_ID);

        cafeteriaViewModel.getCafeteriaMenus().observe(getViewLifecycleOwner(), this::showCafeteriaMenus);
        cafeteriaViewModel.fetchCafeteriaMenus(cafeteriaId, menuDate);
    }

    private void showCafeteriaMenus(List<CafeteriaMenu> cafeteriaMenus) {
        List<CafeteriaMenu> filtered = new ArrayList<>();

        for (CafeteriaMenu menu : cafeteriaMenus) {
            boolean shouldShow = Utils.getSettingBool(
                    requireContext(),
                    "card_cafeteria_" + menu.getTypeShort(),
                    "tg".equals(menu.getTypeShort()) || "ae".equals(menu.getTypeShort())
            );

            if (shouldShow) {
                filtered.add(menu);
            }
        }

        adapter.update(filtered);
    }

}
