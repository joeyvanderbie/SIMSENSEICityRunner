package org.hva.cityrunner.plus.activities;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.hva.cityrunner.data.LatLon;
import nl.sense_os.service.ISenseServiceCallback;
import nl.sense_os.service.commonsense.SenseApi;
import nl.sense_os.service.constants.SenseDataTypes;

import org.hva.cityrunner.access.AccessibleAlertBuilder;
import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.Version;
import org.hva.cityrunner.plus.activities.search.SearchActivity;
import org.hva.cityrunner.plus.render.MapRenderRepositories;
import org.hva.cityrunner.sensei.data.RouteNeighbourhood;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.data.UserData;
import org.hva.cityrunner.sensei.db.RouteDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.cityrunner.sensei.db.UserDataSource;
import org.hva.cityrunner.plus.R;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainMenuActivity extends Activity implements  OnItemSelectedListener{

	private static final String FIRST_TIME_APP_RUN = "FIRST_TIME_APP_RUN"; //$NON-NLS-1$
	private static final String VECTOR_INDEXES_CHECK = "VECTOR_INDEXES_CHECK"; //$NON-NLS-1$
	private static final String TIPS_SHOW = "TIPS_SHOW"; //$NON-NLS-1$
	private static final String VERSION_INSTALLED = "VERSION_INSTALLED"; //$NON-NLS-1$
	private static final String EXCEPTION_FILE_SIZE = "EXCEPTION_FS"; //$NON-NLS-1$
	
	private static final String CONTRIBUTION_VERSION_FLAG = "CONTRIBUTION_VERSION_FLAG";
	
	public static final int APP_EXIT_CODE = 4;
	public static final String APP_EXIT_KEY = "APP_EXIT_KEY";
	
	private ProgressDialog startProgressDialog;
	private Spinner neighbourhoods;
	private boolean registerSensorsOnce = false;
	
	public void checkPreviousRunsForExceptions(boolean firstTime) {
		long size = getPreferences(MODE_WORLD_READABLE).getLong(EXCEPTION_FILE_SIZE, 0);
		final OsmandApplication app = ((OsmandApplication) getApplication());
		final File file = app.getAppPath(OsmandApplication.EXCEPTION_PATH);
		if (file.exists() && file.length() > 0) {
			if (size != file.length() && !firstTime) {
				String msg = MessageFormat.format(getString(R.string.previous_run_crashed), OsmandApplication.EXCEPTION_PATH);
				Builder builder = new AccessibleAlertBuilder(MainMenuActivity.this);
				builder.setMessage(msg).setNeutralButton(getString(R.string.close), null);
				builder.setPositiveButton(R.string.send_report, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "j.h.f.van.der.bie@hva.nl" }); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
						intent.setType("vnd.android.cursor.dir/email"); //$NON-NLS-1$
						intent.putExtra(Intent.EXTRA_SUBJECT, "City Runner bug"); //$NON-NLS-1$
						StringBuilder text = new StringBuilder();
						text.append("\nDevice : ").append(Build.DEVICE); //$NON-NLS-1$
						text.append("\nBrand : ").append(Build.BRAND); //$NON-NLS-1$
						text.append("\nModel : ").append(Build.MODEL); //$NON-NLS-1$
						text.append("\nProduct : ").append(Build.PRODUCT); //$NON-NLS-1$
						text.append("\nBuild : ").append(Build.DISPLAY); //$NON-NLS-1$
						text.append("\nVersion : ").append(Build.VERSION.RELEASE); //$NON-NLS-1$
						text.append("\nApp Version : ").append(Version.getAppName(app)); //$NON-NLS-1$
						try {
							PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
							if (info != null) {
								text.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode); //$NON-NLS-1$ //$NON-NLS-2$
							}
						} catch (NameNotFoundException e) {
						}
						intent.putExtra(Intent.EXTRA_TEXT, text.toString());
						startActivity(Intent.createChooser(intent, getString(R.string.send_report)));
					}

				});
				builder.show();
			}
			getPreferences(MODE_WORLD_WRITEABLE).edit().putLong(EXCEPTION_FILE_SIZE, file.length()).commit();
		} else {
			if (size > 0) {
				getPreferences(MODE_WORLD_WRITEABLE).edit().putLong(EXCEPTION_FILE_SIZE, 0).commit();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == APP_EXIT_CODE){
			getMyApplication().closeApplication(this);
		}
	}
	
	public static Animation getAnimation(int left, int top){
		Animation anim = new TranslateAnimation(TranslateAnimation.RELATIVE_TO_SELF, left, 
				TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, top, TranslateAnimation.RELATIVE_TO_SELF, 0);
		anim.setDuration(700);
		anim.setInterpolator(new AccelerateInterpolator());
		return anim;
	}
	
	public static void onCreateMainMenu(Window window, final Activity activity){
		View head = (View) window.findViewById(R.id.Headliner);
		head.startAnimation(getAnimation(0, -1));
		
		View leftview = (View) window.findViewById(R.id.MapButton);
		leftview.startAnimation(getAnimation(-1, 0));
		leftview = (View) window.findViewById(R.id.FavoritesButton);
		leftview.startAnimation(getAnimation(-1, 0));
		
		View rightview = (View) window.findViewById(R.id.SettingsButton);
		rightview.startAnimation(getAnimation(1, 0));
		rightview = (View) window.findViewById(R.id.SearchButton);
		rightview.startAnimation(getAnimation(1, 0));
		
		String textVersion = Version.getAppVersion(((OsmandApplication) activity.getApplication()));
		final TextView textVersionView = (TextView) window.findViewById(R.id.TextVersion);
		textVersionView.setText(textVersion);
		SharedPreferences prefs = activity.getApplicationContext().getSharedPreferences("org.hva.cityrunner.settings", MODE_WORLD_READABLE);
		
		// only one commit should be with contribution version flag
//		 prefs.edit().putBoolean(CONTRIBUTION_VERSION_FLAG, true).commit();
		if (prefs.contains(CONTRIBUTION_VERSION_FLAG)) {
			SpannableString content = new SpannableString(textVersion);
			content.setSpan(new ClickableSpan() {
				
				@Override
				public void onClick(View widget) {
					final Intent mapIntent = new Intent(activity, ContributionVersionActivity.class);
					activity.startActivityForResult(mapIntent, 0);
					// test geo activity
//					String uri = "geo:0,0?q=Amsterdamseweg";
//					Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
//					activity.startActivity(intent);
				}
			}, 0, content.length(), 0);
			textVersionView.setText(content);
			textVersionView.setMovementMethod(LinkMovementMethod.getInstance());
		}
		View helpButton = window.findViewById(R.id.HelpButton);
		helpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				TipsAndTricksActivity tactivity = new TipsAndTricksActivity(activity);
//				Dialog dlg = tactivity.getDialogToShowTips(true, false);
//				dlg.show();
				activity.startActivity(new Intent(activity,WelcomeActivity.class));
			}
		});
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
	
	
	private List<String> getSortedGPXFilenames(File dir) {
		return getSortedGPXFilenames(dir, null);
	}
	
	private List<String> getSortedGPXFilenames(File dir,String sub) {
		final List<String> list = new ArrayList<String>();
		readGpxDirectory(dir, list, "");
		Collections.sort(list, new Comparator<String>() {
			@Override
			public int compare(String object1, String object2) {
				if (object1.compareTo(object2) < 0) {
					return -1;
				} else if (object1.equals(object2)) {
					return 0;
				}
				return 1;
			}

		});
		return list;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);
		boolean exit = false;
		if(getIntent() != null){
			Intent intent = getIntent();
			if(intent.getExtras() != null && intent.getExtras().containsKey(APP_EXIT_KEY)){
				exit = true;
			}
		}

		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.menu);
		
		onCreateMainMenu(getWindow(), this);

		Window window = getWindow();
		final Activity activity = this;
		
		SharedPreferences prefs = activity.getApplicationContext().getSharedPreferences("org.hva.cityrunner.settings", MODE_WORLD_READABLE);
		if (!prefs.contains("SENSE_REGISTERED")) {
			registerSensorsOnce = true;
			prefs.edit().putBoolean("SENSE_REGISTERED", true);
		}
		
		//make buttons from available tracks
