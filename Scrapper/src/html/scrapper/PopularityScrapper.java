package html.scrapper;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mysql.jdbc.Statement;





public class PopularityScrapper {

	public static void main(String[] args) throws Exception {
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/jupiter2?autoReconnect=true", "root", "sgivre");
		
		Statement statement = (Statement) connection.createStatement();
		ResultSet rs = statement.executeQuery("SELECT iata FROM airports WHERE country_iso = 'US' AND test = 0 AND iata IS NOT NULL");

		while(rs.next()){
			String iataCode = rs.getString("iata");
			long pop = getPopFromIATA(iataCode);
			connection.createStatement().executeUpdate("UPDATE airports SET test = "+pop+" WHERE iata = '"+iataCode+"'");
		}
	}
	
	public static long getPopFromIATA(String iataCode){
		try{
        URL url = new URL("http://www.gcr1.com/5010web/airport.cfm?Site="+iataCode);
        Document doc = (Document) Jsoup.parse(url, 10000);
        Element table = doc.select("table").get(9);
        Element tr = table.select("tr").get(5);
        Element td = tr.select("td").get(1);
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        Number number = format.parse(td.text());
        return number.longValue();
		}catch(Exception e){return 0;}
	}
	
}
