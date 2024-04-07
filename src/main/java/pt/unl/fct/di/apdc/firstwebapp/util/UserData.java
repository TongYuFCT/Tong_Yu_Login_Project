package pt.unl.fct.di.apdc.firstwebapp.util;

import com.google.cloud.datastore.Value;

public class UserData {
	
	public String username;
	public String password;
	public String email;
	public String role;
	
	public UserData() {
		
	}
	
	public UserData(String username, String password,String email,String role) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
	}

	public String getUsername() {
		// TODO Auto-generated method stub
		return this.username;
	}

	public String getPassword() {
		// TODO Auto-generated method stub
		return this.password;
	}

	public String getEmail() {
		// TODO Auto-generated method stub
		return this.email;
	}
	
	
	
}
