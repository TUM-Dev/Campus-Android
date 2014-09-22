package de.tum.in.tumcampus.auxiliary;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.activities.MainActivity;

public class DemoModeStartActivity extends Activity {
	class RequestTask extends AsyncTask<String, String, String> {

		private String calcRemainingDays(int currentSeconds) {
			int daysLeft = DUE_DATE_IN_SECONDS - currentSeconds;
			daysLeft = daysLeft / SECONDS_PER_DAY + 1;
			return String.valueOf(daysLeft);
		}

		@Override
		protected String doInBackground(String... uri) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response;
			String responseString = null;
			try {
				response = httpclient.execute(new HttpGet(uri[0]));
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
					Utils.log("Result: " + responseString);
				} else {
					// Closes the connection.
					response.getEntity().getContent().close();
					throw new IOException(statusLine.getReasonPhrase());
				}
			} catch (ClientProtocolException e) {
				Utils.log(e);
			} catch (IOException e) {
                Utils.log(e);
			}
			return responseString;
		}

		private boolean isInTime(int currentSeconds) {
            return currentSeconds < DUE_DATE_IN_SECONDS;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progress.setVisibility(View.GONE);

			if (result == null) {
				Utils.showToast(DemoModeStartActivity.this, R.string.no_internet_connection);
				txtTime.setText("---");
				button.setEnabled(false);
			} else if (isInTime(Integer.valueOf(result))) {
				String day;
				if (Integer.valueOf(result) == 1) {
					day = " " + getResources().getString(R.string.day_left);
				} else {
					day = " " + getResources().getString(R.string.days_left);
				}
				txtTime.setText(calcRemainingDays(Integer.valueOf(result))
						+ day);
				button.setEnabled(true);
			} else {
				txtTime.setText(R.string.date_expired);
				button.setEnabled(false);
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setVisibility(View.VISIBLE);
			txtTime.setText("---");
			button.setEnabled(false);
		}
	}
	private final static String WEB_SERVICE_URL = "http://www.timeapi.org/utc/now?format=%25s";
	private final static int SEPTEMBER_8_2013_IN_SECONDS = 1378677595;
	private final static int SECONDS_PER_DAY = 60 * 60 * 24;

	private final static int DUE_DATE_IN_SECONDS = SEPTEMBER_8_2013_IN_SECONDS;
	private TextView txtTime;
	private ProgressBar progress;

	private Button button;

	public DemoModeStartActivity() {
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_start_tca:
			finish();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.demo_mode_start_activity);

		txtTime = (TextView) findViewById(R.id.txt_time_elapsed);
		progress = (ProgressBar) findViewById(R.id.progress);
		button = (Button) findViewById(R.id.btn_start_tca);

		button.setEnabled(false);
		new RequestTask().execute(WEB_SERVICE_URL);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_demo_mode_start_activity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_update:
			new RequestTask().execute(WEB_SERVICE_URL);
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
