package info.givre;

import utils.Member;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MemberHome extends Activity {

	private Member member;
	
	public MemberHome(Member member) {
		this.member = member;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member);
		
		TextView welcomeView = (TextView) findViewById(R.string.welcome);
		
		welcomeView.setText(R.string.welcome+" "+member.getLogin());
		
	}
	
}
