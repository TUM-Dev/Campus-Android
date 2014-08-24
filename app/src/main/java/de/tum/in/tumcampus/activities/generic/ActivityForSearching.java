package de.tum.in.tumcampus.activities.generic;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Generic class for searching. Provides basic functions for a text search field
 * and typical processes related to search.
 * 
 * @author Sascha Moecker
 * 
 */
public abstract class ActivityForSearching extends ActionBarActivity {
    protected static final int MIN_SEARCH_LENGTH = 4;

	protected RelativeLayout errorLayout;
	private int layoutId;
	protected RelativeLayout progressLayout;
    protected SearchView mSearchView;

    public ActivityForSearching(int layoutIt) {
		this.layoutId = layoutIt;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layoutId);

        progressLayout = (RelativeLayout) findViewById(R.id.progress_layout);
        errorLayout = (RelativeLayout) findViewById(R.id.error_layout);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_activity_for_searching, menu);

        // Get SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        // Optical tweaks to match application theme
        styleSearchView();

        if (mSearchView != null) {
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    requestSearch(s);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });
        }
		return true;
	}

    private void styleSearchView() {
        // Adjust small lense icon and hint text
        SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.append(getString(R.string.search));
        Drawable searchIcon = getResources().getDrawable(R.drawable.ic_action_search);
        int textSize = (int) (searchAutoComplete.getTextSize() * 1.4);
        searchIcon.setBounds(0, 0, textSize, textSize);
        ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        searchAutoComplete.setHint(ssb);
        searchAutoComplete.setHintTextColor(getResources().getColor(R.color.searchview_hint_color));
        searchAutoComplete.setTextColor(Color.WHITE);
        searchAutoComplete.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI|EditorInfo.IME_ACTION_SEARCH);

        // Adjust the color of the border shown below the EditText
        View searchPlate = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_plate);
        searchPlate.setBackgroundResource(R.drawable.searchview_textfield_holo_blue);

        // Set a new x symbol
        ImageView searchCloseIcon = (ImageView)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchCloseIcon.setImageResource(R.drawable.ic_action_cancel);

        // Replace collapsed icon
        ImageView collapsedSearchIcon = (ImageView) mSearchView.findViewById(android.support.v7.appcompat.R.id.search_button);
        Drawable bigSearchIcon = getResources().getDrawable(R.drawable.ic_action_search);
        collapsedSearchIcon.setImageDrawable(bigSearchIcon);
    }

    // Abstract method for the search algorithm, has to be implemented by the
	// inheriting class
	public abstract void performSearchAlgorithm(String query);

	private boolean requestSearch(String query) {
        if(query.length()<ActivityForSearching.MIN_SEARCH_LENGTH) {
            return false;
        }
		progressLayout.setVisibility(View.VISIBLE);

		if (!Utils.isConnected(this)) {
			Toast.makeText(this, R.string.no_internet_connection,
					Toast.LENGTH_SHORT).show();
			errorLayout.setVisibility(View.VISIBLE);
			progressLayout.setVisibility(View.GONE);
			return false;
		}

		Utils.hideKeyboard(this, mSearchView);

		performSearchAlgorithm(query);
		return true;
	}
}
