package Job_Portal.Module;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jdk.jfr.Category;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class JobRequerement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Long JobID;

    private  int minimumAge;

     private  float MaximumPercentage ;

     private String Qualification;

     private String Category;

     private String State ;


}
