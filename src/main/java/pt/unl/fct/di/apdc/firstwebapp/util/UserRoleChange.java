package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserRoleChange {
	
	public String loginUser;
	public String loginRole;
	public String newUser;
	public String newRole;
	
	public UserRoleChange() {
		
	}
	public UserRoleChange(String loginUser, String loginRole, String newUser,String newRole) {
		this.loginUser= loginUser;
		this.loginRole = loginRole;
		this.newUser = newUser;
		this.newRole = newRole;
	}

}
