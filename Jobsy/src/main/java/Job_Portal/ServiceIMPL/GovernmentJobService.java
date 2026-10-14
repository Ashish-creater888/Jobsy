package Job_Portal.ServiceIMPL;



import Job_Portal.Module.JOB;
import Job_Portal.Repo.JobRepo;
import Job_Portal.Service.JobService;
import Job_Portal.Service.GovermentJobScraper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
public class GovernmentJobService {

    private static final Logger logger =
            LoggerFactory.getLogger(GovernmentJobService.class);

    private final List<GovermentJobScraper> scrapers;
    private final JobRepo jobRepo;
    private final JobService jobService;

    public GovernmentJobService(
            List<GovermentJobScraper> scrapers,
            JobRepo jobRepo,
            JobService jobService) {
        this.scrapers = scrapers;
        this.jobRepo = jobRepo;
        this.jobService = jobService;
    }

    public List<JOB> fetchAllJobs() {
        AtomicInteger failedScrapers = new AtomicInteger();
        List<JOB> extractedJobs = scrapers.parallelStream()
                .flatMap(scraper -> {
                    try {
                        List<JOB> jobs = scraper.fetchJobs();
                        return jobs == null ? Stream.<JOB>empty() : jobs.stream();
                    } catch (Exception e) {
                        logger.error(
                                "Scraper {} failed",
                                scraper.getClass().getSimpleName(),
                                e
                        );
                        failedScrapers.incrementAndGet();
                        return Stream.empty();
                    }
                })
                .toList();

        if (!scrapers.isEmpty() && failedScrapers.get() == scrapers.size()) {
            throw new IllegalStateException(
                    "No jobs could be extracted. Check the configured government sources and try again."
            );
        }
        return extractedJobs;
    }

    public List<JOB> fetchAndSaveNewJobs() {
        Set<String> seenUrls = new HashSet<>();
        List<JOB> savedJobs = new ArrayList<>();

        for (JOB job : fetchAllJobs()) {
            String officialUrl = job.getOfficialUrl();
            if (officialUrl == null || officialUrl.isBlank()) {
                logger.warn("Skipping scraped job without an official URL");
                continue;
            }
            if (!seenUrls.add(officialUrl)
                    || jobRepo.existsByOfficialUrl(officialUrl)) {
                continue;
            }

            savedJobs.add(jobService.createJob(job));
        }

        logger.info("Saved {} new jobs from government websites", savedJobs.size());
        return savedJobs;
    }
}