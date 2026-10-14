package Job_Portal.Service;

import Job_Portal.Module.JOB;
import Job_Portal.Module.Notification;
import Job_Portal.Module.User;

import java.util.List;

public interface NotificationService {

    void createNotifications(
            JOB job,
            List<User> users
    );


}