package Job_Portal.Module;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private String mobile;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ToString.Exclude
    private String password;
    private Integer age;
    private String qualification;
    private String caste;
    private String location;

    private LocalDate dateOfBirth;
    private String gender;
    private String fatherName;
    private String motherName;
    private String nationality;
    private String state;
    private String district;
    private String pincode;

    @Column(length = 2048)
    private String profilePhoto;

    private String category;
    private String obcType;
    private String casteName;
    private String casteCertificateNumber;
    private LocalDate casteCertificateIssueDate;
    private LocalDate casteCertificateValidity;
    private String ewsCertificateNumber;
    private LocalDate ewsCertificateValidity;
    private Boolean pwd;
    private String disabilityType;
    private Double disabilityPercentage;
    private String disabilityCertificateNumber;
    private Boolean exServiceman;
    @Column(length = 1000)
    private String exServicemanServiceDetails;
    private Boolean sportsQuota;
    private Boolean freedomFighterDependent;
    private Boolean governmentEmployeeStatus;

    @Column(length = 1000)
    private String address;
    private Double tenthPercentage;
    private Double hscPercentage;
    private Boolean disability;
    private Double workExperienceYears;
    @Column(length = 2000)
    private String certificatesAndLicences;
    private Double heightCm;
    private Double chestCm;
    private String physicalFitnessStatus;
    @Column(length = 1000)
    private String otherEligibilityDetails;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Qualification> qualifications = new ArrayList<>();

}
