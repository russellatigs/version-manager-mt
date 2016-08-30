package gov.igs.versionmanager.db;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.igs.versionmanager.model.Job;
import gov.igs.versionmanager.util.SpatialDataUtility;

@Component
public class JobDataAccessor {

	@Autowired
	private SpatialDataUtility sdUtil;

	private EntityManager em;

	@PostConstruct
	public void init() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("version-manager-mt");
		em = emf.createEntityManager();
	}

	public void createJob(Job job) {
		em.getTransaction().begin();
		em.persist(job);
		em.getTransaction().commit();
	}

	public void updateJobToExported(String user, String jobid, Integer numfeaturesexported,
			Integer numfeatureclassesexported) {
		Job job = getJob(jobid);

		em.getTransaction().begin();
		job.setStatus(Job.Status.EXPORTED);
		job.setExportdate(new Date());
		job.setExportedby(user);
		job.setNumfeaturesexported(numfeaturesexported);
		job.setNumfeatureclassesexported(numfeatureclassesexported);
		em.getTransaction().commit();
	}

	public void updateJobToCheckedIn(String user, String jobid, Integer numfeaturescheckedin,
			Integer numfeatureclassescheckedin) {
		Job job = getJob(jobid);

		em.getTransaction().begin();
		job.setStatus(Job.Status.CHECKEDIN);
		job.setCheckindate(new Date());
		job.setCheckedinby(user);
		job.setNumfeaturescheckedin(numfeaturescheckedin);
		job.setNumfeatureclassescheckedin(numfeatureclassescheckedin);
		em.getTransaction().commit();
	}

	public void updateJobToPosted(String user, String jobid) {
		Job job = getJob(jobid);

		em.getTransaction().begin();
		job.setStatus(Job.Status.POSTED);
		job.setPostedtogoldby(user);
		job.setPosttogolddate(new Date());
		em.getTransaction().commit();
	}

	public void deleteJob(String jobid) {
		em.getTransaction().begin();
		em.remove(getJob(jobid));
		em.getTransaction().commit();
	}

	public List<Job> getAllJobs() {
		return em.createNamedQuery("Job.findAll").getResultList();
	}

	public Job getJob(String jobid) {
		return em.find(Job.class, Long.parseLong(jobid));
	}

	public Job getLatestJobForCell(String cid) {
		try {
			return getJob(em
					.createNativeQuery("select max(jobid) from job where cid = '" + cid + "'")
					.getSingleResult().toString());
		} catch( Exception e ) {
			e.printStackTrace();
			return null;
		}
	}
}