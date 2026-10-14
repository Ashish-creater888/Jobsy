package Job_Portal.Repo;

import Job_Portal.Module.JobEligibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobEligibilityRepo extends JpaRepository<JobEligibility, Integer> {

    List<JobEligibility> findByJobId(Integer jobId);
}