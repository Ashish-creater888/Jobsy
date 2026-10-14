package Config;

import Job_Portal.Config.EmailWorker;
import Job_Portal.Module.Notification;
import Job_Portal.Repo.NotificationRepo;
import Job_Portal.Service.Email_Service;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailWorkerTest {

    @Test
    void sendsPendingNotificationAndMarksItSent() {
        NotificationRepo notificationRepo = mock(NotificationRepo.class);
        Email_Service emailService = mock(Email_Service.class);
        EmailWorker worker = new EmailWorker(notificationRepo, emailService);

        Notification notification = new Notification();
        notification.setId(9);
        notification.setEmail("asha@example.com");
        notification.setSubject("Matching job");
        notification.setMessage("A job matches your profile.");
        notification.setStatus("PENDING");

        when(notificationRepo.findTop50ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(notification));
        when(notificationRepo.findById(notification.getId()))
                .thenReturn(Optional.of(notification));

        worker.processEmails();

        verify(emailService).sendEmail(
                notification.getEmail(),
                notification.getSubject(),
                notification.getMessage()
        );
        assertEquals("SENT", notification.getStatus());
        assertTrue(notification.isEmailSent());
        assertNotNull(notification.getSentAt());
    }
}
