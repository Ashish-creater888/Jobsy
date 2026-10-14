package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Module.Notification;
import Job_Portal.Module.User;
import Job_Portal.Repo.NotificationRepo;
import Job_Portal.Service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationIMPL implements NotificationService {

    private static final Logger logger =
            LoggerFactory.getLogger(NotificationIMPL.class);

    private final NotificationRepo notificationRepo;

    public NotificationIMPL(NotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Override
    public void createNotifications(
            JOB job,
            List<User> users) {

        for (User user : users) {
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                logger.warn(
                        "Skipping job-match email for user {} because no email address is configured",
                        user.getId()
                );
                continue;
            }

            Notification notification = new Notification();

            notification.setUserId(user.getId());
            notification.setJobId(job.getId());
            notification.setEmail(user.getEmail());
            notification.setSubject("A government job matches your profile");
            notification.setMessage(buildEmailBody(user, job));
            notification.setStatus("PENDING");
            notification.setAttempts(0);
            notification.setEmailSent(false);

            notificationRepo.save(notification);
        }
    }

    private String buildEmailBody(User user, JOB job) {
        StringBuilder body = new StringBuilder()
                .append("Hello ")
                .append(user.getName() == null || user.getName().isBlank()
                        ? "Jobsy user"
                        : user.getName())
                .append(",\n\n")
                .append("Based on the details in your profile, you appear to meet the eligibility requirements for this government job:\n\n")
                .append("Job title: ")
                .append(valueOrFallback(job.getTitle(), "Not provided"))
                .append('\n');

        if (job.getDepartment() != null && !job.getDepartment().isBlank()) {
            body.append("Department: ").append(job.getDepartment()).append('\n');
        }
        if (job.getLocation() != null && !job.getLocation().isBlank()) {
            body.append("Location: ").append(job.getLocation()).append('\n');
        }
        if (job.getApplicationEnd() != null) {
            body.append("Application deadline: ")
                    .append(job.getApplicationEnd())
                    .append('\n');
        }
        if (job.getOfficialUrl() != null && !job.getOfficialUrl().isBlank()) {
            body.append("\nApply through the official link: ")
                    .append(job.getOfficialUrl())
                    .append('\n');
        } else {
            body.append("\nAn official application link was not provided. ")
                    .append("Please check the recruiting department's website for application details.\n");
        }

        return body.append("\nPlease review the official notification carefully before applying. ")
                .append("The recruiting organization makes the final eligibility decision.")
                .toString();
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}