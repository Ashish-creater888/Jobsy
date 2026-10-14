package Job_Portal.Module;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Setter
@Getter
@Entity
public class JOB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 2000)
    private String title;

    private String department;

    private String description;

    // Job requirements
    private String qualification;

    private String requiredStream;

    private Double minimumPercentage;

    private Integer minAge;

    private Integer maxAge;

    private LocalDate ageAsOfDate;

    @Convert(converter = CategoryAgeLimitsConverter.class)
    @Column(length = 2000)
    private Map<String, Integer> categoryAgeLimits;

    private String caste;

    private String location;

    private String requiredNationality;

    private Double minimumExperienceYears;

    private String requiredCertificate;

    private Double minimumHeightCm;

    private Double maximumHeightCm;

    private Double minimumChestCm;

    private Double maximumChestCm;

    private String requiredFitnessStatus;

    @Column(length = 1000)
    private String otherConditions;

    private Boolean validCategoryCertificateRequired;

    private Double minimumTenthPercentage;

    private Double minimumHscPercentage;

    private Boolean disabilityRequired;

    private LocalDate applicationStart;

    private LocalDate applicationEnd;

    private String officialUrl;

    private String notificationId;

    public JOB() {
    }
}