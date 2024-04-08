package pt.unl.fct.di.apdc.firstwebapp.util;


public class UserData {
	
	public String username;
	public String password;
	public String email;
	public String nome;
	public String telefone;
	
	public UserData() {
		
	}
	
	public UserData(String username, String password,String email,String nome,String telefone) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.nome = nome;
		this.telefone = telefone;
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
	
	public String getNome() {
		return this.nome;
	}
	public String getTelefone() {
		return this.telefone;
	}
	
	
	
}
