package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserRemovalRequest {

    public String loginUser;
    public String loginRole;
    public String targetUser;

    // 无参构造函数
    public UserRemovalRequest() {
    }

    // 全参构造函数
    public UserRemovalRequest(String loginUser, String loginRole, String targetUser) {
        this.loginUser = loginUser;
        this.loginRole = loginRole;
        this.targetUser = targetUser;
    }
}
