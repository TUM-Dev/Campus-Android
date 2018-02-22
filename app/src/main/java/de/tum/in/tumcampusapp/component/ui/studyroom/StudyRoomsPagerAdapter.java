package de.tum.in.tumcampusapp.component.ui.studyroom;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import de.tum.in.tumcampusapp.utils.Const;

/**
 * A {@link FragmentStatePagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class StudyRoomsPagerAdapter extends FragmentStatePagerAdapter {

    private int mCurrentStudyRoomGroupId;

    public StudyRoomsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * getItem is called to instantiate the fragment for the given page.
     *
     * @param position spinner position
     * @return the fragment
     */
    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new StudyRoomGroupDetailsFragment();
        Bundle args = new Bundle();
        args.putInt(Const.STUDY_ROOM_GROUP_ID, mCurrentStudyRoomGroupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return 1;
    }

    public void setStudyRoomGroupId(Activity mainActivity, int mSelectedStudyRoomGroupId) {
        mCurrentStudyRoomGroupId = mSelectedStudyRoomGroupId;
    }
}
