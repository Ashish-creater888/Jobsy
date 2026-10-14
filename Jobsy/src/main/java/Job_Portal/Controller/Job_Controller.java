package Job_Portal.Controller;

import Job_Portal.Module.JOB;
import Job_Portal.Service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job")
@Tag(name = "Jobs", description = "Browse and manage saved government job listings")
public class Job_Controller {

    @Autowired
    private JobService jobService;

    @GetMapping("/GetAllJob")
    @Operation(summary = "List saved jobs")
    public List<JOB> getAllJobs() {
        return jobService.getAllJobs();
    }

    // GET job by ID
    @GetMapping("/{id}")
    @Operation(summary = "Get a saved job by ID")
    public JOB getJobById(@PathVariable Integer id) {
        return jobService.getJobById(id);
    }

    @PostMapping("/CreateJob")
    @Operation(summary = "Create a job and notify eligible users")
    public JOB createJob(@RequestBody JOB job) {

        return jobService.createJob(job);
    }
}
