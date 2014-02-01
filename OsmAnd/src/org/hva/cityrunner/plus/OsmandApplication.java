package org.hva.cityrunner.plus;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hva.cityrunner.IndexConstants;
import org.hva.cityrunner.Location;
import org.hva.cityrunner.data.FavouritePoint;
import org.hva.cityrunner.render.RenderingRulesStorage;
import org.hva.cityrunner.util.Algorithms;
import nl.sense_os.platform.SenseApplication;

import org.hva.cityrunner.PlatformUtilCityRunner;
import org.hva.cityrunner.access.AccessibleToast;
import org.hva.cityrunner.plus.GPXUtilities.GPXFile;
import org.hva.cityrunner.plus.GPXUtilities.WptPt;
import org.hva.cityrunner.plus.activities.DayNightHelper;
import org.hva.cityrunner.plus.activities.LiveMonitoringHelper;
import org.hva.cityrunner.plus.activities.OsmandIntents;
import org.hva.cityrunner.plus.activities.SavingTrackHelper;
import org.hva.cityrunner.plus.activities.SettingsActivity;
import org.hva.cityrunner.plus.api.ExternalServiceAPI;
import org.hva.cityrunner.plus.api.InternalOsmAndAPI;
import org.hva.cityrunner.plus.api.InternalToDoAPI;
import org.hva.cityrunner.plus.api.SQLiteAPI;
import org.hva.cityrunner.plus.api.SQLiteAPIImpl;
import org.hva.cityrunner.plus.api.SettingsAPI;
import org.hva.cityrunner.plus.render.NativeOsmandLibrary;
import org.hva.cityrunner.plus.render.RendererRegistry;
import org.hva.cityrunner.plus.resources.ResourceManager;
import org.hva.cityrunner.plus.routing.RoutingHelper;
import org.hva.cityrunner.plus.voice.CommandPlayer;
import org.hva.cityrunner.plus.voice.CommandPlayerException;
import org.hva.cityrunner.plus.voice.CommandPlayerFactory;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.AccelDataSource;
import org.hva.cityrunner.sensei.db.LocationDataSource;
import org.hva.cityrunner.sensei.db.QueueDataSource;
import org.hva.cityrunner.sensei.db.RouteDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.cityrunner.sensei.db.UserDataSource;
import org.hva.cityrunner.plus.R;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockExpandableListActivity;
import com.actionbarsherlock.app.SherlockListActivity;


public class OsmandApplication extends SenseApplication implements ClientContext {
	public static final String EXCEPTION_PATH = "exception.log"; //$NON-NLS-1$
	private static final org.apache.commons.logging.Log LOG = PlatformUtilCityRunner.getLog(OsmandApplication.class);

	ResourceManager manager = null;
	PoiFiltersHelper poiFilters = null;
	RoutingHelper routingHelper = null;
	RouteRunDataSource routeRunDataSource = null;
	RouteDataSource routeDataSource = null;
	LocationDataSource locationDataSource = null;
	QueueDataSource queueDataSource = null;
	AccelDataSource accelDataSource = null;
	UserDataSource userDataSource = null;
	FavouritesDbHelper favorites = null;
	CommandPlayer player = null;

	OsmandSettings osmandSettings = null;

	DayNightHelper daynightHelper;
	NavigationService navigationService;
	RendererRegistry rendererRegistry;
	OsmAndLocationProvider locationProvider;
	OsmAndTaskManager taskManager;

	// start variables
	private ProgressDialogImplementation startDialog;
	private List<String> startingWarnings;
	private Handler uiHandler;
	private GPXFile gpxFileToDisplay;
	private SavingTrackHelper savingTrackHelper;
	private LiveMonitoringHelper liveMonitoringHelper;
	private TargetPointsHelper targetPointsHelper;

	private boolean applicationInitializing = false;
	private Locale prefferedLocale = null;
	
	SettingsAPI settingsAPI;
	ExternalServiceAPI externalServiceAPI;
	InternalToDoAPI internalToDoAPI;
	InternalOsmAndAPI internalOsmAndAPI;
	SQLiteAPI sqliteAPI;
	
	public RouteRunData currentRouteRun;
	public int team_id = 0;
	
