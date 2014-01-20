package net.osmand.sensei.data;

public class UserData {
	int id;
	String name;
	String email;
	String password;
	int teamid;
	
	public UserData(){
		
	}
	
	public UserData(String name, String email, String password, int teamid){
		this.name = name;
		this.email = email;
		this.password = password;
		this.teamid = teamid;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getTeamid() {
		return teamid;
	}
	public void setTeamid(int teamid) {
		this.teamid = teamid;
	}
	
	

}
