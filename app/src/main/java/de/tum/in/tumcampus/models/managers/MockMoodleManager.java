package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleEventsList;
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

    public MockMoodleManager(MoodleUpdateDelegate delegate) {
        super(delegate);
    }


    public Map<?,?> getCoursesList(){
         Map<String, String> USER_COURSES = new HashMap<String, String>(){{
            put("Deutsch als Fremdsprache B1.2",""); put("Praktikum - Betriebssysteme - Google Android (IN0012, IN2106, IN4004)","Welcome to the Android Practical Summer Term Course  (SS15) !");
            put("Basic  Moodle and Mountaineering", "This course is for senior students planning an ascent of Mont Blanc in July. It is also designed to take Moodle newbies through a number of activities showing off the best of Moodle.");
            put("Psychology in Cinema ","In this course we study three films: Spider, A Beautiful Mind, and Fight Club. The main focus of the course will be the ways in which psychosis is represented in the films in terms of macro, plot, narrative structure and micro etc. We consider the wider cultural meaning and implication of films dealing with psychology" );
        }};

        return USER_COURSES;
    }

    @Override
    public Map<?, ?> getCoursesId() {
        return null;
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
    public MoodleEventsList getMoodleUserEventsList() {
        //TODO create stub data and variables needed
        return null;
    }

    @Override
    public void setMoodleUserEventsList(MoodleEventsList moodleUserEventsList) {
        //TODO create stub data and variables needed
    }

    @Override
    public MoodleCourse getMoodleUserCourseInfo() {
        //TODO create stub data and variables needed
        String json = "[{\"id\":471,\"name\":\"Course welcome\",\"visible\":1,\"summary\":\"<p><img src=\\\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1107\\/course\\/section\\/471\\/PyschoBanner.png\\\" alt=\\\"Banner 3 Films Studied (Fair use)\\\" width=\\\"474\\\" height=\\\"214\\\" style=\\\"vertical-align:text-bottom;margin:0 .5em;\\\" class=\\\"img-responsive\\\" \\/><br \\/><\\/p><p>Message from your tutor:<a href=\\\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1107\\/course\\/section\\/471\\/coursewelcomePC.mp3\\\">coursewelcomePC.mp3<\\/a><\\/p>\",\"summaryformat\":1,\"modules\":[{\"id\":704,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/forum\\/view.php?id=704\",\"name\":\"Announcements from your tutor\",\"instance\":76,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/forum\\/1433827014\\/icon\",\"modname\":\"forum\",\"modplural\":\"Forums\",\"indent\":0},{\"id\":705,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/choice\\/view.php?id=705\",\"name\":\"Prior Knowledge assessment\",\"instance\":10,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/choice\\/1433827014\\/icon\",\"modname\":\"choice\",\"modplural\":\"Choices\",\"indent\":0},{\"id\":723,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/quiz\\/view.php?id=723\",\"name\":\"Factual recall test\",\"instance\":80,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/quiz\\/1433827014\\/icon\",\"modname\":\"quiz\",\"modplural\":\"Quizzes\",\"indent\":0},{\"id\":728,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/chat\\/view.php?id=728\",\"name\":\"Course chat\",\"instance\":3,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/chat\\/1433827014\\/icon\",\"modname\":\"chat\",\"modplural\":\"Chats\",\"indent\":0},{\"id\":756,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/choice\\/view.php?id=756\",\"name\":\"Let's make a date!\",\"instance\":12,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/choice\\/1433827014\\/icon\",\"modname\":\"choice\",\"modplural\":\"Choices\",\"indent\":0},{\"id\":778,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/wiki\\/view.php?id=778\",\"name\":\"wiki\",\"instance\":14,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/wiki\\/1433827014\\/icon\",\"modname\":\"wiki\",\"modplural\":\"Wikis\",\"indent\":0}]},{\"id\":472,\"name\":\"Background information\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[{\"id\":719,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/glossary\\/view.php?id=719\",\"name\":\"Concepts and Characters \",\"instance\":84,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/glossary\\/1433827014\\/icon\",\"modname\":\"glossary\",\"modplural\":\"Glossaries\",\"indent\":0},{\"id\":722,\"name\":\"Films reading:\",\"instance\":113,\"description\":\"<div class=\\\"no-overflow\\\"><h5>Films reading:<\\/h5><\\/div>\",\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/label\\/1433827014\\/icon\",\"modname\":\"label\",\"modplural\":\"Labels\",\"indent\":1},{\"id\":707,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/book\\/view.php?id=707\",\"name\":\"Useful links\",\"instance\":6,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/book\\/1433827014\\/icon\",\"modname\":\"book\",\"modplural\":\"Books\",\"indent\":0,\"contents\":[{\"type\":\"content\",\"filename\":\"structure\",\"filepath\":\"\\/\",\"filesize\":0,\"fileurl\":null,\"content\":\"[{\\\"title\\\":\\\"A beautiful Mind\\\",\\\"href\\\":\\\"33\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]},{\\\"title\\\":\\\"Fight Club\\\",\\\"href\\\":\\\"34\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]},{\\\"title\\\":\\\"Spider\\\",\\\"href\\\":\\\"35\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]}]\",\"timecreated\":1405700525,\"timemodified\":1408275058,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/33\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1116\\/mod_book\\/chapter\\/33\\/index.html\",\"content\":\"A beautiful Mind\",\"timecreated\":1405700525,\"timemodified\":1408275058,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/34\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1116\\/mod_book\\/chapter\\/34\\/index.html\",\"content\":\"Fight Club\",\"timecreated\":1405700525,\"timemodified\":1408275058,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/35\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1116\\/mod_book\\/chapter\\/35\\/index.html\",\"content\":\"Spider\",\"timecreated\":1405700525,\"timemodified\":1408275058,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null}]},{\"id\":708,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/book\\/view.php?id=708\",\"name\":\"Video resources\",\"instance\":7,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/book\\/1433827014\\/icon\",\"modname\":\"book\",\"modplural\":\"Books\",\"indent\":0,\"contents\":[{\"type\":\"content\",\"filename\":\"structure\",\"filepath\":\"\\/\",\"filesize\":0,\"fileurl\":null,\"content\":\"[{\\\"title\\\":\\\"Trailer: A beautiful mind\\\",\\\"href\\\":\\\"36\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]},{\\\"title\\\":\\\"Trailer: Fight club\\\",\\\"href\\\":\\\"37\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]},{\\\"title\\\":\\\"Trailer: Spider\\\",\\\"href\\\":\\\"38\\\\\\/index.html\\\",\\\"level\\\":0,\\\"subitems\\\":[]}]\",\"timecreated\":1405700678,\"timemodified\":1408275072,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/36\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1117\\/mod_book\\/chapter\\/36\\/index.html\",\"content\":\"Trailer: A beautiful mind\",\"timecreated\":1405700678,\"timemodified\":1408275072,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/37\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1117\\/mod_book\\/chapter\\/37\\/index.html\",\"content\":\"Trailer: Fight club\",\"timecreated\":1405700678,\"timemodified\":1408275072,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null},{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/38\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1117\\/mod_book\\/chapter\\/38\\/index.html\",\"content\":\"Trailer: Spider\",\"timecreated\":1405700678,\"timemodified\":1408275072,\"sortorder\":0,\"userid\":null,\"author\":null,\"license\":null}]},{\"id\":709,\"name\":\"Pyschology reading:\",\"instance\":112,\"description\":\"<div class=\\\"no-overflow\\\"><h5>Pyschology reading:<\\/h5><\\/div>\",\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/label\\/1433827014\\/icon\",\"modname\":\"label\",\"modplural\":\"Labels\",\"indent\":1},{\"id\":710,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/resource\\/view.php?id=710\",\"name\":\"Osborne:Transference\\/Counter transference in the Psycho-analysis process\",\"instance\":35,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/core\\/1433827014\\/f\\/pdf-24\",\"modname\":\"resource\",\"modplural\":\"Files\",\"indent\":0,\"contents\":[{\"type\":\"file\",\"filename\":\"2011SC213_SamuelOsborne.pdf\",\"filepath\":\"\\/\",\"filesize\":70884,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1119\\/mod_resource\\/content\\/1\\/2011SC213_SamuelOsborne.pdf?forcedownload=1\",\"timecreated\":1405701236,\"timemodified\":1405701249,\"sortorder\":1,\"userid\":8,\"author\":\"Heather Reyes\",\"license\":\"cc-sa\"}]},{\"id\":711,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/folder\\/view.php?id=711\",\"name\":\"Categories and Causes of Mental illness\",\"instance\":6,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/folder\\/1433827014\\/icon\",\"modname\":\"folder\",\"modplural\":\"Folders\",\"indent\":0,\"contents\":[{\"type\":\"file\",\"filename\":\"Classification of mental disorders.pdf\",\"filepath\":\"\\/\",\"filesize\":168217,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1120\\/mod_folder\\/content\\/4\\/Classification%20of%20mental%20disorders.pdf?forcedownload=1\",\"timecreated\":1412340981,\"timemodified\":1412340983,\"sortorder\":0,\"userid\":13,\"author\":\"Jeffrey Sanders\",\"license\":\"cc-sa\"},{\"type\":\"file\",\"filename\":\"CausesMentalIllness.docx\",\"filepath\":\"\\/\",\"filesize\":296084,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1120\\/mod_folder\\/content\\/4\\/CausesMentalIllness.docx?forcedownload=1\",\"timecreated\":1412341071,\"timemodified\":1412341073,\"sortorder\":0,\"userid\":13,\"author\":\"Jeffrey Sanders\",\"license\":\"cc-sa\"},{\"type\":\"file\",\"filename\":\"PsychoDefinitions.odt\",\"filepath\":\"\\/\",\"filesize\":20784,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1120\\/mod_folder\\/content\\/4\\/PsychoDefinitions.odt?forcedownload=1\",\"timecreated\":1412341191,\"timemodified\":1412341192,\"sortorder\":0,\"userid\":13,\"author\":\"Jeffrey Sanders\",\"license\":\"cc-sa\"}]}]},{\"id\":473,\"name\":\"Analysis\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[{\"id\":706,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/forum\\/view.php?id=706\",\"name\":\"Course discussion\",\"instance\":77,\"description\":\"<div class=\\\"no-overflow\\\">In this space we'll discuss aspects of the films  and of psychology in cinema in general. Feel free to raise issues which you think might help you with later assignments and group projects. This is a hive mind area! You can rate others and view ratings.<\\/div>\",\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/forum\\/1433827014\\/icon\",\"modname\":\"forum\",\"modplural\":\"Forums\",\"indent\":0},{\"id\":724,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/assign\\/view.php?id=724\",\"name\":\"From Concept to Reality: Trauma and Film\",\"instance\":111,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/assign\\/1433827014\\/icon\",\"modname\":\"assign\",\"modplural\":\"Assignments\",\"indent\":0}]},{\"id\":474,\"name\":\"Group Projects and Individual tasks\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[{\"id\":714,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/choice\\/view.php?id=714\",\"name\":\"Select your focus film\",\"instance\":11,\"description\":\"<div class=\\\"no-overflow\\\"><p>Select here the film you wish to do your in depth group study on. Groups are limited so act fast!<\\/p><\\/div>\",\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/choice\\/1433827014\\/icon\",\"modname\":\"choice\",\"modplural\":\"Choices\",\"indent\":0},{\"id\":715,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/assign\\/view.php?id=715\",\"name\":\"Group Project\",\"instance\":107,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/assign\\/1433827014\\/icon\",\"modname\":\"assign\",\"modplural\":\"Assignments\",\"indent\":0},{\"id\":716,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/assign\\/view.php?id=716\",\"name\":\"Dissertation: Fight club\",\"instance\":108,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/assign\\/1433827014\\/icon\",\"modname\":\"assign\",\"modplural\":\"Assignments\",\"indent\":0},{\"id\":757,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/quiz\\/view.php?id=757\",\"name\":\"Grammar help with your essays\",\"instance\":83,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/quiz\\/1433827014\\/icon\",\"modname\":\"quiz\",\"modplural\":\"Quizzes\",\"indent\":0},{\"id\":760,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/forum\\/view.php?id=760\",\"name\":\"Discussions about your group projects\",\"instance\":81,\"description\":\"<div class=\\\"no-overflow\\\"><p>A forum in separate groups so you can all talk about your group project without being distracted by others\\u00a0;-)<\\/p><\\/div>\",\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/forum\\/1433827014\\/icon\",\"modname\":\"forum\",\"modplural\":\"Forums\",\"indent\":0}]},{\"id\":475,\"name\":\"Reflection and Feedback\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[{\"id\":713,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/survey\\/view.php?id=713\",\"name\":\"Survey: COLLES\",\"instance\":6,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/survey\\/1433827014\\/icon\",\"modname\":\"survey\",\"modplural\":\"Surveys\",\"indent\":0},{\"id\":725,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/wiki\\/view.php?id=725\",\"name\":\"Your course notes wiki (collaborative)\",\"instance\":13,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/wiki\\/1433827014\\/icon\",\"modname\":\"wiki\",\"modplural\":\"Wikis\",\"indent\":0},{\"id\":712,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/wiki\\/view.php?id=712\",\"name\":\"Your course notes wiki (Private)\",\"instance\":12,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/wiki\\/1433827014\\/icon\",\"modname\":\"wiki\",\"modplural\":\"Wikis\",\"indent\":0},{\"id\":720,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/feedback\\/view.php?id=720\",\"name\":\"Feedback: Psychology in Cinema Evaluation\",\"instance\":10,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/feedback\\/1433827014\\/icon\",\"modname\":\"feedback\",\"modplural\":\"Feedback\",\"indent\":0},{\"id\":748,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/assign\\/view.php?id=748\",\"name\":\"Reflective journal\",\"instance\":113,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/assign\\/1433827014\\/icon\",\"modname\":\"assign\",\"modplural\":\"Assignments\",\"indent\":0}]},{\"id\":487,\"name\":\"Topic 5\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":488,\"name\":\"Module 1\",\"visible\":1,\"summary\":\"<p>anwnciw<br \\/><\\/p>\",\"summaryformat\":1,\"modules\":[{\"id\":763,\"url\":\"http:\\/\\/school.demo.moodle.net\\/mod\\/page\\/view.php?id=763\",\"name\":\"Lesson 1\",\"instance\":78,\"visible\":1,\"modicon\":\"http:\\/\\/school.demo.moodle.net\\/theme\\/image.php\\/more\\/page\\/1433827014\\/icon\",\"modname\":\"page\",\"modplural\":\"Pages\",\"indent\":0,\"contents\":[{\"type\":\"file\",\"filename\":\"index.html\",\"filepath\":\"\\/\",\"filesize\":0,\"fileurl\":\"http:\\/\\/school.demo.moodle.net\\/webservice\\/pluginfile.php\\/1245\\/mod_page\\/content\\/index.html?forcedownload=1\",\"timecreated\":null,\"timemodified\":1433845855,\"sortorder\":1,\"userid\":null,\"author\":null,\"license\":null}]}]},{\"id\":489,\"name\":\"Topic 7\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":490,\"name\":\"Topic 8\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":491,\"name\":\"Topic 9\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":492,\"name\":\"Topic 10\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":493,\"name\":\"Topic 11\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":494,\"name\":\"Topic 12\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":495,\"name\":\"Topic 13\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":496,\"name\":\"Topic 14\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":497,\"name\":\"Topic 15\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]},{\"id\":498,\"name\":\"Topic 16\",\"visible\":1,\"summary\":\"\",\"summaryformat\":1,\"modules\":[]}]";
        MoodleCourse course = new MoodleCourse(json);
        return course;
    }

    @Override
    public void setMoodleUserCourseInfo(MoodleCourse moodleUserCourseInfo) {
        //TODO create stub data and variables needed

    }

    @Override
    public ArrayList<MoodleEvent> getUserEvents() {
        ArrayList<MoodleEvent> mockEvents = new ArrayList<MoodleEvent>();

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

    @Override
    public void requestUserEvents(Context currentContext) {
        //TODO create stub data and variables needed

    }

    @Override
    public String getToken(){
        return "thisisaToken";
    }
}
