package pt.unl.fct.di.apdc.firstwebapp.util;

public class PasswordChangeRequest {

    public String loginUser;
    public String newPassword;

   
    public PasswordChangeRequest() {
    }

    
    public PasswordChangeRequest(String loginUser, String newPassword) {
        this.loginUser = loginUser;
        this.newPassword = newPassword;
    }
}
