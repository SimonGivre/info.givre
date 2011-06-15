package info.givre;

import java.util.Collections;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import utils.Member;
import utils.Serie;

public class BetaSeriesXMLHandler extends DefaultHandler {

	private String token;
	private Member member;
	private String currentElt;
	
	public BetaSeriesXMLHandler(){
		init();
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(currentElt.equals("login"))
			member = new Member(String.valueOf(ch),"avatar",Collections.singletonList(new Serie("Dexter")));
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(localName.equals("token")){
			currentElt = null;
		}
		else if(localName.equals("member")){
			currentElt = null;
		}
		else if(localName.equals("login")){
			currentElt = null;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(localName.equals("token")){
			currentElt = "token";
		}
		else if(localName.equals("member")){
			currentElt = "member";
		}
		else if(localName.equals("login")){
			currentElt = "login";
		}
	}

	private void init(){
		currentElt = null;
		token = null;
		member = null;
	}

	public String getToken() {
		return token;
	}

	public Member getMember() {
		return member;
	}
	
	
	
}
