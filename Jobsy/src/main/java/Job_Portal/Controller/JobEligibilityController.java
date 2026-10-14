package Job_Portal.Controller;

import Job_Portal.Module.JobEligibility;
import Job_Portal.Module.JOB;
import Job_Portal.Service.EligibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/eligibility")
@Tag(name = "Eligibility", description = "Create requirements and find jobs matching a user profile")
public class JobEligibilityController {

    private final EligibilityService jobEligibilityService;

    public JobEligibilityController(EligibilityService jobEligibilityService) {
        this.jobEligibilityService = jobEligibilityService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create eligibility requirements")
    public JobEligibility createEligibility(
            @RequestBody JobEligibility eligibility) {

        return jobEligibilityService.saveEligibility(eligibility);
    }

    @GetMapping("/users/{userId}/jobs")
    @Operation(summary = "Get jobs matching a user profile")
    public List<JOB> getEligibleJobs(@PathVariable Integer userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid user ID is required"
            );
        }
        return jobEligibilityService.getEligibleJobs(userId);
    }
}