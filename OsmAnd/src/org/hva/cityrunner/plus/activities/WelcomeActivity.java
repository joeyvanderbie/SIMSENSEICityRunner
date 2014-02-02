package org.hva.cityrunner.plus.activities;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.sensei.data.UserData;
import org.hva.cityrunner.sensei.db.UserDataSource;

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
        
        UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
 		uds.open();
 		UserData ud = uds.getUserData();
 		uds.close();
 		
 		TextView account = (TextView) findViewById(R.id.account);
 		account.setText("Your Sense account is:" +
        "\n Email "+ud.getEmail()+
        "\n Password "+ud.getPassword());
 		
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
