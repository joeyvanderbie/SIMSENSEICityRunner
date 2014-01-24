package org.hva.cityrunner.plus.resources;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;

import org.hva.cityrunner.ResultMatcher;
import org.hva.cityrunner.data.Building;
import org.hva.cityrunner.data.City;
import org.hva.cityrunner.data.LatLon;
import org.hva.cityrunner.data.MapObject;
import org.hva.cityrunner.data.Street;
import org.hva.cityrunner.util.MapUtils;


public interface RegionAddressRepository {
	
	public String getName();
	
	public LatLon getEstimatedRegionCenter();
	
	// is called on low memory
	public void clearCache();
	
	// called to close resources
	public void close();
	
	public boolean useEnglishNames();
	
	public void setUseEnglishNames(boolean useEnglishNames);
	

	
	public void preloadCities(ResultMatcher<City> resultMatcher);
	
	public void preloadBuildings(Street street, ResultMatcher<Building> resultMatcher);
	
	public void preloadStreets(City o, ResultMatcher<Street> resultMatcher);
	
	
	public List<City> getLoadedCities();
	
	// Returns city or postcode (if id < 0)
	public City getCityById(long id, String name);
	
	public Street getStreetByName(City cityOrPostcode, String name);
	
	public Building getBuildingByName(Street street, String name);
	
	public List<Street> getStreetsIntersectStreets(Street st);
	
	void addCityToPreloadedList(City city);
	
	public List<City> fillWithSuggestedCities(String name, ResultMatcher<City> resultMatcher, boolean searchVillagesMode, LatLon currentLocation);
	
	public List<MapObject> searchMapObjectsByName(String name, ResultMatcher<MapObject> resultMatcher);
	
	
	
	public static class MapObjectNameDistanceComparator implements Comparator<MapObject> {
		
		private final boolean useEnName;
		private Collator collator = Collator.getInstance();
		private final LatLon location;

		public MapObjectNameDistanceComparator(boolean useEnName, LatLon location){
			this.useEnName = useEnName;
			this.location = location;
		}

		@Override
		public int compare(MapObject object1, MapObject object2) {
			if(object1 == null || object2 == null){
				return object2 == object1 ? 0 : (object1 == null ? -1 : 1); 
			} else {
				int c = collator.compare(object1.getName(useEnName), object2.getName(useEnName));
				if(c == 0 && location != null){
					LatLon l1 = object1.getLocation();
					LatLon l2 = object2.getLocation();
					if(l1 == null || l2 == null){
						return l2 == l1 ? 0 : (l1 == null ? -1 : 1);
					}
					return Double.compare(MapUtils.getDistance(location, l1), MapUtils.getDistance(location, l2));
				}
				return c;
			}
		}
	}

}
