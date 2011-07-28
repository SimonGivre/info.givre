package html.scrapper;
import geo.google.GeoAddressStandardizer;
import geo.google.datamodel.GeoAddress;
import geo.google.datamodel.GeoCoordinate;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mysql.jdbc.Statement;

public class GeoPositionScrapper {

	public static void main(String[] args) throws Exception {
		
		String query = "SELECT d.id AS id,CONCAT( REPLACE(v.location,'(RC)','') , ',' , REPLACE(REPLACE(v.city,'(',''),')',''),',',v.country) AS address " +
					   "FROM titan1.d JOIN titan1.v_depot v USING(id) " +
					   "WHERE d.del = 0 AND d.pay_local = 1 AND v.city LIKE '%Toronto (__)'";
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		JLabel jUserName = new JLabel("User");
		JTextField user = new JTextField();
		JLabel jPassword = new JLabel("Password");
		JTextField password = new JPasswordField();
		Object[] ob = {jUserName, user, jPassword, password};
		JOptionPane.showConfirmDialog(null, ob, "Please input user & password", JOptionPane.OK_CANCEL_OPTION);
		
		Connection connection = DriverManager.getConnection("jdbc:mysql://opsmdb-01.lhr1.traveljigsaw.com:3306/titan1?autoReconnect=true", user.getText(), password.getText());
		Statement statement = (Statement) connection.createStatement();
		ResultSet rs = statement.executeQuery(query);

		List<String[]> values = new ArrayList<String[]>();
		while(rs.next()) values.add(new String[]{rs.getString("id"),rs.getString("address")});
		
		rs.close();
		statement.close();
		connection.close();
		
		
		//String[][] values = CSVParser.parse(new FileReader("C:/Documents and Settings/givres/Desktop/toronto_to_fix.csv"),';');
		GeoAddressStandardizer st = new GeoAddressStandardizer("ABQIAAAA5B2f_QgM3SAOkFA_Inq1iRRLJfNN1CM5XLxwFDDB0m-HFueJ2hSTrxSkUvuggxsxgDXiAiKj5Zw6fA");
		FileWriter fw = new FileWriter("C:/Documents and Settings/givres/Desktop/toronto_fixed.sql");
		for (String[] supplier : values){
			try{
				List<GeoAddress> addresses = st.standardizeToGeoAddresses(supplier[1]);
				if(addresses!=null && !addresses.isEmpty()){
					GeoCoordinate coords = addresses.get(0).getCoordinate();
					fw.append("UPDATE titan1.d SET latitude = '"+coords.getLatitude()+"', longitude='"+coords.getLongitude()+"' WHERE id = "+supplier[0]+";\n");
				}
				else fw.append("-- Failed to get location id="+supplier[0]+" address='"+supplier[1]+"'\n");
			}
			catch(Exception e){
				fw.append("-- Failed to get location id="+supplier[0]+" address='"+supplier[1]+"'\n");
			}
		}
		fw.flush();
		fw.close();
	}
	
}
