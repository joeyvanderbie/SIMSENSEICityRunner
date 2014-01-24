package org.hva.cityrunner.plus.activities;

import org.hva.cityrunner.plus.activities.search.SearchActivity;
import org.hva.cityrunner.sensei.sensors.MoodActivity;

import android.app.Activity;

public class OsmandIntents {
	
	public static Class<? extends Activity> getSettingsActivity(){
		return SettingsActivity.class;
	}
	
	public static Class<MapActivity> getMapActivity(){
		return MapActivity.class;
	}
	
	public static Class<SearchActivity> getSearchActivity(){
		return SearchActivity.class;
	}
	
	public static Class<FavouritesActivity> getFavoritesActivity(){
		return FavouritesActivity.class;
	}

	public static Class<MainMenuActivity> getMainMenuActivity() {
		return MainMenuActivity.class;
	}
	
	public static Class<? extends Activity> getDownloadIndexActivity() {
		return DownloadIndexActivity.class;
	}
	
	public static Class<? extends Activity> getPluginsActivity() {
		return PluginsActivity.class;
	}
	
	public static Class<? extends Activity> getLocalIndexActivity() {
		return LocalIndexesActivity.class;
	}
	
	public static Class<? extends Activity> getRunFinishedActivity() {
		return RunFinishedActivity.class;
	}
	
	public static Class<? extends Activity> getMoodActivity() {
		return MoodActivity.class;
	}

}
