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

import gov.igs.versionmanager.db.DataAccess;
import gov.igs.versionmanager.model.CreateJob;
import gov.igs.versionmanager.model.Job;

@CrossOrigin
@RestController
@RequestMapping(value = "/jobs")
public class JobController {

	@Autowired
	private DataAccess da;

	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	public Job createJob(@RequestBody CreateJob createJob) {
		if (createJob.isValid()) {
			Job job = new Job();
			job.setJobid((new Date()).getTime());
			job.setName(createJob.getName());
			job.setStatus("NEW");
			job.setCreationdate(new Date());
			job.setCreatedby(createJob.getCreatedBy());
			job.setLatitude(createJob.getLatitude());
			job.setLongitude(createJob.getLongitude());
			da.createJob(job);
			return job;
		} else {
			System.out.println("Job is not valid!");
			return null;
		}
	}

	@RequestMapping(value = "/{jobid}/file", method = RequestMethod.GET, produces = "application/json")
	public String exportJob(@PathVariable(value = "jobid") String jobid) {
		return "Hello, Health Check here";
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.POST, produces = "application/json")
	public String checkInJob(@PathVariable(value = "jobid") String jobid) {
		return "Hello, Health Check here";
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.PUT, produces = "application/json")
	public String postJobToGold(@PathVariable(value = "jobid") String jobid) {
		return "Hello, Health Check here";
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.DELETE)
	public void deleteJob(@PathVariable(value = "jobid") String jobid) {
		da.deleteJob(jobid);
	}

	@RequestMapping(method = RequestMethod.GET, produces = "application/json")
	public List<Job> getAllJobs() {
		return da.getAllJobs();
	}

	@RequestMapping(value = "/{jobid}", method = RequestMethod.GET, produces = "application/json")
	public Job getJobDetails(@PathVariable(value = "jobid") String jobid) {
		return da.getJobDetails(jobid);
	}
}