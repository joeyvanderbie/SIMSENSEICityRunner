package net.osmand.plus.activities;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.data.UserData;
import net.osmand.sensei.db.UserDataSource;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class UserDataActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userdata);
		
		Button next = (Button) findViewById(R.id.NextButton);
		
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveValues();

				Intent intentSettings = new Intent(UserDataActivity.this,MainMenuActivity.class);
				intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intentSettings);
			}
		});
Button prev = (Button) findViewById(R.id.PreviousButton);
		
		prev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				saveValues();
				finish();
			}
		});
	
	}
	
	private void saveValues(){
		EditText height = (EditText) findViewById(R.id.height);
		EditText weight = (EditText) findViewById(R.id.weight);
		EditText teamnr = (EditText) findViewById(R.id.TeamNR);
		EditText age = (EditText) findViewById(R.id.age);
		
		Spinner genderSpin = (Spinner) findViewById(R.id.gender_spinner);
		 String gender = genderSpin.getSelectedItem() != null?genderSpin.getSelectedItem().toString():"n.a";
		String heightS = height.getText().toString();
		String weightS = weight.getText().toString();
		String teamS = teamnr.getText().toString();
		String ageS = age.getText().toString();
		
		double heightD = Double.parseDouble(heightS.equals("")?"0":heightS);
		double weightD = Double.parseDouble(weightS.equals("")?"0":weightS);
		int team = Integer.parseInt(teamS.equals("")?"0":teamS);
		int ageInt = Integer.parseInt(ageS.equals("")?"0":ageS);
		
		 UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
	 		uds.open();
	 		UserData user = uds.getUserData();
	 		if(user != null){ 
		 		user.setWeight(weightD);
		 		user.setHeight(heightD);
		 		user.setTeamid(team);
		 		user.setAge(ageInt);
		 		user.setGender(gender);
		 		uds.update(user);
		 	
	 		}else{
	 			uds.add("", "", "", team, heightD, weightD, ageInt, gender);
	 		}
	 		uds.close();
		
	}

	
}
