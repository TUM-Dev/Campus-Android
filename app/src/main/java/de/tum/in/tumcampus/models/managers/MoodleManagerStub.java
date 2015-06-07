package de.tum.in.tumcampus.models.managers;

import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampus.models.MoodleObject;
import de.tum.in.tumcampus.models.MoodleUserCourses;

/**
 * Created by a2k on 6/7/2015.
 * This class is a stub for MoodleManager
 */
public class MoodleManagerStub {

    private String userName;
    private String password;

    private final Map<String, String> USER_COURSES = new HashMap<String, String>(){{
            put("Deutsch als Fremdsprache B1.2",""); put("Praktikum - Betriebssysteme - Google Android (IN0012, IN2106, IN4004)","Welcome to the Android Practical Summer Term Course  (SS15) !");
            put("Basic  Moodle and Mountaineering", "This course is for senior students planning an ascent of Mont Blanc in July. It is also designed to take Moodle newbies through a number of activities showing off the best of Moodle.");
            put("Psychology in Cinema ","In this course we study three films: Spider, A Beautiful Mind, and Fight Club. The main focus of the course will be the ways in which psychosis is represented in the films in terms of macro, plot, narrative structure and micro etc. We consider the wider cultural meaning and implication of films dealing with psychology" );
    }};

    public Map<?,?> getCoursesList(){
        return USER_COURSES;
    }

    public Object getCourseInfo(String CourseFullName){
        /**
         * returns courses full documents. To be decided later
         */
        return null;
    }

}
