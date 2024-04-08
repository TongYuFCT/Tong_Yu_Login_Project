package pt.unl.fct.di.apdc.firstwebapp.util;

public class PasswordChangeRequest {

    public String loginUser;
    public String currentPassword;
    public String newPassword;

   
    public PasswordChangeRequest() {
    }

    
    public PasswordChangeRequest(String loginUser, String currentPassword, String newPassword) {
        this.loginUser = loginUser;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
}
