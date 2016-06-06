package gov.igs.versionmanager.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.igs.versionmanager.db.JobDataAccessor;
import gov.igs.versionmanager.db.SpatialDataAccessor;
import gov.igs.versionmanager.model.BaseJob;
import gov.igs.versionmanager.model.CreateJob;
import gov.igs.versionmanager.model.Job;

@RestController
@CrossOrigin
@RequestMapping(value = "/jobs")
public class JobController {

	@Autowired
	private JobDataAccessor jda;

	@Autowired
	private SpatialDataAccessor sda;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public Job createJob(@RequestBody CreateJob createJob, @RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		if (createJob.isValid()) {
			Job job = new Job();
			job.setJobid((new Date()).getTime());
			job.setName(createJob.getName());
			job.setStatus("NEW");
			job.setCreationdate(new Date());
			job.setCreatedby(createJob.getUser());
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
			System.out.println("Job Request is not valid!");
			return null;
		}
	}

	@RequestMapping(value = "/{jobid}/file", method = RequestMethod.GET, produces = "application/json")
	public Job exportJob(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob,
			@RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		// Selects all features for Job, writes to a .dump file, and returns to
		// user.
		jda.updateJobToExported(baseJob.getUser(), jobid, 1, 2); // numfeaturesexported,
																	// numfeatureclassesexported);

		return getJobDetails(jobid, user);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.POST, produces = "application/json")
	public Job checkInJob(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob,
			@RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		// Service updates all changed fields on the target features, other than
		// ID.
		// CHECK TO MAKE SURE IN THE EXPORTED STATE FIRST
		jda.updateJobToCheckedIn(baseJob.getUser(), jobid, 3, 4); // numfeaturescheckedin,
																	// numfeatureclassescheckedin);

		return getJobDetails(jobid, user);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.PUT, produces = "application/json")
	public Job postJobToGold(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob,
			@RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		// Calls the “MergeWorkspace” procedure.
		// CHECK TO MAKE SURE IN THE CHECKED-IN STATE FIRST
		jda.updateJobToPosted(baseJob.getUser(), jobid);

		return getJobDetails(jobid, user);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.DELETE)
	public void deleteJob(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return;

		try {
			sda.removeWorkspace(jobid);
			jda.deleteJob(jobid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Job> getAllJobs(@RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		return jda.getAllJobs();
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.GET, produces = "application/json")
	public Job getJobDetails(@PathVariable(value = "jobid") String jobid, @RequestHeader("VMUser") String user) {
		if (!userCheck(user))
			return null;

		return jda.getJob(jobid);
	}

	private boolean userCheck(String user) {
		if (user != null & user.length() > 0) {
			return true;
		}
		return false;
	}
}