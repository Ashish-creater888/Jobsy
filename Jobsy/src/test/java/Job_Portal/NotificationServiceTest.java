package Job_Portal;

import Job_Portal.Module.Notification;
import Job_Portal.Service.NotificationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class NotificationServiceTest {

    @Autowired
    private NotificationService notificationService;

    @Test
    public void EmailSentSuccessfully() {

        assertNotNull(notificationService);

        System.out.println("My first test");

        Notification notification = new Notification();

    }
}