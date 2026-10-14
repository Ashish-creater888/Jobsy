package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import org.springframework.stereotype.Service;

@Service
public class JobRequirementParser {

    public JOB parse(String text, String officialUrl) {

        JOB job = new JOB();

        job.setOfficialUrl(officialUrl);

        // Extract title
        // Extract qualification
        // Extract age
        // Extract dates
        // Extract vacancies
        // Extract location
        // Extract caste/category

        return job;
    }
}
