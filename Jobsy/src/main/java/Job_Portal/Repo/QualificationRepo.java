package Job_Portal.Repo;

import Job_Portal.Module.Qualification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QualificationRepo extends JpaRepository<Qualification, Integer> {

    List<Qualification> findByUserId(Integer userId);
}