package org.hva.cityrunner.plus.activities;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.sensei.data.UserData;
import org.hva.cityrunner.sensei.db.UserDataSource;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class UserDataActivity extends Activity {

	EditText height;
	EditText weight;
	EditText teamnr;
	EditText age;
	Spinner genderSpin;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userdata);

		height = (EditText) findViewById(R.id.height);
		weight = (EditText) findViewById(R.id.weight);
		teamnr = (EditText) findViewById(R.id.TeamNR);
		age = (EditText) findViewById(R.id.age);

		genderSpin = (Spinner) findViewById(R.id.gender_spinner);

		UserDataSource uds = ((OsmandApplication) getApplication())
				.getUserDataSource();
		uds.open();
		UserData user = uds.getUserData();
		if (user != null) {
			if(user.getHeight() > 0){
				height.setText(user.getHeight() + "");
			}if(user.getWeight() > 0){
				weight.setText(user.getWeight() + "");
			}
			if(user.getTeamid() > 0){
				teamnr.setText(user.getTeamid() + "");
			}
			if(user.getAge() > 0){
				age.setText(user.getAge() + "");
			}

			if (user.getGender() != null) {
				if (user.getGender().equals("Male")) {
					genderSpin.setSelection(1);
				} else if (user.getGender().equals("Female")) {
					genderSpin.setSelection(2);
				}
			}
		}
		uds.close();

		Button next = (Button) findViewById(R.id.NextButton);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveValues();
				startActivity(new Intent(UserDataActivity.this,
						ExplainActivity.class));
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

	private void saveValues() {

		String gender = genderSpin.getSelectedItem() != null ? genderSpin
				.getSelectedItem().toString() : "n.a";
		String heightS = height.getText().toString();
		String weightS = weight.getText().toString();
		String teamS = teamnr.getText().toString();
		String ageS = age.getText().toString();

		double heightD = Double.parseDouble(heightS.equals("") ? "0" : heightS);
		double weightD = Double.parseDouble(weightS.equals("") ? "0" : weightS);
		int team = Integer.parseInt(teamS.equals("") ? "0" : teamS);
		int ageInt = Integer.parseInt(ageS.equals("") ? "0" : ageS);

		UserDataSource uds = ((OsmandApplication) getApplication())
				.getUserDataSource();
		uds.open();
		UserData user = uds.getUserData();
		if (user != null) {
			user.setWeight(weightD);
			user.setHeight(heightD);
			user.setTeamid(team);
			user.setAge(ageInt);
			user.setGender(gender);
			uds.update(user);

		} else {
			uds.add("", "", "", team, heightD, weightD, ageInt, gender);
		}
		uds.close();

	}

}
