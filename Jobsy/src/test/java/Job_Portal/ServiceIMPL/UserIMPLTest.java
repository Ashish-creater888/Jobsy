package Job_Portal.ServiceIMPL;

import Job_Portal.DTO.Login;
import Job_Portal.Module.JOB;
import Job_Portal.Module.Qualification;
import Job_Portal.Module.User;
import Job_Portal.Repo.QualificationRepo;
import Job_Portal.Repo.UserRepo;
import Job_Portal.Service.EligibilityService;
import Job_Portal.Service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserIMPLTest {

    private final UserRepo userRepo = mock(UserRepo.class);
    private final QualificationRepo qualificationRepo = mock(QualificationRepo.class);
    private final EligibilityService eligibilityService = mock(EligibilityService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();
    private final UserIMPL userService =
            new UserIMPL(
                    userRepo,
                    qualificationRepo,
                    passwordEncoder,
                    eligibilityService,
                    notificationService
            );

    @Test
    void registrationHashesPasswordAndDoesNotSerializeIt() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password123");

        when(userRepo.existsByEmailIgnoreCase(user.getEmail())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.Save(user);
        String serializedUser = new ObjectMapper().writeValueAsString(savedUser);

        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
        assertFalse(serializedUser.contains("password"));
        assertFalse(serializedUser.contains("password123"));
    }

    @Test
    void registrationStoresSeparateEducationRecordsForSavedUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password123");
        user.setCategory("OBC");
        Qualification qualification = new Qualification();
        qualification.setQualification("B.Tech");
        qualification.setDegreeName("Bachelor of Technology");
        user.setQualifications(List.of(qualification));

        when(userRepo.existsByEmailIgnoreCase(user.getEmail())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(42);
            return savedUser;
        });

        User savedUser = userService.Save(user);

        assertTrue(savedUser.getId() == 42);
        assertTrue("OBC".equals(savedUser.getCaste()));
        assertTrue(user.getQualifications() == null);
        assertTrue(qualification.getUserId() == 42);
        verify(qualificationRepo).saveAll(List.of(qualification));
    }

    @Test
    void registrationQueuesEmailsForJobsTheNewProfileMatches() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password123");
        JOB matchingJob = new JOB();
        matchingJob.setId(12);

        when(userRepo.existsByEmailIgnoreCase(user.getEmail())).thenReturn(false);
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(42);
            return savedUser;
        });
        when(eligibilityService.getEligibleJobs(42)).thenReturn(List.of(matchingJob));

        User savedUser = userService.Save(user);

        verify(notificationService).createNotifications(
                matchingJob,
                List.of(savedUser)
        );
    }

    @Test
    void successfulLoginMigratesLegacyPlaintextPassword() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password123");
        when(userRepo.findAllByEmailIgnoreCaseOrderByIdAsc(user.getEmail()))
                .thenReturn(List.of(user));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.Login(new Login(user.getEmail(), "password123"));

        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
        verify(userRepo).save(user);
    }
}
