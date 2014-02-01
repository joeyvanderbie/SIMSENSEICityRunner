package org.hva.cityrunner.plus.activities;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class UsageDataActivity extends Activity {

	EditText nr_people;
	EditText remarks;
	EditText teamnr;
	CheckBox headphones;
	Spinner phone_position;
int tracknr;
int run_id;
Button next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usagedata);

		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			tracknr = extras.getInt("track", 0);
			run_id = extras.getInt("run_id", 0);
		}
		
		nr_people = (EditText) findViewById(R.id.number_people);
		remarks = (EditText) findViewById(R.id.remarks);
		headphones = (CheckBox) findViewById(R.id.checkbox_used_earphones);
		
		next = (Button) findViewById(R.id.NextButton);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveValues();
				Intent intentSettings = new Intent(UsageDataActivity.this,
						OsmandIntents.getRunFinishedActivity());
				//intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intentSettings);
			}
		});
	next.setActivated(false);
		
		phone_position = (Spinner) findViewById(R.id.spinner_phone_position);
		phone_position.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				next.setActivated(true);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});

	

	}

	private void saveValues() {

		String str_phone_position = phone_position.getSelectedItem() != null ? phone_position
				.getSelectedItem().toString() : "n.a";
				
		String str_nr_people = nr_people.getText().toString();
		String str_remarks = remarks.getText().toString();
		int heightD = Integer.parseInt(str_nr_people.equals("") ? "0" : str_nr_people);
		
		RouteRunDataSource uds = ((OsmandApplication) getApplication())
				.getRouteRunDataSource();
		uds.open();
		RouteRunData rrd = uds.getRouteRun(run_id);
		rrd.setNumber_people(heightD);
		rrd.setPhone_position(str_phone_position);
		rrd.setHeadphones(headphones.isChecked());
		rrd.setRemarks(str_remarks);
		uds.update(rrd);
		uds.close();

	}

}
