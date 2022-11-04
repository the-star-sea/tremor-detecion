package com.example.mediapipehandtracking.responseClasses;

import com.google.gson.annotations.SerializedName;

public class ResponseClass{

	@SerializedName("id")
	private int id;

	@SerializedName("password")
	private String password;

	@SerializedName("username")
	private String username;

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setPassword(String password){
		this.password = password;
	}

	public String getPassword(){
		return password;
	}

	public void setUsername(String username){
		this.username = username;
	}

	public String getUsername(){
		return username;
	}

	@Override
 	public String toString(){
		return 
			"ResponseClass{" + 
			"password = '" + password + '\'' + 
			",username = '" + username + '\'' + 
			"}";
		}
}