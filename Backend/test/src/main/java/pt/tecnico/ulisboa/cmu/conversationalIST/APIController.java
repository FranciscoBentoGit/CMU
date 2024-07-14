package pt.tecnico.ulisboa.cmu.conversationalIST;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class APIController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    private Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }

    @GetMapping("/signup")
    private String signUp(@RequestParam(value = "username") String username, @RequestParam(value = "passwd") String password) throws Exception {
        return new PasswordStorage().signUp(username, password);
    }

    @GetMapping("/auth")
    private String authenticateUser(@RequestParam(value = "username") String username, @RequestParam(value = "passwd") String password) throws Exception {
        return new PasswordStorage().authenticateUser(username, password);
    }

}
