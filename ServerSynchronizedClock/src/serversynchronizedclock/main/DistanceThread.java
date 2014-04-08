package serversynchronizedclock.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Thread to calculate the distance between the remote server and the user's
 * device
 * 
 * @author Lee King Hung
 * 
 */
public class DistanceThread extends Thread {

	// URL of the remote server
	private String host;

	// distance between the remote server and the user's device
	private double distance = -1;

	private String errorMessage = "";

	// context of the activity requesting the distance
	private Context ctx;

	private final String GPS_ERROR_MSG = "GPS Not Open";
	private final String INTERNET_ERROR = "Internet Error";

	// Earth's radius in km
	private final double EARTH_RADIUS = 6373;

	public DistanceThread(Context ctx, String host) {
		this.host = host;
		this.ctx = ctx;
	}

	@Override
	public void run() {

		try {

			// Gets the latitude and the longitude of the remote server and the
			// user's device
			double[] serverLocation = getServerLocation();
			double[] userLocation = getUserLocation();

			// Difference in the latitude and the longitude
			double dlat = serverLocation[0] - userLocation[0];
			double dlon = serverLocation[1] - userLocation[1];

			double temp1 = Math.pow(Math.sin(dlat / 2), 2)
					+ Math.cos(userLocation[0]) * Math.cos(serverLocation[0])
					* Math.pow(Math.sin(dlon / 2), 2);
			double temp2 = 2 * Math.atan2(Math.sqrt(temp1),
					Math.sqrt(1 - temp1));
			distance = temp2 * EARTH_RADIUS;

		} catch (Exception e) {
			errorMessage = e.getMessage();
		}

	}

	/**
	 * Converts degree into radian
	 * 
	 * @param deg
	 *            degree.
	 * @return radian.
	 */
	private double degToRad(double deg) {
		return deg * Math.PI / 180;
	}

	/**
	 * Get the remote server's latitude and longitude by using he website
	 * http://freegeoip.net/xml/ with server's IP as parameter. This website
	 * will return a XML file containing the information of the IP.
	 * 
	 * @return double array containing latitude and longitude.
	 * @throws Exception
	 */
	private double[] getServerLocation() throws Exception {

		double latitude = 0;
		double longitude = 0;

		try {
			String ip = InetAddress.getByName(new URL(host).getHost())
					.getHostAddress();

			URL url = new URL("http://freegeoip.net/xml/" + ip);
			URLConnection urlConn = url.openConnection();
			InputStreamReader inStream = new InputStreamReader(
					urlConn.getInputStream());
			BufferedReader buff = new BufferedReader(inStream);
			
			// Read through the XML file one by one
			while (true) {
				String curLine = buff.readLine();
				if (curLine == null)
					break;
				else {
					if (curLine.contains("Latitude"))
						latitude = extractInfo(curLine);
					else if (curLine.contains("Longitude"))
						longitude = extractInfo(curLine);
				}
			}
		} catch (Exception e) {
			throw new Exception(INTERNET_ERROR);
		}

		return new double[] { degToRad(latitude), degToRad(longitude) };
	}

	/**
	 * Extracts double from a string
	 * 
	 * @param str
	 *            string containing the double.
	 * @return double in str.
	 */
	private double extractInfo(String str) {
		double value = 0;
		Pattern pattern = Pattern.compile("\\d+\\.\\d+");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			value = Double.parseDouble(matcher.group(0));
		}
		return value;
	}

	/**
	 * Gets user's device's location from GPS information
	 * 
	 * @return double array containing latitude and longitude.
	 * @throws Exception
	 */
	private double[] getUserLocation() throws Exception {

		LocationManager lm = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
		Location location = lm
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		// location will return null if the user does not open GPS.
		if (location == null) {
			throw new Exception(GPS_ERROR_MSG);
		}

		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		return new double[] { degToRad(latitude), degToRad(longitude) };
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public double getDistance() {
		return distance;
	}
}