	@Override
	public void onCreate() {
		long timeToStart = System.currentTimeMillis();
		if (Version.getAppName(this).equals("OsmAnd~")) {
			if (android.os.Build.VERSION.SDK_INT >= 9) {
				try {
					Class.forName("org.hva.cityrunner.plus.base.EnableStrictMode").newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		super.onCreate();
		 
		tracksFromAssetsToSD();
		voiceFromAssetsToSD();
		
		settingsAPI = new org.hva.cityrunner.plus.api.SettingsAPIImpl(this);
		externalServiceAPI = new org.hva.cityrunner.plus.api.ExternalServiceAPIImpl(this);
		internalToDoAPI = new org.hva.cityrunner.plus.api.InternalToDoAPIImpl(this);
		internalOsmAndAPI = new org.hva.cityrunner.plus.api.InternalOsmAndAPIImpl(this);
		sqliteAPI = new SQLiteAPIImpl(this);

		// settings used everywhere so they need to be created first
		osmandSettings = createOsmandSettingsInstance();
		// always update application mode to default
		if(!osmandSettings.FOLLOW_THE_ROUTE.get()){
			osmandSettings.APPLICATION_MODE.set(osmandSettings.DEFAULT_APPLICATION_MODE.get());
		}
		
		applyTheme(this);
		
		routingHelper = new RoutingHelper(this, player);
		taskManager = new OsmAndTaskManager(this);
		manager = new ResourceManager(this);
		daynightHelper = new DayNightHelper(this);
		locationProvider = new OsmAndLocationProvider(this);
		savingTrackHelper = new SavingTrackHelper(this);
		liveMonitoringHelper = new LiveMonitoringHelper(this);
		uiHandler = new Handler();
		rendererRegistry = new RendererRegistry();
		targetPointsHelper = new TargetPointsHelper(this);
//		if(!osmandSettings.FOLLOW_THE_ROUTE.get()) {
//			targetPointsHelper.clearPointToNavigate(false);
//		}
		
		checkPrefferedLocale();
		startApplication();
		if (LOG.isDebugEnabled()) {
			LOG.debug("Time to start application " + (System.currentTimeMillis() - timeToStart) + " ms. Should be less < 800 ms");
		}
		timeToStart = System.currentTimeMillis();
		OsmandPlugin.initPlugins(this);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Time to init plugins " + (System.currentTimeMillis() - timeToStart) + " ms. Should be less < 800 ms");
		}
		
		
	}
	private void voiceFromAssetsToSD(){
		try {
			AssetManager assetFiles = getAssets();

			// MyHtmlFiles is the name of folder from inside our assets folder
			String[] files = assetFiles.list("voice/en");

			File dir = new File(Environment.getExternalStorageDirectory() + "/osmand/voice/en");
			// have the object build the directory structure, if needed.
			dir.mkdirs();
			
			// Initialize streams
			InputStream in = null;
			OutputStream out = null;

			for (int i = 0; i < files.length; i++) {

//				if (files[i].toString().equalsIgnoreCase("images")
//						|| files[i].toString().equalsIgnoreCase("js")) {
//
//					/*
//					 * @Do nothing. images and js are folders but they will be
//					 * interpreted as files.
//					 * 
//					 * @This is to prevent the app from throwing file not found
//					 * exception.
//					 */
//
//				} else {

					/*
					 * @Folder name is also case sensitive
					 * 
					 * @MyHtmlFiles is the folder from our assets
					 */
					in = assetFiles.open("voice/en/" + files[i]);

					/*
					 * Currently we will copy the files to the root directory
					 * but you should create specific directory for your app
					 */
					out = new FileOutputStream(
							Environment.getExternalStorageDirectory() + "/osmand/voice/en/"
									+ files[i]);
					copyAssetFiles(in, out);

//				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private void tracksFromAssetsToSD(){
		try {
			AssetManager assetFiles = getAssets();

			// MyHtmlFiles is the name of folder from inside our assets folder
			String[] files = assetFiles.list("tracks");

			File dir = new File(Environment.getExternalStorageDirectory() + "/osmand/tracks");
			// have the object build the directory structure, if needed.
			dir.mkdirs();
			
			// Initialize streams
			InputStream in = null;
			OutputStream out = null;

			for (int i = 0; i < files.length; i++) {

//				if (files[i].toString().equalsIgnoreCase("images")
//						|| files[i].toString().equalsIgnoreCase("js")) {
//
//					/*
//					 * @Do nothing. images and js are folders but they will be
//					 * interpreted as files.
//					 * 
//					 * @This is to prevent the app from throwing file not found
//					 * exception.
//					 */
//
//				} else {

					/*
					 * @Folder name is also case sensitive
					 * 
					 * @MyHtmlFiles is the folder from our assets
					 */
					in = assetFiles.open("tracks/" + files[i]);

					/*
					 * Currently we will copy the files to the root directory
					 * but you should create specific directory for your app
					 */
					out = new FileOutputStream(
							Environment.getExternalStorageDirectory() + "/osmand/tracks/"
									+ files[i]);
					copyAssetFiles(in, out);

//				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	// Buffer size used.
		private final static int BUFFER_SIZE = 1024;

	private static void copyAssetFiles(InputStream in, OutputStream out) {
		try {

			byte[] buffer = new byte[BUFFER_SIZE];
			int read;

			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}

			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		if (routingHelper != null) {
			routingHelper.getVoiceRouter().onApplicationTerminate(this);
		}
	}

	public RendererRegistry getRendererRegistry() {
		return rendererRegistry;
	}
	
	public OsmAndTaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Creates instance of OsmandSettings
	 * 
	 * @return Reference to instance of OsmandSettings
	 */
	protected OsmandSettings createOsmandSettingsInstance() {
		return new OsmandSettings(this);
	}
	
	public OsmAndLocationProvider getLocationProvider() {
		return locationProvider;
	}

	/**
	 * Application settings
	 * 
	 * @return Reference to instance of OsmandSettings
	 */
	public OsmandSettings getSettings() {
		if (osmandSettings == null) {
			LOG.error("Trying to access settings before they were created");
		}
		return osmandSettings;
	}

	public SavingTrackHelper getSavingTrackHelper() {
		return savingTrackHelper;
	}

	public LiveMonitoringHelper getLiveMonitoringHelper() {
		return liveMonitoringHelper;
	}

	public PoiFiltersHelper getPoiFilters() {
		if (poiFilters == null) {
			poiFilters = new PoiFiltersHelper(this);
		}
		return poiFilters;
	}

	public void setGpxFileToDisplay(GPXFile gpxFileToDisplay, boolean showCurrentGpxFile) {
		this.gpxFileToDisplay = gpxFileToDisplay;
		osmandSettings.SHOW_CURRENT_GPX_TRACK.set(showCurrentGpxFile);
		if (gpxFileToDisplay == null) {
			getFavorites().setFavoritePointsFromGPXFile(null);
		} else {
			List<FavouritePoint> pts = new ArrayList<FavouritePoint>();
			for (WptPt p : gpxFileToDisplay.points) {
				FavouritePoint pt = new FavouritePoint();
				pt.setLatitude(p.lat);
				pt.setLongitude(p.lon);
				if (p.name == null) {
					p.name = "";
				}
				pt.setName(p.name);
				pts.add(pt);
			}
			gpxFileToDisplay.proccessPoints();
			getFavorites().setFavoritePointsFromGPXFile(pts);
		}
	}

	public GPXFile getGpxFileToDisplay() {
		return gpxFileToDisplay;
	}

	public FavouritesDbHelper getFavorites() {
		if (favorites == null) {
			favorites = new FavouritesDbHelper(this);
		}
		return favorites;
	}
	
//	public SenseiDbHelper get SenseiDbHelper(){
//		if(senseiDbHelper == null){
//			senseiDbHelper = new SenseiDbHelper(this);
//		}
//		return senseiDbHelper;
//	}
	
	public RouteRunDataSource getRouteRunDataSource(){
		if(routeRunDataSource == null){
			routeRunDataSource = new RouteRunDataSource(this);
		}
		return routeRunDataSource;
	}
	
	public AccelDataSource getAccelDataSource(){
		if(accelDataSource == null){
			accelDataSource = new AccelDataSource(this);
		}
		return accelDataSource;
	}
	
	public QueueDataSource getQueueDataSource(){
		if(queueDataSource == null){
			queueDataSource = new QueueDataSource(this);
		}
		return queueDataSource;
	}
	
	public RouteDataSource getRouteDataSource(){
		if(routeDataSource == null){
			routeDataSource = new RouteDataSource(this);
		}
		return routeDataSource;
	}
	
	public UserDataSource getUserDataSource(){
		if(userDataSource == null){
			userDataSource = new UserDataSource(this);
		}
		return userDataSource;
	}
	
	public LocationDataSource getLocationDataSource(){
		if(locationDataSource == null){
			locationDataSource = new LocationDataSource(this);
		}
		return locationDataSource;
	}

	public ResourceManager getResourceManager() {
		return manager;
	}

	public DayNightHelper getDaynightHelper() {
		return daynightHelper;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		manager.onLowMemory();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (prefferedLocale != null && !newConfig.locale.getLanguage().equals(prefferedLocale.getLanguage())) {
			super.onConfigurationChanged(newConfig);
			// ugly fix ! On devices after 4.0 screen is blinking when you rotate device!
			if (Build.VERSION.SDK_INT < 14) {
				newConfig.locale = prefferedLocale;
			}
			getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
			Locale.setDefault(prefferedLocale);
		} else {
			super.onConfigurationChanged(newConfig);
		}
	}

	public void checkPrefferedLocale() {
		Configuration config = getBaseContext().getResources().getConfiguration();
		String lang = osmandSettings.PREFERRED_LOCALE.get();
		if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
			prefferedLocale = new Locale(lang);
			Locale.setDefault(prefferedLocale);
			config.locale = prefferedLocale;
			getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}

	}

	public static final int PROGRESS_DIALOG = 5;

	/**
	 * @param activity
	 *            that supports onCreateDialog({@link #PROGRESS_DIALOG}) and returns @param progressdialog
	 * @param progressDialog
	 *            - it should be exactly the same as onCreateDialog
	 * @return
	 */
	public void checkApplicationIsBeingInitialized(Activity activity, ProgressDialog progressDialog) {
		// start application if it was previously closed
		startApplication();
		synchronized (OsmandApplication.this) {
			if (startDialog != null) {
				try {
					SpecialPhrases.setLanguage(this, osmandSettings);
				} catch (IOException e) {
					LOG.error("I/O exception", e);
					Toast error = Toast.makeText(this, "Error while reading the special phrases. Restart OsmAnd if possible",
							Toast.LENGTH_LONG);
					error.show();
				}

				progressDialog.setTitle(getString(R.string.loading_data));
				progressDialog.setMessage(getString(R.string.reading_indexes));
				activity.showDialog(PROGRESS_DIALOG);
				startDialog.setDialog(progressDialog);
			} else if (startingWarnings != null) {
				//showWarnings(startingWarnings, activity);
			}
		}
	}

	public boolean isApplicationInitializing() {
		return startDialog != null;
	}

	public RoutingHelper getRoutingHelper() {
		return routingHelper;
	}

	public CommandPlayer getPlayer() {
		return player;
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext) {
		showDialogInitializingCommandPlayer(uiContext, true);
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext, boolean warningNoneProvider) {
		showDialogInitializingCommandPlayer(uiContext, warningNoneProvider, null);
	}

	public void showDialogInitializingCommandPlayer(final Activity uiContext, boolean warningNoneProvider, Runnable run) {
		String voiceProvider = osmandSettings.VOICE_PROVIDER.get();
		if (voiceProvider == null || OsmandSettings.VOICE_PROVIDER_NOT_USE.equals(voiceProvider)) {
			if (warningNoneProvider && voiceProvider == null) {
				Builder builder = new AlertDialog.Builder(uiContext);
				builder.setCancelable(true);
				builder.setNegativeButton(R.string.default_buttons_cancel, null);
				builder.setPositiveButton(R.string.default_buttons_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(uiContext, SettingsActivity.class);
						intent.putExtra(SettingsActivity.INTENT_KEY_SETTINGS_SCREEN, SettingsActivity.SCREEN_NAVIGATION_SETTINGS);
						uiContext.startActivity(intent);
					}
				});
				builder.setTitle(R.string.voice_is_not_available_title);
				builder.setMessage(R.string.voice_is_not_available_msg);
				builder.show();
			}

		} else {
			if (player == null || !Algorithms.objectEquals(voiceProvider, player.getCurrentVoice())) {
				initVoiceDataInDifferentThread(uiContext, voiceProvider, run);
			}
		}

	}

	private void initVoiceDataInDifferentThread(final Activity uiContext, final String voiceProvider, final Runnable run) {
		final ProgressDialog dlg = ProgressDialog.show(uiContext, getString(R.string.loading_data),
				getString(R.string.voice_data_initializing));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (player != null) {
						player.clear();
					}
					player = CommandPlayerFactory.createCommandPlayer(voiceProvider, OsmandApplication.this, uiContext);
					routingHelper.getVoiceRouter().setPlayer(player);
					dlg.dismiss();
					if (run != null && uiContext != null) {
						uiContext.runOnUiThread(run);
					}
				} catch (CommandPlayerException e) {
					dlg.dismiss();
					showWarning(uiContext, e.getError());
				}
			}
		}).start();
	}

