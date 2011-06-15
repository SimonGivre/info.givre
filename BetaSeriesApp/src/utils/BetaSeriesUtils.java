package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.MapView.ReticleDrawMode;

import android.util.Log;

public class BetaSeriesUtils {

	public static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(String.format("%x", b));
		}
		return sb.toString();
	}

	public static String requestTokenValue(String url) {
		try{
		JSONObject result = request(url);
		JSONObject root = result.getJSONObject("root");
		return root.getJSONObject("member").getString("token");
		}catch(Exception e){return null;}
	}
	
	public static JSONObject request(String url) {
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				return new JSONObject(builder.toString());
			} else {
				Log.e(BetaSeriesUtils.class.toString(), "Failed to download file");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
