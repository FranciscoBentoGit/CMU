package pt.tecnico.ulisboa.cmu.conversationalIST;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordStorage {
    // Simulates database of users!
    private HashMap<String, UserInfo> userDatabase = new HashMap<>();

    public String authenticateUser(String inputUser, String inputPass) throws Exception {
        UserInfo user = userDatabase.get(inputUser);
        System.out.println(user);
        if (user == null) {
            return "User does not exist.";
        } else {
            if (inputPass.equals(user.userPassword)) {
                return "Log in successful: " + user.userName;
            } else {
                return "Password does not match.";
            }
        }
    }

    public String signUp(String userName, String password) throws Exception {
        System.out.println(userDatabase);
        if (userDatabase.get(userName) != null)
            return "User already exists. Please choose another username.";
        else {
            UserInfo user = new UserInfo();
            user.userPassword = password;
            user.userName = userName;
            saveUser(user);
            return "Account created: " + user.userName;
        }
    }

    public void saveUser(UserInfo user) {
        userDatabase.put(user.userName, user);
    }
}
class UserInfo {
    String userPassword;
    String userName;
}
