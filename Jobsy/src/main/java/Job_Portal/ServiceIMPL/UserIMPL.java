package Job_Portal.ServiceIMPL;

import Job_Portal.DTO.Login;
import Job_Portal.Module.Qualification;
import Job_Portal.Module.JOB;
import Job_Portal.Module.User;
import Job_Portal.Repo.QualificationRepo;
import Job_Portal.Repo.UserRepo;
import Job_Portal.Service.UserService;
import Job_Portal.Service.EligibilityService;
import Job_Portal.Service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class UserIMPL implements UserService {

    private final UserRepo userRepo;
    private final QualificationRepo qualificationRepo;
    private final PasswordEncoder passwordEncoder;
    private final EligibilityService eligibilityService;
    private final NotificationService notificationService;

    public UserIMPL(
            UserRepo userRepo,
            QualificationRepo qualificationRepo,
            PasswordEncoder passwordEncoder,
            EligibilityService eligibilityService,
            NotificationService notificationService) {
        this.userRepo = userRepo;
        this.qualificationRepo = qualificationRepo;
        this.passwordEncoder = passwordEncoder;
        this.eligibilityService = eligibilityService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public User Save(User user) {
        if (user == null
                || user.getEmail() == null || user.getEmail().isBlank()
                || user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required"
            );
        }
        if (userRepo.existsByEmailIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }
        if (user.getDateOfBirth() != null) {
            user.setAge(Period.between(user.getDateOfBirth(), LocalDate.now()).getYears());
        }
        if ((user.getCaste() == null || user.getCaste().isBlank())
                && user.getCategory() != null) {
            user.setCaste("General / UR".equalsIgnoreCase(user.getCategory())
                    ? "General"
                    : user.getCategory());
        }
        if ((user.getLocation() == null || user.getLocation().isBlank())
                && user.getDistrict() != null) {
            user.setLocation(user.getDistrict());
        }

        List<Qualification> qualifications = user.getQualifications();
        if (qualifications != null) {
            qualifications.stream()
                    .filter(qualification -> qualification != null
                            && qualification.getQualification() != null)
                    .forEach(qualification -> {
                        if ("10th".equalsIgnoreCase(qualification.getQualification())
                                && user.getTenthPercentage() == null) {
                            user.setTenthPercentage(qualification.getPercentage());
                        }
                        if ("12th".equalsIgnoreCase(qualification.getQualification())
                                && user.getHscPercentage() == null) {
                            user.setHscPercentage(qualification.getPercentage());
                        }
                    });
        }
        if (user.getPwd() != null) {
            user.setDisability(user.getPwd());
        }
        user.setQualifications(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);

        if (qualifications != null && !qualifications.isEmpty()) {
            for (Qualification qualification : qualifications) {
                if (qualification == null
                        || qualification.getQualification() == null
                        || qualification.getQualification().isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Select a qualification for each education entry"
                    );
                }
                qualification.setId(null);
                qualification.setUserId(savedUser.getId());
            }
            qualificationRepo.saveAll(qualifications);
        }

        List<JOB> eligibleJobs = eligibilityService.getEligibleJobs(savedUser.getId());
        for (JOB job : eligibleJobs) {
            notificationService.createNotifications(
                    job,
                    List.of(savedUser)
            );
        }
        return savedUser;
    }

    @Override
    public User Login(Login loginDTO) {
        if (loginDTO == null
                || loginDTO.getEmail() == null || loginDTO.getEmail().isBlank()
                || loginDTO.getPassword() == null || loginDTO.getPassword().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        List<User> users = userRepo.findAllByEmailIgnoreCaseOrderByIdAsc(loginDTO.getEmail());

        if (users.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        for (User user : users) {
            String storedPassword = user.getPassword();
            if (storedPassword == null || loginDTO.getPassword() == null) {
                continue;
            }

            boolean isLegacyPassword = !storedPassword.startsWith("$2a$")
                    && !storedPassword.startsWith("$2b$")
                    && !storedPassword.startsWith("$2y$");
            boolean passwordMatches = isLegacyPassword
                    ? storedPassword.equals(loginDTO.getPassword())
                    : passwordEncoder.matches(loginDTO.getPassword(), storedPassword);

            if (passwordMatches) {
                if (isLegacyPassword) {
                    user.setPassword(passwordEncoder.encode(loginDTO.getPassword()));
                    userRepo.save(user);
                }
                return user;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }
}