//		final File dir = ((OsmandApplication) getApplication()).getAppPath(IndexConstants.GPX_INDEX_DIR);
//		final List<String> list = getSortedGPXFilenames(dir);
//		
//		LinearLayout tracks = (LinearLayout) window.findViewById(R.id.Tracks);
//		LinearLayout.LayoutParams lpButton = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
//		int margin = 8;
//		lpButton.setMargins(margin, margin, margin, margin);
//		
//		LinearLayout.LayoutParams lpLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		lpLayout.setMargins(margin, margin, margin, margin);
//		LinearLayout row = null;
//		int itemsPerLayout = 2;
//		for(int i = 0; i < list.size(); i++){
//			final String tracknr  = list.get(i);
//			if(i % itemsPerLayout == 0){
//				row = new LinearLayout(this);
//				row.setLayoutParams(lpLayout);
//				row.setWeightSum(2);
//				row.setOrientation(LinearLayout.HORIZONTAL);
//				tracks.addView(row);
//			}
//			//create new track here and add to main view
//			Button track = new Button(this);
//			track.setText("Track "+tracknr.substring(0, tracknr.length()-4));
//			track.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					final Intent mapIndent = new Intent(activity, OsmandIntents.getMapActivity());
//					mapIndent.putExtra("track",Integer.parseInt(tracknr.substring(0, tracknr.length()-4)));
//					activity.startActivityForResult(mapIndent, 0);
//				}
//			});
//			track.setBackgroundColor(Color.parseColor(getString(R.color.color_white)));
//			track.setTextColor(Color.parseColor(getString(R.color.color_black)));
//			
//			//Load image belonging to track
//
////			String fname = c.getFilesDir().getAbsolutePath()+"/myfile.png"; Bitmap bm = BitmapFactory.decodeFile(fname); iv.setImageBitmap(bm);
//			
//			track.setLayoutParams(lpButton);
//			row.addView(track);
//
//		}
		
		//Login on Sense
		attemptLogin();
		
		neighbourhoods = (Spinner) findViewById(R.id.neighbourhoodList);
		loadSpinnerData();
		neighbourhoods.setOnItemSelectedListener(this);
		
		//To-Do remove old buttons\
		
