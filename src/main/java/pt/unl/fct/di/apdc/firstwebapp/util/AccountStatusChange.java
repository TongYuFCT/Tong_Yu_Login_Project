package pt.unl.fct.di.apdc.firstwebapp.util;


public class AccountStatusChange {
  public String loginUser;
  public String loginRole;
  public String newUser;
  public String newStatus;

  public AccountStatusChange() { }

  public AccountStatusChange(String loginUser, String loginRole, String newUser, String newStatus) {
    this.loginUser = loginUser;
    this.loginRole = loginRole;
    this.newUser = newUser;
    this.newStatus = newStatus;
  }
}
