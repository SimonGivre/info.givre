package info.givre;


import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import utils.BetaSeriesUtils;
import utils.Constants;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	public void connectAction(View view){
		if(view!=null & view.getId() == R.id.Button01){
			//Button connectButton = (Button) this.findViewById(R.id.Button01);
			EditText login = (EditText) this.findViewById(R.id.EditText01);
			EditText passwd = (EditText) this.findViewById(R.id.EditText02);

			if (login.getText().length() != 0 || passwd.getText().length() != 0) {

				if (login.getText().length() == 0) {
					Toast.makeText(
							this,
							"Please enter your login", Toast.LENGTH_LONG).show();
					return;
				}
				if (passwd.getText().length() == 0) {
					Toast.makeText(
							this,
							"Please enter your password", Toast.LENGTH_LONG).show();
					return;
				}

				String passwdMD5 = null;
				try{
					MessageDigest md = MessageDigest.getInstance("MD5");
					md.update(passwd.getText().toString().getBytes());
					passwdMD5 = BetaSeriesUtils.toHexString(md.digest());
				}
				catch (NoSuchAlgorithmException nsae) {
					passwdMD5 = null;
				}

				if(passwdMD5 == null){
					Toast.makeText(
							this,
							"Failed processing your password", Toast.LENGTH_LONG).show();
					return;
				}

				String api = "?key="+Constants.apiKey;
				String strURL = Constants.rootURL + Constants.requestMemberAuthURL +api+
				"&login="+login.getText().toString()+"&password="+passwdMD5;

				HttpClient httpclient = new DefaultHttpClient();

				try{
					api += "&token="+BetaSeriesUtils.requestTokenValue(strURL);
					try{
						strURL = Constants.rootURL + Constants.requestMemberInfoURL + api;

						//	        	sourceUrl = new URL(strURL);
						//	        	handler = new BetaSeriesXMLHandler();
						//	        	xr.setContentHandler(handler);
						//	        	xr.parse(new InputSource(sourceUrl.openStream()));

						String res = BetaSeriesUtils.request(strURL).toString();
						Toast.makeText(this,res, 20000).show();

//						String tmpDir = System.getProperty("java.io.tmpdir");
//						Toast.makeText(this,tmpDir, Toast.LENGTH_LONG).show();
//						File file = new File(tmpDir,"memberInfoExemple.xml");
//						file.createNewFile();
//						FileWriter fw = new FileWriter(file);
//						fw.write(responseBody);
//						fw.flush();
//						fw.close();

					}catch(Exception e){
						Toast.makeText(this,e.toString(), Toast.LENGTH_LONG).show();
					}
					finally{
						strURL = Constants.rootURL + Constants.destroyToken + api;
						httpclient.execute(new HttpGet(strURL));
					}

				}catch(Exception e){
					Toast.makeText(this,e.toString(), Toast.LENGTH_LONG).show();
				}
			}
			else{
				try{
					SAXParserFactory spf = SAXParserFactory.newInstance();
					SAXParser sp = spf.newSAXParser();
					XMLReader xr = sp.getXMLReader();
					
					BetaSeriesXMLHandler handler = new BetaSeriesXMLHandler();
					xr.setContentHandler(handler);
					AssetManager assetManager = this.getAssets();
					xr.parse(new InputSource(assetManager.open("memberInfoExemple.xml")));
					
					MemberHome memberHome = new MemberHome(handler.getMember());
					
					this.finish();
				}
				catch(Exception e){
					
				}
			}
		}
	}

}