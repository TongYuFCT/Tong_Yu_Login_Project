package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserAttributeUpdateRequest {
    public String loginUser;
    public String loginRole;
    public String targetUser;
    public String telefone;
    public String estado;
    public String role;

    public UserAttributeUpdateRequest(){
    	
    }
    
    public UserAttributeUpdateRequest(String loginUser, String loginRole, String targetUser, String telefone, String estado, String role) {
    	this.loginUser = loginUser;
    	this.loginRole = loginRole;
    	this.targetUser = targetUser;
    	this.telefone = telefone;
    	this.estado = estado;
    	this.role = role;
    	
    }
}
