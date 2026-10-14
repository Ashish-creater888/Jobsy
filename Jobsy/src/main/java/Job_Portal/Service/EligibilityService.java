package Job_Portal.Service;

import Job_Portal.Module.JOB;
import Job_Portal.Module.JobEligibility;
import Job_Portal.Module.User;

import java.util.List;

public interface EligibilityService {

    List<User> findEligibleUsers(JOB job);

    List<JOB> getEligibleJobs(Integer userId);

    JobEligibility saveEligibility(JobEligibility eligibility);
}