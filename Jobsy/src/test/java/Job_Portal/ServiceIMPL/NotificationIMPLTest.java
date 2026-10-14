package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Module.Notification;
import Job_Portal.Module.User;
import Job_Portal.Repo.NotificationRepo;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationIMPLTest {

    @Test
    void queuesEmailForMatchingUser() {
        NotificationRepo notificationRepo = mock(NotificationRepo.class);
        NotificationIMPL service = new NotificationIMPL(notificationRepo);

        User user = new User();
        user.setId(4);
        user.setName("Asha");
        user.setEmail("asha@example.com");

        JOB job = new JOB();
        job.setId(12);
        job.setTitle("Research Scientist");
        job.setDepartment("NRSC");
        job.setOfficialUrl("https://example.com/apply");

        service.createNotifications(job, List.of(user));

        ArgumentCaptor<Notification> notificationCaptor =
                ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepo).save(notificationCaptor.capture());
        Notification notification = notificationCaptor.getValue();

        assertEquals(user.getId(), notification.getUserId());
        assertEquals(job.getId(), notification.getJobId());
        assertEquals(user.getEmail(), notification.getEmail());
        assertEquals("PENDING", notification.getStatus());
        assertFalse(notification.isEmailSent());
        assertTrue(notification.getSubject().contains("matches your profile"));
        assertTrue(notification.getMessage().contains("you appear to meet the eligibility requirements"));
        assertTrue(notification.getMessage().contains(job.getTitle()));
        assertTrue(notification.getMessage().contains(
                "Apply through the official link: " + job.getOfficialUrl()
        ));
        assertTrue(notification.getMessage().contains(
                "The recruiting organization makes the final eligibility decision."
        ));
    }
}
