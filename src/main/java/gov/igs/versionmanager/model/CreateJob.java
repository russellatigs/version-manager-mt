package gov.igs.versionmanager.model;

import java.math.BigDecimal;

public class CreateJob {
	
	private String name;
	private String provider;
	private BigDecimal latitude;
	private BigDecimal longitude;

	public boolean isPopulated() {
		if (name != null && provider != null && latitude != null && longitude != null) {
			return true;
		}
		return false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}
	
	public String getProvider() {
		return this.provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}	
}