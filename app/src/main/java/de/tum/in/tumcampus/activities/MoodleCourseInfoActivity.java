package de.tum.in.tumcampus.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.net.URL;
import java.util.List;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForDownloadingExternal;
import de.tum.in.tumcampus.activities.generic.ProgressActivity;
import de.tum.in.tumcampus.adapters.MoodleCourseInfoExpandableAdapter;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleCourseContent;
import de.tum.in.tumcampus.models.MoodleCourseModule;
import de.tum.in.tumcampus.models.MoodleCourseSection;
import de.tum.in.tumcampus.models.managers.MoodleManager;
import de.tum.in.tumcampus.models.managers.MoodleUpdateDelegate;
import de.tum.in.tumcampus.models.managers.RealMoodleManager;

/**
 * Created by a2k on 6/10/2015.
 * This activity shows the contents of a course page in moodle. The user can choose the contents, which he
 * is interested in, and they if they are files, they will be downloaded, otherwise a browser will be opened
 * to show the corresponding URL.
 */
public class MoodleCourseInfoActivity extends ProgressActivity implements MoodleUpdateDelegate, ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupClickListener {


    MoodleManager realManager;
    private Intent intent;
    private String courseName;
    private int courseId;
    private MoodleCourseInfoExpandableAdapter dataAdapter;
    private List<MoodleCourseSection> sections;
    private ExpandableListView view_course_sections;
    private TextView title;

    public MoodleCourseInfoActivity() {super(R.layout.activity_moodle_course_info);}

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        intent = getIntent();
        baseSetup();
        showLoadingStart();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        refresh();
    }

    /**
     *
     * this method is called
     * when internet connection is back
     */
    @Override
    public void onRefresh() {
        realManager.requestUserCourseInfo(this, courseId);
        showLoadingStart();
        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_moodle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.moodle_my_courses:
                // don't need to start my_courses
                // it is already on the activity stack!
                finish();
                return true;
            case R.id.events:
                Intent eventIntent = new Intent(this,MoodleEventsActivity.class);
                eventIntent.putExtra("user_token", realManager.getToken());
                startActivity(eventIntent);
                finish();
                return true;

        }
        return false;
    }


    /**
     * handles the click events on the child items in the expandable list
     * @param parent
     * @param v
     * @param groupPosition
     * @param childPosition
     * @param id
     * @return
     */
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        try {
            MoodleCourseContent content = (MoodleCourseContent) dataAdapter.getChild(groupPosition, childPosition);
            if (content != null) {
                URL fileURL = content.getFileurl();
                String urlString = fileURL.toString();

                // if this url was related to moodle add the token
                if (urlString.indexOf("school.demo.moodle.net") >= 0 ||
                        urlString.indexOf("moodle.tum.de") >= 0)
                    urlString = completeURL(urlString);

                if (urlString != null){
                    Utils.log("Got this URL " + urlString);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                    showLoadingStart();
                    startActivity(browserIntent);
                    return true;
                }else {
                    Utils.showToast(this.getApplicationContext(), R.string.no_url);
                    return true;
                }
            } else
                Utils.log(String.format("No Content found for group posiiton %d and childPosition %d", groupPosition, childPosition));
            return false;
        }catch (Exception e) {
            Utils.log(e);
            Utils.showToast(this.getApplicationContext(), "Sorry unable to find the URL");
            return false;
        }
    }

    /**
     *  this method completes the url sent as input adding the user toking
     * @param urlString
     * @return newUrlString
     */
    private String completeURL(String urlString ) {
        String newUrlString;

        if (urlString.contains("?"))
            newUrlString = urlString + "&token=" + realManager.getToken();
        else
            newUrlString = "?token=" + realManager.getToken();

        return newUrlString;
    }
    /**
     * handles the event on clicks on the group items of the expandable list. If it is a section header,
     * does nothing. If it is a module and it has children, expand the group. if it does not have children
     * it means that it has an URL associated with it. Open this URL in a new browser activity.
     * @param parent
     * @param v
     * @param groupPosition
     * @param id
     * @return
     */
    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        try {

            if (dataAdapter.getChildrenCount(groupPosition) == 0) {
                Object header = dataAdapter.getGroup(groupPosition);

                // do nothing for a course section
                if (header instanceof MoodleCourseSection)
                    return true;
                else {
                    URL url = ((MoodleCourseModule) header).getUrl();
                    if (url != null) {

                        String urlWithToken = completeURL(url.toString());
                        Utils.log(String.format("Got this URL %s", url.toString()));
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlWithToken));
                        showLoadingStart();
                        startActivity(browserIntent);
                        return true;
                    } else {
                        Utils.showToast(this.getApplicationContext(), R.string.no_url);
                        return true;
                    }
                }
            } else
                // if this group view has childs just expand or hide no URL is available
                if (parent.isGroupExpanded(groupPosition) == false) {

                    //  API Level 14+ allows you to animate the Group expansion...
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                        parent.expandGroup(groupPosition, true);

                        //  else just expand the Group without animation
                    else
                        parent.expandGroup(groupPosition);
                } else
                    //  collapse the ListView Group/s
                    parent.collapseGroup(groupPosition);
            return true;
        }catch(Exception e){
            Utils.log(e);
            return false;
        }
    }

    private void baseSetup(){
        try {
            // getting info from intent
            courseName = intent.getStringExtra("course_name");
            courseName = (courseName!=null) ? courseName : getString(R.string.moodle_course_name_not_found);
            courseId = intent.getIntExtra("course_id", -1);
            if (courseId == -1) {
                Utils.log(String.format("Warning! course id is -1=defaultValue for course %s", courseName));
                Utils.showToast(this, getResources().getString(R.string.moodle_course_id_not_found));
                return;
            }

            realManager = RealMoodleManager.getInstance(this, this);
            realManager.requestUserCourseInfo(this, courseId);

            //UI setup and adapter
            baseUISetup();

        }catch (Exception e){
            Utils.log(e);
            Utils.showToast(this, getResources().getString(R.string.error_something_wrong));
            finish();
        }
    }

    public void baseUISetup(){

        // initializing UI elements
        view_course_sections = (ExpandableListView) findViewById(R.id.moodleCourseSections);
        view_course_sections.setOnGroupClickListener(this);
        view_course_sections.setOnChildClickListener(this);
        title = (TextView) findViewById(R.id.courseTitle);
        title.setText(courseName);

        //setting the dataAdapter
        dataAdapter = new MoodleCourseInfoExpandableAdapter(sections, this);
        view_course_sections.setAdapter(dataAdapter);

    }

    /**
     * called by moodleManager when the requested data is ready
     */
    @Override
    public void refresh() {
        try {
            if (!NetUtils.isConnected(this)) {
                Utils.showToast(this, R.string.no_internet_connection);
                showNoInternetLayout();
                return;
            }
            MoodleCourse moodleCourse = realManager.getMoodleUserCourseInfo();
            sections = moodleCourse.getSections();
            //setting the dataAdapter
            dataAdapter = new MoodleCourseInfoExpandableAdapter(sections, this);
            view_course_sections.setAdapter(dataAdapter);
            showLoadingEnded();
        } catch (Exception e) {
            Utils.log(e);
            Utils.showToast(this, R.string.error_something_wrong);
            showLoadingEnded();
        }

    }
}
