package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserRepository repository;

    UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/user_alive")
    String user_alive() {
        return "user_alive";
    }

    @GetMapping("/users")
    List<User> all() {
        return repository.findAll();
    }

    @GetMapping("/users/create")
    String createUser(@RequestParam(value = "username") String username, @RequestParam(value = "passwd") String password) {
        if (repository.findUserByName(username) != null)
            return "Error: account already exist.";
        if (username.equals("") || password.equals(""))
            return "Error: invalid username or password.";

        User newUser = new User(username, password);
        repository.save(newUser);
        return "Account created: " + newUser.getUsername();
    }

    @GetMapping("/users/{username}")
    User findUser(@PathVariable String username) {
        return repository.findUserByName(username);
    }

    @GetMapping("/users/login")
    String login(@RequestParam(value = "username") String username, @RequestParam(value = "passwd") String password) {
        User user = repository.findUserByName(username);
        if (user == null)
            return "Error: account does not exist.";
        if (!user.getPasswd().equals(password))
            return "Error: wrong password.";
        return "Successful login";
    }

    @GetMapping("/users/delete/{username}")
    String deleteUser(@PathVariable String username) {
        if (repository.findUserByName(username) == null)
            return "Error: account does not exist.";
        repository.deleteUserByName(username);
        return "Account deleted successfully.";
    }

    @GetMapping("/users/upgrade")
    String upgradeAccount(@RequestParam(value = "username") String username, @RequestParam(value = "passwd") String password) {
        if (repository.findUserByName(username) == null)
            return "Error: account does not exist.";
        if (username.equals("") || password.equals(""))
            return "Error: invalid password.";
        if (repository.modifyPassword(username, password) == null){
            return "Error: login not Successful";
        }
        return "Successful modified";
    }
}
