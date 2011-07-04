package html.scrapper;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class TimezoneScrapper {

	private static final int THREAD_LIMIT = 20;
	public static final String DB_URL = "jdbc:mysql://localhost:3306/jupiter2?autoReconnect=true";
	public static final String USER = "root";
	public static final String PASSWD = "sgivre";
	
	public static void main(String[] args) throws Exception {
		
		Class.forName("com.mysql.jdbc.Driver");
		
		ObjectPool connectionPool = new GenericObjectPool(null,THREAD_LIMIT+1);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(DB_URL,USER,PASSWD);
        new PoolableConnectionFactory(connectionFactory,connectionPool,null,null,false,true);
        
        Class.forName("org.apache.commons.dbcp.PoolingDriver");
        PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        driver.registerPool("myPool",connectionPool);
        Connection connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:myPool");
        
		Statement statement = connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT efid,latitude, longitude FROM places");
		
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_LIMIT);
		
		List<Job> jobs = new ArrayList<Job>();
		while(rs.next()){
			//jobs.add(new Job(new Data(rs.getString("efid"),rs.getString("latitude"),rs.getString("longitude"))));
			executorService.execute(new Job(new Data(rs.getString("efid"),rs.getString("latitude"),rs.getString("longitude"))));
		}
		
		//executorService.shutdown();
		executorService.shutdown();
		while(!executorService.isTerminated())Thread.sleep(5000);
		connection.close();
		connectionPool.close();
		
	}
	
	public static float getTimezone(String lat, String lng){
		try{
			
        URL url = new URL("http://www.earthtools.org/timezone/"+lat+"/"+lng);
        Document doc = (Document) Jsoup.parse(url, 10000);
        Element table = doc.select("offset").first();
        return Float.parseFloat(table.text());
		}catch(Exception e){return 0;}
	}
	
	private static class Job implements Runnable{
		private Data d;
		public Job(Data d) {
			super();
			this.d = d;
		}
		public void run() {
			Connection connection = null;
			try{
				while(connection== null){
					try{
						connection = DriverManager.getConnection("jdbc:apache:commons:dbcp:myPool");
					}catch(Exception e){
						Thread.sleep(500);
					}
				}
				float timezone = getTimezone(d.latitude,d.longitude);
				connection.createStatement().executeUpdate("INSERT INTO timezone_tmp VALUES ('"+d.efid+"',"+timezone+")");
			}catch(Exception e){
				e.printStackTrace();
			}
			finally{
				if(connection!=null)
					try {
						connection.close();
					} catch (SQLException e) {}
			}
		}
	}
	
	private static class Data{
		public String efid;
		public String latitude;
		public String longitude;
		
		public Data(String efid, String latitude, String longitude) {
			super();
			this.efid = efid;
			this.latitude = latitude;
			this.longitude = longitude;
		}
	}
	
}
