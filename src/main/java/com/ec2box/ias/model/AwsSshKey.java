package com.ec2box.ias.model;

public class AwsSshKey{

	private String keyName;
	private String sshKey;
	private String region;

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getSshKey() {
		return sshKey;
	}

	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}
	
	@Override
	public String toString(){
		return "Keyname: "+ this.keyName;
	}

}
