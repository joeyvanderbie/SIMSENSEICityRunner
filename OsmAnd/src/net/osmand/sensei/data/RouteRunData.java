package net.osmand.sensei.data;

public class RouteRunData {
	
	int id;
	int route_id; 
	int team_id;
	long start_datetime;
	long end_datetime;
	
	public int getRoute_id() {
		return route_id;
	}
	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}
	
	public int getTeam_id() {
		return team_id;
	}
	public void setTeam_id(int team_id) {
		this.team_id = team_id;
	}
	public int getId() {
		return id;
	}
	public void setId(int run_id) {
		this.id = run_id;
	}
	public long getStart_datetime() {
		return start_datetime;
	}
	public void setStart_datetime(long start_datetime) {
		this.start_datetime = start_datetime;
	}
	public long getEnd_datetime() {
		return end_datetime;
	}
	public void setEnd_datetime(long end_datetime) {
		this.end_datetime = end_datetime;
	}
	
}
