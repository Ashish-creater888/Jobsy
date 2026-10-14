package Job_Portal.ServiceIMPL;

import Job_Portal.Module.JOB;
import Job_Portal.Module.JobEligibility;
import Job_Portal.Module.Qualification;
import Job_Portal.Module.User;
import Job_Portal.Repo.JobEligibilityRepo;
import Job_Portal.Repo.JobRepo;
import Job_Portal.Repo.QualificationRepo;
import Job_Portal.Repo.UserRepo;
import Job_Portal.Service.EligibilityService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.Period;

@Service
public class EligibilityIMPL implements EligibilityService {

    private final UserRepo userRepo;
    private final QualificationRepo qualificationRepo;
    private final JobRepo jobRepo;
    private final JobEligibilityRepo eligibilityRepo;

    public EligibilityIMPL(
            UserRepo userRepo,
            QualificationRepo qualificationRepo,
            JobRepo jobRepo,
            JobEligibilityRepo eligibilityRepo) {

        this.userRepo = userRepo;
        this.qualificationRepo = qualificationRepo;
        this.jobRepo = jobRepo;
        this.eligibilityRepo = eligibilityRepo;
    }

    // =========================================================
    // SAVE JOB ELIGIBILITY
    // =========================================================

    @Override
    public JobEligibility saveEligibility(JobEligibility eligibility) {

        return eligibilityRepo.save(eligibility);
    }

    // =========================================================
    // FIND ELIGIBLE USERS FOR A JOB
    // =========================================================

    @Override
    public List<User> findEligibleUsers(JOB job) {

        List<User> allUsers = userRepo.findAll();

        List<JobEligibility> requirements = getRequirementsForJob(job);

        List<User> eligibleUsers = new ArrayList<>();

        if (requirements.isEmpty()) {
            return eligibleUsers;
        }

        // Check every user
        for (User user : allUsers) {

            List<Qualification> qualifications =
                    qualificationRepo.findByUserId(user.getId());

            // Check every requirement
            for (JobEligibility requirement : requirements) {
                if (matches(user, qualifications, requirement)) {
                    eligibleUsers.add(user);
                    break;
                }
            }
        }

        return eligibleUsers;
    }

    // =========================================================
    // CHECK AGE
    // =========================================================

    private boolean checkAge(
            User user,
            JobEligibility requirement) {

        Integer maximumAge = getCategoryMaximumAge(user, requirement);
        boolean categoryAgeLimitsConfigured =
                requirement.getCategoryAgeLimits() != null
                        && !requirement.getCategoryAgeLimits().isEmpty();
        if (requirement.getMinimumAge() == null
                && maximumAge == null) {
            return !categoryAgeLimitsConfigured
                    || hasText(user.getCategory())
                    || hasText(user.getCaste());
        }

        LocalDate ageDate = requirement.getAgeAsOfDate() == null
                ? LocalDate.now()
                : requirement.getAgeAsOfDate();
        Integer age = user.getDateOfBirth() == null
                ? requirement.getAgeAsOfDate() == null ? user.getAge() : null
                : Period.between(user.getDateOfBirth(), ageDate).getYears();
        if (age == null || age < 0) {
            return false;
        }

        if (requirement.getMinimumAge() != null
                && age < requirement.getMinimumAge()) {
            return false;
        }

        if (maximumAge != null && age > maximumAge) {
            return false;
        }

        return true;
    }

    // =========================================================
    // CHECK CASTE
    // =========================================================

    private boolean checkCaste(
            User user,
            JobEligibility requirement) {

        // If no caste requirement is specified,
        // everyone is allowed.
        if (requirement.getCaste() == null
                || requirement.getCaste().isBlank()) {

            return true;
        }

        return sameText(requirement.getCaste(), user.getCaste())
                || sameText(requirement.getCaste(), user.getCategory())
                || sameText(requirement.getCaste(), user.getObcType())
                || sameText(requirement.getCaste(), user.getCasteName());
    }

