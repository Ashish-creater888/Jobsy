package Job_Portal.Repo;

import Job_Portal.Module.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepo
        extends JpaRepository<Notification, Integer> {

    List<Notification> findTop50ByStatusOrderByCreatedAtAsc(
            String status
    );
}