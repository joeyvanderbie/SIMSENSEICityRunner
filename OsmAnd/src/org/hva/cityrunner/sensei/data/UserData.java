package org.hva.cityrunner.sensei.data;

public class UserData {
	int id;
	String name;
	String email;
	String password;
	int teamid;
	double height;
	double weight;
	int age;
	String gender;
	
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public UserData(){
		
	}
	
	public UserData(String name, String email, String password){
		this.name = name;
		this.email = email;
		this.password = password;
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
