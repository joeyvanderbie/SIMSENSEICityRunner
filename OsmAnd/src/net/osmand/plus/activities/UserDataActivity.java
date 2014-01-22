package net.osmand.plus.activities;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.data.UserData;
import net.osmand.sensei.db.UserDataSource;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class UserDataActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userdata);
		
		Button save = (Button) findViewById(R.id.SaveButton);
		
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveValues();
			}
		});
	
	}
	
	private void saveValues(){
		EditText height = (EditText) findViewById(R.id.height);
		EditText weight = (EditText) findViewById(R.id.weight);
		EditText teamnr = (EditText) findViewById(R.id.team_nr_input);
		
		double heightD = Double.parseDouble(height.getText().toString());
		double weightD = Double.parseDouble(weight.getText().toString());
		int team = Integer.parseInt(teamnr.getText().toString());
		
		 UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
	 		uds.open();
	 		UserData user = uds.getUserData();
	 		if(user != null){ 
		 		user.setWeight(weightD);
		 		user.setHeight(heightD);
		 		user.setTeamid(team);
		 		uds.update(user);
		 	
	 		}else{
	 			uds.add("", "", "", team, heightD, weightD);
	 		}
	 		uds.close();
		
	}

	
}
