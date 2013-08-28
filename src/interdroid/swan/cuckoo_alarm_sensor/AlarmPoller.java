package interdroid.swan.cuckoo_alarm_sensor;

import android.annotation.SuppressLint;
import interdroid.swan.cuckoo_sensors.CuckooPoller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 * A sensor for alarm in the Netherlands
 * 
 * @author roelof &lt;rkemp@cs.vu.nl&gt;
 * 
 */
public class AlarmPoller implements CuckooPoller {

	/**
	 * The region configuration.
	 */
	public static final String REGION_CONFIG = "region";

	/**
	 * The type configuration.
	 */
	public static final String TYPE_CONFIG = "type";

	/**
	 * The recent field.
	 */
	public static final String RECENT_FIELD = "recent";

	@SuppressLint("DefaultLocale")
	@Override
	public Map<String, Object> poll(String valuePath,
			Map<String, Object> configuration) {
		Map<String, Object> result = new HashMap<String, Object>();
		// put your polling code here
		String region = (String) configuration.get("region");
		String type = (String) configuration.get("type");
		String suffix;
		if (region.equals("all") && type.equals("")) {
			suffix = "all.rss";
		} else if (region.equals("all")) {
			suffix = "discipline/" + type + ".rss";
		} else if (type.equals("")) {
			suffix = "region/"
					+ region.replace(" ", "-").replace("--", "-").toLowerCase()
					+ ".rss";
		} else {
			suffix = "region/"
					+ region.replace(" ", "-").replace("--", "-").toLowerCase()
					+ "/" + type + ".rss";
		}
		String url = "http://alarmeringen.nl/feeds/" + suffix;
		HttpParams httpParams = new BasicHttpParams();
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = httpClient.execute(httpGet);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = null;
			String reply = "";
			while ((line = reader.readLine()) != null) {
				reply += line;
			}
			reader.close();
			String[] items = reply.split("<item>");
			String recent = items[1];
			recent = recent.substring(
					recent.indexOf("<title>") + "<title>".length(),
					recent.indexOf("</title>"));
			result.put("recent", recent);
		} catch (ClientProtocolException e) {
			// ignore
		} catch (IOException e) {
			// ignore
		}
		return result;
	}

	@Override
	public long getInterval(Map<String, Object> configuration, boolean remote) {
		if (remote) {
			return 30000; // twice every minute
		} else {
			return 6 * 60000; // 6 min.
		}
	}
}