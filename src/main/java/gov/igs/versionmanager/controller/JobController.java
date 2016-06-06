package gov.igs.versionmanager.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
	public Job createJob(@RequestBody CreateJob createJob) {
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
	public Job exportJob(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob) {

		// Selects all features for Job, writes to a .dump file, and returns to
		// user.
		jda.updateJobToExported(baseJob.getUser(), jobid, 1, 2); // numfeaturesexported,
																	// numfeatureclassesexported);

		return getJobDetails(jobid);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.POST, produces = "application/json")
	public Job checkInJob(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob) {

		// Service updates all changed fields on the target features, other than
		// ID.
		jda.updateJobToCheckedIn(baseJob.getUser(), jobid, 3, 4); // numfeaturescheckedin,
																	// numfeatureclassescheckedin);

		return getJobDetails(jobid);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.PUT, produces = "application/json")
	public Job postJobToGold(@PathVariable(value = "jobid") String jobid, @RequestBody BaseJob baseJob) {

		// Calls the “MergeWorkspace” procedure.
		jda.updateJobToPosted(baseJob.getUser(), jobid);

		return getJobDetails(jobid);
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.DELETE)
	public void deleteJob(@PathVariable(value = "jobid") String jobid) {
		try {
			sda.removeWorkspace(jobid);
			jda.deleteJob(jobid);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Job> getAllJobs() {
		return jda.getAllJobs();
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.GET, produces = "application/json")
	public Job getJobDetails(@PathVariable(value = "jobid") String jobid) {
		return jda.getJob(jobid);
	}
}