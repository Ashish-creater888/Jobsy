package Job_Portal.Controller;

import Job_Portal.Module.Qualification;
import Job_Portal.Repo.QualificationRepo;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qualification")
public class QualificationController {

    private final QualificationRepo qualificationRepo;

    public QualificationController(QualificationRepo qualificationRepo) {
        this.qualificationRepo = qualificationRepo;
    }

    @PostMapping("/create")
    public Qualification createQualification(
            @RequestBody Qualification qualification) {

        return qualificationRepo.save(qualification);
    }
}