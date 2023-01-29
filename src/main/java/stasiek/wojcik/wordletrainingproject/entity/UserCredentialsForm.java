package stasiek.wojcik.wordletrainingproject.entity;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCredentialsForm {

    private String username;
    private String password;
}
