package serversynchronizedclock.main;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private Server st;
	private Date now;
	private double distance;

	private SimpleDateFormat serverDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");
	private SimpleDateFormat DisplayDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd    HH:mm");

	private String host = "http://staging-api.lovebyte.us/datetime";

	// TextViews to display time and distance
	private TextView textViewClock;
	private TextView textViewDistance;

	// Handler to update UI
	private final Handler handler = new Handler();

	private Context ctx = this;

	// Timers for synchronisation with server
	private Timer timerSync = new Timer();

	// Timers to increase one second to the time
	private Timer timerUpdateUI = new Timer();

	private final String INTERNET_ERROR_MSG = "There is Internet connection problem, server synchronization cannot be done.";

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		st = new Server(this, host, serverDateFormat);
		textViewClock = (TextView) findViewById(R.id.textViewClock);
		textViewDistance = (TextView) findViewById(R.id.textViewDistance);

		if (synchronize()) {

			timerSync.scheduleAtFixedRate(timerTaskSync, 0, 60 * 1000);
			timerUpdateUI.scheduleAtFixedRate(timerTaskUpdateUI, 0, 1000);

			try {
				distance = st.getDistance();
				textViewDistance.setText(String.format("%.2f", distance)
						+ " km");
			} catch (Exception e) {
				textViewDistance.setText("0.00 km");
				Toast.makeText(
						this,
						"Unable to calculate the physical distance between the remote server and your device:\n"
								+ e.getMessage(), Toast.LENGTH_LONG).show();
			}

		} else {
			showErrorMessage(ctx, INTERNET_ERROR_MSG);
		}

	}

	/**
	 * Gets the current time of the remote server.
	 * @return
	 */
	private boolean synchronize() {

		if (st.requestTime()) {
			now = st.getNow();
		} else
			return false;
		return true;
	}

	/**
	 * Shows error Toast Message
	 * @param ctx
	 * @param errorMessage
	 */
	private void showErrorMessage(Context ctx, String errorMessage) {
		Toast.makeText(ctx, errorMessage, Toast.LENGTH_LONG).show();
	}

	// TimerTask for time synchronisation
	private TimerTask timerTaskSync = new TimerTask() {

		@Override
		public void run() {
			if (!synchronize()) {
				timerSync.cancel();
				handler.post(showError);
			}
		}
	};

	// TimerTask to update UI (or increase the time by 1 second)
	private TimerTask timerTaskUpdateUI = new TimerTask() {

		@Override
		public void run() {
			try {
				now = new Date(now.getTime() + 1000);
				handler.post(updateUI);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	// Runnable to update textViewClock
	private Runnable updateUI = new Runnable() {
		@Override
		public void run() {
			String cur = DisplayDateFormat.format(now.getTime());
			textViewClock.setText(cur);
		}
	};

	// Runnable for error message Toast showing 
	private Runnable showError = new Runnable() {

		@Override
		public void run() {
			showErrorMessage(ctx, INTERNET_ERROR_MSG);
		}

	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
