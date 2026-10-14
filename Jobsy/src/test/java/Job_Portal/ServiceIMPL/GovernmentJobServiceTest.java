package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Repo.JobRepo;
import Job_Portal.Service.GovermentJobScraper;
import Job_Portal.Service.JobService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GovernmentJobServiceTest {

    @Test
    void savesOnlyNewScrapedJobsUsingOfficialUrlAsTheDuplicateKey() {
        JobRepo jobRepo = mock(JobRepo.class);
        JobService jobService = mock(JobService.class);
        JOB newJob = job("New vacancy", "https://official.example/new");
        JOB existingJob = job("Existing vacancy", "https://official.example/existing");
        JOB duplicateInBatch = job("Duplicate vacancy", "https://official.example/new");
        JOB withoutUrl = job("Missing official URL", null);
        GovermentJobScraper scraper =
                () -> List.of(newJob, existingJob, duplicateInBatch, withoutUrl);
        GovernmentJobService service = new GovernmentJobService(
                List.of(scraper),
                jobRepo,
                jobService
        );

        when(jobRepo.existsByOfficialUrl(newJob.getOfficialUrl())).thenReturn(false);
        when(jobRepo.existsByOfficialUrl(existingJob.getOfficialUrl())).thenReturn(true);
        when(jobService.createJob(any(JOB.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
        );

        List<JOB> savedJobs = service.fetchAndSaveNewJobs();

        assertEquals(List.of(newJob), savedJobs);
        verify(jobService).createJob(newJob);
        verify(jobService, never()).createJob(existingJob);
        verify(jobRepo, never()).existsByOfficialUrl(null);
    }

    private JOB job(String title, String officialUrl) {
        JOB job = new JOB();
        job.setTitle(title);
        job.setOfficialUrl(officialUrl);
        return job;
    }
}
