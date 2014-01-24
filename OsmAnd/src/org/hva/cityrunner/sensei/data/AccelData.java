package org.hva.cityrunner.sensei.data;

public class AccelData {
	private long timestamp;
	private double x;
	private double y;
	private double z;
	private long id;
	private long run_id;
	
	
	public AccelData(long timestamp, double x, double y, double z, long run_id) {
		this.timestamp = timestamp;
		this.x = x;
		this.y = y;
		this.z = z;
		this.run_id = run_id;
	}
	
	public long getRun_id() {
		return run_id;
	}
	public void setRun_id(long run_id) {
		this.run_id = run_id;
	}
	public AccelData() {
		// TODO Auto-generated constructor stub
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}
	
	public String toString()
	{
		return "t="+timestamp+", x="+x+", y="+y+", z="+z;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	

}
