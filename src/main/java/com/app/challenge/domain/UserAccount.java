package com.app.challenge.domain;

import java.util.Date;

public class UserAccount {
	private int id;
	private String userName;
	private String userEmail;
	private String deviceId;
	private String deviceType;
	private int totalChallenges;
	private int totalWins;
	private Date createdDate;
	private Date lastUpdatedDate;
	private UserToken userToken;
	private String userImage;
	private String status;
	
	public int getId() {
		return id;
	}

	public String getUserName() {
		return userName;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public int getTotalChallenges() {
		return totalChallenges;
	}

	public int getTotalWins() {
		return totalWins;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public Date getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public void setTotalChallenges(int totalChallenges) {
		this.totalChallenges = totalChallenges;
	}

	public void setTotalWins(int totalWins) {
		this.totalWins = totalWins;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public void setLastUpdatedDate(Date lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public UserToken getUserToken() {
		return userToken;
	}

	public void setUserToken(UserToken userToken) {
		this.userToken = userToken;
	}

	public String getUserImage() {
		return userImage;
	}

	public void setUserImage(String userImage) {
		this.userImage = userImage;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