	public NavigationService getNavigationService() {
		return navigationService;
	}

	public void setNavigationService(NavigationService navigationService) {
		this.navigationService = navigationService;
	}


	private void fullExit() {
		// http://stackoverflow.com/questions/2092951/how-to-close-android-application
		System.runFinalizersOnExit(true);
		System.exit(0);
	}

	public synchronized void closeApplication(final Activity activity) {
//		if (getNavigationService() != null) {
//			Builder bld = new AlertDialog.Builder(activity);
//			bld.setMessage(R.string.background_service_is_enabled_question);
//			bld.setPositiveButton(R.string.default_buttons_yes, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					closeApplicationAnyway(activity, true);
//				}
//			});
//			bld.setNegativeButton(R.string.default_buttons_no, new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					closeApplicationAnyway(activity, false);
//				}
//			});
//			bld.show();
//		} else {
			closeApplicationAnyway(activity, true);
//		}
	}

	private void closeApplicationAnyway(final Activity activity, boolean disableService) {
		if (applicationInitializing) {
			manager.close();
		}
		applicationInitializing = false;

		activity.finish();

		if (getNavigationService() == null) {
			fullExit();
		}
		else if (disableService) {
			final Intent serviceIntent = new Intent(this, NavigationService.class);
			stopService(serviceIntent);

			new Thread(new Runnable() {
				public void run() {
					//wait until the service has fully stopped
					while (getNavigationService() != null) {
						try {
							Thread.sleep(100);
						}
							catch (InterruptedException e) {
						}
					}

					fullExit();
				}
			}).start();
		}
	}

