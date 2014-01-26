package org.hva.cityrunner.plus.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hva.cityrunner.CallbackWithObject;
import org.hva.cityrunner.IndexConstants;
import org.hva.cityrunner.access.AccessibleToast;
import org.hva.cityrunner.plus.ApplicationMode;
import org.hva.cityrunner.plus.GPXUtilities;
import org.hva.cityrunner.plus.GPXUtilities.GPXFile;
import org.hva.cityrunner.plus.GPXUtilities.WptPt;
import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class NavigateToStartActivity  extends Activity{
	
	int tracknr = 0;
	int run_id = 0;
	String nextActivity = "map";
	 private NavigateToStartActivity activity;
	 private TextView startingPoint;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	
		setContentView(R.layout.activity_navigatetostart);
        
        activity = this;
        Bundle extras = getIntent().getExtras();

		if (extras != null) {
			tracknr = extras.getInt("track", 0);
			run_id = extras.getInt("run_id", 0);
			nextActivity = extras.getString("nextActivity", "map");
		}
        
        Button next = (Button) findViewById(R.id.NextButton);
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Intent mapIndent = new Intent(activity, OsmandIntents.getMoodActivity());//OsmandIntents.getMapActivity());
				mapIndent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				mapIndent.putExtra("track",tracknr);
				mapIndent.putExtra("run_id",run_id);
				mapIndent.putExtra("nextActivity", "map");
				activity.startActivityForResult(mapIndent, 0);
			}
		});
		
		Button prev = (Button) findViewById(R.id.PreviousButton);
		prev.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		startingPoint = (TextView) findViewById(R.id.point_text);
		//laad gpx file (functie uit mapAcitivyLayers
		navigateUsingGPX(ApplicationMode.PEDESTRIAN, tracknr);

		//haal eerste punt uit gpx file
		//geef eerste punt weer op kaart
		//maak link naar google maps voor eerste punt.
		
		//maak startbutton inactief
		//luister naar locatie
		//update afstand van locatie op scherm.
		//als locatie binnen 100m van startlocatie is, maak start button actief.
		
		
		
	}
	
	
	 public void navigateUsingGPX(final ApplicationMode appMode, int trackNumber) {
			
			getGPXFile(new CallbackWithObject<GPXFile>(){

				@Override
				public boolean processResult(GPXFile result) {
					final WptPt start = result.tracks.get(0).segments.get(0).points.get(0);
					startingPoint = (TextView) activity.findViewById(R.id.point_text);
					String text = "Lat:"+start.lat+"\nLong:"+start.lon;
					startingPoint.setText(text);
					
					
					Button showPoint = (Button) activity.findViewById(R.id.locationButton);
					showPoint.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(android.content.Intent.ACTION_VIEW, 
									Uri.parse("http://maps.google.com/maps?daddr="+start.lat+","+start.lon));
									activity.startActivity(intent);
						}
					});
					
					
					return true;
				} 
				
				
			}, trackNumber);
	 }
	 
	 public void getGPXFile(final CallbackWithObject<GPXFile> callbackWithObject, int nr){
			GPXFile gx = new GPXFile();
			gx.showCurrentTrack = true;
			final File dir = getMyApplication().getAppPath(IndexConstants.GPX_INDEX_DIR);
			final List<String> list = getSortedGPXFilenames(dir);
			
			loadGPXFileInDifferentThread(callbackWithObject, true, dir, gx,
					nr+".gpx");
		}
		
		private void loadGPXFileInDifferentThread(final CallbackWithObject<GPXFile> callbackWithObject,
				final boolean convertCloudmade, final File dir, final GPXFile currentFile, final String... filename) {
			final ProgressDialog dlg = ProgressDialog.show(activity, getString(R.string.loading),
					getString(R.string.loading_data));
			new Thread(new Runnable() {
				@Override
				public void run() {
					GPXFile r = currentFile; 
					for(String fname : filename) {
						final File f = new File(dir, fname);
						GPXFile res = GPXUtilities.loadGPXFile(activity.getMyApplication(), f, convertCloudmade);
						GPXUtilities.mergeGPXFileInto(res, r);
						r = res;
					}
					final GPXFile res = r;
					dlg.dismiss();
					if (res != null) {
						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (res.warning != null) {
									AccessibleToast.makeText(activity, res.warning, Toast.LENGTH_LONG).show();
								} else {
									callbackWithObject.processResult(res);
								}
							}
						});
					}
				}

			}, "Loading gpx").start(); //$NON-NLS-1$
		}
		
		public OsmandApplication getMyApplication() {
			return ((OsmandApplication) getApplication());
		}
		
		private List<String> getSortedGPXFilenames(File dir) {
			return getSortedGPXFilenames(dir, null);
		}
		
		private List<String> getSortedGPXFilenames(File dir,String sub) {
			final List<String> list = new ArrayList<String>();
			readGpxDirectory(dir, list, "");
			Collections.sort(list, new Comparator<String>() {
				@Override
				public int compare(String object1, String object2) {
					if (object1.compareTo(object2) > 0) {
						return -1;
					} else if (object1.equals(object2)) {
						return 0;
					}
					return 1;
				}

			});
			return list;
		}
		
		private void readGpxDirectory(File dir, final List<String> list, String parent) {
			if (dir != null && dir.canRead()) {
				File[] files = dir.listFiles();
				if (files != null) {
					for (File f : files) {
						if (f.getName().toLowerCase().endsWith(".gpx")) { //$NON-NLS-1$
							list.add(parent + f.getName());
						} else if (f.isDirectory()) {
							readGpxDirectory(f, list, parent + f.getName() + "/");
						}
					}
				}
			}
		}
}
