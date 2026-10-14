package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Module.User;
import Job_Portal.Repo.JobRepo;
import Job_Portal.Service.EligibilityService;
import Job_Portal.Service.JobService;
import Job_Portal.Service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JOBIMPL implements JobService {

    private final JobRepo jobRepo;
    private final EligibilityService eligibilityService;
    private final NotificationService notificationService;

    public JOBIMPL(
            JobRepo jobRepo,
            EligibilityService eligibilityService,
            NotificationService notificationService) {

        this.jobRepo = jobRepo;
        this.eligibilityService = eligibilityService;
        this.notificationService = notificationService;
    }

    @Override
    public List<JOB> getAllJobs() {
        return jobRepo.findAll();
    }

    @Override
    public JOB getJobById(Integer id) {

        return jobRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        ));
    }

    @Override
    public JOB saveJob(JOB job) {
        return createJob(job);
    }

    @Override
    public JOB updateJob(Integer id, JOB job) {

        JOB existingJob = jobRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        ));

        existingJob.setTitle(job.getTitle());
        existingJob.setDepartment(job.getDepartment());
        existingJob.setDescription(job.getDescription());
        existingJob.setApplicationStart(job.getApplicationStart());
        existingJob.setApplicationEnd(job.getApplicationEnd());
        existingJob.setOfficialUrl(job.getOfficialUrl());

        return jobRepo.save(existingJob);
    }

    @Override
    public void deleteJob(Integer id) {

        JOB existingJob = jobRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Job not found with id: " + id
                        ));

        jobRepo.delete(existingJob);
    }

    @Override
    @Transactional
    public JOB createJob(JOB job) {

        JOB savedJob = jobRepo.save(job);

        List<User> eligibleUsers =
                eligibilityService.findEligibleUsers(savedJob);

        notificationService.createNotifications(
                savedJob,
                eligibleUsers
        );

        return savedJob;
    }
}