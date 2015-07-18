package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Map;

import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleEvent;
import de.tum.in.tumcampus.models.MoodleEventsList;
import de.tum.in.tumcampus.models.MoodleToken;
import de.tum.in.tumcampus.models.MoodleUser;
import de.tum.in.tumcampus.models.MoodleUserCourseList;

/**
 * Created by carlodidomenico on 08/06/15.
 * Interface that implements the MockObject Pattern in order to have a mockobject that will represent
 * the calls to the MoodleAPI.
 */
public abstract class MoodleManager {

    MoodleToken moodleUserToken = null;
    MoodleUser moodleUserInfo = null;
    MoodleUserCourseList moodleUserCourseList = null;


    MoodleEventsList moodleUserEventsList = null;
    MoodleCourse moodleUserCourseInfo = null;
    MoodleUpdateDelegate delegate;

    public abstract MoodleToken getMoodleUserToken();

    public abstract void setMoodleUserToken(MoodleToken moodleUserToken);

    public abstract MoodleUser getMoodleUserInfo();

    public abstract void setMoodleUserInfo(MoodleUser moodleUserInfo);

    public abstract MoodleUserCourseList getMoodleUserCourseList();

    public abstract void setMoodleUserCourseList(MoodleUserCourseList moodleUserCourseList);

    public abstract MoodleCourse getMoodleUserCourseInfo();

    public abstract void setMoodleUserCourseInfo(MoodleCourse moodleUserCourseInfo);

    public abstract ArrayList<MoodleEvent> getUserEvents();

    public abstract Map<String, String> getCoursesList();

    public abstract Map<String, Integer> getCoursesId();

    public abstract MoodleEventsList getMoodleUserEventsList();

    public abstract void setMoodleUserEventsList(MoodleEventsList moodleUserEventsList);

    public abstract String getToken();

    /**
     * Moodle API Calls
     */
    public abstract void requestUserToken(Context currentContext, String username, String password);

    public abstract void requestUserData(Context currentContext);

    public abstract void requestUserCourseList(Context currentContext);

    public abstract void requestUserCourseInfo(Context currentContext, int courseId);

    public abstract void requestUserEvents(Context currentContext);

    public abstract boolean loadUserToken();

    public MoodleManager(MoodleUpdateDelegate delegate) {
        setDelegate(delegate);
    }

    public MoodleUpdateDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(MoodleUpdateDelegate delegate) {
        this.delegate = delegate;
    }

}
