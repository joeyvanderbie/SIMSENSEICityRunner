package org.hva.cityrunner.sensei.data;

import org.hva.createit.digitallife.sam.Affect;
import org.hva.createit.digitallife.sam.AffectDomain;

public class AffectData extends Affect{
	int runstate;
	int run_id;
	
	public AffectData(){
		
	}
	
	public AffectData(AffectDomain pleasure, AffectDomain arousal, AffectDomain dominance, long datetime, int runstate, int run_id){
		super(pleasure, arousal, dominance, datetime);
		this.runstate  = runstate;
		this.run_id = run_id;
	}

	public int getRunstate() {
		return runstate;
	}

	public void setRunstate(int runstate) {
		this.runstate = runstate;
	}

	public int getRun_id() {
		return run_id;
	}

	public void setRun_id(int run_id) {
		this.run_id = run_id;
	}
	
	public double[] getSimpleAffect(){
		double[] pad = new double[3];
		pad[0] = this.getPleasure().getDomain_value();
		pad[1] = this.getArousal().getDomain_value();
		pad[2] = this.getDominance().getDomain_value();
		return pad;
	}
}
