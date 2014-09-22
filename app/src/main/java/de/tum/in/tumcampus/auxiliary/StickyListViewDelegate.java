package de.tum.in.tumcampus.auxiliary;

import android.view.View;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;

/**
 * Wrapper class to make {@link se.emilsjolander.stickylistheaders.StickyListHeadersListView} usable
 * with {@link uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout}
 */
public class StickyListViewDelegate implements ViewDelegate {

    @Override
    public boolean isReadyForPull(View view, final float x, final float y) {
        boolean ready = false;

        // First we check whether we're scrolled to the top
        StickyListHeadersListView absListView = (StickyListHeadersListView) view;
        if (absListView.getCount() == 0) {
            ready = true;
        } else if (absListView.getFirstVisiblePosition() == 0) {
            final View firstVisibleChild = absListView.getChildAt(0);
            ready = firstVisibleChild != null && firstVisibleChild.getTop() >= absListView.getPaddingTop();
        }
        return ready;
    }
}