    // =========================================================
    // CHECK LOCATION
    // =========================================================

    private boolean checkLocation(
            User user,
            JobEligibility requirement) {

        // If no location requirement is specified,
        // everyone is allowed.
        if (requirement.getLocation() == null
                || requirement.getLocation().isBlank()) {

            return true;
        }

        return sameText(requirement.getLocation(), user.getLocation())
                || sameText(requirement.getLocation(), user.getState())
                || sameText(requirement.getLocation(), user.getDistrict());
    }

    // =========================================================
    // CHECK QUALIFICATION
    // =========================================================

    private boolean checkQualification(
            User user,
            List<Qualification> qualifications,
            JobEligibility requirement) {

        boolean hasQualificationRequirement =
                requirement.getRequiredQualification() != null
                        && !requirement.getRequiredQualification().isBlank();
        boolean hasStreamRequirement =
                requirement.getRequiredStream() != null
                        && !requirement.getRequiredStream().isBlank();
        boolean hasPercentageRequirement =
                requirement.getMinimumPercentage() != null;
        boolean hasPassingYearRequirement =
                requirement.getPassingYear() != null;

        if (!hasQualificationRequirement
                && !hasStreamRequirement
                && !hasPercentageRequirement
                && !hasPassingYearRequirement) {
            return true;
        }

        if (hasQualificationRequirement
                && !hasStreamRequirement
                && !hasPercentageRequirement
                && !hasPassingYearRequirement
                && user.getQualification() != null
                && meetsQualificationLevel(
                        user.getQualification(),
                        requirement.getRequiredQualification())) {
            return true;
        }

        if (qualifications == null || qualifications.isEmpty()) {
            return false;
        }

        for (Qualification qualification : qualifications) {

            if (qualification == null) {
                continue;
            }

            // -----------------------------------------
            // Qualification
            // -----------------------------------------

            boolean qualificationMatch = true;

            if (requirement.getRequiredQualification() != null
                    && !requirement.getRequiredQualification().isBlank()) {

                if (qualification.getQualification() == null) {
                    qualificationMatch = false;
                } else {

                    qualificationMatch =
                            meetsQualificationLevel(
                                    qualification.getQualification(),
                                    requirement.getRequiredQualification()
                            );
                }
            }

            if (!qualificationMatch) {
                continue;
            }

            // -----------------------------------------
            // Stream
            // -----------------------------------------

            boolean streamMatch = true;

            if (requirement.getRequiredStream() != null
                    && !requirement.getRequiredStream().isBlank()) {

                if (qualification.getStream() == null) {
                    streamMatch = false;
                } else {

                    streamMatch =
                            requirement.getRequiredStream()
                                    .equalsIgnoreCase(
                                            qualification.getStream()
                                    );
                }
            }

            if (!streamMatch) {
                continue;
            }

            // -----------------------------------------
            // Percentage
            // -----------------------------------------

            boolean percentageMatch = true;

            if (requirement.getMinimumPercentage() != null) {

                Double actualPercentage = qualification.getPercentage();
                if (actualPercentage == null
                        && qualification.getTotalMarks() != null
                        && qualification.getObtainedMarks() != null
                        && qualification.getTotalMarks() > 0) {
                    actualPercentage = qualification.getObtainedMarks()
                            / qualification.getTotalMarks() * 100;
                }
                percentageMatch = actualPercentage != null
                        && actualPercentage >= requirement.getMinimumPercentage();
            }

            if (!percentageMatch) {
                continue;
            }

            // -----------------------------------------
            // Passing Year
            // -----------------------------------------

            boolean passingYearMatch = true;

            if (requirement.getPassingYear() != null) {

                if (qualification.getPassingYear() == null) {
                    passingYearMatch = false;
                } else {

                    /*
                     * Current rule:
                     *
                     * User passing year >= required passing year
                     *
                     * Example:
                     * Required = 2024
                     * User = 2024  -> eligible
                     * User = 2025  -> eligible
                     * User = 2023  -> not eligible
                     */

                    passingYearMatch =
                            qualification.getPassingYear()
                                    >= requirement.getPassingYear();
                }
            }

            if (!passingYearMatch) {
                continue;
            }

            // All qualification conditions matched
            return true;
        }

        return false;
    }

