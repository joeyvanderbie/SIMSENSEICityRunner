package org.hva.cityrunner.plus.routing;

import java.util.List;

import org.hva.cityrunner.Location;
import org.hva.cityrunner.data.LatLon;
import org.hva.cityrunner.router.RouteCalculationProgress;

import org.hva.cityrunner.plus.ApplicationMode;
import org.hva.cityrunner.plus.ClientContext;
import org.hva.cityrunner.plus.routing.RouteProvider.GPXRouteParams;
import org.hva.cityrunner.plus.routing.RouteProvider.RouteService;

public class RouteCalculationParams {

	public Location start;
	public LatLon end;
	public List<LatLon> intermediates;
	
	public ClientContext ctx;
	public ApplicationMode mode;
	public RouteService type;
	public GPXRouteParams gpxRoute;
	public RouteCalculationResult previousToRecalculate;
	public boolean fast;
	public boolean optimal;
	public boolean leftSide;
	public RouteCalculationProgress calculationProgress;
	public boolean preciseRouting;
}