	public synchronized void startApplication() {
		if (applicationInitializing) {
			return;
		}
		applicationInitializing = true;
		startDialog = new ProgressDialogImplementation(this, null, false);

		startDialog.setRunnable("Initializing app", new Runnable() { //$NON-NLS-1$
					@Override
					public void run() {
						startApplicationBackground();
					}
				});
		startDialog.run();

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

	}

	public String exportFavorites(File f) {
		GPXFile gpx = new GPXFile();
		for (FavouritePoint p : getFavorites().getFavouritePoints()) {
			if (p.isStored()) {
				WptPt pt = new WptPt();
				pt.lat = p.getLatitude();
				pt.lon = p.getLongitude();
				pt.name = p.getName() + "_" + p.getCategory();
				gpx.points.add(pt);
			}
		}
		return GPXUtilities.writeGpxFile(f, gpx, this);
	}

	private void startApplicationBackground() {
		List<String> warnings = new ArrayList<String>();
		try {
			if (!Version.isBlackberry(this)) {
				if (osmandSettings.NATIVE_RENDERING_FAILED.get()) {
					osmandSettings.SAFE_MODE.set(true);
			//		osmandSettings.NATIVE_RENDERING_FAILED.set(false);
					warnings.add(getString(R.string.native_library_not_supported));
				} else {
					osmandSettings.SAFE_MODE.set(false);
					osmandSettings.NATIVE_RENDERING_FAILED.set(true);
					startDialog.startTask(getString(R.string.init_native_library), -1);
					RenderingRulesStorage storage = rendererRegistry.getCurrentSelectedRenderer();
					try{
						boolean initialized = NativeOsmandLibrary.getLibrary(storage, this) != null;
						osmandSettings.NATIVE_RENDERING_FAILED.set(false);
						if (!initialized) {
							LOG.info("Native library could not be loaded!");
							osmandSettings.NATIVE_RENDERING_FAILED.set(true);
							osmandSettings.SAFE_MODE.set(true);

						}
					}catch(UnsatisfiedLinkError er){
						LOG.debug("Native library could not be loaded!", er);
						osmandSettings.NATIVE_RENDERING_FAILED.set(true);
						osmandSettings.SAFE_MODE.set(true);
					}
				}
			}
			warnings.addAll(manager.reloadIndexes(startDialog));
			player = null;
			if (savingTrackHelper.hasDataToSave()) {
				startDialog.startTask(getString(R.string.saving_gpx_tracks), -1);
				warnings.addAll(savingTrackHelper.saveDataToGpx());
			}

			// restore backuped favorites to normal file
			final File appDir = getAppPath(null);
			File save = new File(appDir, FavouritesDbHelper.FILE_TO_SAVE);
			File bak = new File(appDir, FavouritesDbHelper.FILE_TO_BACKUP);
			if (bak.exists() && (!save.exists() || bak.lastModified() > save.lastModified())) {
				if (save.exists()) {
					save.delete();
				}
				bak.renameTo(save);
			}
		} finally {
			synchronized (OsmandApplication.this) {
				final ProgressDialog toDismiss;
				if (startDialog != null) {
					toDismiss = startDialog.getDialog();
				} else {
					toDismiss = null;
				}
				startDialog = null;

				if (toDismiss != null) {
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							if (toDismiss != null) {
								// TODO handling this dialog is bad, we need a better standard way
								toDismiss.dismiss();
								// toDismiss.getOwnerActivity().dismissDialog(PROGRESS_DIALOG);
							}
						}
					});
					//showWarnings(warnings, toDismiss.getContext());
				} else {
					startingWarnings = warnings;
				}
			}
		}
	}

	protected void showWarnings(List<String> warnings, final Context uiContext) {
		if (warnings != null && !warnings.isEmpty()) {
			final StringBuilder b = new StringBuilder();
			boolean f = true;
			for (String w : warnings) {
				if (f) {
					f = false;
				} else {
					b.append('\n');
				}
				b.append(w);
			}
			showWarning(uiContext, b.toString());
		}
	}

	private void showWarning(final Context uiContext, final String b) {
		uiHandler.post(new Runnable() {
			@Override
			public void run() {
				AccessibleToast.makeText(uiContext, b.toString(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private class DefaultExceptionHandler implements UncaughtExceptionHandler {

		private UncaughtExceptionHandler defaultHandler;
		private PendingIntent intent;

		public DefaultExceptionHandler() {
			defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
			intent = PendingIntent.getActivity(OsmandApplication.this.getBaseContext(), 0,
					new Intent(OsmandApplication.this.getBaseContext(), OsmandIntents.getMainMenuActivity()), 0);
		}

		@Override
		public void uncaughtException(final Thread thread, final Throwable ex) {
			File file = getAppPath(EXCEPTION_PATH);
			try {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(out);
				ex.printStackTrace(printStream);
				StringBuilder msg = new StringBuilder();
				msg.append("Version  " + Version.getFullVersion(OsmandApplication.this) + "\n"). //$NON-NLS-1$ 
						append(DateFormat.format("dd.MM.yyyy h:mm:ss", System.currentTimeMillis()));
				try {
					PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
					if (info != null) {
						msg.append("\nApk Version : ").append(info.versionName).append(" ").append(info.versionCode); //$NON-NLS-1$ //$NON-NLS-2$
					}
				} catch (Throwable e) {
				}
				msg.append("\n"). //$NON-NLS-1$//$NON-NLS-2$
						append("Exception occured in thread " + thread.toString() + " : \n"). //$NON-NLS-1$ //$NON-NLS-2$
						append(new String(out.toByteArray()));

				if (file.getParentFile().canWrite()) {
					BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
					writer.write(msg.toString());
					writer.close();
				}
				if (routingHelper.isFollowingMode()) {
					AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 2000, intent);
					System.exit(2);
				}
				defaultHandler.uncaughtException(thread, ex);
			} catch (Exception e) {
				// swallow all exceptions
				android.util.Log.e(PlatformUtilCityRunner.TAG, "Exception while handle other exception", e); //$NON-NLS-1$
			}

		}
	}

	
	public TargetPointsHelper getTargetPointsHelper() {
		return targetPointsHelper;
	}
	
	@Override
	public void showShortToastMessage(int msgId, Object... args) {
		AccessibleToast.makeText(this, getString(msgId, args), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void showToastMessage(int msgId, Object... args) {
		AccessibleToast.makeText(this, getString(msgId, args), Toast.LENGTH_LONG).show();
	}

	@Override
	public void showToastMessage(String msg) {
		AccessibleToast.makeText(this, msg, Toast.LENGTH_LONG).show();		
	}
	
	@Override
	public SettingsAPI getSettingsAPI() {
		return settingsAPI;
	}

	@Override
	public ExternalServiceAPI getExternalServiceAPI() {
		return externalServiceAPI;
	}

	@Override
	public InternalToDoAPI getTodoAPI() {
		return internalToDoAPI;
	}

	@Override
	public InternalOsmAndAPI getInternalAPI() {
		return internalOsmAndAPI;
	}

	@Override
	public SQLiteAPI getSQLiteAPI() {
		return sqliteAPI;
	}

	@Override
	public void runInUIThread(Runnable run) {
		uiHandler.post(run);
	}

	@Override
	public void runInUIThread(Runnable run, long delay) {
		uiHandler.postDelayed(run, delay);
	}
	
	public void runMessageInUIThreadAndCancelPrevious(final int messageId, final Runnable run, long delay) {
		Message msg = Message.obtain(uiHandler, new Runnable() {
			
			@Override
			public void run() {
				if(!uiHandler.hasMessages(messageId)) {
					run.run();
				}
			}
		});
		msg.what = messageId;
		uiHandler.removeMessages(messageId);
		uiHandler.sendMessageDelayed(msg, delay);
	}
	
	@Override
	public File getAppPath(String path) {
		if(path == null) {
			path = "";
		}
		return new File(getSettings().getExternalStorageDirectory(), IndexConstants.APP_DIR + path);
	}

	@Override
	public Location getLastKnownLocation() {
		return locationProvider.getLastKnownLocation();
	}
	
	
	public void applyTheme(Context c) {
		int t = R.style.OsmandLightDarkActionBarTheme;
		if (osmandSettings.OSMAND_THEME.get() == OsmandSettings.OSMAND_DARK_THEME) {
			t = R.style.OsmandDarkTheme;
		} else if (osmandSettings.OSMAND_THEME.get() == OsmandSettings.OSMAND_LIGHT_THEME) {
			t = R.style.OsmandLightTheme;
		} else if (osmandSettings.OSMAND_THEME.get() == OsmandSettings.OSMAND_LIGHT_DARK_ACTIONBAR_THEME) {
			t = R.style.OsmandLightDarkActionBarTheme;
		}
		setLanguage(c);
		c.setTheme(t);
		if (osmandSettings.OSMAND_THEME.get() == OsmandSettings.OSMAND_LIGHT_DARK_ACTIONBAR_THEME
				&& Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar ab = null;
			if (c instanceof SherlockActivity) {
				ab = ((SherlockActivity) c).getSupportActionBar();
			} else if (c instanceof SherlockListActivity) {
				ab = ((SherlockListActivity) c).getSupportActionBar();
			} else if (c instanceof SherlockExpandableListActivity) {
				ab = ((SherlockExpandableListActivity) c).getSupportActionBar();
			}
			if (ab != null) {
				BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(R.drawable.bg_striped);
				bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
				ab.setBackgroundDrawable(bg);
			}
		}
	}
	
	public void setLanguage(Context context) {
		if (prefferedLocale != null) {
			Configuration config = context.getResources().getConfiguration();
			String lang = prefferedLocale.getLanguage();
			if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
				prefferedLocale = new Locale(lang);
				Locale.setDefault(prefferedLocale);
				config.locale = prefferedLocale;
				context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
			}
		}
	}
	
}
