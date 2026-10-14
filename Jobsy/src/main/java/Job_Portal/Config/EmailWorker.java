package Job_Portal.Config;

import Job_Portal.Module.Notification;
import Job_Portal.Repo.NotificationRepo;
import Job_Portal.Service.Email_Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "jobsy.email-worker.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class EmailWorker {

    private static final Logger logger =
            LoggerFactory.getLogger(EmailWorker.class);

    private final NotificationRepo notificationRepo;
    private final Email_Service emailService;

    public EmailWorker(
            NotificationRepo notificationRepo,
            Email_Service emailService) {
        this.notificationRepo = notificationRepo;
        this.emailService = emailService;
    }

    @Scheduled(fixedDelay = 5000)
    public void processEmails() {

        List<Notification> pending =
                notificationRepo
                        .findTop50ByStatusOrderByCreatedAtAsc("PENDING");

        for (Notification notification : pending) {
            processOne(Long.valueOf(notification.getId()));
        }
    }

    public void processOne(Long id) {

        Notification notification = notificationRepo.findById(Math.toIntExact(id))
                .orElse(null);

        if (notification == null
                || !"PENDING".equals(notification.getStatus())) {
            return;
        }

        notification.setStatus("PROCESSING");
        notificationRepo.saveAndFlush(notification);

        try {
            emailService.sendEmail(
                    notification.getEmail(),
                    notification.getSubject(),
                    notification.getMessage());

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notification.setLastError(null);
            notification.setEmailSent(true);

        } catch (Exception e) {
            int attempts = notification.getAttempts() + 1;
            notification.setAttempts(attempts);

            notification.setLastError(
                    e.getMessage() == null || e.getMessage().isBlank()
                            ? "Email sending failed"
                            : e.getMessage());

            notification.setStatus(
                    attempts < 3 ? "PENDING" : "FAILED");
            logger.error(
                    "Failed to send job notification {} (attempt {})",
                    notification.getId(),
                    attempts,
                    e
            );
        }

        notificationRepo.save(notification);
    }
}