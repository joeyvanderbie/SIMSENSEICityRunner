package org.hva.cityrunner.map;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.hva.cityrunner.PlatformUtilCityRunner;
import org.hva.cityrunner.map.OsmandRegionInfo.OsmAndRegionInfo;

import org.apache.commons.logging.Log;

public class RegionRegistry {
	public static final String fileName = "countries.reginfo";
	private static final Log log = PlatformUtilCityRunner.getLog(RegionRegistry.class);
	private static RegionRegistry r = null;
	
	private List<RegionCountry> countries = new ArrayList<RegionCountry>();
	
	public static RegionRegistry getRegionRegistry(){
		if(r == null) {
			try {
				long t = -System.currentTimeMillis();
				r = new RegionRegistry();
				InputStream in = RegionRegistry.class.getResourceAsStream(RegionRegistry.fileName);
				OsmAndRegionInfo regInfo = OsmAndRegionInfo.newBuilder().mergeFrom(in).build();
				for(int j = 0; j < regInfo.getRegionInfo().getRegionsCount(); j++) {
					r.countries.add(RegionCountry.construct(regInfo.getRegionInfo().getRegions(j)));
				}
				t += System.currentTimeMillis();
				log.info("Initialize regions from file" + t + " ms");
			} catch (IOException e) {
				log.error("IO exception reading regions", e);
			}
		}
		return r;
	}
	
	
	public List<RegionCountry> getCountries() {
		return countries;
	}
	
}
