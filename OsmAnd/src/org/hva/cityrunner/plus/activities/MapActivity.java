package org.hva.cityrunner.plus.activities;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hva.cityrunner.Location;
import org.hva.cityrunner.StateChangedListener;
import org.hva.cityrunner.access.AccessibleActivity;
import org.hva.cityrunner.access.AccessibleToast;
import org.hva.cityrunner.access.MapAccessibilityActions;
import org.hva.cityrunner.data.LatLon;
import org.hva.cityrunner.map.MapTileDownloader.DownloadRequest;
import org.hva.cityrunner.map.MapTileDownloader.IMapDownloaderCallback;
import org.hva.cityrunner.plus.ApplicationMode;
import org.hva.cityrunner.plus.BusyIndicator;
import org.hva.cityrunner.plus.OsmAndLocationProvider.OsmAndLocationListener;
import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.OsmandPlugin;
import org.hva.cityrunner.plus.OsmandSettings;
import org.hva.cityrunner.plus.PoiFilter;
import org.hva.cityrunner.plus.R;
import org.hva.cityrunner.plus.TargetPointsHelper;
import org.hva.cityrunner.plus.Version;
import org.hva.cityrunner.plus.activities.MapActivityActions.DirectionDialogStyle;
import org.hva.cityrunner.plus.base.FailSafeFuntions;
import org.hva.cityrunner.plus.base.MapViewTrackingUtilities;
import org.hva.cityrunner.plus.render.RendererRegistry;
import org.hva.cityrunner.plus.resources.ResourceManager;
import org.hva.cityrunner.plus.routing.RouteProvider.GPXRouteParams;
import org.hva.cityrunner.plus.routing.RoutingHelper;
import org.hva.cityrunner.plus.routing.RoutingHelper.IRouteInformationListener;
import org.hva.cityrunner.plus.routing.RoutingHelper.RouteCalculationProgressCallback;
import org.hva.cityrunner.plus.views.AnimateDraggingMapThread;
import org.hva.cityrunner.plus.views.OsmandMapLayer;
import org.hva.cityrunner.plus.views.OsmandMapTileView;
import org.hva.cityrunner.render.RenderingRulesStorage;
import org.hva.cityrunner.sensei.data.LocationData;
import org.hva.cityrunner.sensei.data.RouteRunData;
import org.hva.cityrunner.sensei.db.LocationDataSource;
import org.hva.cityrunner.sensei.db.RouteRunDataSource;
import org.hva.cityrunner.sensei.sensors.AccelerometerListener;
import org.hva.cityrunner.sensei.sensors.GyroscopeListener;
import org.hva.cityrunner.util.Algorithms;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MapActivity extends AccessibleActivity implements
		IRouteInformationListener, OsmAndLocationListener {

	private static final int SHOW_POSITION_MSG_ID = 7;
	private static final int LONG_KEYPRESS_MSG_ID = 28;
	private static final int LONG_KEYPRESS_DELAY = 500;

	private static MapViewTrackingUtilities mapViewTrackingUtilities;

	/** Called when the activity is first created. */
	private OsmandMapTileView mapView;

	private MapActivityActions mapActions;
	private MapActivityLayers mapLayers;

	// Notification status
	private NotificationManager mNotificationManager;
	private int APP_NOTIFICATION_ID = 1;

	// handler to show/hide trackball position and to link map with delay
	private Handler uiHandler = new Handler();
	// App variables
	public OsmandApplication app;
	private OsmandSettings settings;

	private Dialog progressDlg = null;

	private ProgressDialog startProgressDialog;
	private List<DialogProvider> dialogProviders = new ArrayList<DialogProvider>(
			2);
	private StateChangedListener<ApplicationMode> applicationModeListener;
	private FrameLayout lockView;
	
	private int route_id = 0;
	private int run_id = 0;
	

	private Notification getNotification() {
		Intent notificationIndent = new Intent(this,
				OsmandIntents.getMapActivity());
		notificationIndent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		Notification notification = new Notification(R.drawable.icon, "", //$NON-NLS-1$
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(this, Version.getAppName(app),
				getString(R.string.go_back_to_osmand), PendingIntent
						.getActivity(this, 0, notificationIndent,
								PendingIntent.FLAG_UPDATE_CURRENT));
		return notification;
	}
	
	public void onBackPressed(){
	     // do something here and don't write super.onBackPressed()
		if(accelerometerListener != null){
			accelerometerListener.submitLastSensorData();
			sensorManager.unregisterListener(accelerometerListener);
		}
		
		if(gyroscopeListener != null){
			gyroscopeListener.submitLastSensorData();
			sensorManager.unregisterListener(gyroscopeListener);
		}
		
		
		super.onBackPressed();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		app = getMyApplication();
		settings = app.getSettings();
		app.applyTheme(this);
		super.onCreate(savedInstanceState);

		mapActions = new MapActivityActions(this);
		mapLayers = new MapActivityLayers(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Full screen is not used here
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.main);
		startProgressDialog = new ProgressDialog(this);
		startProgressDialog.setCancelable(true);
		app.checkApplicationIsBeingInitialized(this, startProgressDialog);
		parseLaunchIntentLocation();
		
		Button finish = (Button) findViewById(R.id.button_Finish_Route);
		finish.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				routeIsFinished(System.currentTimeMillis());
			}
		});

		mapView = (OsmandMapTileView) findViewById(R.id.MapView);
		mapView.setTrackBallDelegate(new OsmandMapTileView.OnTrackBallListener() {
			@Override
			public boolean onTrackBallEvent(MotionEvent e) {
				showAndHideMapPosition();
				return MapActivity.this.onTrackballEvent(e);
			}
		});
		mapView.setAccessibilityActions(new MapAccessibilityActions(this));
		if (mapViewTrackingUtilities == null) {
			mapViewTrackingUtilities = new MapViewTrackingUtilities(app);
		}
		mapViewTrackingUtilities.setMapView(mapView);

		// Do some action on close
		startProgressDialog
				.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						app.getResourceManager().getRenderer().clearCache();
						mapView.refreshMap(true);
					}
				});

		app.getResourceManager().getMapTileDownloader()
				.addDownloaderCallback(new IMapDownloaderCallback() {
					@Override
					public void tileDownloaded(DownloadRequest request) {
						if (request != null && !request.error
								&& request.fileToSave != null) {
							ResourceManager mgr = app.getResourceManager();
							mgr.tileDownloaded(request);
						}
						if (request == null || !request.error) {
							mapView.tileDownloaded(request);
						}
					}
				});
		createProgressBarForRouting();
		mapLayers.createLayers(mapView);
		// This situtation could be when navigation suddenly crashed and after
		// restarting
		// it tries to continue the last route