//		View showMap = window.findViewById(R.id.MapButton);
//		showMap.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent mapIndent = new Intent(activity, OsmandIntents.getMapActivity());
//				activity.startActivityForResult(mapIndent, 0);
//			}
//		});
//		View settingsButton = window.findViewById(R.id.SettingsButton);
//		settingsButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent settings = new Intent(activity, OsmandIntents.getSettingsActivity());
//				activity.startActivity(settings);
//			}
//		});
//
//		View favouritesButton = window.findViewById(R.id.FavoritesButton);
//		favouritesButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent favorites = new Intent(activity, OsmandIntents.getFavoritesActivity());
//				favorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//				activity.startActivity(favorites);
//			}
//		});
//
		final View closeButton = window.findViewById(R.id.CloseButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getMyApplication().closeApplication(activity);
			}
		});
//		View searchButton = window.findViewById(R.id.SearchButton);
//		searchButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent search = new Intent(activity, OsmandIntents.getSearchActivity());
//				search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//				activity.startActivity(search);
//			}
//		});
		if(exit){
			getMyApplication().closeApplication(activity);
			return;
		}
		OsmandApplication app = getMyApplication();
		
		app.showDialogInitializingCommandPlayer(this);
		
		// restore follow route mode
//		if(app.getSettings().FOLLOW_THE_ROUTE.get() && !app.getRoutingHelper().isRouteCalculated()){
//			final Intent mapIndent = new Intent(this, OsmandIntents.getMapActivity());
//			startActivityForResult(mapIndent, 0);
//			return;
//		}
		startProgressDialog = new ProgressDialog(this);
		getMyApplication().checkApplicationIsBeingInitialized(this, startProgressDialog);
		SharedPreferences pref = getPreferences(MODE_WORLD_WRITEABLE);
		boolean firstTime = false;
		if(!pref.contains(FIRST_TIME_APP_RUN)){
			firstTime = true;
			pref.edit().putBoolean(FIRST_TIME_APP_RUN, true).commit();
			pref.edit().putString(VERSION_INSTALLED, Version.getFullVersion(app)).commit();
			
			applicationInstalledFirstTime();
		} else {
			int i = pref.getInt(TIPS_SHOW, 0);
			if (i < 7){
				pref.edit().putInt(TIPS_SHOW, ++i).commit();
			}
			boolean appVersionChanged = false;
			if(!Version.getFullVersion(app).equals(pref.getString(VERSION_INSTALLED, ""))){
				pref.edit().putString(VERSION_INSTALLED, Version.getFullVersion(app)).commit();
				appVersionChanged = true;
			}
						
			if (i <= 1 || appVersionChanged) {
//				TipsAndTricksActivity tipsActivity = new TipsAndTricksActivity(this);
//				Dialog dlg = tipsActivity.getDialogToShowTips(!appVersionChanged, false);
//				dlg.show();

				activity.startActivity(new Intent(activity,WelcomeActivity.class));
			} else {
				if (startProgressDialog.isShowing()) {
					startProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							checkVectorIndexesDownloaded();
						}
					});
				} else {
					checkVectorIndexesDownloaded();
				}
			}
		}
		checkPreviousRunsForExceptions(firstTime);
	}
	
	   /**
     * Function to load the spinner data from SQLite database
     * */
    private void loadSpinnerData() {
		final OsmandApplication app = ((OsmandApplication) getApplication());

        // database handler
    	RouteDataSource routeDs = app.getRouteDataSource();
    	routeDs.open();
    	 List<String> lables =routeDs.getAllRoutesString();
    	routeDs.close();
    	lables.add(0, this.getString(R.string.select_neighbourhood));
    	
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lables);
 
        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        neighbourhoods = (Spinner) findViewById(R.id.neighbourhoodList);
        neighbourhoods.setAdapter(dataAdapter);
    }
    
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position,
            long id) {
    	if(position != 0){
	        // On selecting a spinner item
	        String label = parent.getItemAtPosition(position).toString();
	        showRoutesForNeighbourhood(label);
    	}else{
    		showNoNeighbourhoodSelected();
    	}
        
    }
 
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
 
    }
    
    private void showNoNeighbourhoodSelected(){
    	LinearLayout tracks = (LinearLayout) findViewById(R.id.Tracks);
		tracks.removeAllViews(); 
    }
    
    private void showRoutesForNeighbourhood(String neighbourhood){
    	final OsmandApplication app = ((OsmandApplication) getApplication());

        // database handler
    	RouteDataSource routeDs = app.getRouteDataSource();
    	routeDs.open();
    	RouteNeighbourhood rn = routeDs.getRouteForNeighbourhood(neighbourhood);
    	routeDs.close();
    	

		LinearLayout tracks = (LinearLayout) findViewById(R.id.Tracks);
		tracks.removeAllViews();
		LinearLayout.LayoutParams lpButton = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1);
		int margin = 8;
		lpButton.setMargins(margin, margin, margin, margin);
		
		LinearLayout.LayoutParams lpLayout = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lpLayout.setMargins(margin, margin, margin, margin);
		LinearLayout row = null;
		int itemsPerLayout = 2;
		for(int i = 0; i < 3; i++){
			String ftrack = "";
			switch(i){
			case 0:
				ftrack = rn.getRoute_h()+".gpx";
				break;
			case 1:
				ftrack = rn.getRoute_v()+".gpx";
				break;
			case 2:
				ftrack = rn.getRoute_a()+".gpx";
				break;
			}
			
			final String tracknr = ftrack;
			if(i % itemsPerLayout == 0){
				row = new LinearLayout(this);
				row.setLayoutParams(lpLayout);
				row.setWeightSum(2);
				row.setOrientation(LinearLayout.HORIZONTAL);
				tracks.addView(row);
			}
			//create new track here and add to main view
			Button track = new Button(this);
			track.setText("Track "+tracknr.substring(0, tracknr.length()-4));
			final Activity activity = this;
			
			track.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					RouteRunDataSource rrds = app.getRouteRunDataSource();
					RouteRunData rrd = new RouteRunData();
					rrd.setRoute_id(Integer.parseInt(tracknr.substring(0, tracknr.length()-4)));
					rrd.setTeam_id(app.team_id);
					rrd.setStart_datetime(System.currentTimeMillis());
					rrds.open();
					rrd = rrds.add(rrd);
					rrds.close();
					app.currentRouteRun = rrd;

					final Intent mapIndent = new Intent(activity, NavigateToStartActivity.class);//OsmandIntents.getMapActivity());
					//mapIndent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
					mapIndent.putExtra("track",Integer.parseInt(tracknr.substring(0, tracknr.length()-4)));
					mapIndent.putExtra("run_id",rrd.getId());
					mapIndent.putExtra("nextActivity", "map");
					activity.startActivityForResult(mapIndent, 0);
					
					
					
//					final Intent mapIndent = new Intent(activity, OsmandIntents.getMoodActivity());//OsmandIntents.getMapActivity());
//					mapIndent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//					mapIndent.putExtra("track",Integer.parseInt(tracknr.substring(0, tracknr.length()-4)));
//					mapIndent.putExtra("run_id",rrd.getId());
//					mapIndent.putExtra("nextActivity", "map");
//					activity.startActivityForResult(mapIndent, 0);
				}
			});
			track.setBackgroundColor(Color.parseColor(getString(R.color.color_white)));
			track.setTextColor(Color.parseColor(getString(R.color.color_black)));
			
			//Load image belonging to track