    private boolean meetsQualificationLevel(String actual, String required) {
        if (actual == null || required == null) {
            return false;
        }
        int actualLevel = qualificationLevel(actual);
        int requiredLevel = qualificationLevel(required);
        if (actualLevel >= 0 && requiredLevel >= 0) {
            return actualLevel >= requiredLevel;
        }
        return actual.trim().equalsIgnoreCase(required.trim());
    }

    private int qualificationLevel(String qualification) {
        String normalized = qualification.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]", "");
        if (normalized.equals("10TH") || normalized.equals("SSC")) {
            return 1;
        }
        if (normalized.equals("12TH") || normalized.equals("HSC")
                || normalized.equals("ITI") || normalized.equals("DIPLOMA")) {
            return 2;
        }
        if (List.of("BA", "BSC", "BCOM", "BCA", "BCS", "BE", "BTECH")
                .contains(normalized)) {
            return 3;
        }
        if (List.of("MA", "MSC", "MCOM", "MCA", "MTECH", "MBA")
                .contains(normalized)) {
            return 4;
        }
        if (normalized.equals("PHD") || normalized.equals("DOCTORATE")) {
            return 5;
        }
        return -1;
    }

    private boolean checkSchoolPercentages(
            User user,
            JobEligibility requirement) {
        return meetsMinimum(user.getTenthPercentage(),
                    requirement.getMinimumTenthPercentage())
                && meetsMinimum(user.getHscPercentage(),
                    requirement.getMinimumHscPercentage());
    }

    private boolean meetsMinimum(Double actual, Double minimum) {
        return minimum == null
                || (actual != null && actual >= minimum);
    }

    private boolean checkDisability(
            User user,
            JobEligibility requirement) {
        return requirement.getDisabilityRequired() == null
                || requirement.getDisabilityRequired()
                        .equals(user.getDisability());
    }

    private boolean matches(
            User user,
            List<Qualification> qualifications,
            JobEligibility requirement) {
        return checkAge(user, requirement)
                && checkCaste(user, requirement)
                && checkLocation(user, requirement)
                && checkQualification(user, qualifications, requirement)
                && checkSchoolPercentages(user, requirement)
                && checkDisability(user, requirement)
                && checkNationality(user, requirement)
                && checkExperience(user, requirement)
                && checkCertificate(user, requirement)
                && checkPhysicalStandards(user, requirement)
                && checkOtherConditions(user, requirement)
                && checkCategoryCertificate(user, requirement)
                && checkApplicationDeadline(requirement);
    }

    private Integer getCategoryMaximumAge(
            User user,
            JobEligibility requirement) {
        Map<String, Integer> categoryAgeLimits =
                requirement.getCategoryAgeLimits();
        if (categoryAgeLimits != null && !categoryAgeLimits.isEmpty()) {
            String category = hasText(user.getCategory())
                    ? user.getCategory()
                    : user.getCaste();
            String categoryKey = normalizeCategory(category);
            if (Boolean.TRUE.equals(user.getPwd()) && !categoryKey.isEmpty()) {
                Integer combinedLimit = findAgeLimit(
                        categoryAgeLimits,
                        categoryKey + "+PWD"
                );
                if (combinedLimit != null) {
                    return combinedLimit;
                }
            }
            if (!categoryKey.isEmpty()) {
                Integer categoryLimit = findAgeLimit(categoryAgeLimits, categoryKey);
                if (categoryLimit != null) {
                    return categoryLimit;
                }
            }
            if (Boolean.TRUE.equals(user.getPwd())) {
                Integer disabilityLimit = findAgeLimit(categoryAgeLimits, "PWD");
                if (disabilityLimit != null) {
                    return disabilityLimit;
                }
            }
        }
        return requirement.getMaximumAge();
    }

    private Integer findAgeLimit(Map<String, Integer> ageLimits, String key) {
        return ageLimits.entrySet().stream()
                .filter(entry -> normalizeCategory(entry.getKey()).equals(key))
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(age -> age >= 0)
                .findFirst()
                .orElse(null);
    }

    private String normalizeCategory(String category) {
        return category == null
                ? ""
                : category.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9+]", "");
    }

    private boolean checkNationality(User user, JobEligibility requirement) {
        return !hasText(requirement.getRequiredNationality())
                || sameText(requirement.getRequiredNationality(), user.getNationality());
    }

    private boolean checkExperience(User user, JobEligibility requirement) {
        return requirement.getMinimumExperienceYears() == null
                || (user.getWorkExperienceYears() != null
                && user.getWorkExperienceYears() >= requirement.getMinimumExperienceYears());
    }

    private boolean checkCertificate(User user, JobEligibility requirement) {
        if (!hasText(requirement.getRequiredCertificate())) {
            return true;
        }
        if (!hasText(user.getCertificatesAndLicences())) {
            return false;
        }
        return Arrays.stream(user.getCertificatesAndLicences().split("[,;\\n]"))
                .map(String::trim)
                .anyMatch(value -> sameText(value, requirement.getRequiredCertificate()));
    }

    private boolean checkPhysicalStandards(User user, JobEligibility requirement) {
        return isWithinRange(
                    user.getHeightCm(),
                    requirement.getMinimumHeightCm(),
                    requirement.getMaximumHeightCm())
                && isWithinRange(
                    user.getChestCm(),
                    requirement.getMinimumChestCm(),
                    requirement.getMaximumChestCm())
                && (!hasText(requirement.getRequiredFitnessStatus())
                || sameText(requirement.getRequiredFitnessStatus(),
                user.getPhysicalFitnessStatus()));
    }

    private boolean isWithinRange(Double actual, Double minimum, Double maximum) {
        return (minimum == null || actual != null && actual >= minimum)
                && (maximum == null || actual != null && actual <= maximum);
    }

    private boolean checkOtherConditions(User user, JobEligibility requirement) {
        return !hasText(requirement.getOtherConditions())
                || sameText(requirement.getOtherConditions(), user.getOtherEligibilityDetails());
    }

    private boolean checkCategoryCertificate(User user, JobEligibility requirement) {
        if (!Boolean.TRUE.equals(requirement.getValidCategoryCertificateRequired())) {
            return true;
        }
        String category = hasText(user.getCategory())
                ? user.getCategory()
                : user.getCaste();
        if (!hasText(category)
                || "GENERAL".equals(normalizeCategory(category))
                || "GENERALUR".equals(normalizeCategory(category))) {
            return true;
        }
        LocalDate validityDate = requirement.getApplicationEnd() == null
                ? LocalDate.now()
                : requirement.getApplicationEnd();
        boolean ews = "EWS".equalsIgnoreCase(category);
        String certificateNumber = ews
                ? user.getEwsCertificateNumber()
                : user.getCasteCertificateNumber();
        LocalDate validity = ews
                ? user.getEwsCertificateValidity()
                : user.getCasteCertificateValidity();
        return hasText(certificateNumber)
                && validity != null
                && !validity.isBefore(validityDate);
    }

    private boolean checkApplicationDeadline(JobEligibility requirement) {
        return requirement.getApplicationEnd() == null
                || !requirement.getApplicationEnd().isBefore(LocalDate.now());
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null
                && first.trim().equalsIgnoreCase(second.trim());
    }

    private List<JobEligibility> getRequirementsForJob(JOB job) {
        List<JobEligibility> requirements = new ArrayList<>(
                eligibilityRepo.findByJobId(job.getId())
        );
        requirements.removeIf(requirement ->
                !hasEligibilityCriteria(requirement)
        );

        JobEligibility jobRequirement = new JobEligibility();
        jobRequirement.setJobId(job.getId());
        jobRequirement.setRequiredQualification(job.getQualification());
        jobRequirement.setRequiredStream(job.getRequiredStream());
        jobRequirement.setMinimumPercentage(job.getMinimumPercentage());
        jobRequirement.setMinimumAge(job.getMinAge());
        jobRequirement.setMaximumAge(job.getMaxAge());
        jobRequirement.setAgeAsOfDate(job.getAgeAsOfDate());
        jobRequirement.setCategoryAgeLimits(job.getCategoryAgeLimits());
        jobRequirement.setCaste(job.getCaste());
        jobRequirement.setLocation(job.getLocation());
        jobRequirement.setRequiredNationality(job.getRequiredNationality());
        jobRequirement.setMinimumExperienceYears(job.getMinimumExperienceYears());
        jobRequirement.setRequiredCertificate(job.getRequiredCertificate());
        jobRequirement.setMinimumHeightCm(job.getMinimumHeightCm());
        jobRequirement.setMaximumHeightCm(job.getMaximumHeightCm());
        jobRequirement.setMinimumChestCm(job.getMinimumChestCm());
        jobRequirement.setMaximumChestCm(job.getMaximumChestCm());
        jobRequirement.setRequiredFitnessStatus(job.getRequiredFitnessStatus());
        jobRequirement.setOtherConditions(job.getOtherConditions());
        jobRequirement.setValidCategoryCertificateRequired(
                job.getValidCategoryCertificateRequired()
        );
        jobRequirement.setMinimumTenthPercentage(
                job.getMinimumTenthPercentage()
        );
        jobRequirement.setMinimumHscPercentage(
                job.getMinimumHscPercentage()
        );
        jobRequirement.setDisabilityRequired(job.getDisabilityRequired());
        jobRequirement.setApplicationEnd(job.getApplicationEnd());

        if (hasEligibilityCriteria(jobRequirement)) {
            requirements.add(jobRequirement);
        }

        return requirements;
    }

    private boolean hasEligibilityCriteria(JobEligibility requirement) {
        return requirement.getMinimumAge() != null
                || requirement.getMaximumAge() != null
                || (requirement.getCategoryAgeLimits() != null
                && !requirement.getCategoryAgeLimits().isEmpty())
                || hasText(requirement.getRequiredQualification())
                || hasText(requirement.getRequiredStream())
                || requirement.getMinimumPercentage() != null
                || hasText(requirement.getRequiredNationality())
                || requirement.getMinimumExperienceYears() != null
                || hasText(requirement.getRequiredCertificate())
                || requirement.getMinimumHeightCm() != null
                || requirement.getMaximumHeightCm() != null
                || requirement.getMinimumChestCm() != null
                || requirement.getMaximumChestCm() != null
                || hasText(requirement.getRequiredFitnessStatus())
                || hasText(requirement.getOtherConditions())
                || Boolean.TRUE.equals(requirement.getValidCategoryCertificateRequired())
                || requirement.getMinimumTenthPercentage() != null
                || requirement.getMinimumHscPercentage() != null
                || requirement.getDisabilityRequired() != null
                || requirement.getPassingYear() != null
                || hasText(requirement.getCaste())
                || hasText(requirement.getLocation());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // =========================================================
    // FIND ELIGIBLE JOBS FOR USER
    // =========================================================

    @Override
    public List<JOB> getEligibleJobs(Integer userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with id: " + userId
                        )
                );

        List<Qualification> qualifications =
                qualificationRepo.findByUserId(userId);

        List<JOB> allJobs = jobRepo.findAll();

        List<JOB> eligibleJobs = new ArrayList<>();

        for (JOB job : allJobs) {

            List<JobEligibility> requirements =
                    getRequirementsForJob(job);

            for (JobEligibility requirement : requirements) {
                if (matches(user, qualifications, requirement)) {
                    eligibleJobs.add(job);
                    break;
                }
            }
        }

        return eligibleJobs;
    }
}