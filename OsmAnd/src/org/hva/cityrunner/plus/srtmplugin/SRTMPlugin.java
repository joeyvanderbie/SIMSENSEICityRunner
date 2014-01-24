package org.hva.cityrunner.plus.srtmplugin;

import org.hva.cityrunner.plus.ApplicationMode;
import org.hva.cityrunner.plus.ContextMenuAdapter;
import org.hva.cityrunner.plus.OsmandApplication;
import org.hva.cityrunner.plus.OsmandPlugin;
import org.hva.cityrunner.plus.OsmandSettings;
import org.hva.cityrunner.plus.ContextMenuAdapter.OnContextMenuClick;
import org.hva.cityrunner.plus.OsmandSettings.CommonPreference;
import org.hva.cityrunner.plus.activities.MapActivity;
import org.hva.cityrunner.plus.views.OsmandMapTileView;
import org.hva.cityrunner.plus.R;


import android.content.DialogInterface;

public class SRTMPlugin extends OsmandPlugin {

	public static final String ID = "osmand.srtm";
	private OsmandApplication app;
	private boolean paid;
	private HillshadeLayer hillshadeLayer;
	private CommonPreference<Boolean> HILLSHADE;
	
	@Override
	public String getId() {
		return ID;
	}

	public SRTMPlugin(OsmandApplication app, boolean paid) {
		this.app = app;
		this.paid = paid;
		OsmandSettings settings = app.getSettings();
		CommonPreference<String> pref = settings.getCustomRenderProperty("contourLines");
		if(pref.get().equals("")) {
			for(ApplicationMode m : ApplicationMode.values()) {
				if(pref.getModeValue(m).equals("")) {
					pref.setModeValue(m, "13");
				}
			}
		}

	}
	
	public boolean isPaid() {
		return paid;
	}

	@Override
	public String getDescription() {
		return app.getString(R.string.srtm_plugin_description);
	}

	@Override
	public String getName() {
		return app.getString(R.string.srtm_plugin_name);
	}

	@Override
	public boolean init(final OsmandApplication app) {
		HILLSHADE = app.getSettings().registerBooleanPreference("hillshade_layer", true);
		return true;
	}

	@Override
	public void registerLayers(MapActivity activity) {
		if (hillshadeLayer != null) {
			activity.getMapView().removeLayer(hillshadeLayer);
		}
		if (HILLSHADE.get()) {
			hillshadeLayer = new HillshadeLayer(activity, this);
			activity.getMapView().addLayer(hillshadeLayer, 0.6f);
		}
	}

	public boolean isHillShadeLayerEnabled() {
		return HILLSHADE.get();
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (HILLSHADE.get()) {
			if (hillshadeLayer == null) {
				registerLayers(activity);
			}
		} else {
			if (hillshadeLayer != null) {
				mapView.removeLayer(hillshadeLayer);
				hillshadeLayer = null;
				activity.refreshMap();
			}
		}
	}
	
	@Override
	public void registerLayerContextMenuActions(final OsmandMapTileView mapView, ContextMenuAdapter adapter, final MapActivity mapActivity) {
		OnContextMenuClick listener = new OnContextMenuClick() {
			@Override
			public void onContextMenuClick(int itemId, int pos, boolean isChecked, DialogInterface dialog) {
				if (itemId == R.string.layer_hillshade) {
					HILLSHADE.set(!HILLSHADE.get());
					updateLayers(mapView, mapActivity);
				}
			}
		};
		adapter.item(R.string.layer_hillshade).selected(HILLSHADE.get()? 1 : 0)
			.icons( R.drawable.ic_action_hillshade_dark, R.drawable.ic_action_hillshade_light).listen(listener).position(9).reg();
	}
	
	@Override
	public void disable(OsmandApplication app) {
	}

}
