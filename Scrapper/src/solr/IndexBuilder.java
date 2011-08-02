package solr;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.eb2.utils.cli.CLIParser;
import org.eb2.utils.db.DBConnection;

public class IndexBuilder {

	private static final String SQL_HOST = "report-serverdb2.db.traveljigsaw.com";
	private static final String SQL_DB = "serverdb2";
	private static final String SQL_USER = "solrindexbuild";
	private static final String SQL_PWD = "baishookoh";
	
	private static final int THREAD_LIMIT = 20;
	
	private static ThreadSafeClientConnManager connectionManager;
	private static DefaultHttpClient httpClient;
	
	private String datacenter  = "all";
	private String[] hosts  = new String[0];
	private String[] exclusions  = new String[0];
	private String port  = "8080";
	private String[] indexes  = new String[0];
	private boolean help  = false;
	
	public static void main(String[] args) throws Exception {
        
		CLIParser parser = new CLIParser();
		
		parser.addOption("h", "help", "print usage");
		parser.addValueOption("d", "datacenter", "the datacenter (lhr, ams, all) default=all", "datacenter", false);
		parser.addMultipleValueOption("t", "hosts", "list of specific hosts name", "hosts", false);
		parser.addMultipleValueOption("e", "exclusions", "list of excluded hosts name", "exclusions", false);
		parser.addValueOption("p", "port", "a specific port default=8080", "port", false);
		parser.addMultipleValueOption("i", "indexes", "list of specific indexes default=airport,en", "indexes", false);
		
        IndexBuilder ib = new IndexBuilder();
        parser.setFor(ib, args);
        List<Future<Boolean>> jobs = new ArrayList<Future<Boolean>>();
        List<String> chosenHosts = new ArrayList<String>();
        
        if(ib.isHelp()){
        	parser.printUsage(new PrintWriter(System.out), "Index builder");
        	return;
        }
        
        if(ib.getHosts().length > 0)
        	Collections.addAll(chosenHosts, ib.getHosts());
        else if(ib.getDatacenter().equalsIgnoreCase("all") || StringUtils.isEmpty(ib.getDatacenter())){
	        chosenHosts.addAll(getAllSolrServersFromDB(null));
        }
        else if(ib.getDatacenter().equalsIgnoreCase("lhr"))
        	chosenHosts.addAll(getAllSolrServersFromDB("lhr"));
        else if(ib.getDatacenter().equalsIgnoreCase("ams"))
        	chosenHosts.addAll(getAllSolrServersFromDB("ams"));
        else{
        	parser.printUsage(new PrintWriter(System.out), "Index builder");
        	return;
        }
        
        if(ib.getExclusions().length > 0){
        	for(String excludedHost : ib.getExclusions()){
	        	Iterator<String> i = chosenHosts.iterator();
        		while(i.hasNext()){
        			String host = i.next();
	        		if(host.contains(excludedHost))i.remove();
	        	}
        	}
        }
        
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
        connectionManager = new ThreadSafeClientConnManager(schemeRegistry);
        connectionManager.setMaxTotal(255);
        connectionManager.setDefaultMaxPerRoute(40);
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 6000);
        httpClient = new DefaultHttpClient(connectionManager, httpParams);
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_LIMIT);
        
        for (final String host : chosenHosts){
        	HostThread hth = null;
        	if(ib.getIndexes().length == 0)
        		hth = new HostThread(httpClient, host, ib.getPort());
        	else
        		hth = new HostThread(httpClient, host, ib.getPort(), ib.getIndexes());
        	Future<Boolean> submited = executorService.submit(hth);
			jobs.add(submited);
        }
        for (Future<Boolean> future : jobs) {
			try {
				future.get();
			} catch (Exception e) {}
		}
        executorService.shutdownNow();
	}

	public String getDatacenter() {
		return datacenter;
	}

	public void setDatacenter(String datacenter) {
		this.datacenter = datacenter;
	}

	public String[] getHosts() {
		return hosts;
	}

	public void setHosts(String[] hosts) {
		this.hosts = hosts;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String[] getIndexes() {
		return indexes;
	}

	public void setIndexes(String[] indexes) {
		this.indexes = indexes;
	}

	public boolean isHelp() {
		return help;
	}

	public void setHelp(boolean help) {
		this.help = help;
	}

	public String[] getExclusions() {
		return exclusions;
	}

	public void setExclusions(String[] exclusions) {
		this.exclusions = exclusions;
	}

	private static List<String> getAllSolrServersFromDB(String datacenter){
		List<String> solrServers = new ArrayList<String>();
		DBConnection connection = new DBConnection();
		try{
			//Connect to DB
			connection.connect("jdbc:mysql://" + SQL_HOST + ":3306/"+SQL_DB, SQL_USER, SQL_PWD, "com.mysql.jdbc.Driver");
			ResultSet rs = connection.runQuery("SELECT DISTINCT NAME FROM servers_asset WHERE NAME LIKE 'solr%"+((StringUtils.isNotEmpty(datacenter))?datacenter+"%":"")+"';", false);
			while (rs.next()) {
				solrServers.add(rs.getString("NAME"));
			}
			rs.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally {
			if (connection != null)connection.close();
		}
		return solrServers;
	}
	
}