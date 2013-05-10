package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.CurriculaActivity;
import de.tum.in.tumcampusapp.activities.GalleryActivity;
import de.tum.in.tumcampusapp.activities.GradesActivity;
import de.tum.in.tumcampusapp.activities.LecturesActivity;
import de.tum.in.tumcampusapp.activities.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.activities.LecturesSearchActivity;
import de.tum.in.tumcampusapp.activities.MockActivity;
import de.tum.in.tumcampusapp.activities.NewsActivity;
import de.tum.in.tumcampusapp.activities.OpeningHoursListActivity;
import de.tum.in.tumcampusapp.activities.PlansActivity;
import de.tum.in.tumcampusapp.activities.TransportationActivity;
import de.tum.in.tumcampusapp.auxiliary.ListMenuEntry;
import de.tum.in.tumcampusapp.fragments.StartSectionFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class StartSectionsPagerAdapter extends FragmentPagerAdapter {

	public static final String LIST_ENTRY_SET = "list_entry_set";
	public static final int NUMBER_OF_PAGES = 4;
	public static final int SECTION_GENERAL_TUM = 0;
	public static final int SECTION_MY_TUM = 1;
	public static final int SECTION_NEWS = 2;
	public static final int SECTION_CONVENIENCE = 3;
	private final Activity activity;

	public StartSectionsPagerAdapter(Activity mainActivity, FragmentManager fm) {
		super(fm);
		activity = mainActivity;
	}

	@Override
	public int getCount() {
		return NUMBER_OF_PAGES;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		Fragment fragment = new StartSectionFragment();
		ArrayList<ListMenuEntry> listMenuEntrySet = new ArrayList<ListMenuEntry>();
		Bundle args = new Bundle();

		// Puts for each section a new ListMenuEntry object in the
		// listMenuEntrySet. Each ListMenuEntry contains a image, some text and
		// the intent which coresponses to the activity which should be started
		switch (position) {
		case SECTION_GENERAL_TUM:
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.zoom, R.string.lecture_search, R.string.lecturessearch_addinfo, new Intent(activity,
					LecturesSearchActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.users, R.string.person_search, R.string.personsearch_addinfo, new Intent(activity,
					MockActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.web, R.string.plans, R.string.plans_addinfo, new Intent(activity, PlansActivity.class)));
			listMenuEntrySet
					.add(new ListMenuEntry(R.drawable.home, R.string.roomfinder, R.string.roomfinder_addinfo, new Intent(activity, MockActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.documents, R.string.study_plans, R.string.studyplans_addinfo, new Intent(activity,
					CurriculaActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.unlock, R.string.opening_hours, R.string.openinghours_addinfo, new Intent(activity,
					OpeningHoursListActivity.class)));
			break;
		case SECTION_MY_TUM:
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.calculator, R.string.lectures, R.string.lectures_addinfo,
					new Intent(activity, LecturesPersonalActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.chart, R.string.grades, R.string.grades_addinfo, new Intent(activity, GradesActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.calendar, R.string.timetable, R.string.timetable_addinfo,
					new Intent(activity, MockActivity.class)));
			listMenuEntrySet
					.add(new ListMenuEntry(R.drawable.finance, R.string.study_fee, R.string.studyfee_addinfo, new Intent(activity, MockActivity.class)));
			break;
		case SECTION_NEWS:
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.fax, R.string.rss_feeds, R.string.rssfeed_addinfo, new Intent(activity, MockActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.news, R.string.tum_news, R.string.tumnews_addinfo, new Intent(activity, NewsActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.music, R.string.events, R.string.events_addinfo, new Intent(activity, MockActivity.class)));
			listMenuEntrySet
					.add(new ListMenuEntry(R.drawable.pictures, R.string.gallery, R.string.gallery_addinfo, new Intent(activity, GalleryActivity.class)));
			break;
		case SECTION_CONVENIENCE:
			listMenuEntrySet
					.add(new ListMenuEntry(R.drawable.show_info, R.string.mvv, R.string.mvv_addinfo, new Intent(activity, TransportationActivity.class)));
			listMenuEntrySet.add(new ListMenuEntry(R.drawable.notepad, R.string.menues, R.string.cafeteria_addinfo, new Intent(activity, MockActivity.class)));
			break;
		}
		args.putSerializable(LIST_ENTRY_SET, listMenuEntrySet);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return activity.getString(R.string.tum_common).toUpperCase(l);
		case 1:
			return activity.getString(R.string.my_tum).toUpperCase(l);
		case 2:
			return activity.getString(R.string.news).toUpperCase(l);
		case 3:
			return activity.getString(R.string.convenience).toUpperCase(l);
		}
		return null;
	}

}