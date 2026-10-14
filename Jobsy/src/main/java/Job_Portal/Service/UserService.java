package Job_Portal.Service;

import Job_Portal.DTO.Login;
import Job_Portal.Module.User;

public interface  UserService {

    User Save(User user);
    User Login(Login loginDTO);
}


