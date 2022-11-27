package com.example.mediapipehandtracking.responseClasses;

import com.google.gson.annotations.SerializedName;

public class ResponseSignalClass {

	@SerializedName("finger_tapping")
	private double fingerTapping;

	@SerializedName("hand_movement")
	private double handMovement;

	@SerializedName("time")
	private Long time;

	@SerializedName("pronation_supination")
	private double pronationSupination;

	public ResponseSignalClass(double fingerTapping, double handMovement, double pronationSupination,Long time) {
		this.fingerTapping = fingerTapping;
		this.handMovement = handMovement;
		this.pronationSupination = pronationSupination;
		this.time = time;
	}

	public void setFingerTapping(double fingerTapping){
		this.fingerTapping = fingerTapping;
	}

	public double getFingerTapping(){
		return fingerTapping;
	}

	public void setHandMovement(double handMovement){
		this.handMovement = handMovement;
	}

	public double getHandMovement(){
		return handMovement;
	}

	public void setTime(Long time){
		this.time = time;
	}

	public Long getTime(){
		return time;
	}

	public void setPronationSupination(int pronationSupination){
		this.pronationSupination = pronationSupination;
	}

	public double getPronationSupination(){
		return pronationSupination;
	}

	@Override
 	public String toString(){
		return 
			"ResponseSignal{" + 
			"finger_tapping = '" + fingerTapping + '\'' + 
			",hand_movement = '" + handMovement + '\'' + 
			",time = '" + time + '\'' + 
			",pronation_supination = '" + pronationSupination + '\'' + 
			"}";
		}
}