package org.hva.cityrunner.sensei.data;

public class QueueData {
	
	int id;
	int run_id; 
	int submitted;
	int accelleft;
	int gyroleft;
	int gpsleft;
	int emotie;
	int finished;
	
	
	
	public int getFinished() {
		return finished;
	}
	public void setFinished(int finished) {
		this.finished = finished;
	}
	public int getAccelleft() {
		return accelleft;
	}
	public void setAccelleft(int accelleft) {
		this.accelleft = accelleft;
	}
	public int getGyroleft() {
		return gyroleft;
	}
	public void setGyroleft(int gyroleft) {
		this.gyroleft = gyroleft;
	}
	public int getGpsleft() {
		return gpsleft;
	}
	public void setGpsleft(int gpsleft) {
		this.gpsleft = gpsleft;
	}
	public int getEmotie() {
		return emotie;
	}
	public void setEmotie(int mood) {
		this.emotie = mood;
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
	public int getSubmitted() {
		return submitted;
	}
	public void setSubmitted(int submitted) {
		this.submitted = submitted;
	}
	
	
	
}
