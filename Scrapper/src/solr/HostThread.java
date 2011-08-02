package solr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.impl.client.DefaultHttpClient;

public class HostThread implements Callable<Boolean> {

	
	private static final int THREAD_LIMIT = 20;
	
	private DefaultHttpClient httpClient = null;
	private String host = null;
	private String port = null;
	private String[] indexes = {"airport","en"};
	private ExecutorService executorService = null;

	public HostThread(DefaultHttpClient httpClient, String host, String port) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.port = port;
		executorService = Executors.newFixedThreadPool(THREAD_LIMIT);
	}

	public HostThread(DefaultHttpClient httpClient, String host, String port, String[] indexes) {
		super();
		this.httpClient = httpClient;
		this.host = host;
		this.port = port;
		this.indexes = indexes;
		executorService = Executors.newFixedThreadPool(THREAD_LIMIT);
	}
	
	public Boolean call() throws Exception {
		boolean succeeded = true;
		try {
			System.out.println("Start Index Builder on "+host);
			List<Future<Boolean>> jobs = new ArrayList<Future<Boolean>>();
			for(final String index : indexes){
				IndexBuilderThread ith = new IndexBuilderThread(httpClient, host, port, index);
				Future<Boolean> submited = executorService.submit(ith);
				jobs.add(submited);
	        }
			int cptFailedIndexes = 0;
			for (Future<Boolean> future : jobs) {
				try {
					Boolean result = future.get();
					succeeded = (succeeded && result);
					if(!succeeded)
						cptFailedIndexes++;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(cptFailedIndexes>0)
				if(cptFailedIndexes==indexes.length)
					System.err.println("All indexes on "+host+" failed.");
				else
					System.err.println("Building indexes on "+host+" partially failed.");
			else
				System.out.println("Building indexes on "+host+" done!");
			executorService.shutdownNow();
		} catch (Exception e) {
			System.err.println("Exception: "+e.getStackTrace()[0]);
			System.err.println("Building indexes on "+host+" failed.");
			succeeded = false;
			executorService.shutdownNow();
		}
		return new Boolean(succeeded);
	}

}
