package gov.igs.versionmanager.model;

public class VMResponse {

	private String status;

	public VMResponse () {
		
	}
	
	public VMResponse(String status) {
		setStatus(status);
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}