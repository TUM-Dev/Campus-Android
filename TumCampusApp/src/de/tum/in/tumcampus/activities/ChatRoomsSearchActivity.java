package de.tum.in.tumcampus.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.generic.ActivityForAccessingTumOnline;
import de.tum.in.tumcampus.adapters.ChatRoomsListAdapter;
import de.tum.in.tumcampus.auxiliary.ChatClient;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.PersonalLayoutManager;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.ChatMember;
import de.tum.in.tumcampus.models.ChatRegistrationId;
import de.tum.in.tumcampus.models.ChatRoom;
import de.tum.in.tumcampus.models.LecturesSearchRow;
import de.tum.in.tumcampus.models.LecturesSearchRowSet;

/**
 * This activity presents the chat rooms of user's 
 * lectures using the TUMOnline web service
 * 
 * @author Jana Pejic
 */
public class ChatRoomsSearchActivity extends ActivityForAccessingTumOnline {	

	/** filtered list which will be shown */
	LecturesSearchRowSet lecturesList = null;

	/** UI elements */
	private ListView lvMyLecturesList;
	private Spinner spFilter;
	
	private ChatRoom currentChatRoom = null;
	private ChatMember currentChatMember = null;
	
	public static final String EXTRA_MESSAGE = "message";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	
	String SENDER_ID = "1028528438269";
	
	static final String TAG = "GCM";
	
	GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    String regId;
	
	/**
	 * 
	 * @param method - The method which should be invoked by the TUMOnline Fetcher
	 * @param layoutId - Default layouts for user interaction
	 */
	public ChatRoomsSearchActivity(String method, int layoutId) {
		super(method, layoutId);
	}
	
	public ChatRoomsSearchActivity() {
		this(Const.LECTURES_PERSONAL, R.layout.activity_chat_rooms);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// bind UI elements
		lvMyLecturesList = (ListView) findViewById(R.id.lvMyLecturesList);
		spFilter = (Spinner) findViewById(R.id.spFilter);

		super.requestFetch();
		// Count the number of times the user used this activity for intelligent reordering 
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (sharedPrefs.getBoolean("implicitly_id", true)) {
			ImplicitCounter.Counter(Const.CHAT_ROOMS_ID, getApplicationContext());
		}
		
		populateCurrentChatMember(sharedPrefs);
	}