//		if (settings.FOLLOW_THE_ROUTE.get()
//				&& !app.getRoutingHelper().isRouteCalculated()
//				&& !app.getRoutingHelper().isRouteBeingCalculated()) {
//			FailSafeFuntions.restoreRoutingMode(this);
//		}

		if (!settings.isLastKnownMapLocation()) {
			// show first time when application ran
			org.hva.cityrunner.Location location = app.getLocationProvider()
					.getFirstTimeRunDefaultLocation();
			if (location != null) {
				mapView.setLatLon(location.getLatitude(),
						location.getLongitude());
				mapView.setZoom(14);
			}
		}
		addDialogProvider(mapActions);
		OsmandPlugin.onMapActivityCreate(this);
		if (lockView != null) {
			((FrameLayout) mapView.getParent()).addView(lockView);
		}

		showAllGPXTracks();

		// Laad alle GPXTracks zien, en wanneer gekozen direct navigatie
		// starten.
		// dit is een testje, uitbouwen zodat 1 directe GPX track via code
		// gekozen wordt en navigatie metten begint.
		// wat misschien sneller werkt is om de dialog(spinner) die weergegeven
		// wordt
		// aan te passen zodat er een grafisch keuze menu is met alle tracks
		// Maar netter is eigenlijk om dit in een aparte view te doen voordat je
		// bij de MapActivity komt.
		// mapActions.navigateUsingGPX(ApplicationMode.PEDESTRIAN);

		Bundle extras = getIntent().getExtras();
		int track;

		if (extras != null) {
			track = extras.getInt("track");
			// if 0 no track navigation is desired
			// tracks start at number 1
			route_id = track;
			if (track != 0) {
				run_id = extras.getInt("run_id");
				mapActions.navigateUsingGPX(ApplicationMode.PEDESTRIAN, track);
			}

		}

	}

	private void showAllGPXTracks() {
		mapLayers.showAllGPXTracks();
		mapView.refreshMap();
	}

	public void addLockView(FrameLayout lockView) {
		this.lockView = lockView;
	}

	private void createProgressBarForRouting() {
		FrameLayout parent = (FrameLayout) mapView.getParent();
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_HORIZONTAL | Gravity.TOP);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		params.topMargin = (int) (60 * dm.density);
		final ProgressBar pb = new ProgressBar(this, null,
				android.R.attr.progressBarStyleHorizontal);
		pb.setIndeterminate(false);
		pb.setMax(100);
		pb.setLayoutParams(params);
		pb.setVisibility(View.GONE);

		parent.addView(pb);
		app.getRoutingHelper().setProgressBar(
				new RouteCalculationProgressCallback() {

					@Override
					public void updateProgress(int progress) {
						pb.setVisibility(View.VISIBLE);
						pb.setProgress(progress);

					}

					@Override
					public void finish() {
						pb.setVisibility(View.GONE);
					}
				});
	}

	@SuppressWarnings("rawtypes")
	public Object getLastNonConfigurationInstanceByKey(String key) {
		Object k = super.getLastNonConfigurationInstance();
		if (k instanceof Map) {
			return ((Map) k).get(key);
		}
		return null;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		LinkedHashMap<String, Object> l = new LinkedHashMap<String, Object>();
		for (OsmandMapLayer ml : mapView.getLayers()) {
			ml.onRetainNonConfigurationInstance(l);
		}
		return l;
	}

	@Override
	protected void onResume() {
		super.onResume();
		cancelNotification();
		if (settings.MAP_SCREEN_ORIENTATION.get() != getRequestedOrientation()) {
			setRequestedOrientation(settings.MAP_SCREEN_ORIENTATION.get());
			// can't return from this method we are not sure if activity will be
			// recreated or not
		}


		app.getLocationProvider().addLocationListener(this);
		app.getLocationProvider().checkIfLastKnownLocationIsValid();
		// for voice navigation
		if (settings.AUDIO_STREAM_GUIDANCE.get() != null) {
			setVolumeControlStream(settings.AUDIO_STREAM_GUIDANCE.get());
		} else {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
		}

		applicationModeListener = new StateChangedListener<ApplicationMode>() {
			@Override
			public void stateChanged(ApplicationMode change) {
				updateApplicationModeSettings();
			}
		};
		settings.APPLICATION_MODE.addListener(applicationModeListener);
		updateApplicationModeSettings();

		String filterId = settings.getPoiFilterForMap();
		PoiFilter poiFilter = app.getPoiFilters().getFilterById(filterId);
		if (poiFilter == null) {
			poiFilter = new PoiFilter(null, app);
		}

		mapLayers.getPoiMapLayer().setFilter(poiFilter);

		// if destination point was changed try to recalculate route
		TargetPointsHelper targets = app.getTargetPointsHelper();
		RoutingHelper routingHelper = app.getRoutingHelper();
		if (routingHelper.isFollowingMode()
				&& (!Algorithms.objectEquals(targets.getPointToNavigate(),
						routingHelper.getFinalLocation()) || !Algorithms
						.objectEquals(targets.getIntermediatePoints(),
								routingHelper.getIntermediatePoints()))) {
			routingHelper.setFinalAndCurrentLocation(targets
					.getPointToNavigate(), targets.getIntermediatePoints(), app
					.getLocationProvider().getLastKnownLocation(),
					routingHelper.getCurrentGPXRoute());
		}
		app.getLocationProvider().resumeAllUpdates();

		if (settings != null && settings.isLastKnownMapLocation()) {
			LatLon l = settings.getLastKnownMapLocation();
			mapView.setLatLon(l.getLatitude(), l.getLongitude());
			mapView.setZoom(settings.getLastKnownMapZoom());
		}

		settings.MAP_ACTIVITY_ENABLED.set(true);
		checkExternalStorage();
		showAndHideMapPosition();

		LatLon cur = new LatLon(mapView.getLatitude(), mapView.getLongitude());
		LatLon latLonToShow = settings.getAndClearMapLocationToShow();
		String mapLabelToShow = settings.getAndClearMapLabelToShow();
		Object toShow = settings.getAndClearObjectToShow();
		if (settings.isRouteToPointNavigateAndClear()) {
			// always enable and follow and let calculate it (GPS is not
			// accessible in garage)
			Location loc = new Location("map");
			loc.setLatitude(mapView.getLatitude());
			loc.setLongitude(mapView.getLongitude());
			mapActions.getDirections(loc, null, DirectionDialogStyle.create());
		}
		if (mapLabelToShow != null && latLonToShow != null) {
			mapLayers.getContextMenuLayer().setSelectedObject(toShow);
			mapLayers.getContextMenuLayer().setLocation(latLonToShow,
					mapLabelToShow);
		}
		if (latLonToShow != null && !latLonToShow.equals(cur)) {
			mapView.getAnimatedDraggingThread().startMoving(
					latLonToShow.getLatitude(), latLonToShow.getLongitude(),
					settings.getMapZoomToShow(), true);
		}
		if (latLonToShow != null) {
			// remember if map should come back to isMapLinkedToLocation=true
			mapViewTrackingUtilities.setMapLinkedToLocation(false);
		}

		View progress = mapLayers.getMapInfoLayer().getProgressBar();
		if (progress != null) {
			app.getResourceManager().setBusyIndicator(
					new BusyIndicator(this, progress));
		}

		OsmandPlugin.onMapActivityResume(this);
		mapView.refreshMap(true);
	}

	public OsmandApplication getMyApplication() {
		return ((OsmandApplication) getApplication());
	}

	public void addDialogProvider(DialogProvider dp) {
		dialogProviders.add(dp);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		for (DialogProvider dp : dialogProviders) {
			dialog = dp.onCreateDialog(id);
			if (dialog != null) {
				return dialog;
			}
		}
		if (id == OsmandApplication.PROGRESS_DIALOG) {
			return startProgressDialog;
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		for (DialogProvider dp : dialogProviders) {
			dp.onPrepareDialog(id, dialog);
		}
	}

	public void changeZoom(float newZoom) {
		newZoom = Math.round(newZoom * OsmandMapTileView.ZOOM_DELTA)
				* OsmandMapTileView.ZOOM_DELTA_1;
		boolean changeLocation = settings.AUTO_ZOOM_MAP.get();
		mapView.getAnimatedDraggingThread().startZooming(newZoom,
				changeLocation);
		if (app.getInternalAPI().accessibilityEnabled())
			AccessibleToast
					.makeText(
							this,
							getString(R.string.zoomIs)
									+ " " + String.valueOf(newZoom), Toast.LENGTH_SHORT).show(); //$NON-NLS-1$
		showAndHideMapPosition();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
//				&& app.getInternalAPI().accessibilityEnabled()) {
//			if (!uiHandler.hasMessages(LONG_KEYPRESS_MSG_ID)) {
//				Message msg = Message.obtain(uiHandler, new Runnable() {
//					@Override
//					public void run() {
//						app.getLocationProvider().emitNavigationHint();
//					}
//				});
//				msg.what = LONG_KEYPRESS_MSG_ID;
//				uiHandler.sendMessageDelayed(msg, LONG_KEYPRESS_DELAY);
//			}
//			return true;
//		} else if (keyCode == KeyEvent.KEYCODE_MENU
//				&& event.getRepeatCount() == 0) {
//			//mapActions.openOptionsMenuAsList();
//			return true;
//		} else if (keyCode == KeyEvent.KEYCODE_SEARCH
//				&& event.getRepeatCount() == 0) {
//			Intent newIntent = new Intent(MapActivity.this,
//					OsmandIntents.getSearchActivity());
//			// causes wrong position caching:
//			// newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//			LatLon loc = getMapLocation();
//			newIntent.putExtra(SearchActivity.SEARCH_LAT, loc.getLatitude());
//			newIntent.putExtra(SearchActivity.SEARCH_LON, loc.getLongitude());
//			startActivity(newIntent);
//			newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			return true;
//		} else if (!app.getRoutingHelper().isFollowingMode()
//				&& OsmandPlugin.getEnabledPlugin(AccessibilityPlugin.class) != null) {
//			// Find more appropriate plugin for it?
//			if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
//					&& event.getRepeatCount() == 0) {
//				if (mapView.isZooming()) {
//					changeZoom(mapView.getZoom() + 2);
//				} else {
//					changeZoom(mapView.getZoom() + 1);
//				}
//				return true;
//			} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
//					&& event.getRepeatCount() == 0) {
//				changeZoom(mapView.getZoom() - 1);
//				return true;
//			}
//		}
		return super.onKeyDown(keyCode, event);
	}

	public void setMapLocation(double lat, double lon) {
		mapView.setLatLon(lat, lon);
		mapViewTrackingUtilities.locationChanged(lat, lon, this);
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_MOVE
				&& settings.USE_TRACKBALL_FOR_MOVEMENTS.get()) {
			float x = event.getX();
			float y = event.getY();
			LatLon l = mapView.getLatLonFromScreenPoint(
					mapView.getCenterPointX() + x * 15,
					mapView.getCenterPointY() + y * 15);
			setMapLocation(l.getLatitude(), l.getLongitude());
			return true;
		}
		return super.onTrackballEvent(event);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	protected void setProgressDlg(Dialog progressDlg) {
		this.progressDlg = progressDlg;
	}

	protected Dialog getProgressDlg() {
		return progressDlg;
	}

	@Override
	protected void onStop() {
		if (app.getRoutingHelper().isFollowingMode()) {
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			if (mNotificationManager != null) {
				mNotificationManager.notify(APP_NOTIFICATION_ID,
						getNotification());
			}
		}
		if (progressDlg != null) {
			progressDlg.dismiss();
			progressDlg = null;
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if(accelerometerListener != null){
			accelerometerListener.submitLastSensorData();
			sensorManager.unregisterListener(accelerometerListener);
		}
		
		if(gyroscopeListener != null){
			gyroscopeListener.submitLastSensorData();
			sensorManager.unregisterListener(gyroscopeListener);
		}
		

		app.getLocationProvider().removeLocationListener(this);
		
		FailSafeFuntions.quitRouteRestoreDialog();
		OsmandPlugin.onMapActivityDestroy(this);
		mapViewTrackingUtilities.setMapView(null);
		cancelNotification();
		app.getResourceManager().getMapTileDownloader()
				.removeDownloaderCallback(mapView);
	}

	private void cancelNotification() {
		if (mNotificationManager == null) {
			mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		}
		if (mNotificationManager != null) {
			mNotificationManager.cancel(APP_NOTIFICATION_ID);
		}
	}

	
	 SensorManager sensorManager;
	 Sensor accelerometer;
	 AccelerometerListener accelerometerListener;
	 GyroscopeListener gyroscopeListener;
	    
	public void followRoute(ApplicationMode appMode, LatLon finalLocation,
			List<LatLon> intermediatePoints,
			org.hva.cityrunner.Location currentLocation, GPXRouteParams gpxRoute) {
		getMapViewTrackingUtilities().backToLocationImpl();
		RoutingHelper routingHelper = app.getRoutingHelper();
		routingHelper.addListener(this);
		
		app.getLocationProvider().addLocationListener(this);
		
		settings.APPLICATION_MODE.set(appMode);
		settings.FOLLOW_THE_ROUTE.set(true);
		if (gpxRoute == null) {
			settings.FOLLOW_THE_GPX_ROUTE.set(null);
		}
		routingHelper.setFollowingMode(true);
		routingHelper.setFinalAndCurrentLocation(finalLocation,
				intermediatePoints, currentLocation, gpxRoute);

		
		 sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	     accelerometer = sensorManager
	                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
	     accelerometerListener = new AccelerometerListener(this);
	        sensorManager.registerListener(accelerometerListener, accelerometer,
	                SensorManager.SENSOR_DELAY_FASTEST);
	        accelerometerListener.startRecording(run_id);
	        
		     Sensor gyroscope = sensorManager
		                .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		     gyroscopeListener = new GyroscopeListener(this);
		        sensorManager.registerListener(gyroscopeListener, gyroscope,
		                SensorManager.SENSOR_DELAY_FASTEST);
		        gyroscopeListener.startRecording(run_id);
		
		app.showDialogInitializingCommandPlayer(MapActivity.this);
	}

	public LatLon getMapLocation() {
		return new LatLon(mapView.getLatitude(), mapView.getLongitude());
	}

	// Duplicate methods to OsmAndApplication
	public LatLon getPointToNavigate() {
		return app.getTargetPointsHelper().getPointToNavigate();
	}

	public RoutingHelper getRoutingHelper() {
		return app.getRoutingHelper();
	}
	
	public void showToast(final int resId, final Object... formatArgs) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				CharSequence msg = getString(resId, formatArgs);
				Toast.makeText(MapActivity.this, msg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
//		if(accelerometerListener != null){
//			accelerometerListener.submitLastSensorData();
//			sensorManager.unregisterListener(accelerometerListener);
//		}
//		
//		if(gyroscopeListener != null){
//			gyroscopeListener.submitLastSensorData();
//			sensorManager.unregisterListener(gyroscopeListener);
//		}
//
//		app.getLocationProvider().removeLocationListener(this);
//		app.getLocationProvider().pauseAllUpdates();
//		app.getDaynightHelper().stopSensorIfNeeded();
//		settings.APPLICATION_MODE.removeListener(applicationModeListener);

		settings.setLastKnownMapLocation((float) mapView.getLatitude(),
				(float) mapView.getLongitude());
		AnimateDraggingMapThread animatedThread = mapView
				.getAnimatedDraggingThread();
		if (animatedThread.isAnimating() && animatedThread.getTargetZoom() != 0) {
			settings.setMapLocationToShow(animatedThread.getTargetLatitude(),
					animatedThread.getTargetLongitude(),
					(int) animatedThread.getTargetZoom());
		}

		settings.setLastKnownMapZoom(mapView.getZoom());
		settings.MAP_ACTIVITY_ENABLED.set(false);
		app.getResourceManager().interruptRendering();
		app.getResourceManager().setBusyIndicator(null);
		OsmandPlugin.onMapActivityPause(this);
	}
	
	

	public void updateApplicationModeSettings() {
		// update vector renderer
		RendererRegistry registry = app.getRendererRegistry();
		RenderingRulesStorage newRenderer = registry
				.getRenderer(settings.RENDERER.get());
		if (newRenderer == null) {
			newRenderer = registry.defaultRender();
		}
		if (registry.getCurrentSelectedRenderer() != newRenderer) {
			registry.setCurrentSelectedRender(newRenderer);
			app.getResourceManager().getRenderer().clearCache();
		}
		mapViewTrackingUtilities.updateSettings();
		app.getRoutingHelper().setAppMode(settings.getApplicationMode());
		if (mapLayers.getMapInfoLayer() != null) {
			mapLayers.getMapInfoLayer().recreateControls();
		}
		mapLayers.updateLayers(mapView);
		app.getDaynightHelper().startSensorIfNeeded(
				new StateChangedListener<Boolean>() {

					@Override
					public void stateChanged(Boolean change) {
						getMapView().refreshMap(true);
					}
				});
		getMapView().refreshMap(true);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (!app.getInternalAPI().accessibilityEnabled()) {
				mapActions.contextMenuPoint(mapView.getLatitude(),
						mapView.getLongitude());
			} else if (uiHandler.hasMessages(LONG_KEYPRESS_MSG_ID)) {
				uiHandler.removeMessages(LONG_KEYPRESS_MSG_ID);
				mapActions.contextMenuPoint(mapView.getLatitude(),
						mapView.getLongitude());
			}
			return true;
		} else if (settings.ZOOM_BY_TRACKBALL.get()) {
			// Parrot device has only dpad left and right
			if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
				changeZoom(mapView.getZoom() - 1);
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
				changeZoom(mapView.getZoom() + 1);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN
				|| keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			int dx = keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ? 15
					: (keyCode == KeyEvent.KEYCODE_DPAD_LEFT ? -15 : 0);
			int dy = keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? 15
					: (keyCode == KeyEvent.KEYCODE_DPAD_UP ? -15 : 0);
			LatLon l = mapView.getLatLonFromScreenPoint(
					mapView.getCenterPointX() + dx, mapView.getCenterPointY()
							+ dy);
			setMapLocation(l.getLatitude(), l.getLongitude());
			return true;
		} else if (OsmandPlugin.onMapActivityKeyUp(this, keyCode)) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void checkExternalStorage() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// ok
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			AccessibleToast.makeText(this, R.string.sd_mounted_ro,
					Toast.LENGTH_LONG).show();
		} else {
			AccessibleToast.makeText(this, R.string.sd_unmounted,
					Toast.LENGTH_LONG).show();
		}
	}

	public void showAndHideMapPosition() {
		mapView.setShowMapPosition(true);
		app.runMessageInUIThreadAndCancelPrevious(SHOW_POSITION_MSG_ID,
				new Runnable() {
					@Override
					public void run() {
						if (mapView.isShowMapPosition()) {
							mapView.setShowMapPosition(false);
							mapView.refreshMap();
						}
					}
				}, 2500);
	}

	public OsmandMapTileView getMapView() {
		return mapView;
	}

	public MapViewTrackingUtilities getMapViewTrackingUtilities() {
		return mapViewTrackingUtilities;
	}

	protected void parseLaunchIntentLocation() {
		Intent intent = getIntent();
		if (intent != null && intent.getData() != null) {
			Uri data = intent.getData();
			if ("http".equalsIgnoreCase(data.getScheme())
					&& "download.osmand.net".equals(data.getHost())
					&& "/go".equals(data.getPath())) {
				String lat = data.getQueryParameter("lat");
				String lon = data.getQueryParameter("lon");
				if (lat != null && lon != null) {
					try {
						double lt = Double.parseDouble(lat);
						double ln = Double.parseDouble(lon);
						String zoom = data.getQueryParameter("z");
						int z = settings.getLastKnownMapZoom();
						if (zoom != null) {
							z = Integer.parseInt(zoom);
						}
						settings.setMapLocationToShow(lt, ln, z,
								getString(R.string.shared_location));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
	}

	public MapActivityActions getMapActions() {
		return mapActions;
	}

	public MapActivityLayers getMapLayers() {
		return mapLayers;
	}

	public static void launchMapActivityMoveToTop(Context activity) {
		Intent newIntent = new Intent(activity, OsmandIntents.getMapActivity());
		newIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		activity.startActivity(newIntent);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		OsmandPlugin.onMapActivityResult(requestCode, resultCode, data);
	}

	public void refreshMap() {
		getMapView().refreshMap();
	}

	@Override
	public void newRouteIsCalculated(boolean newRoute) {
		// TODO Auto-generated method stub

	}

	@Override
	public void routeWasCancelled() {
		// eerst alle routes cancellen voor je dit doet.

		// RoutingHelper routingHelper = app.getRoutingHelper();
		// routingHelper.removeListener(this);
		//
		// NavUtils.navigateUpFromSameTask(this);
	}

	@Override
	public void routeIsFinished(long finishTimestamp) {

//		mapActions.stopNavigationAction(mapView);
//		RoutingHelper routingHelper = app.getRoutingHelper();
//		routingHelper.setFinalAndCurrentLocation(null, new ArrayList<LatLon>(), mapActions.getLastKnownLocation(),
//				routingHelper.getCurrentGPXRoute());
//		settings.APPLICATION_MODE.set(settings.DEFAULT_APPLICATION_MODE.get());
		
		app.getLocationProvider().removeLocationListener(this);
		
		
		RouteRunData rrd = app.currentRouteRun;
		accelerometerListener.submitLastSensorData();
		sensorManager.unregisterListener(accelerometerListener);
		
		if(gyroscopeListener != null){
			gyroscopeListener.submitLastSensorData();
			sensorManager.unregisterListener(gyroscopeListener);
		}
		
		
		if (rrd != null) {
			rrd.setEnd_datetime(finishTimestamp);

			RouteRunDataSource rrds = app.getRouteRunDataSource();
			rrds.open();
			rrds.update(rrd);
			rrds.close();
			
			app.currentRouteRun = null;

			final Intent intentSettings = new Intent(this,
					OsmandIntents.getMoodActivity());
			intentSettings.putExtra("track",route_id);
			intentSettings.putExtra("run_id",rrd.getId());
			intentSettings.putExtra("nextActivity", "finished");
			intentSettings.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);//setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			this.startActivity(intentSettings);
		} else {
			NavUtils.navigateUpFromSameTask(this);
		}

	}
	
	
	public void addCurrentLocationToDB(Location currentLocation){
		LocationDataSource lds = app.getLocationDataSource();
		lds.open();
		lds.add(new LocationData(currentLocation, run_id));
		lds.close();
	}


	@Override
	public void updateLocation(Location location) {
		if(location != null){
			addCurrentLocationToDB(location);
		}
	}
	

}
