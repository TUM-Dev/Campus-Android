package de.tum.in.tumcampusapp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.activities.SearchActivity;
import de.tum.in.tumcampusapp.adapters.SearchPagerAdapter;
import de.tum.in.tumcampusapp.auxiliary.Const;
import de.tum.in.tumcampusapp.data.SearchAction;
import de.tum.in.tumcampusapp.data.SearchLecture;
import de.tum.in.tumcampusapp.data.SearchPerson;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Fragment for each start-category-page.
 */
public class SearchFragment extends SherlockFragment implements OnItemClickListener, TUMOnlineRequestFetchListener {

	private Activity activity;
	private TUMOnlineRequest requestHandler;

	/** UI Elements */
	private View rootView;
	private ListView lvFound;

	/** Default layouts for user interaction */
	private RelativeLayout noTokenLayout;
	private RelativeLayout progressLayout;
	private RelativeLayout emptyLayout;
	private RelativeLayout errorLayout;
	private RelativeLayout failedTokenLayout;

	/** Our specifc settings for the indiviudal types */
	private SearchAction action;

	private int type;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.activity = this.getActivity();

		// Get our Type
		this.type = this.getArguments().getInt(SearchPagerAdapter.argPos);

		// If valid inflate the resulting layout with the results
		if (this.type >= 0) {
			this.rootView = inflater.inflate(R.layout.fragment_search_results, container, false);
			this.progressLayout = (RelativeLayout) this.activity.findViewById(R.id.progress_layout);
			this.failedTokenLayout = (RelativeLayout) this.activity.findViewById(R.id.failed_layout);
			this.noTokenLayout = (RelativeLayout) this.activity.findViewById(R.id.no_token_layout);
			this.errorLayout = (RelativeLayout) this.activity.findViewById(R.id.error_layout);
			this.emptyLayout = (RelativeLayout) this.activity.findViewById(R.id.empty_layout);
		} else {
			this.rootView = inflater.inflate(R.layout.layout_error, container, false);
			return this.rootView;
		}

		// bind GUI elements
		this.lvFound = (ListView) this.activity.findViewById(R.id.lvFound);

		// Do some specific stuff
		switch (this.type) {
		case SearchPagerAdapter.TAB_ROOMS:

			break;
		case SearchPagerAdapter.TAB_PERSONS:
			this.action = new SearchPerson(this.activity);
			break;
		case SearchPagerAdapter.TAB_LVS:
			this.action = new SearchLecture(this.activity);
			break;
		default:

			break;
		}

		return this.rootView;
	}

	public void doSearch(String query) {
		// If we don't have our handle do nothing
		if (this.action == null) {
			Log.d(this.getClass().getSimpleName() + this.type, "No action for " + this.getArguments().getInt(SearchPagerAdapter.argPos));
			return;
		}

		// Create our new handle
		this.requestHandler = new TUMOnlineRequest(this.action.getTumAction(), this.activity);

		// set the query string as parameter for the TUMOnline request
		this.requestHandler.setParameter("pSuche", query);

		// Check if we have a Token
		String accessToken = PreferenceManager.getDefaultSharedPreferences(this.activity).getString(Const.ACCESS_TOKEN, null);
		if (accessToken == null) {
			Log.i(this.getClass().getSimpleName() + this.type, "No token was set");
			this.showNoTok(true);
			return;
		}

		// Do the pull
		Log.i(this.getClass().getSimpleName() + this.type, "TUMOnline token is <" + accessToken + ">");
		this.showProgress(true);
		this.requestHandler.fetchInteractive(this.activity, this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

	}

	@Override
	public void onFetch(String rawResponse) {
		BaseAdapter adapter = null;
		try {
			adapter = this.action.handleResponse(rawResponse);
		} catch (Exception e) {
			Log.d(this.getClass().getSimpleName() + this.type, "wont work: " + e.getMessage());
			this.showError(true);
			Toast.makeText(this.activity, R.string.no_search_result, Toast.LENGTH_SHORT).show();
		}

		if (adapter == null) {
			this.lvFound.setAdapter(null);
			this.showEmpty(true);
			Log.d(this.getClass().getSimpleName() + this.type, "Returned Adapter empty");
			return;
		}
		Log.d(this.getClass().getSimpleName() + this.type, "Returned Adapter has " + adapter.getCount());

		// Setup the adapter
		this.lvFound.setAdapter(adapter);

		// deal with clicks on items in the ListView
		this.lvFound.setOnItemClickListener(new OnItemClickListener() {
			@SuppressWarnings("static-access")
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				// each item represents the current FindLecturesRow
				// item
				Object o = SearchFragment.this.lvFound.getItemAtPosition(position);

				Intent i = new Intent(SearchFragment.this.activity, SearchFragment.this.action.getDetailsActivity());
				i.putExtras(SearchFragment.this.action.getBundle(o));
				// load LectureDetails
				SearchFragment.this.startActivity(i);
			}
		});

		// Hide all messages
		this.hideAll();

		// Tell pager to do something
		((SearchActivity) this.activity).pagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onFetchCancelled() {
		// TODO Auto-generated method stub

	}

	private void showError(boolean show) {
		if (show) {
			this.hideAll();
			this.errorLayout.setVisibility(View.VISIBLE);
		} else {
			this.errorLayout.setVisibility(View.GONE);
		}

	}

	private void showProgress(boolean show) {
		if (show) {
			this.hideAll();
			this.progressLayout.setVisibility(View.VISIBLE);
		} else {
			this.progressLayout.setVisibility(View.GONE);
		}
	}

	private void showNoTok(boolean show) {
		if (show) {
			this.hideAll();
			this.noTokenLayout.setVisibility(View.VISIBLE);
		} else {
			this.noTokenLayout.setVisibility(View.GONE);
		}
	}

	private void showTokErr(boolean show) {
		if (show) {
			this.hideAll();
			this.failedTokenLayout.setVisibility(View.VISIBLE);
		} else {
			this.failedTokenLayout.setVisibility(View.GONE);
		}
	}

	private void showEmpty(boolean show) {
		if (show) {
			this.hideAll();
			this.emptyLayout.setVisibility(View.VISIBLE);
		} else {
			this.emptyLayout.setVisibility(View.GONE);
		}
	}

	private void hideAll() {
		this.showError(false);
		this.showNoTok(false);
		this.showProgress(false);
		this.showTokErr(false);
		this.showEmpty(false);
	}

	@Override
	public void onCommonError(String errorReason) {
		Toast.makeText(this.activity, errorReason, Toast.LENGTH_SHORT).show();
		this.showError(true);
	}

	@Override
	public void onFetchError(String errorReason) {
		Toast.makeText(this.activity, errorReason, Toast.LENGTH_SHORT).show();
		this.showTokErr(true);
	}

}
