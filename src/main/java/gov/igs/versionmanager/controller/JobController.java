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
import org.springframework.web.bind.annotation.RestController;

import gov.igs.versionmanager.db.JobDataAccessor;
import gov.igs.versionmanager.db.SpatialDataAccessor;
import gov.igs.versionmanager.model.CreateJob;
import gov.igs.versionmanager.model.Job;

@RestController
@CrossOrigin
@RequestMapping(value = "/jobs")
public class JobController {

	private Logger log = Logger.getLogger(JobController.class.getName());

	@Autowired
	private JobDataAccessor jda;

	@Autowired
	private SpatialDataAccessor sda;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public Job createJob(@RequestBody CreateJob createJob, @RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return null;
		}

		if (createJob.isValid()) {
			Job job = new Job();
			job.setJobid((new Date()).getTime());
			job.setName(createJob.getName());
			job.setStatus("NEW");
			job.setCreationdate(new Date());
			job.setCreatedby(user);
			job.setLatitude(createJob.getLatitude());
			job.setLongitude(createJob.getLongitude());

			try {
				sda.createWorkspace(job.getJobid(), createJob.getLatitude(), createJob.getLongitude());
				jda.createJob(job);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return job;
		} else {
			log.log(Level.SEVERE, "Job Request is not valid!");
			return null;
		}
	}

	@RequestMapping(value = "/{jobid}/file", method = RequestMethod.GET)
	public void exportJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user,
			HttpServletResponse response) {
		if (!userCheck(user)) {
			return;
		}

		if (getJobDetails(jobid, user) != null) {
			// Selects features for Job, writes to a .dump file, and returns
			// file.
			try {
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
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.POST, produces = "application/json")
	public Job checkInJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return null;
		}

		if (getJobDetails(jobid, user) != null) {

			// Service updates all changed fields on the target features, other
			// than
			// ID.
			// CHECK TO MAKE SURE IN THE EXPORTED STATE FIRST
			jda.updateJobToCheckedIn(user, jobid, 3, 4); // numfeaturescheckedin,
															// numfeatureclassescheckedin);
		} else {
			return null;
		}

		return getJobDetails(jobid, user);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.PUT, produces = "application/json")
	public Job postJobToGold(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return null;
		}

		if (getJobDetails(jobid, user) != null) {

			// Calls the “MergeWorkspace” procedure.
			// CHECK TO MAKE SURE IN THE CHECKED-IN STATE FIRST
			jda.updateJobToPosted(user, jobid);

		} else {
			return null;
		}

		return getJobDetails(jobid, user);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.DELETE)
	public void deleteJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return;
		}

		if (getJobDetails(jobid, user) != null) {
			try {
				sda.removeWorkspace(jobid);
				jda.deleteJob(jobid);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Job> getAllJobs(@RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return null;
		}

		return jda.getAllJobs();
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.GET, produces = "application/json")
	public Job getJobDetails(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user)) {
			return null;
		}

		Job job = jda.getJob(jobid);

		if (job == null) {
			log.log(Level.SEVERE, "Job does not exist!");
			return null;
		} else {
			return job;
		}
	}

	private boolean userCheck(String user) {
		if (user != null & user.length() > 0) {
			return true;
		}

		log.log(Level.SEVERE, "User does not exist!");
		return false;
	}
}