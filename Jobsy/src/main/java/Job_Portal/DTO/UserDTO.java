package Job_Portal.DTO;


import lombok.Getter;

@Getter
public class UserDTO {


    private String name;
    private String email;
    private String mobile;
    private Integer age;
    private String qualification;
    private String caste;
    private String location;

    public UserDTO() {
    }

    public UserDTO(String name, String email, String mobile,
                   Integer age, String qualification, String caste,
                   String location) {

        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.age = age;
        this.qualification = qualification;
        this.caste = caste;
        this.location = location;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
