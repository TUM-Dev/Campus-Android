package de.tum.in.tumcampusapp.activities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import de.tum.in.tumcampusapp.R;

/**
 * Shows the information of the TCA important news and errors
 * 
 * 
 */
public class ImportantNewsActivity extends SherlockActivity {
	private void displayVersionName() {
		String TCAsString = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();

			HttpGet request = new HttpGet("http://vmbaumgarten1.informatik.tu-muenchen.de/tca/info.txt");
			HttpResponse response = client.execute(request);

			StatusLine stat = response.getStatusLine();
			if (stat.getStatusCode() != 200) {
				Log.d("Info", "Cannot fetch important news - wrong status code or server down - no important news?");
			} else {
				HttpEntity responseEntity = response.getEntity();
				InputStream is = responseEntity.getContent();
				ByteArrayOutputStream content = new ByteArrayOutputStream();

				int i = 0;
				byte[] buffer = new byte[1024];
				while ((i = is.read(buffer)) != -1) {
					content.write(buffer, 0, i);
				}
				TCAsString = new String(content.toByteArray());
				Log.d("News", TCAsString);
			}
		} catch (IOException e) {
			Log.e("Error", "Cannot fetch important news - unknown exception");
		}
		TextView tv = (TextView) this.findViewById(R.id.info_text);
		tv.setText(TCAsString);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.importantnews);
		this.displayVersionName();
	}

}
