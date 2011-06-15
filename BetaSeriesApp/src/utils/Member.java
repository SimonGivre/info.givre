package utils;

import java.util.List;

public class Member {

	private String login;
	private String avatarUrl;
	private List<Serie> series = null;
	
	
	
	public Member(String login, List<Serie> series) {
		super();
		this.login = login;
		this.series = series;
	}



	public Member(String login, String avatarUrl, List<Serie> series) {
		super();
		this.login = login;
		this.avatarUrl = avatarUrl;
		this.series = series;
	}



	public String getLogin() {
		return login;
	}



	public String getAvatarUrl() {
		return avatarUrl;
	}



	public List<Serie> getSeries() {
		return series;
	}

	
	
}
