package gov.igs.versionmanager.model;

public class CreateJob {
	
	private String name;
	private String provider;
	private String cid;
	
	public boolean isPopulated() {
		if (name != null && provider != null && cid != null) {
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

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}
	
	public String getProvider() {
		return this.provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}	
}