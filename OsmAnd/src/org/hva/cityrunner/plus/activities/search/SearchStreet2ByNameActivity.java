package org.hva.cityrunner.plus.activities.search;

import java.util.Comparator;
import java.util.List;

import org.hva.cityrunner.data.City;
import org.hva.cityrunner.data.MapObject.MapObjectComparator;
import org.hva.cityrunner.data.Street;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.resources.RegionAddressRepository;
import org.hva.cityrunner.plus.R;


import android.os.AsyncTask;
import android.view.View;

public class SearchStreet2ByNameActivity extends SearchByNameAbstractActivity<Street> {
	private RegionAddressRepository region;
	private City cityOrPostcode;
	private Street street1;
	
	@Override
	protected Comparator<? super Street> createComparator() {
		return new MapObjectComparator(getMyApplication().getSettings().usingEnglishNames());
	}
	
	@Override
	public AsyncTask<Object, ?, ?> getInitializeTask() {
		return new AsyncTask<Object, Void, List<Street>>(){
			@Override
			protected void onPostExecute(List<Street> result) {
				setLabelText(R.string.incremental_search_street);
				progress.setVisibility(View.INVISIBLE);
				finishInitializing(result);
			}
			
			@Override
			protected void onPreExecute() {
				setLabelText(R.string.loading_streets);
				progress.setVisibility(View.VISIBLE);
			}
			@Override
			protected List<Street> doInBackground(Object... params) {
				region = ((OsmandApplication)getApplication()).getResourceManager().getRegionRepository(settings.getLastSearchedRegion());
				if(region != null){
					cityOrPostcode = region.getCityById(settings.getLastSearchedCity(), settings.getLastSearchedCityName());
					if(cityOrPostcode != null){
						street1 = region.getStreetByName(cityOrPostcode, (settings.getLastSearchedStreet()));
					}
					if(cityOrPostcode != null && street1 != null){
						return region.getStreetsIntersectStreets(street1);
					}
				}
				return null;
			}
		};
	}
	
	
	
	@Override
	public String getText(Street obj) {
		return obj.getName(region.useEnglishNames());
	}
	
	@Override
	public void itemSelected(Street obj) {
		settings.setLastSearchedIntersectedStreet(obj.getName(region.useEnglishNames()), obj.getLocation());
		finish();
	}
}
