package solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class IndexBuilderThread implements Callable<Boolean> {

	private static int CHECK_FREQ = 10; // Seconds
	
	private DefaultHttpClient httpClient = null;
	private String host = null;
	private String index = null;
	private String port = null;
	
	public IndexBuilderThread(DefaultHttpClient httpClient, String host, String port,
			String index) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.port = port;
		this.index = index;
	}

	public Boolean call() throws Exception {
		boolean succeeded = true;
		try {
			System.out.println("Indexing "+index+" on "+host);
			succeeded = fullImportIndex(host, index);
			if(!succeeded)
				System.err.println("Indexing "+index+" on "+host+" failed.");
			else
				System.out.println("Index "+index+" on "+host+" done!");
		} catch (Exception e) {
			System.err.println("Exception: "+e.getStackTrace()[0]);
			System.err.println("Indexing "+index+" on "+host+" failed.");
			succeeded = false;
		}
		return Boolean.valueOf(succeeded);
	}


	private boolean fullImportIndex(String host, String index)
	throws HttpException, IOException, InterruptedException {
		while(isIndexBusy(host, index))Thread.sleep(CHECK_FREQ*1000);
		executeSolrCommand(host, index,"full-import");
		while(isIndexBusy(host, index))Thread.sleep(CHECK_FREQ*1000);
		if(indexingFailed(host, index)){
			return false;
		}
		else{
			return true;
		}
	}

	private void executeSolrCommand(String host, String index, String command) throws HttpException, IOException{
		String url = "http://"+host+":"+port+"/solr/"+index+"/select?qt=/dataimport&command="+command;
		HttpPost method = new HttpPost(url);
		HttpResponse response = httpClient.execute(method);
		HttpEntity entity = response.getEntity();
		if (entity == null || response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
			httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			throw new HttpException("HTTP Status Code "+response.getStatusLine().getStatusCode());
		}
		httpClient.getConnectionManager().closeExpiredConnections();
		httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
	}	

	private boolean isIndexBusy(String host, String index) throws HttpException, IOException{
		String url = "http://"+host+":"+port+"/solr/"+index+"/select?qt=/dataimport&command=status";
		HttpPost method = new HttpPost(url);
		HttpResponse response = httpClient.execute(method);
		HttpEntity entity = response.getEntity();
		if (entity != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			StringBuilder sb = new StringBuilder();
			InputStream in = entity.getContent();
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
			for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
				sb.append(line);
			}
			in.close();
			buffReader.close();
			String strResponse = sb.toString();
			httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			//Is index busy?
			if(strResponse.contains("<str name=\"status\">idle</str>")){
				return false;
			}
			else 
				return true;
		}
		else{
			httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			throw new HttpException("HTTP Status Code "+response.getStatusLine().getStatusCode());
		}

	}

	private boolean indexingFailed(String host, String index) throws HttpException, IOException{
		String url = "http://"+host+":"+port+"/solr/"+index+"/select?qt=/dataimport&command=status";
		HttpPost method = new HttpPost(url);
		HttpResponse response = httpClient.execute(method);
		HttpEntity entity = response.getEntity();
		if (entity != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			StringBuilder sb = new StringBuilder();
			InputStream in = entity.getContent();
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));
			for (String line = buffReader.readLine(); line != null; line = buffReader.readLine()) {
				sb.append(line);
			}
			in.close();
			buffReader.close();
			String strResponse = sb.toString();
			httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			//Did it failed?
			if(strResponse.contains("Indexing failed. Rolled back all changes.") && strResponse.contains("Rolledback"))
				return true;
			else
				return false;
		}
		else{
			httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			throw new HttpException("HTTP Status Code "+response.getStatusLine().getStatusCode());
		}

	}
	
	
	
}
