package serversynchronizedclock.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.os.SystemClock;

/**
 * The Server class is used to get the get the current time of the remote server
 * and to predict the physical distance between the remote server and user's
 * device.
 * 
 * @author Lee King Hung
 * 
 */
public class Server {

	private Date now;
	private SimpleDateFormat sdf;
	private String host;
	private Context ctx;

	/**
	 * 
	 * @param ctx
	 *            context of the activity that request for the time.
	 * @param host
	 *            URL of the remote server.
	 * @param sdf
	 *            date format of the time in the remote server.
	 */
	public Server(Context ctx, String host, SimpleDateFormat sdf) {
		this.host = host;
		this.sdf = sdf;
		this.ctx = ctx;
	}

	public Date getNow() {
		return now;
	}

	/**
	 * Gets the get the current time of the remote server.
	 * 
	 * @return true if request is successful.
	 */
	public boolean requestTime() {

		long requestTicks = SystemClock.elapsedRealtime();

		TimeThread timeThread = new TimeThread(host, sdf);
		timeThread.start();
		while (timeThread.getState() != Thread.State.TERMINATED)
			;

		if (timeThread.getNow() != null) {
			long responseTicks = SystemClock.elapsedRealtime();

			// round trip time is (responseTicks - requestTicks),
			// the offset needed to be added into the received time is the
			// time from the server sending the current time to
			// device receiving the time,
			// which is the half of the round trip time.
			int clockOffset = (int) ((responseTicks - requestTicks) / 2);

			now = new Date(timeThread.getNow().getTime() + clockOffset);
		} else
			return false;

		return true;

	}

	/**
	 * Calculates the physical distance between the remote server and user's
	 * device.
	 * 
	 * @return the physical distance between the remote server and user's
	 *         device.
	 * @throws Exception
	 */
	public double getDistance() throws Exception {

		DistanceThread distanceThread = new DistanceThread(ctx, host);
		distanceThread.start();
		while (distanceThread.getState() != Thread.State.TERMINATED)
			;

		if (distanceThread.getDistance() < 0)
			throw new Exception(distanceThread.getErrorMessage());

		return distanceThread.getDistance();
	}

}
