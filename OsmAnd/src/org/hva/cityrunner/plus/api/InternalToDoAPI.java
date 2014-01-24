package org.hva.cityrunner.plus.api;

import java.io.File;
import java.util.List;

import org.hva.cityrunner.binary.BinaryMapIndexReader;
import org.hva.cityrunner.map.ITileSource;
import org.hva.cityrunner.map.TileSourceManager.TileSourceTemplate;

public interface InternalToDoAPI {

	public BinaryMapIndexReader[] getRoutingMapFiles();
	
	public ITileSource newSqliteTileSource(File dir, List<TileSourceTemplate> knownTemplates);


}