//			String fname = c.getFilesDir().getAbsolutePath()+"/myfile.png"; Bitmap bm = BitmapFactory.decodeFile(fname); iv.setImageBitmap(bm);
			
			track.setLayoutParams(lpButton);
			row.addView(track);
		}
    	
    }

    
	private void applicationInstalledFirstTime() {
		boolean netOsmandWasInstalled = false;
		try {
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo("org.hva.cityrunner", PackageManager.GET_META_DATA);
			netOsmandWasInstalled = applicationInfo != null && !Version.isFreeVersion(getMyApplication());
		} catch (NameNotFoundException e) {
			netOsmandWasInstalled = false;
		}
		
//		if(netOsmandWasInstalled){
//			Builder builder = new AccessibleAlertBuilder(this);
//			builder.setMessage(R.string.osmand_net_previously_installed);
//			builder.setPositiveButton(R.string.default_buttons_ok, null);
//			builder.show();
//		} else {
//			Builder builder = new AccessibleAlertBuilder(this);
//			builder.setMessage(R.string.first_time_msg);
//			builder.setPositiveButton(R.string.first_time_download, new DialogInterface.OnClickListener() {
//
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					startActivity(new Intent(MainMenuActivity.this, OsmandIntents.getDownloadIndexActivity()));
//				}
//
//			});
//			builder.setNegativeButton(R.string.first_time_continue, null);
//			builder.show();
//		}
	}
	
	protected void checkVectorIndexesDownloaded() {
		MapRenderRepositories maps = getMyApplication().getResourceManager().getRenderer();
		SharedPreferences pref = getPreferences(MODE_WORLD_WRITEABLE);
		boolean check = pref.getBoolean(VECTOR_INDEXES_CHECK, true);
		// do not show each time 
		if (check && new Random().nextInt() % 5 == 1) {
			Builder builder = new AccessibleAlertBuilder(this);
			if(maps.isEmpty()){
				builder.setMessage(R.string.vector_data_missing);
			} else if(!maps.basemapExists()){
				builder.setMessage(R.string.basemap_missing);
			} else {
				return;
			}
			builder.setPositiveButton(R.string.download_files, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(MainMenuActivity.this, DownloadIndexActivity.class));
				}

			});
			builder.setNeutralButton(R.string.vector_map_not_needed, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getPreferences(MODE_WORLD_WRITEABLE).edit().putBoolean(VECTOR_INDEXES_CHECK, false).commit();
				}
			});
			builder.setNegativeButton(R.string.first_time_continue, null);
			builder.show();
		}
		
	}

	private OsmandApplication getMyApplication() {
		return (OsmandApplication) getApplication();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if(id == OsmandApplication.PROGRESS_DIALOG){
			return startProgressDialog;
		}
		return super.onCreateDialog(id);
	}
	

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH
                && event.getRepeatCount() == 0) {
			final Intent search = new Intent(MainMenuActivity.this, SearchActivity.class);
			search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(search);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    public static void backToMainMenuDialog(final Activity a, final LatLon searchLocation) {
		final Dialog dlg = new Dialog(a, R.style.Dialog_Fullscreen);
		final View menuView = (View) a.getLayoutInflater().inflate(R.layout.menu, null);
		menuView.setBackgroundColor(Color.argb(200, 150, 150, 150));
		dlg.setContentView(menuView);
		MainMenuActivity.onCreateMainMenu(dlg.getWindow(), a);
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				ColorDrawable colorDraw = ((ColorDrawable) menuView.getBackground());
				colorDraw.setAlpha((int) (interpolatedTime * 200));
			}
		};
		anim.setDuration(700);
		anim.setInterpolator(new AccelerateInterpolator());
		menuView.setAnimation(anim);

