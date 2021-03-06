package gov.igs.versionmanager.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * The persistent class for the JOB database table.
 * 
 */
@Entity
@Table(name = "JOB")
@NamedQuery(name = "Job.findAll", query = "SELECT j FROM Job j")
public class Job extends VMResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	public class Status {
		public static final String NEW = "NEW";
		public static final String EXPORTED = "EXPORTED";
		public static final String CHECKEDIN = "CHECKEDIN";
		public static final String POSTED = "POSTED";
	}

	@Id
	@Column(name = "JOBID")
	private Long jobid;

	@Lob
	@Column(name = "CHECKEDINBY")
	private String checkedinby;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CHECKINDATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS z")
	private Date checkindate;

	@Lob
	@Column(name = "CREATEDBY")
	private String createdby;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATIONDATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS z")
	private Date creationdate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "EXPORTDATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS z")
	private Date exportdate;

	@Lob
	@Column(name = "EXPORTEDBY")
	private String exportedby;

	@Lob
	@Column(name = "CID")
	private String cid;

	@Lob
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "LATITUDE")
	private BigDecimal latitude;

	@Column(name = "LONGITUDE")
	private BigDecimal longitude;

	@Column(name = "NUMFEATURECLASSESCHECKEDIN")
	private Integer numfeatureclassescheckedin;

	@Column(name = "NUMFEATURECLASSESEXPORTED")
	private Integer numfeatureclassesexported;

	@Column(name = "NUMFEATURESCHECKEDIN")
	private Integer numfeaturescheckedin;

	@Column(name = "NUMFEATURESEXPORTED")
	private Integer numfeaturesexported;

	@Lob
	@Column(name = "POSTEDTOGOLDBY")
	private String postedtogoldby;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "POSTTOGOLDDATE")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS z")
	private Date posttogolddate;

	@Lob
	@Column(name = "STATUS")
	private String status;
	
	@Lob
	@Column(name = "PROVIDER")
	private String provider;	

	@Lob
	@Column(name = "SPECIFICATION")
	private String specification;

	public Job() {
	}

	public Long getJobid() {
		return this.jobid;
	}

	public void setJobid(Long jobid) {
		this.jobid = jobid;
	}

	public String getCheckedinby() {
		return this.checkedinby;
	}

	public void setCheckedinby(String checkedinby) {
		this.checkedinby = checkedinby;
	}

	public Date getCheckindate() {
		return this.checkindate;
	}

	public void setCheckindate(Date checkindate) {
		this.checkindate = checkindate;
	}

	public String getCreatedby() {
		return this.createdby;
	}

	public void setCreatedby(String createdby) {
		this.createdby = createdby;
	}

	public Date getCreationdate() {
		return this.creationdate;
	}

	public void setCreationdate(Date creationdate) {
		this.creationdate = creationdate;
	}

	public Date getExportdate() {
		return this.exportdate;
	}

	public void setExportdate(Date exportdate) {
		this.exportdate = exportdate;
	}

	public String getExportedby() {
		return this.exportedby;
	}

	public void setExportedby(String exportedby) {
		this.exportedby = exportedby;
	}

	public String getCid() {
		return this.cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getNumfeatureclassescheckedin() {
		return this.numfeatureclassescheckedin;
	}

	public void setNumfeatureclassescheckedin(Integer numfeatureclassescheckedin) {
		this.numfeatureclassescheckedin = numfeatureclassescheckedin;
	}

	public Integer getNumfeatureclassesexported() {
		return this.numfeatureclassesexported;
	}

	public void setNumfeatureclassesexported(Integer numfeatureclassesexported) {
		this.numfeatureclassesexported = numfeatureclassesexported;
	}

	public Integer getNumfeaturescheckedin() {
		return this.numfeaturescheckedin;
	}

	public void setNumfeaturescheckedin(Integer numfeaturescheckedin) {
		this.numfeaturescheckedin = numfeaturescheckedin;
	}

	public Integer getNumfeaturesexported() {
		return this.numfeaturesexported;
	}

	public void setNumfeaturesexported(Integer numfeaturesexported) {
		this.numfeaturesexported = numfeaturesexported;
	}

	public String getPostedtogoldby() {
		return this.postedtogoldby;
	}

	public void setPostedtogoldby(String postedtogoldby) {
		this.postedtogoldby = postedtogoldby;
	}

	public Date getPosttogolddate() {
		return this.posttogolddate;
	}

	public void setPosttogolddate(Date posttogolddate) {
		this.posttogolddate = posttogolddate;
	}

	public String getStatus() {
		return this.status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getProvider() {
		return this.provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}
	
	public BigDecimal getLatitude() {
		return this.latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return this.longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}
}