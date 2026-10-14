package Job_Portal.Repo;

import Job_Portal.Module.JOB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepo extends JpaRepository<JOB, Integer> {

    boolean existsByOfficialUrl(String officialUrl);
}
