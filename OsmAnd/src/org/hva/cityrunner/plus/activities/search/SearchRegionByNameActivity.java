package org.hva.cityrunner.plus.activities.search;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import org.hva.cityrunner.data.LatLon;

import org.hva.cityrunner.access.AccessibleToast;
import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.resources.RegionAddressRepository;
import org.hva.cityrunner.plus.R;


import android.os.Bundle;
import android.widget.Toast;


public class SearchRegionByNameActivity extends SearchByNameAbstractActivity<RegionAddressRepository> {
	
	@Override
	protected Comparator<? super RegionAddressRepository> createComparator() {
		return new Comparator<RegionAddressRepository>() {
			Collator col = Collator.getInstance();
			@Override
			public int compare(RegionAddressRepository lhs,
					RegionAddressRepository rhs) {
				return col.compare(lhs.getName(), rhs.getName());
			}
		};
	}
	
	@Override
	protected LatLon getLocation(RegionAddressRepository item) {
		return item.getEstimatedRegionCenter();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLabelText(R.string.choose_available_region);
		if(((OsmandApplication)getApplication()).getResourceManager().getAddressRepositories().isEmpty()){
			AccessibleToast.makeText(this, R.string.none_region_found, Toast.LENGTH_LONG).show();
		}
		initialListToFilter = new ArrayList<RegionAddressRepository>(((OsmandApplication)getApplication()).getResourceManager().getAddressRepositories());
		NamesAdapter namesAdapter = new NamesAdapter(new ArrayList<RegionAddressRepository>(initialListToFilter),createComparator()); //$NON-NLS-1$
		setListAdapter(namesAdapter);
	}
	
	
	@Override
	public String getText(RegionAddressRepository obj) {
		return obj.getName().replace('_', ' ');
	}

	@Override
	public void itemSelected(RegionAddressRepository obj) {
		((OsmandApplication) getApplication()).getSettings().setLastSearchedRegion(obj.getName(), obj.getEstimatedRegionCenter());
		finish();
	}
	
}
