package solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.Ostermiller.util.Parallelizer;

public class IndexBuilder {

	//private static String[] solrHosts = {"ts2"};
	private static String[] solrAMSHosts = {"solr-02","solr-06","solr-08","solr-10"};
	//private static String[] solrAMSHosts = {"solr-02","solr-04","solr-06","solr-08","solr-10"};
	private static String[] solrLHRHosts = {"solr-01","solr-03","solr-05","solr-07","solr-09"};
	private static String port = "8080";
	//private static String port = "8080";
	
	private static String[] indexes = {"airport","en"};
	private static int TIME_FREQ = 10; // Seconds
	
	private static ClientConnectionManager connectionManager;
	private static DefaultHttpClient httpClient;
	
	/**
	 * Import Data from Databases to Indexes
	 */
	public static void main(String[] args) throws Exception {
		HttpParams params = new BasicHttpParams();
        params.setIntParameter(ConnManagerParams.MAX_TOTAL_CONNECTIONS, 255);
        params.setLongParameter(ConnManagerParams.TIMEOUT, 60000);
        params.setParameter(ConnManagerParams.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(40));
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(connectionManager, params);
        Parallelizer solrServerJobs = new Parallelizer();
        for (final String host : solrLHRHosts){
        	solrServerJobs.run(
                new Runnable(){
                	public void run() {
                		fullImportHostsIndexes(host);
					}
                }
            );
        }
        solrServerJobs.join();
	}

	public static void fullImportHostsIndexes(final String host) {
		try {
			System.out.println("Start Index Builder on "+host);
			Parallelizer indexJobs = new Parallelizer();
			for(final String index : indexes){
	        	indexJobs.run(
	                new Runnable(){
	                	public void run() {
	                		try {
								fullImportIndex(host, index);
							} catch (Exception e) {
								System.err.println("Indexing "+index+" on "+host+" failed.");
								e.printStackTrace();
							}
						}
	                }
	            );
	        }
	        indexJobs.join();
			System.out.println("Building indexes on "+host+" done!");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void fullImportIndex(String host, String index)
			throws HttpException, IOException, InterruptedException {
		while(isIndexBusy(host, index))Thread.sleep(TIME_FREQ*1000);
		System.out.println("Index="+index+" on "+host);
		executeSolrCommand(host, index,"full-import");
		while(isIndexBusy(host, index))Thread.sleep(TIME_FREQ*1000);
		System.out.println("Index="+index+" on "+host+" done!");
	}

	private static String timestamp() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_hhmm");  
	    return df.format(new Date());
	}

	public static void executeSolrCommand(String host, String index, String command) throws HttpException, IOException{
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
	
	public static boolean isIndexBusy(String host, String index) throws HttpException, IOException{
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
            String strResponse = sb.toString();
            httpClient.getConnectionManager().closeExpiredConnections();
			httpClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
			//Is index busy?
			if(strResponse.contains("<str name=\"status\">idle</str>")){
				//Did it failed?
				if(strResponse.contains("Indexing failed. Rolled back all changes."))
					System.err.println("Indexing "+index+" on "+host+" failed. Rolled back all changes.");
				return false;
			}
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