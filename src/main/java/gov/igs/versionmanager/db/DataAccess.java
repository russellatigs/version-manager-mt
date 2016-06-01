package gov.igs.versionmanager.db;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import gov.igs.versionmanager.model.Job;

@Component
public class DataAccess {

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
	//
	// public String exportJob(@PathVariable(value = "jobid") String jobid) {
	// return "Hello, Health Check here";
	// }
	//
	// public String checkInJob(@PathVariable(value = "jobid") String jobid) {
	// return "Hello, Health Check here";
	// }
	//
	// public String postJobToGold(@PathVariable(value = "jobid") String jobid)
	// {
	// return "Hello, Health Check here";
	// }

	public void deleteJob(String jobid) {
		em.getTransaction().begin();
		em.remove(getJobDetails(jobid));
		em.getTransaction().commit();
	}

	public List<Job> getAllJobs() {
		CriteriaQuery<Job> cq = em.getCriteriaBuilder().createQuery(Job.class);
		Root<Job> job = cq.from(Job.class);
		cq.select(job);

		return em.createQuery(cq).getResultList();
	}

	public Job getJobDetails(String jobid) {
		return em.find(Job.class, Long.parseLong(jobid));
	}
}