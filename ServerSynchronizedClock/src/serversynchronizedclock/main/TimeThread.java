package serversynchronizedclock.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Thread to request the current time of the remote server
 * 
 * @author Lee King Hung
 * 
 */
public class TimeThread extends Thread {

	private Date now;
	private String host;
	private SimpleDateFormat sdf;

	/**
	 * 
	 * @param host
	 *            URL of the remote server
	 * @param sdf
	 *            date format of the remote server
	 */
	public TimeThread(String host, SimpleDateFormat sdf) {
		this.host = host;
		this.sdf = sdf;
	}

	@Override
	public void run() {

		URL url = null;
		URLConnection urlConn = null;
		InputStreamReader inStream = null;
		BufferedReader buff = null;

		try {
			url = new URL(host);
			urlConn = url.openConnection();
			inStream = new InputStreamReader(urlConn.getInputStream());
			buff = new BufferedReader(inStream);

			// the first line of the content contains the time needed
			String strNow = buff.readLine();
			now = sdf.parse(strNow);
		} catch (Exception e) {
		}
	}

	public Date getNow() {
		return now;
	}
}
