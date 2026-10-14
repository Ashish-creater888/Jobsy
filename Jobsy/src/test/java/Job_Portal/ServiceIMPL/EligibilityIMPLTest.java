package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Module.Qualification;
import Job_Portal.Module.User;
import Job_Portal.Repo.JobEligibilityRepo;
import Job_Portal.Repo.JobRepo;
import Job_Portal.Repo.QualificationRepo;
import Job_Portal.Repo.UserRepo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EligibilityIMPLTest {

    @Test
    void matchesAllConfiguredProfileAndNoticeRequirements() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        User user = new User();
        user.setId(1);
        user.setAge(30);
        user.setCategory("OBC");
        user.setCaste("OBC");
        user.setNationality("Indian");
        user.setState("Maharashtra");
        user.setWorkExperienceYears(3.0);
        user.setCertificatesAndLicences("Driving Licence, Typing Certificate");
        user.setHeightCm(174.0);
        user.setChestCm(90.0);
        user.setPhysicalFitnessStatus("Fit");
        user.setOtherEligibilityDetails("Eligible resident of Maharashtra");
        user.setCasteCertificateNumber("OBC-123");
        user.setCasteCertificateValidity(LocalDate.now().plusDays(14));

        Qualification qualification = new Qualification();
        qualification.setQualification("B.Tech");
        qualification.setStream("Computer Science");
        qualification.setPercentage(72.0);

        JOB job = new JOB();
        job.setId(10);
        job.setQualification("B.E.");
        job.setRequiredStream("Computer Science");
        job.setMinimumPercentage(60.0);
        job.setMinAge(21);
        job.setMaxAge(25);
        job.setCategoryAgeLimits(Map.of("OBC", 35));
        job.setCaste("OBC");
        job.setLocation("Maharashtra");
        job.setRequiredNationality("Indian");
        job.setMinimumExperienceYears(2.0);
        job.setRequiredCertificate("Driving Licence");
        job.setMinimumHeightCm(170.0);
        job.setMaximumHeightCm(180.0);
        job.setMinimumChestCm(85.0);
        job.setMaximumChestCm(95.0);
        job.setRequiredFitnessStatus("Fit");
        job.setOtherConditions("Eligible resident of Maharashtra");
        job.setValidCategoryCertificateRequired(true);
        job.setApplicationEnd(LocalDate.now().plusDays(7));

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(qualificationRepo.findByUserId(user.getId()))
                .thenReturn(List.of(qualification));
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(user), service.findEligibleUsers(job));
    }

    @Test
    void doesNotMatchWhenARequiredCertificateIsMissingFromTheProfile() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        User user = new User();
        user.setId(1);
        user.setCertificatesAndLicences("First Aid");

        JOB job = new JOB();
        job.setId(10);
        job.setRequiredCertificate("Driving Licence");
        job.setApplicationEnd(LocalDate.now().plusDays(7));

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(qualificationRepo.findByUserId(user.getId())).thenReturn(List.of());
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(), service.findEligibleUsers(job));
    }

    @Test
    void doesNotMatchJobsPastTheirApplicationDeadline() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        User user = new User();
        user.setId(1);
        user.setWorkExperienceYears(5.0);

        JOB job = new JOB();
        job.setId(10);
        job.setMinimumExperienceYears(1.0);
        job.setApplicationEnd(LocalDate.now().minusDays(1));

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(qualificationRepo.findByUserId(user.getId())).thenReturn(List.of());
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(), service.findEligibleUsers(job));
    }

    @Test
    void matchesUserAgainstRequirementsSavedOnJob() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        User user = new User();
        user.setId(1);
        user.setAge(25);
        user.setQualification("Engineering");
        user.setCaste("General");
        user.setLocation("Delhi");
        user.setTenthPercentage(85.0);
        user.setHscPercentage(78.5);
        user.setDisability(true);

        JOB job = new JOB();
        job.setId(10);
        job.setQualification("Engineering");
        job.setMinAge(21);
        job.setMaxAge(30);
        job.setCaste("General");
        job.setLocation("Delhi");
        job.setMinimumTenthPercentage(80.0);
        job.setMinimumHscPercentage(75.0);
        job.setDisabilityRequired(true);

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(qualificationRepo.findByUserId(user.getId())).thenReturn(List.of());
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(user), service.findEligibleUsers(job));
    }

    @Test
    void rejectsUserWhoDoesNotMeetSchoolPercentageOrDisabilityRequirement() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        User user = new User();
        user.setId(1);
        user.setTenthPercentage(79.0);
        user.setHscPercentage(80.0);
        user.setDisability(false);

        JOB job = new JOB();
        job.setId(10);
        job.setMinimumTenthPercentage(80.0);
        job.setMinimumHscPercentage(75.0);
        job.setDisabilityRequired(true);

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(qualificationRepo.findByUserId(user.getId())).thenReturn(List.of());
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(), service.findEligibleUsers(job));
    }

    @Test
    void doesNotNotifyUsersWhenJobHasNoEligibilityRequirements() {
        UserRepo userRepo = mock(UserRepo.class);
        QualificationRepo qualificationRepo = mock(QualificationRepo.class);
        JobRepo jobRepo = mock(JobRepo.class);
        JobEligibilityRepo eligibilityRepo = mock(JobEligibilityRepo.class);
        EligibilityIMPL service = new EligibilityIMPL(
                userRepo,
                qualificationRepo,
                jobRepo,
                eligibilityRepo
        );

        JOB job = new JOB();
        job.setId(10);

        when(userRepo.findAll()).thenReturn(List.of(new User()));
        when(eligibilityRepo.findByJobId(job.getId())).thenReturn(List.of());

        assertEquals(List.of(), service.findEligibleUsers(job));
    }
}
