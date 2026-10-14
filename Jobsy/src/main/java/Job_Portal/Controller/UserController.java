package Job_Portal.Controller;

import Job_Portal.DTO.Login;
import Job_Portal.Module.User;
import Job_Portal.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
@Tag(name = "Users", description = "Register and authenticate Jobsy users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Register a user profile")
    public User saveUser(@RequestBody User user) {
        return userService.Save(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in a user")
    public User login(@RequestBody Login loginDTO) {
        return userService.Login(loginDTO);
    }
}