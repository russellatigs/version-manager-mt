package gov.igs.versionmanager.model;

import java.math.BigDecimal;

public class CreateJob {

	private String name;
	private BigDecimal latitude;
	private BigDecimal longitude;

	public boolean isValid() {
		if (name != null && isLatitudeValid() && isLongitudeValid()) {
			return true;
		}
		return false;
	}

	public boolean isLatitudeValid() {
		if (latitude != null && latitude.compareTo(BigDecimal.valueOf(-90)) > -1
				&& latitude.compareTo(BigDecimal.valueOf(90)) < 1) {
			return true;
		}
		return false;
	}

	public boolean isLongitudeValid() {
		if (longitude != null && longitude.compareTo(BigDecimal.valueOf(-180)) > -1
				&& longitude.compareTo(BigDecimal.valueOf(180)) < 1) {
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
}
