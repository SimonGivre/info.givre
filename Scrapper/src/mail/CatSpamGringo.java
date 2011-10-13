package mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class CatSpamGringo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try{
			
			String host = "smtp.gmail.com";
			
			JLabel jUserName = new JLabel("User");
			JTextField user = new JTextField();
			JLabel jPassword = new JLabel("Password");
			JTextField password = new JPasswordField();
			JLabel jVictim = new JLabel("Victim");
			JTextField victim = new JTextField();
			JLabel jCount = new JLabel("Count");
			JTextField count = new JTextField();
			JLabel jSubject = new JLabel("Subject");
			JTextField subject = new JTextField();
			Object[] ob = {jUserName, user, jPassword, password,jVictim,victim,jCount,count,jSubject,subject};
			JOptionPane.showConfirmDialog(null, ob, "Please input host name, user & password", JOptionPane.OK_CANCEL_OPTION);
			
			Properties props = System.getProperties();
			props.put("mail.smtp.starttls.enable", "true"); // added this line
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.user", user.getText());
			props.put("mail.smtp.password", password.getText());
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");
			
			String[] to = {victim.getText()}; // added this line
			
			Session session = Session.getDefaultInstance(props, null);
			
			for(int j=0;j<Integer.parseInt(count.getText());j++){
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(user.getText()));
				
				InternetAddress[] toAddress = new InternetAddress[to.length];
				
				// To get the array of addresses
				for( int i=0; i < to.length; i++ ) { // changed from a while loop
					toAddress[i] = new InternetAddress(to[i]);
				}
				
				for( int i=0; i < toAddress.length; i++) { // changed from a while loop
					message.addRecipient(Message.RecipientType.TO, toAddress[i]);
				}
				message.setSubject(j+" "+subject.getText());
				String body = "<html><head></head><body><img src=\"http://images.cheezburger.com/completestore/2010/1/19/129083956209386445.jpg\"/></body></html>";
				message.setContent(body, "text/html");
				Transport transport = session.getTransport("smtp");
				transport.connect(host, user.getText(), password.getText());
			    transport.sendMessage(message, message.getAllRecipients());
			    transport.close();
			    Thread.sleep(1000);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void mail() throws Exception{
		
	}
	
}
