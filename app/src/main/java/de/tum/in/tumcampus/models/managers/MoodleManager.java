package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import java.util.List;
import java.util.Map;

import de.tum.in.tumcampus.models.MoodleCourse;
import de.tum.in.tumcampus.models.MoodleEvent;
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
    MoodleCourse moodleUserCourseInfo = null;

    public abstract MoodleToken getMoodleUserToken();
    public abstract void setMoodleUserToken(MoodleToken moodleUserToken);
    public abstract MoodleUser getMoodleUserInfo();
    public abstract void setMoodleUserInfo(MoodleUser moodleUserInfo);
    public abstract MoodleUserCourseList getMoodleUserCourseList();
    public abstract void setMoodleUserCourseList(MoodleUserCourseList moodleUserCourseList);
    public abstract MoodleCourse getMoodleUserCourseInfo();
    public abstract void setMoodleUserCourseInfo(MoodleCourse moodleUserCourseInfo);
    public abstract List<MoodleEvent> getUserEvents();
    public abstract Map<?,?> getCoursesList();

    /**
     * Moodle API Calls
     */
    public abstract void requestUserToken(Context currentContext, String username, String password);
    public abstract void requestUserData(Context currentContext);
    public abstract void requestUserCourseList(Context currentContext);
    public abstract void requestUserCourseInfo(Context currentContext, int courseId);


}
