package Job_Portal.Service;

import Job_Portal.Module.JOB;

import java.util.List;

public interface JobService {

    List<JOB> getAllJobs() ;

    JOB getJobById(Integer id);

    JOB saveJob(JOB job);

    JOB updateJob(Integer id, JOB job);

    void deleteJob(Integer id);


    JOB createJob(JOB job);
}
