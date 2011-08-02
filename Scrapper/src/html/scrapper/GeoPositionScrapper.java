package html.scrapper;
import geo.google.GeoAddressStandardizer;
import geo.google.datamodel.GeoAddress;
import geo.google.datamodel.GeoCoordinate;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.mysql.jdbc.Statement;

public class GeoPositionScrapper {

	public static void main(String[] args) throws Exception {
		
		/*String query = "SELECT d.id AS id,CONCAT( SUBSTRING_INDEX(REPLACE(v.location,'(RC)',''),'(',1), ',' , REPLACE(REPLACE(v.city,'(',''),')',''),',',v.country) AS address " +
					   "FROM titan1.d JOIN titan1.v_depot v USING(id) " +
					   "WHERE d.del = 0 AND d.pay_local = 1 AND v.city LIKE '%Toronto (__)'";*/
		
		String query = "SELECT d.id AS id,CONCAT( SUBSTRING_INDEX(REPLACE(REPLACE(v.location,'(RC)',''),'(Rc)',''),'(',1), ',' , REPLACE(REPLACE(v.city,'(',''),')',''),',',v.country) AS address " +
					   "FROM titan1.d JOIN titan1.v_depot v USING(id) " +
					   "WHERE d.del = 0 AND d.pay_local = 1 AND v.city LIKE '%(__)' AND v.country = 'Canada'";
		
		/*String query = "SELECT d.id AS id, CONCAT( REPLACE(REPLACE(v.location,'(RC)',''),'(Rc)','') , ',' , REPLACE(REPLACE(v.city,'(',''),')','')) AS address " +
					   "FROM titan1.d JOIN titan1.v_depot v USING(id) " +
					   "WHERE d.del = 0 AND d.pay_local = 1 AND v.city LIKE '%Canada%'";*/
		
		Class.forName("com.mysql.jdbc.Driver").newInstance();
		JLabel jHost = new JLabel("Host");
		JTextField host = new JTextField();
		JLabel jUserName = new JLabel("User");
		JTextField user = new JTextField();
		JLabel jPassword = new JLabel("Password");
		JTextField password = new JPasswordField();
		Object[] ob = {jHost, host, jUserName, user, jPassword, password};
		JOptionPane.showConfirmDialog(null, ob, "Please input host name, user & password", JOptionPane.OK_CANCEL_OPTION);
		
		if(StringUtils.isEmpty(host.getText()) || StringUtils.isEmpty(user.getText()) || (StringUtils.isEmpty(password.getText())))return;
		Connection connection = DriverManager.getConnection("jdbc:mysql://"+host.getText()+":3306/titan1?autoReconnect=true", user.getText(), password.getText());
		if(connection==null)return;
		Statement statement = (Statement) connection.createStatement();
		ResultSet rs = statement.executeQuery(query);

		List<String[]> values = new ArrayList<String[]>();
		while(rs.next()) values.add(new String[]{rs.getString("id"),rs.getString("address")});
		
		rs.close();
		statement.close();
		connection.close();
		
		System.out.println("Data ok");
		
		GeoAddressStandardizer st = new GeoAddressStandardizer("ABQIAAAA5B2f_QgM3SAOkFA_Inq1iRRLJfNN1CM5XLxwFDDB0m-HFueJ2hSTrxSkUvuggxsxgDXiAiKj5Zw6fA");
		FileWriter fw = new FileWriter("C:/Documents and Settings/givres/Desktop/location_fixed-"+new Date().getTime()+".sql");
		for (String[] supplier : values){
			try{
				List<GeoAddress> addresses = st.standardizeToGeoAddresses(supplier[1]);
				if(addresses!=null && !addresses.isEmpty()){
					GeoCoordinate coords = addresses.get(0).getCoordinate();
					fw.append("UPDATE titan1.d SET latitude = '"+coords.getLatitude()+"', longitude='"+coords.getLongitude()+"' WHERE id = "+supplier[0]+"; -- location id="+supplier[0]+" address='"+supplier[1]+"'\n");
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
