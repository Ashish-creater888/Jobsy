package Job_Portal.Controller;



import Job_Portal.Service.Email_Service;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final Email_Service emailService;

    public EmailController(Email_Service emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public String sendEmail(@RequestParam String to) {

        emailService.sendEmail(
                to,
                "Jobsy - New Government Job",
                "Hello,\n\n" +
                        "A new government job matching your profile is available.\n\n" +
                        "Regards,\n" +
                        "Jobsy"
        );

        return "Email sent successfully";
    }
}