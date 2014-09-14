package de.tum.in.tumcampus.auxiliary;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.widget.AbsListView;

/**
 * FIXME
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

        // Then we have to check whether the fas scroller is enabled, and check we're not starting
        // the gesture from the scroller
        /*if (ready && absListView.isFastScrollEnabled() && isFastScrollAlwaysVisible(absListView)) {
            switch (getVerticalScrollbarPosition(absListView)) {
                case View.SCROLLBAR_POSITION_RIGHT:
                    ready = x < absListView.getRight() - absListView.getVerticalScrollbarWidth();
                    break;
                case View.SCROLLBAR_POSITION_LEFT:
                    ready = x > absListView.getVerticalScrollbarWidth();
                    break;
            }
        }*/

        return ready;
    }

    int getVerticalScrollbarPosition(StickyListHeadersListView absListView) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                CompatV11.getVerticalScrollbarPosition(absListView) :
                Compat.getVerticalScrollbarPosition(absListView);
    }

    boolean isFastScrollAlwaysVisible(StickyListHeadersListView absListView) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                CompatV11.isFastScrollAlwaysVisible(absListView) :
                Compat.isFastScrollAlwaysVisible(absListView);
    }

    static class Compat {
        static int getVerticalScrollbarPosition(StickyListHeadersListView absListView) {
            return View.SCROLLBAR_POSITION_RIGHT;
        }
        static boolean isFastScrollAlwaysVisible(StickyListHeadersListView absListView) {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    static class CompatV11 {
        static int getVerticalScrollbarPosition(StickyListHeadersListView absListView) {
            return absListView.getVerticalScrollbarPosition();
        }
        static boolean isFastScrollAlwaysVisible(StickyListHeadersListView absListView) {
            return absListView.isFastScrollAlwaysVisible();
        }
    }
}