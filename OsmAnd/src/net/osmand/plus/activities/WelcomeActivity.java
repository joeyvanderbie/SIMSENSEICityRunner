package net.osmand.plus.activities;

import net.osmand.plus.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity  extends Activity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_welcome);
        Linkify.addLinks((TextView) findViewById(R.id.welcome_text), Linkify.ALL);
        
        Button next = (Button) findViewById(R.id.NextButton);
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(WelcomeActivity.this,UserDataActivity.class));
			}
		});
		
		Button prev = (Button) findViewById(R.id.PreviousButton);
		prev.setVisibility(View.GONE);
	}
}
