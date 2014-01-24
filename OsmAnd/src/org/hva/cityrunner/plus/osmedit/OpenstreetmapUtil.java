package org.hva.cityrunner.plus.osmedit;

import org.hva.cityrunner.data.Amenity;
import org.hva.cityrunner.osm.edit.EntityInfo;
import org.hva.cityrunner.osm.edit.Node;

public interface OpenstreetmapUtil {
	
	public EntityInfo getEntityInfo();
	
	public Node commitNodeImpl(OsmPoint.Action action, Node n, EntityInfo info, String comment, boolean closeChangeSet);
	
	public void closeChangeSet();
	
	public Node loadNode(Amenity n);
	
}
