package net.osmand.plus.activities;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.sensei.data.RouteRunData;
import net.osmand.sensei.db.RouteRunDataSource;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class RunFinishedActivity extends SherlockActivity {
	
	private OsmandApplication app;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(R.string.run_finished_title);
		// R.drawable.tab_settings_screen_icon
	
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.run_finished);
		app = getMyApplication();
		RouteRunDataSource rrds = app.getRouteRunDataSource();
		rrds.open();
		RouteRunData rrd = rrds.getLastRouteRun();
		
		TextView duration = (TextView) findViewById(R.id.time_info);
		duration.setText(""+(rrd.getEnd_datetime() - rrd.getStart_datetime()) / 3600);
		
		TextView routeNr = (TextView) findViewById(R.id.routeNumber);
		routeNr.setText(""+rrd.getRoute_id());
		
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        NavUtils.navigateUpFromSameTask(this);
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	public OsmandApplication getMyApplication() {
		return ((OsmandApplication) getApplication());
	}
}
