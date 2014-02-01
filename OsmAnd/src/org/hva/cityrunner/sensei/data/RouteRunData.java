package org.hva.cityrunner.sensei.data;

public class RouteRunData {
	
	int id;
	int route_id; 
	int team_id;
	long start_datetime;
	long end_datetime;
	int number_people;
	String remarks;
	boolean headphones;
	String phone_position;
	
	public int getNumber_people() {
		return number_people;
	}
	public void setNumber_people(int number_people) {
		this.number_people = number_people;
	}
	public String getRemarks() {
		return remarks;
	}
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	public boolean isHeadphones() {
		return headphones;
	}
	public void setHeadphones(boolean headphones) {
		this.headphones = headphones;
	}
	public String getPhone_position() {
		return phone_position;
	}
	public void setPhone_position(String phone_position) {
		this.phone_position = phone_position;
	}
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