	private void populateCurrentChatMember(final SharedPreferences sharedPrefs) {
		String lrzId = sharedPrefs.getString(Const.LRZ_ID, ""); // TODO: what if it's empty?
		// TODO: Ne moze ovako, moram da proverim ima li ga na serveru!!!		
		if (sharedPrefs.contains(Const.CHAT_ROOM_DISPLAY_NAME)) {
			// If this is not the first time this user is opening the chat,
			// we GET their data from the server using their lrzId
			List<ChatMember> members = ChatClient.getInstance().getMember(lrzId);
			currentChatMember = members.get(0);
			checkPlayServicesAndRegister();
		} else {
			// If the user is opening the chat for the first time, we need to display
			// a dialog where they can enter their desired display name
			currentChatMember = new ChatMember(lrzId);
			
			LinearLayout layout = new LinearLayout(this);
			layout.setOrientation(LinearLayout.VERTICAL);

			final EditText etDisplayName = new EditText(this);
			etDisplayName.setHint(R.string.display_name);
			layout.addView(etDisplayName);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomsSearchActivity.this);
			builder.setTitle(R.string.chat_display_name_title)
				.setView(layout)
				.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						currentChatMember.setDisplayName(etDisplayName.getText().toString()); // TODO: Disallow empty display name
						
						// Save display name in shared preferences
						Editor editor = sharedPrefs.edit();
						editor.putString(Const.CHAT_ROOM_DISPLAY_NAME, currentChatMember.getDisplayName());
						editor.commit();
						
						// After the user has entered their display name, 
						// send a request to the server to create the new member
						currentChatMember = ChatClient.getInstance().createMember(currentChatMember);
						
						checkPlayServicesAndRegister();
					}
				});
			
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	
	
	@Override
	public void onFetch(String rawResponse) {
		// deserialize the XML
		Serializer serializer = new Persister();
		try {
			lecturesList = serializer.read(LecturesSearchRowSet.class, rawResponse);
		} catch (Exception e) {
			Log.d("SIMPLEXML", "wont work: " + e.getMessage());
			progressLayout.setVisibility(View.GONE);
			failedTokenLayout.setVisibility(View.VISIBLE);
			e.printStackTrace();
		}

		// set Spinner data (semester)
		List<String> filters = new ArrayList<String>();

		try { // NTK quickfix

			filters.add(getString(R.string.all));
			for (int i = 0; i < lecturesList.getLehrveranstaltungen().size(); i++) {
				String item = lecturesList.getLehrveranstaltungen().get(i).getSemester_id();
				if (filters.indexOf(item) == -1) {
					filters.add(item);
				}
			}

			// simple adapter for the spinner
			ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, filters);
			spFilter.setAdapter(spinnerArrayAdapter);
			spFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

				/**
				 * if an item in the spinner is selected, we have to filter the
				 * results which are displayed in the ListView
				 * 
				 * -> tList will be the data which will be passed to the
				 * FindLecturesListAdapter
				 */
				@Override
				public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					String filter = spFilter.getItemAtPosition(arg2).toString();
					if (filter == getString(R.string.all)) {
						setListView(lecturesList.getLehrveranstaltungen());
					} else {
						// do filtering for the given semester
						List<LecturesSearchRow> filteredList = new ArrayList<LecturesSearchRow>();
						for (int i = 0; i < lecturesList.getLehrveranstaltungen().size(); i++) {
							LecturesSearchRow item = lecturesList.getLehrveranstaltungen().get(i);
							if (item.getSemester_id().equals(filter)) {
								filteredList.add(item);
							}
						}
						// listview gets filtered list
						setListView(filteredList);
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// select [Alle], if none selected either
					spFilter.setSelection(0);
					setListView(lecturesList.getLehrveranstaltungen());
				}
			});

			setListView(lecturesList.getLehrveranstaltungen());
			progressLayout.setVisibility(View.GONE);

		} catch (Exception e) { // NTK quickfix
			Log.e("TumCampus", "No lectures available" + e.getMessage());
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		PersonalLayoutManager.setColorForId(this, R.id.spFilter);
        // Check device for Play Services APK.
        checkPlayServices();
	}

	/**
	 * Sets all data concerning the FindLecturesListView.
	 * 
	 * @param lecturesList
	 *            filtered list of lectures
	 */
	private void setListView(List<LecturesSearchRow> lecturesList) {
		// set ListView to data via the FindLecturesListAdapter
		lvMyLecturesList.setAdapter(new ChatRoomsListAdapter(this, lecturesList));

		// handle on click events by showing its LectureDetails
		lvMyLecturesList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				LecturesSearchRow item = (LecturesSearchRow) lvMyLecturesList.getItemAtPosition(position);

				// set bundle for LectureDetails and show it
				Bundle bundle = new Bundle();
				final Intent intent = new Intent(ChatRoomsSearchActivity.this, ChatActivity.class);
				intent.putExtras(bundle);
				
				String chatRoomUid = item.getSemester_id() + ":" + item.getTitel();
				
				currentChatRoom = new ChatRoom(chatRoomUid);
				ChatClient.getInstance().createGroup(currentChatRoom, new Callback<ChatRoom>() {	
					@Override
					public void success(ChatRoom newlyCreatedChatRoom, Response arg1) {
						// The POST request is successful because the chat room did not exist
						// The newly created chat room is returned
						Log.d("Success creating chat room", newlyCreatedChatRoom.toString());
						currentChatRoom = newlyCreatedChatRoom;
						
						showTermsIfNeeded(intent);
					}
					@Override
					public void failure(RetrofitError arg0) {
						// The POST request in unsuccessful because the chat room already exists,
						// so we are trying to retrieve it with an additional GET request
						Log.d("Failure creating chat room - trying to GET it from the server", arg0.toString());
						List<ChatRoom> chatRooms = ChatClient.getInstance().getChatRoom(currentChatRoom);
						currentChatRoom = chatRooms.get(0);
						
						showTermsIfNeeded(intent);
					}
				});
			}

			
			/**
			 * Show terms under which the chat is provided by the application developers
			 * @param intent
			 */
			private void showTermsIfNeeded(final Intent intent) {
				final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ChatRoomsSearchActivity.this);
				//sharedPrefs.edit().remove(Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName()).commit();
				
				// If the terms have not been shown for this chat room, show them
				if (!sharedPrefs.getBoolean(Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName(), false)) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomsSearchActivity.this);
					builder.setTitle(R.string.chat_terms_title)
						.setMessage(getResources().getString(R.string.chat_terms_body))
						.setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (currentChatMember.getLrzId() != null) {
									ChatClient.getInstance().joinChatRoom(currentChatRoom, currentChatMember, new Callback<ChatRoom>() {
										@Override
										public void success(ChatRoom arg0, Response arg1) {
											Log.d("Success joining chat room", arg0.toString());
											// Remember in sharedPrefs that the terms dialog was shown
											Editor editor = sharedPrefs.edit();
											editor.putBoolean(Const.CHAT_TERMS_SHOWN + "_" + currentChatRoom.getName(), true);
											editor.commit();
											// We need to move to the next activity now and provide the necessary data for it
											// We are sure that both currentChatRoom and currentChatMember exist
											intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
											intent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(currentChatMember));
											startActivity(intent);
										}
										@Override
										public void failure(RetrofitError arg0) {
											Log.e("Failure joining chat room", arg0.toString());
										}
									});
								}
							}
						});
					
					AlertDialog alertDialog = builder.create();
					alertDialog.show();
				} else { // If the terms were already shown, just enter the chat room
					// We need to move to the next activity now and provide the necessary data for it
					// We are sure that both currentChatRoom and currentChatMember exist
					intent.putExtra(Const.CURRENT_CHAT_ROOM, new Gson().toJson(currentChatRoom));
					intent.putExtra(Const.CURRENT_CHAT_MEMBER, new Gson().toJson(currentChatMember));
					startActivity(intent);
				}
			}
		});
	}

	
	private void checkPlayServicesAndRegister() {
		// Check device for Play Services APK. If check succeeds,
		// proceed with GCM registration.
		if (checkPlayServices()) {
		    gcm = GoogleCloudMessaging.getInstance(this);
		    //getGCMPreferences(getApplicationContext()).edit().remove(Const.GCM_REG_ID).commit();
		    regId = getRegistrationId(getApplicationContext());

		    if (regId.isEmpty()) {
		        registerInBackground();
		    }
		} else {
		    Log.i(TAG, "No valid Google Play Services APK found.");
		}
	}
	
	/**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(Const.GCM_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return getSharedPreferences(ChatRoomsSearchActivity.class.getSimpleName(),
	            Context.MODE_PRIVATE);
	}
	
	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
	    try {
	        PackageInfo packageInfo = context.getPackageManager()
	                .getPackageInfo(context.getPackageName(), 0);
	        return packageInfo.versionCode;
	    } catch (NameNotFoundException e) {
	        // should never happen
	        throw new RuntimeException("Could not get package name: " + e);
	    }
	}
	
	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                	Context context = getApplicationContext();
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "GCM registration successful";

                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regId);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
            	Utils.showLongCenteredToast(ChatRoomsSearchActivity.this, msg);
            }
        }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		ChatClient.getInstance().uploadRegistrationId(currentChatMember.getUserId(), new ChatRegistrationId(regId), new Callback<ChatRegistrationId>() {
			@Override
			public void success(ChatRegistrationId arg0, Response arg1) {
				Log.d("Success uploading GCM registration id", arg0.toString());
			}
			
			@Override
			public void failure(RetrofitError arg0) {
				Log.e("Failure uploading GCM registration id", arg0.toString());
			}
		});
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(Const.GCM_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
}
