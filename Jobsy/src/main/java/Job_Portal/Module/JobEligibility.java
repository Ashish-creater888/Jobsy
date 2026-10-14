package Job_Portal.Module;

import jakarta.persistence.Entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@Entity
public class JobEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer jobId;

    private Integer minimumAge;
    private Integer maximumAge;

    private LocalDate ageAsOfDate;

    private LocalDate applicationEnd;

    @Convert(converter = CategoryAgeLimitsConverter.class)
    @Column(length = 2000)
    private java.util.Map<String, Integer> categoryAgeLimits;

    private String requiredQualification;
    private String requiredStream;

    private Double minimumPercentage;

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

    private Integer passingYear;

    private String caste;

    private String location;

    public JobEligibility() {
    }

}
