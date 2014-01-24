package org.hva.cityrunner.plus.api;

import java.io.File;
import java.util.List;

import org.hva.cityrunner.binary.BinaryMapIndexReader;
import org.hva.cityrunner.map.ITileSource;
import org.hva.cityrunner.map.TileSourceManager.TileSourceTemplate;

import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.SQLiteTileSource;

public class InternalToDoAPIImpl implements InternalToDoAPI {

	private OsmandApplication app;

	public InternalToDoAPIImpl(OsmandApplication app) {
		this.app = app;
	}

	@Override
	public BinaryMapIndexReader[] getRoutingMapFiles() {
		return app.getResourceManager().getRoutingMapFiles();
	}

	@Override
	public ITileSource newSqliteTileSource(File dir, List<TileSourceTemplate> knownTemplates) {
		return new SQLiteTileSource(app, dir, knownTemplates);
	}

}
