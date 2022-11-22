package com.example.mediapipehandtracking.responseClasses;

import com.google.gson.annotations.SerializedName;

public class ResponseSignalIdClass{

	@SerializedName("finger_tapping")
	private double fingerTapping;

	@SerializedName("hand_movement")
	private double handMovement;

	@SerializedName("signal_id")
	private int signalId;

	@SerializedName("time")
	private Long time;

	@SerializedName("pronation_supination")
	private double pronationSupination;

	public void setFingerTapping(double fingerTapping){
		this.fingerTapping = fingerTapping;
	}

	public double getFingerTapping(){
		return fingerTapping;
	}

	public void setHandMovement(int handMovement){
		this.handMovement = handMovement;
	}

	public double getHandMovement(){
		return handMovement;
	}

	public void setSignalId(int signalId){
		this.signalId = signalId;
	}

	public int getSignalId(){
		return signalId;
	}

	public void setTime(Long time){
		this.time = time;
	}

	public Long getTime(){
		return time;
	}

	public void setPronationSupination(double pronationSupination){
		this.pronationSupination = pronationSupination;
	}

	public double getPronationSupination(){
		return pronationSupination;
	}

	@Override
 	public String toString(){
		return 
			"ResponseSignalIdClass{" + 
			"finger_tapping = '" + fingerTapping + '\'' + 
			",hand_movement = '" + handMovement + '\'' + 
			",signal_id = '" + signalId + '\'' + 
			",time = '" + time + '\'' + 
			",pronation_supination = '" + pronationSupination + '\'' + 
			"}";
		}
}