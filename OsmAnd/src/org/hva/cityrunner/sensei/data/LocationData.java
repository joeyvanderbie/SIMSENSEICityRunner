package org.hva.cityrunner.sensei.data;

import org.hva.cityrunner.Location;

public class LocationData extends Location{
	private int run_id;
	private int id;
	
	public LocationData(){
		super("locationData");
	}
	
	public LocationData(Location l, int run_id) {
		super(l);
		this.run_id = run_id;
	}
	
	public LocationData(Location l, int run_id, int id) {
		super(l);
		this.run_id = run_id;
		this.id = id;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRun_id() {
		return run_id;
	}

	public void setRun_id(int run_id) {
		this.run_id = run_id;
	}

}
