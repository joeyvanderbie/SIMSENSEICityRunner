package org.hva.cityrunner.plus;

import java.io.File;

import org.hva.cityrunner.Location;

import org.hva.cityrunner.plus.api.ExternalServiceAPI;
import org.hva.cityrunner.plus.api.InternalOsmAndAPI;
import org.hva.cityrunner.plus.api.InternalToDoAPI;
import org.hva.cityrunner.plus.api.SQLiteAPI;
import org.hva.cityrunner.plus.api.SettingsAPI;
import org.hva.cityrunner.plus.render.RendererRegistry;
import org.hva.cityrunner.plus.routing.RoutingHelper;


/*
 * In Android version ClientContext should be cast to Android.Context for backward compatibility
 */
public interface ClientContext {
	
	public String getString(int resId, Object... args);
	
	public File getAppPath(String extend);
	
	public void showShortToastMessage(int msgId, Object... args);
	
	public void showToastMessage(int msgId, Object... args);
	
	public void showToastMessage(String msg);
	
	public RendererRegistry getRendererRegistry();

	public OsmandSettings getSettings();
	
	public SettingsAPI getSettingsAPI();
	
	public ExternalServiceAPI getExternalServiceAPI();
	
	public InternalToDoAPI getTodoAPI();
	
	public InternalOsmAndAPI getInternalAPI();
	
	public SQLiteAPI getSQLiteAPI();
	
	// public RendererAPI getRendererAPI();
	
	public void runInUIThread(Runnable run);

	public void runInUIThread(Runnable run, long delay);
	
	public RoutingHelper getRoutingHelper();
	
	public Location getLastKnownLocation();

}
