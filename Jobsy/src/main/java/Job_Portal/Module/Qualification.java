package Job_Portal.Module;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Qualification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;

    private String qualification;

    private String degreeName;

    private String stream;

    private String universityBoard;

    private String collegeInstitute;

    private Double percentage;

    private Double cgpa;

    private Double totalMarks;

    private Double obtainedMarks;

    private String resultStatus;

    private Integer passingYear;

    public Qualification() {
    }
}