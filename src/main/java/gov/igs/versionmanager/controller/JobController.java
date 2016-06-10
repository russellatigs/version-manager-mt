package gov.igs.versionmanager.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gov.igs.versionmanager.db.JobDataAccessor;
import gov.igs.versionmanager.db.SpatialDataAccessor;
import gov.igs.versionmanager.model.CreateJob;
import gov.igs.versionmanager.model.Job;
import gov.igs.versionmanager.model.VMResponse;

@RestController
@CrossOrigin
@RequestMapping(value = "/jobs")
public class JobController {

	private Logger log = Logger.getLogger(JobController.class.getName());

	@Autowired
	private JobDataAccessor jda;

	@Autowired
	private SpatialDataAccessor sda;
	
	private enum Method { EXPORT, CHECKIN, POST, DELETE };

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public VMResponse createJob(@RequestBody CreateJob createJob, @RequestHeader("VMUser") String user) {
		try {
			if (createJob.isValid()) {
				Job job = new Job();
				job.setJobid((new Date()).getTime());
				job.setName(createJob.getName());
				job.setStatus(Job.Status.NEW);
				job.setCreationdate(new Date());
				job.setCreatedby(user);
				job.setLatitude(createJob.getLatitude());
				job.setLongitude(createJob.getLongitude());
	
				sda.createWorkspace(job);
				
				return job;
			}
			else {
				log.log(Level.SEVERE, "Job Request is not valid!");
				return new VMResponse("Job Request is not valid!");				
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new VMResponse(e.getMessage());
		}
	}

	@RequestMapping(value = "/{jobid}/file", method = RequestMethod.GET)
	public void exportJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user,
			HttpServletResponse response) {
		try {	
			// CHECK TO MAKE SURE IN THE RIGHT STATE FIRST			
			jobStatusCheck(getJobDetails(jobid, user), Method.EXPORT);
			
			// Selects features, writes to a .dump file, and returns file.
			File file = sda.exportWorkspace(jobid, user);
			FileInputStream fis = new FileInputStream(file);

			// Set the content type and attachment header.
			response.addHeader("Content-disposition", "attachment;filename=" + file.getName());
			response.setContentType(MediaType.TEXT_PLAIN_VALUE);
			response.addHeader("Content-Length", Long.toString(file.length()));

			// Copy the stream to the response's output stream.
			IOUtils.copy(fis, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.POST, produces = "application/json")
	public VMResponse checkInJob(@PathVariable(value = "jobid") String jobid, @RequestParam final MultipartFile file, @RequestHeader("VMUser") String user) {
		try {
			// CHECK TO MAKE SURE IN THE EXPORTED STATE FIRST
			jobStatusCheck( getJobDetails(jobid, user), Method.CHECKIN);
					
			sda.checkInFile(jobid, user, file.getInputStream());
			
			return getJobDetails(jobid, user);				
		} catch (Exception e) {
			e.printStackTrace();
			return new VMResponse(e.getMessage());
		}
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.PUT, produces = "application/json")
	public VMResponse postJobToGold(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		try {
			// CHECK TO MAKE SURE IN THE CHECKED-IN STATE FIRST				
			jobStatusCheck(getJobDetails(jobid, user), Method.POST);
			
			// Calls the “MergeWorkspace” procedure.
			jda.updateJobToPosted(user, jobid);
			
			return getJobDetails(jobid, user);
		} catch (Exception e) {
			e.printStackTrace();
			return new VMResponse(e.getMessage());
		}
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.DELETE)
	public VMResponse deleteJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		try {
			// CHECK TO MAKE SURE IN THE RIGHT STATE FIRST				
			jobStatusCheck(getJobDetails(jobid, user), Method.DELETE);

			sda.removeWorkspace(jobid);
			
			return new VMResponse("Job deleted succcessfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new VMResponse(e.getMessage());
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Job> getAllJobs(@RequestHeader("VMUser") String user) {
		return jda.getAllJobs();
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.GET, produces = "application/json")
	public Job getJobDetails(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		Job job = jda.getJob(jobid);

		if (job == null) {
			log.log(Level.SEVERE, "Job does not exist!");
			return null;
		} else {
			return job;
		}
	}
	
	private void jobStatusCheck(Job job, Method method) throws Exception {
		if( job == null ) {
			throw new Exception("Job does not exist!");
		}
		
		switch(method) {
		case EXPORT:
			if( job.getStatus().equals(Job.Status.CHECKEDIN) || job.getStatus().equals(Job.Status.POSTED)) {
				throw new Exception("Cannot export a job after it has been checked in!");
			}		
			break;
		case POST:
			if( !job.getStatus().equals(Job.Status.CHECKEDIN) ) {
				throw new Exception("Jobs must be checked in to be posted to gold!");
			}			
			break;
		case CHECKIN:
			if( !job.getStatus().equals(Job.Status.EXPORTED) ) {
				throw new Exception("Jobs must be exported to be checked in!");
			}				
			break;
		case DELETE:
			break;
		}
	}
}