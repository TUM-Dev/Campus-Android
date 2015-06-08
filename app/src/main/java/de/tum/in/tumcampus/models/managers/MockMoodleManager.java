package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.MoodleUser;
import de.tum.in.tumcampus.models.MoodleUserCourseList;

/**
 * Created by a2k on 6/7/2015.
 * This class is a stub for RealMoodleManager
 */
public class MockMoodleManager extends MoodleManager{

    private String userName;
    private String password;


    public Map<?,?> getCoursesList(){
         Map<String, String> USER_COURSES = new HashMap<String, String>(){{
            put("Deutsch als Fremdsprache B1.2",""); put("Praktikum - Betriebssysteme - Google Android (IN0012, IN2106, IN4004)","Welcome to the Android Practical Summer Term Course  (SS15) !");
            put("Basic  Moodle and Mountaineering", "This course is for senior students planning an ascent of Mont Blanc in July. It is also designed to take Moodle newbies through a number of activities showing off the best of Moodle.");
            put("Psychology in Cinema ","In this course we study three films: Spider, A Beautiful Mind, and Fight Club. The main focus of the course will be the ways in which psychosis is represented in the films in terms of macro, plot, narrative structure and micro etc. We consider the wider cultural meaning and implication of films dealing with psychology" );
        }};

        return USER_COURSES;
    }

    public Object getCourseInfo(String CourseFullName){
        /**
         * returns courses full documents. To be decided later
         */
        return null;
    }



    @Override
    public MoodleToken getMoodleUserToken() {
        //TODO create stub data and variables needed
        return null;
    }

    @Override
    public void setMoodleUserToken(MoodleToken moodleUserToken) {
        //TODO create stub data and variables needed

    }

    @Override
    public MoodleUser getMoodleUserInfo() {
        //TODO create stub data and variables needed
        return null;
    }

    @Override
    public void setMoodleUserInfo(MoodleUser moodleUserInfo) {

    }

    @Override
    public MoodleUserCourseList getMoodleUserCourseList() {
        //TODO create stub data and variables needed
        return null;
    }

    @Override
    public void setMoodleUserCourseList(MoodleUserCourseList moodleUserCourseList) {
        //TODO create stub data and variables needed

    }

    @Override
    public MoodleCourse getMoodleUserCourseInfo() {
        //TODO create stub data and variables needed

        return null;
    }

    @Override
    public void setMoodleUserCourseInfo(MoodleCourse moodleUserCourseInfo) {
        //TODO create stub data and variables needed

    }

    @Override
    public List<MoodleEvent> getUserEvents() {
        List<MoodleEvent> mockEvents = new ArrayList<MoodleEvent>();
        mockEvents.add(new MoodleEvent("Planning Meeting for Field Trip","Our group is meeting in Room 78 to " +
                "decide our itinerary for the Field Trip",1437575700,3600));

        mockEvents.add(new MoodleEvent("Midterm examp Deautch als fremd Sprache B1.2","Every one should be available in the class. "
                ,1437665700,1800));

        mockEvents.add(new MoodleEvent("Praktikum introduction Cloud databases","Attendence is mandatory",
                1435330800,2000));
        return mockEvents;
    }

    @Override
    public void requestUserToken(Context currentContext, String username, String password) {
        //TODO create stub data and variables needed
    }

    @Override
    public void requestUserData(Context currentContext) {
        //TODO create stub data and variables needed
    }

    @Override
    public void requestUserCourseList(Context currentContext) {
        //TODO create stub data and variables needed
    }

    @Override
    public void requestUserCourseInfo(Context currentContext, int courseId) {
        //TODO create stub data and variables needed

    }
}
