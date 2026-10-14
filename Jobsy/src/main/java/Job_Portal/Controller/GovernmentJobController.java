package Job_Portal.Controller;

import Job_Portal.Module.JOB;
import Job_Portal.ServiceIMPL.GovernmentJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/jobs")
@Tag(name = "Government job extraction", description = "Import and save new vacancies from configured official sources")
public class GovernmentJobController {

    private final GovernmentJobService governmentJobService;

    public GovernmentJobController(
            GovernmentJobService governmentJobService) {

        this.governmentJobService = governmentJobService;
    }

    @PostMapping("/extract")
    @Operation(summary = "Extract and save new jobs")
    public List<JOB> extractAndSaveJobs() {
        return governmentJobService.fetchAndSaveNewJobs();
    }
}