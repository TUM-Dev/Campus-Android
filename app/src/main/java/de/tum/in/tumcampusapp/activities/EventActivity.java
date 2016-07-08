package de.tum.in.tumcampusapp.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.generic.BaseActivity;
import de.tum.in.tumcampusapp.auxiliary.Utils;

public class EventActivity extends BaseActivity {
    private static final DateFormat DTF = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY);
    private static final DateFormat TF = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
    private static final DateFormat DF = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL);

    private static final String TITLE = "IKOM 2016";
    private static final List<List<EventScheduleEntry>> SCHEDULE = Arrays.asList(
            Arrays.asList(
                    new EventScheduleEntry(parse("20.06.2016 09:30"), 30, "Eröffnung der IKOM"),
                    new EventScheduleEntry(parse("20.06.2016 10:00"), 60, "IKOM Arena | Elektro- & Medizintechnik"),
                    new EventScheduleEntry(parse("20.06.2016 11:00"), 45, "Gastvortrag: Prof. Dr. Axel Stepken, Vorstandsvorsitzender (CEO), TÜV SÜD AG, Hörsaal MW 0350"),
                    new EventScheduleEntry(parse("20.06.2016 11:45"), 15, ""),
                    new EventScheduleEntry(parse("20.06.2016 12:30"), 30, "IKOM Arena | Engineering Branchenvergleich"),
                    new EventScheduleEntry(parse("20.06.2016 13:00"), 30, ""),
                    new EventScheduleEntry(parse("20.06.2016 14:00"), 15, "Gründerzeit in der IKOM Arena"),
                    new EventScheduleEntry(parse("20.06.2016 14:15"), 30, ""),
                    new EventScheduleEntry(parse("20.06.2016 15:00"), 30, "Gastvortrag: Dr. Till Reuter, Vorstandsvorsitzender (CEO), KUKA AG, Hörsaal MW 1801"),
                    new EventScheduleEntry(parse("20.06.2016 15:30"), 30, ""),
                    new EventScheduleEntry(parse("20.06.2016 16:30"), 30, "Verlosung in Hof 0 und Ende des Forumstages")
            ), Arrays.asList(
                    new EventScheduleEntry(parse("21.06.2016 09:30"), 30, "Eröffnung der IKOM<br><font color=\"#84a115\">Eröffnung der IKOM Start Up</font>"),
                    new EventScheduleEntry(parse("21.06.2016 10:00"), 60, "IKOM Arena | Fahrzeugtechnik"),
                    new EventScheduleEntry(parse("21.06.2016 11:00"), 45, "IKOM Arena | Logistics & Simulation Software<br><font color=\"#84a115\">IKOM Start Up Pitch</font>"),
                    new EventScheduleEntry(parse("21.06.2016 11:45"), 15, "IKOM Arena | Patentanwälte"),
                    new EventScheduleEntry(parse("21.06.2016 12:30"), 30, ""),
                    new EventScheduleEntry(parse("21.06.2016 13:00"), 30, "Gastvortrag: Dominik Asam, Vorstand Finanzen, Infineon Techonologies AG, Hörsaal MW 0250"),
                    new EventScheduleEntry(parse("21.06.2016 14:15"), 45, "IKOM Arena | High-Tech Entwicklung\nIKOM Start-Up: Gastvortrag: Yorck Hernandez, CEO CrowdPatent"),
                    new EventScheduleEntry(parse("21.06.2016 15:00"), 30, "<font color=\"#84a115\">IKOM Start Up: Gastvortrag: Dominik Guber, Gründer Braufässchen</font>"),
                    new EventScheduleEntry(parse("21.06.2016 15:30"), 90, "Gastvortrag | Markus Tischer, Vorstand International Operations and Services, KRONES AG"),
                    new EventScheduleEntry(parse("21.06.2016 16:00"), 30, "<font color=\"#84a115\">IKOM Start Up: Ende des Messetages</font>"),
                    new EventScheduleEntry(parse("21.06.2016 16:30"), 30, "Verlosung in Hof 0 und Ende des Forumstages")
            ), Arrays.asList(
                    new EventScheduleEntry(parse("22.06.2016 09:30"), 30, "Eröffnung der IKOM"),
                    new EventScheduleEntry(parse("22.06.2016 10:00"), 60, "IKOM Arena | Technologieberatung"),
                    new EventScheduleEntry(parse("22.06.2016 11:00"), 45, "IKOM Arena | Softwareentwicklung und -beratung"),
                    new EventScheduleEntry(parse("22.06.2016 11:45"), 15, ""),
                    new EventScheduleEntry(parse("22.06.2016 12:30"), 30, ""),
                    new EventScheduleEntry(parse("22.06.2016 13:00"), 30, "IKOM Arena | Dienstleistung und Maschinenbau"),
                    new EventScheduleEntry(parse("22.06.2016 14:00"), 15, ""),
                    new EventScheduleEntry(parse("22.06.2016 14:15"), 30, ""),
                    new EventScheduleEntry(parse("22.06.2016 15:00"), 30, ""),
                    new EventScheduleEntry(parse("22.06.2016 15:30"), 30, "Gastvortrag: Oliver Zipse, Vorstand Produktion, BMW AG, Hörsaal MW 1801"),
                    new EventScheduleEntry(parse("22.06.2016 16:30"), 30, "Verlosung in Hof 0 und Ende des Forumstages")
            ), Arrays.asList(
                    new EventScheduleEntry(parse("23.06.2016 09:30"), 30, "Eröffnung der IKOM"),
                    new EventScheduleEntry(parse("23.06.2016 10:00"), 60, "IKOM Arena | Consulting I: Management & Technologie"),
                    new EventScheduleEntry(parse("23.06.2016 11:00"), 45, "IKOM Arena | Finance"),
                    new EventScheduleEntry(parse("23.06.2016 11:45"), 15, ""),
                    new EventScheduleEntry(parse("23.06.2016 12:30"), 30, ""),
                    new EventScheduleEntry(parse("23.06.2016 13:00"), 30, "IKOM Arena | Consulting II: Management & IT"),
                    new EventScheduleEntry(parse("23.06.2016 14:00"), 15, ""),
                    new EventScheduleEntry(parse("23.06.2016 14:15"), 30, ""),
                    new EventScheduleEntry(parse("23.06.2016 15:00"), 30, "Gastvortrag: Axel Strotbek, Vorstand Finanz und Organisation, AUDI AG, Hörsaal MW 1801"),
                    new EventScheduleEntry(parse("23.06.2016 15:30"), 30, ""),
                    new EventScheduleEntry(parse("23.06.2016 16:30"), 30, "Verlosung in Hof 0 und Ende des Forumstages")
            )
    );

    public EventActivity() {
        super(R.layout.activity_event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new EventAdapter(getSupportFragmentManager()));
        getSupportActionBar().setTitle(TITLE);
    }

    private static class EventAdapter extends FragmentPagerAdapter {

        public EventAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return EventDayFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return SCHEDULE.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return DF.format(SCHEDULE.get(position).get(0).startTime);
        }
    }

    public static class EventDayFragment extends Fragment {
        private static final String ARGS_DAY = "day";
        private int day;
        private TableLayout table;

        public static EventDayFragment newInstance(int day) {
            EventDayFragment res = new EventDayFragment();
            Bundle args = new Bundle();
            args.putInt(ARGS_DAY, day);
            res.setArguments(args);
            return res;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.content_event, container, false);
            day = getArguments().getInt(ARGS_DAY, 0);
            table = (TableLayout) view.findViewById(R.id.event_table);
            List<EventScheduleEntry> events = SCHEDULE.get(day);

            int sumDuration = 0;
            for (EventScheduleEntry event : events) {
                sumDuration += event.duration;
            }

            for (EventScheduleEntry event : events) {
                View tr = inflater.inflate(R.layout.event_tablerow, table, false);
                TextView timeView = (TextView) tr.findViewById(R.id.time);
                TextView descriptionView = (TextView) tr.findViewById(R.id.event_description);

                tr.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, 0, event.duration / (float) sumDuration));
                timeView.setText(TF.format(event.startTime));
                descriptionView.setText(Utils.fromHtml(event.title));
                table.addView(tr);
            }
            return view;
        }
    }

    private static class EventScheduleEntry {
        public final Date startTime;
        public final int duration; // in minutes
        public final String title;

        public EventScheduleEntry(Date startTime, int duration, String title) {
            this.startTime = startTime;
            this.duration = duration;
            this.title = title;
        }
    }

    private static Date parse(String string) {
        try {
            return DTF.parse(string);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

}
