package de.tum.in.tumcampusapp.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;

import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;
import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.R.string;
import de.tum.in.tumcampusapp.activities.CafeteriaActivity;
import de.tum.in.tumcampusapp.activities.CalendarActivity;
import de.tum.in.tumcampusapp.activities.CurriculaActivity;
import de.tum.in.tumcampusapp.activities.EventsActivity;
import de.tum.in.tumcampusapp.activities.FeedsActivity;
import de.tum.in.tumcampusapp.activities.GalleryActivity;
import de.tum.in.tumcampusapp.activities.GradesActivity;
import de.tum.in.tumcampusapp.activities.InformationActivity;
import de.tum.in.tumcampusapp.activities.LecturesPersonalActivity;
import de.tum.in.tumcampusapp.activities.LecturesSearchActivity;
import de.tum.in.tumcampusapp.activities.NewsActivity;
import de.tum.in.tumcampusapp.activities.OpeningHoursListActivity;
import de.tum.in.tumcampusapp.activities.OrganisationActivity;
import de.tum.in.tumcampusapp.activities.PersonsSearchActivity;
import de.tum.in.tumcampusapp.activities.PlansActivity;
import de.tum.in.tumcampusapp.activities.RoomfinderActivity;
import de.tum.in.tumcampusapp.activities.TransportationActivity;
import de.tum.in.tumcampusapp.activities.TuitionFeesActivity;
import de.tum.in.tumcampusapp.activities.wizzard.WizNavStartActivity;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.auxiliary.ListMenuEntry;
import de.tum.in.tumcampusapp.fragments.StartSectionFragment;
import de.tum.in.tumcampusapp.preferences.UserPreferencesActivity;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class StartSectionsPagerAdapter extends FragmentPagerAdapter {
	public static final String IMAGE_FOR_CATEGORY = "image_for_category";
	public static final String LIST_ENTRY_SET = "list_entry_set";
	public static final int NUMBER_OF_PAGES = 5;
	public static final int SECTION_CONVENIENCE = 4;
	public static final int SECTION_GENERAL_TUM = 0;
	public static final int SECTION_PERSONALIZED = 1;
	public static final int SECTION_MY_TUM = 2;
	public static final int SECTION_NEWS = 3;
	private Activity activity;
	private String MVV="mvv_id";
	private String MENUES="menues_id";
	private String ROOM="roomfinder_id";
	private String LECTURE="lectures_id";
	private SharedPreferences sharedPrefs;
	HashMap<String, ListMenuEntry> MyPref=new HashMap<String, ListMenuEntry>();
	
	

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
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity);
		
		//it will initialize hashmap with listmenuEntry
		initializeHashMap();

		// Puts for each section a new ListMenuEntry object in the
		// listMenuEntrySet. Each ListMenuEntry contains a image, some text and
		// the intent which corresponds to the activity which should be started


		String prefString=activity.getString(R.string.pref_string);
				String[] _Array=prefString.split(",");
		
		switch (position) {
		case SECTION_PERSONALIZED:
			args.putInt("POSITION", SECTION_PERSONALIZED);
			args.putInt(IMAGE_FOR_CATEGORY, R.drawable.img_tum_building);
			for (String  _node : _Array) {
				if(_node.equals(MVV) || (_node.equals(MENUES)) || (_node.equals(ROOM)) || (_node.equals(LECTURE)))
				{
					if (sharedPrefs.getBoolean(_node,true))
						
					{
						listMenuEntrySet.add(MyPref.get(_node));
					}
					
				}
				else{
				
				if (sharedPrefs.getBoolean(_node, false))
					
				{
					listMenuEntrySet.add(MyPref.get(_node));
				}
				}
			}
			
			
			
			break;
		case SECTION_GENERAL_TUM:
			args.putInt("POSITION", SECTION_GENERAL_TUM);
			args.putInt(IMAGE_FOR_CATEGORY, R.drawable.img_tum_building);
			listMenuEntrySet.add(MyPref.get("lectures_id"));			
			listMenuEntrySet.add(MyPref.get("person_search_id"));
			listMenuEntrySet.add(MyPref.get("plans_id"));
			listMenuEntrySet.add(MyPref.get("roomfinder_id"));
			listMenuEntrySet.add(MyPref.get("study_plans_id"));
			listMenuEntrySet.add(MyPref.get("opening_hours_id"));
			listMenuEntrySet.add(MyPref.get("organisations_id"));
			break;
		case SECTION_MY_TUM:
			args.putInt("POSITION", SECTION_MY_TUM);
			args.putInt(IMAGE_FOR_CATEGORY, R.drawable.img_tum_flags);
			listMenuEntrySet.add(MyPref.get("my_lectures_id"));
			listMenuEntrySet.add(MyPref.get("my_grades_id"));

			// TODO This feature is not extensively helpful, because the
			// CalendarActivity and the MyLecturesActivity already implement
			// this functionality
			// listMenuEntrySet.add(new ListMenuEntry(R.drawable.calendar,
			// R.string.lecture_schedule, R.string.lecture_schedule_extra,
			// new Intent(activity, LectureScheduleActivity.class)));

			listMenuEntrySet.add(MyPref.get("calender_id"));
			listMenuEntrySet.add(MyPref.get("tuition_fees_id"));
			break;
		case SECTION_NEWS:
			args.putInt("POSITION", SECTION_NEWS);
			args.putInt(IMAGE_FOR_CATEGORY, R.drawable.img_tum_building_main);
			listMenuEntrySet.add(MyPref.get("rss_feeds_id"));
			listMenuEntrySet.add(MyPref.get("tum_news_id"));
			listMenuEntrySet.add(MyPref.get("events_id"));
			listMenuEntrySet.add(MyPref.get("gallery_id"));
			break;
		case SECTION_CONVENIENCE:
			args.putInt("POSITION", SECTION_CONVENIENCE);
			args.putInt(IMAGE_FOR_CATEGORY, R.drawable.img_tum_parabel);
			listMenuEntrySet.add(MyPref.get("mvv_id"));
			
			listMenuEntrySet.add(MyPref.get("menues_id"));
			listMenuEntrySet.add(MyPref.get("information_id"));
			break;
		}
		args.putSerializable(LIST_ENTRY_SET, listMenuEntrySet);
		fragment.setArguments(args);
		return fragment;
	}
	public void initializeHashMap()
	{
		MyPref.put("lectures_id",  new ListMenuEntry(R.drawable.zoom,
				R.string.lecture_search, R.string.lecturessearch_addinfo,
				new Intent(activity, LecturesSearchActivity.class)));
		MyPref.put("mvv_id", new ListMenuEntry(R.drawable.show_info,
				R.string.mvv, R.string.mvv_addinfo, new Intent(activity,
						TransportationActivity.class)));
		MyPref.put("menues_id", new ListMenuEntry(R.drawable.shopping_cart,
				R.string.menues, R.string.cafeteria_addinfo, new Intent(
						activity, CafeteriaActivity.class)));
		MyPref.put("grades_id", new ListMenuEntry(R.drawable.chart,
				R.string.my_grades, R.string.grades_addinfo, new Intent(
						activity, GradesActivity.class)));
		MyPref.put("rss_feeds_id", new ListMenuEntry(R.drawable.fax,
				R.string.rss_feeds, R.string.rssfeed_addinfo, new Intent(
						activity, FeedsActivity.class)));
		MyPref.put("calender_id", new ListMenuEntry(R.drawable.calendar,
						R.string.schedule, R.string.schedule_extras, new Intent(
								activity, CalendarActivity.class)));
		MyPref.put("study_plans_id", new ListMenuEntry(R.drawable.documents,
				R.string.study_plans, R.string.studyplans_addinfo,
				new Intent(activity, CurriculaActivity.class)));
		MyPref.put("events_id", new ListMenuEntry(R.drawable.camera,
				R.string.events, R.string.events_addinfo, new Intent(
						activity, EventsActivity.class)));
		MyPref.put("gallery_id", new ListMenuEntry(R.drawable.pictures,
				R.string.gallery, R.string.gallery_addinfo, new Intent(
						activity, GalleryActivity.class)));
		MyPref.put("person_search_id", new ListMenuEntry(R.drawable.users,
				R.string.person_search, R.string.personsearch_addinfo,
				new Intent(activity, PersonsSearchActivity.class)));
		MyPref.put("plans_id", new ListMenuEntry(R.drawable.web,
					R.string.plans, R.string.plans_addinfo, new Intent(
							activity, PlansActivity.class)));
		MyPref.put("roomfinder_id", new ListMenuEntry(R.drawable.home,
				R.string.roomfinder, R.string.roomfinder_addinfo,
				new Intent(activity, RoomfinderActivity.class)));
		MyPref.put("opening_hours_id", new ListMenuEntry(R.drawable.unlock,
					R.string.opening_hours, R.string.openinghours_addinfo,
					new Intent(activity, OpeningHoursListActivity.class)));
		MyPref.put("organisations_id", new ListMenuEntry(R.drawable.chat,
				R.string.organisations, R.string.organisations_addinfo,
				new Intent(activity, OrganisationActivity.class)));
		MyPref.put("my_lectures_id", new ListMenuEntry(R.drawable.calculator,
				R.string.my_lectures, R.string.lectures_addinfo,
				new Intent(activity, LecturesPersonalActivity.class)));
		MyPref.put("my_grades_id", new ListMenuEntry(R.drawable.chart,
				R.string.my_grades, R.string.grades_addinfo, new Intent(
						activity, GradesActivity.class)));
		MyPref.put("tuition_fees_id", new ListMenuEntry(R.drawable.finance,
					R.string.tuition_fees, R.string.tuitionfee_addinfo,
					new Intent(activity, TuitionFeesActivity.class)));
		MyPref.put("tum_news_id", new ListMenuEntry(R.drawable.mail,
					R.string.tum_news, R.string.tumnews_addinfo, new Intent(
							activity, NewsActivity.class)));
		MyPref.put("information_id", new ListMenuEntry(R.drawable.about,
					R.string.information, R.string.information_addinfo,
					new Intent(activity, InformationActivity.class)));
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return activity.getString(R.string.tum_common).toUpperCase(l);
		
		case 1:
			return activity.getString(R.string.personalized).toUpperCase(l);
						
		case 2:
			return activity.getString(R.string.my_tum).toUpperCase(l);
			
		case 3:
			return activity.getString(R.string.news).toUpperCase(l);
		
		case 4:
			return activity.getString(R.string.extras).toUpperCase(l);
		}
		return null;
	}

}