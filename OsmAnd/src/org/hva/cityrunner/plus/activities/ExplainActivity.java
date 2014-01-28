package org.hva.cityrunner.plus.activities;

import org.hva.cityrunner.plus.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ExplainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_explain);

		Button next = (Button) findViewById(R.id.NextButton);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intentSettings = new Intent(ExplainActivity.this,
						MainMenuActivity.class);
//				intentSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intentSettings);
			}
		});

		Button prev = (Button) findViewById(R.id.PreviousButton);
		prev.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
