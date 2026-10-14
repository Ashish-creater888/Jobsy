package Job_Portal.Controller;


import Job_Portal.Module.JOB;
import Job_Portal.Service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/jobs")
@Tag(name = "Admin jobs", description = "Create, update, and remove job listings")
public class AdminJobController {

    private final JobService jobService;

    public AdminJobController(JobService jobService) {
        this.jobService = jobService;
    }

    // CREATE JOB
    @PostMapping("Create")
    @Operation(summary = "Create a job listing")
    public JOB createJob(@RequestBody JOB job) {
        return jobService.createJob(job);
    }

    // UPDATE JOB
    @PutMapping("/{id}")
    @Operation(summary = "Update a job listing")
    public JOB updateJob(
            @PathVariable Integer id,
            @RequestBody JOB job) {

        return jobService.updateJob(id, job);
    }

    // DELETE JOB
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job listing")
    public ResponseEntity<String> deleteJob(
            @PathVariable Integer id) {

        jobService.deleteJob(id);

        return ResponseEntity.ok("Job deleted successfully");
    }
}