//		View showMap = dlg.findViewById(R.id.MapButton);
//		showMap.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				dlg.dismiss();
//			}
//		});
//		View settingsButton = dlg.findViewById(R.id.SettingsButton);
//		settingsButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent settings = new Intent(a, OsmandIntents.getSettingsActivity());
//				a.startActivity(settings);
//				dlg.dismiss();
//			}
//		});
//
//		View favouritesButton = dlg.findViewById(R.id.FavoritesButton);
//		favouritesButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent favorites = new Intent(a, OsmandIntents.getFavoritesActivity());
//				favorites.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//				a.startActivity(favorites);
//				dlg.dismiss();
//			}
//		});

		View closeButton = dlg.findViewById(R.id.CloseButton);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dlg.dismiss();
				// 1. Work for almost all cases when user open apps from main menu
				Intent newIntent = new Intent(a, OsmandIntents.getMainMenuActivity());
				newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				newIntent.putExtra(MainMenuActivity.APP_EXIT_KEY, MainMenuActivity.APP_EXIT_CODE);
				a.startActivity(newIntent);
				// 2. good analogue but user will come back to the current activity onResume()
				// so application is not reloaded !!!
				// moveTaskToBack(true);
				// 3. bad results if user comes from favorites
				// a.setResult(MainMenuActivity.APP_EXIT_CODE);
				// a.finish();
	
				
			}
		});

