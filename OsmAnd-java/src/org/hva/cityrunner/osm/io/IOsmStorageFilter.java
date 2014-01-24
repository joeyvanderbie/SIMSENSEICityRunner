package org.hva.cityrunner.osm.io;

import org.hva.cityrunner.osm.edit.Entity;
import org.hva.cityrunner.osm.edit.Entity.EntityId;

public interface IOsmStorageFilter {
	
	public boolean acceptEntityToLoad(OsmBaseStorage storage, EntityId entityId, Entity entity);

}