//		View searchButton = dlg.findViewById(R.id.SearchButton);
//		searchButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				final Intent search = new Intent(a, OsmandIntents.getSearchActivity());
//				LatLon loc = searchLocation;
//				search.putExtra(SearchActivity.SEARCH_LAT, loc.getLatitude());
//				search.putExtra(SearchActivity.SEARCH_LON, loc.getLongitude());
//				// causes wrong position caching:  search.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//				search.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				a.startActivity(search);
//				dlg.dismiss();
//			}
//		});
		menuView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});

		dlg.show();
		// Intent newIntent = new Intent(a, MainMenuActivity.class);
		// newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(newIntent);
	}
	
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, 0, 0, R.string.exit_Button);
    	return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			getMyApplication().closeApplication(this);
			return true;
		}
		return false;
	}
	
	//Sense functions
	private boolean busy;
    private String mEmail;
    private String mPassword;
    private static final String TAG = "Sense";

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form
     * errors (invalid email, missing fields, etc.), the errors are presented and no actual login
     * attempt is made.
     */
    private void attemptLogin() {
        if (busy) {
            return;
        }

        //To-DO: retrieve user values from database
        
        UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
 		uds.open();
 		UserData user = uds.getUserData();
 		uds.close();
 		
      

             // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
           // mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);

            // log in (you only need to do this once, Sense will remember the login)
            try {
            	  mEmail = user.getEmail();
                  mPassword = user.getPassword();
            	((OsmandApplication) getApplication()).getSensePlatform().login(mEmail, SenseApi.hashPassword(mPassword),
                        mServiceCallback);
                // this is an asynchronous call, we get a callback when the login is complete
                busy = true;
            } catch (IllegalStateException e) {
                Log.w(TAG, "Failed to log in at SensePlatform!", e);
                onLoginFailure(false);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to log in at SensePlatform!", e);
                onLoginFailure(false);
            } catch (NullPointerException e){
            	//No account available
            	 onLoginFailure(false);
            }
        }
	
	 private void onLoginFailure(final boolean forbidden) {

			 	//retrieve login values from database
		        //if values not exist register user
		 		UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
		 		uds.open();
		 		UserData user = uds.getUserData();
		 		uds.close();
		 		if(user == null){
		 			attemptRegistration();
		 		
		 		}else if(user.getEmail() == null || user.getPassword() == null) {
		        	attemptRegistration();
		        	
		        }else if(user.getEmail().equals("") || user.getPassword().equals("")){
		        	attemptRegistration();
		        	
	 			}else{
		        	// update UI
			        runOnUiThread(new Runnable() {

			            @Override
			            public void run() {
			                showProgress(false);

			                    Toast.makeText(MainMenuActivity.this, R.string.login_failure, Toast.LENGTH_LONG)
			                            .show();
			            }
			        });
		        }

		 
		 
	        
	    }

	    private void onLoginSuccess() {
	    	if(registerSensorsOnce){
	            registerSensorsOnce = false;
	    		registerSIMSensors();
	    		
	    	}
        	
	        // update UI
	        runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                showProgress(false);
	                Toast.makeText(MainMenuActivity.this, R.string.login_success, Toast.LENGTH_LONG)
	                        .show();
	            }
	        });

	        //setResult(RESULT_OK);
	       // finish();
	    }
	    
	    
	    
	    private ISenseServiceCallback mServiceCallback = new ISenseServiceCallback.Stub() {

	        @Override
	        public void onChangeLoginResult(int result) throws RemoteException {

	            busy = false;

	            if (result == -2) {
	                // login forbidden
	                onLoginFailure(true);

	            } else if (result == -1) {
	                // login failed
	                onLoginFailure(false);

	            } else {
	                onLoginSuccess();
	            }
	        }

	        @Override
	        public void onRegisterResult(int result) throws RemoteException {
	            // not used
	        }

	        @Override
	        public void statusReport(int status) throws RemoteException {
	            // not used
	        }
	    };
	    /**
	     * Shows the progress UI and hides the login form.
	     */
	    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	    private void showProgress(final boolean show) {
	        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
	        // for very easy animations. If available, use these APIs to fade-in
	        // the progress spinner.
	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
	            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

//	            mLoginStatusView.setVisibility(View.VISIBLE);
//	            mLoginStatusView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0)
//	                    .setListener(new AnimatorListenerAdapter() {
//	                        @Override
//	                        public void onAnimationEnd(Animator animation) {
//	                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//	                        }
//	                    });
//
//	            mLoginFormView.setVisibility(View.VISIBLE);
//	            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1)
//	                    .setListener(new AnimatorListenerAdapter() {
//	                        @Override
//	                        public void onAnimationEnd(Animator animation) {
//	                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//	                        }
//	                    });
	        } else {
	            // The ViewPropertyAnimator APIs are not available, so simply show
	            // and hide the relevant UI components.
//	            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//	            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
	        }
	    }
	    
	    
	    
		private void registerSIMSensors() {
			new Thread() {

				@Override
				public void run() {
					boolean error = false;
					//creeer hier alle sensoren die je wilt maken
					if(!((OsmandApplication) getApplication()).getSensePlatform().addDataPoint(
							"runrecord",
							"runrecord", 
							"runrecord",
							SenseDataTypes.JSON,
							"{\"runid\":\"0\",\"startdatetime\":\""+(System.currentTimeMillis()-10000)+"\",\"enddatetime\":\""+System.currentTimeMillis()+"\",\"id\":\""+0+"\"}",
							System.currentTimeMillis())){
						error = true;
					}	
					if(!((OsmandApplication) getApplication()).getSensePlatform().addDataPoint(
							"affectbutton",
							"AffectAB", 
							"affectbutton",
							SenseDataTypes.JSON,
							"{\"Pleasure\":\""+0+"\",\"Dominance\":\""+0+"\",\"Arousal\":\""+0+"\",\"tracknr\":\""+0+"\",\"runstate\":\""+0+"\"}",
							System.currentTimeMillis())){
						error = true;
					}if(!((OsmandApplication) getApplication()).getSensePlatform().addDataPoint(
											"sensei_accelerometer", 
											"sensei_accelerometer",
											"sensei_accelerometer", 
											SenseDataTypes.JSON, 
											"{\"x-axis\":\""+0+"\",\"y-axis\":\""+0+"\",\"z-axis\":\""+0+"\",\"run_id\":\""+0+"\",\"timestamp\":\""+0+"\"}"
											, System.currentTimeMillis())){
							error = true;
					}
						
					
					if(!error){
						
					//als alle nodige sensoren gemaakt zijn
					//en alleen dan! Voeg gebruiker toe aan groep
//					mApplication.getSensePlatform().addUserToGroup("9568",
//							"9e6a6f89a42e911c4c31f06d17510c39");
//					}
					//test group
					//Simtest
//						{
//						      "accepted": null,
//						      "id": "9869",
//						      "name": "simtest",
//						      "description": "test data voor SIM 2014",
//						      "public": false
//						    }
//						pass: c8d84a64f40f7b6eaeb7c97cc22141a3 - 99BaardHaren(
						((OsmandApplication) getApplication()).getSensePlatform().addUserToGroup("9869",
							"c8d84a64f40f7b6eaeb7c97cc22141a3");
					}
				}
			}.start();
		}
		
	    private void onRegistrationFailure(final boolean forbidden) {

	        // update UI
	        runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                showProgress(false);
	                    Toast.makeText(MainMenuActivity.this, R.string.register_failure,
	                            Toast.LENGTH_LONG).show();
	            }
	        });
	    }

	    private void onRegistrationSuccess() {

	        // update UI
	        runOnUiThread(new Runnable() {

	            @Override
	            public void run() {
	                showProgress(false);
	                Toast.makeText(MainMenuActivity.this, R.string.register_success,
	                        Toast.LENGTH_LONG).show();
	                attemptLogin();
		        	//retry login
	            }
	        });
	    }
	    
	    /**
	     * Attempts to register the account specified by the registration form. If there are form errors
	     * (invalid email, missing fields, etc.), the errors are presented and no actual registration
	     * attempt is made.
	     */
	    private void attemptRegistration() {
	        if (mBusy) {
	            return;
	        }


	        // Store values at the time of the registration attempt.
	        mEmail = "senseiuser+sim"+System.currentTimeMillis()+"@gmail.com";
	        mPassword = "22hond";
	        
	        UserDataSource uds = ((OsmandApplication) getApplication()).getUserDataSource();
	 		uds.open();
	 		UserData user = uds.getUserData();
	 		if(user == null){
	 			user = new UserData(mEmail, mEmail, mPassword, 0);
	 			uds.add(user);
	 		}else{
	 			user.setEmail(mEmail);
	 			user.setPassword(mPassword);
	 			user.setName(mEmail);
	 			uds.update(user);
	 			}
	 		uds.close();
	 		
	        
	        //add username and password to database

	        boolean cancel = false;
	        View focusView = null;

	            // Show a progress spinner, and kick off a background task to perform the user
	            // registration attempt.
	            showProgress(true);

	            // register using SensePlatform
	            try {
	            	((OsmandApplication) getApplication()).getSensePlatform().registerUser(mEmail,
	                        SenseApi.hashPassword(mPassword), mEmail, null, null, null, null, null,
	                        null, mSenseCallback);
	                // this is an asynchronous call, we get a callback when the registration is complete
	                mBusy = true;
	            } catch (IllegalStateException e) {
	                Log.w(TAG, "Failed to register at SensePlatform!", e);
	                onRegistrationFailure(false);
	            } catch (RemoteException e) {
	                Log.w(TAG, "Failed to register at SensePlatform!", e);
	                onRegistrationFailure(false);
	            }
	    }

private boolean mBusy;
private ISenseServiceCallback mSenseCallback = new ISenseServiceCallback.Stub() {

    @Override
    public void onChangeLoginResult(int result) throws RemoteException {
        // not used
    }

    @Override
    public void onRegisterResult(int result) throws RemoteException {
        mBusy = false;

        if (result == -2) {
            // registration forbidden
            onRegistrationFailure(true);

        } else if (result == -1) {
            // registration failed
            onRegistrationFailure(false);

        } else {
            onRegistrationSuccess();
        }
    }

    @Override
    public void statusReport(int status) throws RemoteException {
        // not used
    }
};
}
