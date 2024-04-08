package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.AccountStatusChange;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.PasswordChangeRequest;
import pt.unl.fct.di.apdc.firstwebapp.util.SignatureUtils;
import pt.unl.fct.di.apdc.firstwebapp.util.UserAttributeUpdateRequest;
import pt.unl.fct.di.apdc.firstwebapp.util.UserRemovalRequest;
import pt.unl.fct.di.apdc.firstwebapp.util.UserRoleChange;

import com.google.gson.Gson;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.core.NewCookie;



@Path("/api")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	KeyFactory keyFactory = datastore.newKeyFactory().setKind("User");
	private static final String key = "my_secret_key";
	public LoginResource() {}
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data) {
    	LOG.fine("Login attempt by user: " + data.getUsername());

        if (!validateUser(data.getUsername(), data.getPassword())) {
            return Response.status(Response.Status.FORBIDDEN).entity(g.toJson("Incorrect username or password.")).build();
        }

        // Simulate getting user role from Datastore
        Key databaseKey = keyFactory.newKey(data.getUsername());
        Entity user = datastore.get(databaseKey);
        String userRole = user.getString("role");

        String id = UUID.randomUUID().toString();
        long currentTime = System.currentTimeMillis();
        String fields = data.username + "." + id + "." + userRole + "." + currentTime + "." + (1000 * 60 * 60 * 2);

        String signature = SignatureUtils.calculateHMac(key, fields);

        if (signature == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(g.toJson("Error while signing token.")).build();
        }

        String tokenValue = fields + "." + signature;
        NewCookie cookie = new NewCookie("session::apdc", tokenValue, "/", null, "comment", 1000 * 60 * 60 * 2, false, true);
        Map<String, String> responseData = new HashMap<>();
        responseData.put("username", data.getUsername());
        responseData.put("role", userRole);
        responseData.put("token", tokenValue);
        return Response.ok(g.toJson(responseData)).cookie(cookie).build();
    }
    
    // tem de existir Key Kind User
    private boolean validateUser(String username, String password) {
        Key key = keyFactory.newKey(username);
        Entity user = datastore.get(key);
        return user != null && user.getString("password").equals(password) && user.getString("estado").equals("ATIVO");
    }
     
    
    
    @POST
    @Path("/changeUserRole")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeUserRole(UserRoleChange user) {
    	int userRoleLevel = convertRole(user.loginRole);
        Key key = keyFactory.newKey(user.newUser);
        Entity newUser = datastore.get(key);
       
        if(newUser != null) {
	        if(userRoleLevel <= 2) {
	            String errorMessage = userRoleLevel == 1 ? "USER: não pode mudar roles" : "GBO: não pode mudar roles";
	            return Response.status(Response.Status.FORBIDDEN).entity(errorMessage).build();
	        }else{	
	            int newUserRoleLevel = convertRole(newUser.getString("role"));
	            if((newUserRoleLevel>= 3 || convertRole(user.newRole)>=3 )&& userRoleLevel==3) {
	            	String errorMessage = "GA: só pode alterar roles entre USER e GBO.";
	            	return Response.status(Response.Status.FORBIDDEN).entity(errorMessage).build();
	            }
	            
	            Entity updatedUser = Entity.newBuilder(newUser)
	                    .set("role", user.newRole)
	                    .build();

	            datastore.update(updatedUser);

	            return Response.ok().entity("Role updated successfully.").build();
	        
	        }
	        
        }else {
        	return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build();
        }
        	
    	
    }
    @POST
    @Path("/changeAccountStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response changeAccountStatus(AccountStatusChange changeRequest) {
    	int userRoleLevel = convertRole(changeRequest.loginRole);
        Key key = keyFactory.newKey(changeRequest.newUser);
        Entity newUser = datastore.get(key);
        if(newUser != null) { 
        	if (userRoleLevel == 1) {
                return Response.status(Response.Status.FORBIDDEN).entity("USER cannot change account statuses.").build();
            }else if (userRoleLevel == 2) {
                int targetUserRoleLevel = convertRole(newUser.getString("role"));
                if (targetUserRoleLevel == 1) { // USER
                    return updateAccountStatus(key, changeRequest.newStatus);
                } else {
                    return Response.status(Response.Status.FORBIDDEN).entity("GBO can only change the status of USER accounts.").build();
                }
            }else if (userRoleLevel == 3) {
                int targetUserRoleLevel = convertRole(newUser.getString("role"));
                if (targetUserRoleLevel <= 2) { // USER 和 GBO
                    return updateAccountStatus(key, changeRequest.newStatus);
                } else {
                    return Response.status(Response.Status.FORBIDDEN).entity("GA cannot change the status of SU or GA accounts.").build();
                }
            }
                
        	return updateAccountStatus(key, changeRequest.newStatus);
         
        	
        	
        }else {
        	return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build();
        }
        
    }
    
    @POST
    @Path("/removeUser")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response removeUser(UserRemovalRequest request) {
    	int userRoleLevel = convertRole(request.loginRole);
    	Key key = keyFactory.newKey(request.targetUser);
        Entity targetUserEntity  = datastore.get(key);
        if(targetUserEntity  != null) { 
        	if(userRoleLevel == 4) {
                datastore.delete(key);
                return Response.ok().entity("User successfully removed.").build();
            }  else if(userRoleLevel == 3) {
                String targetUserRole = targetUserEntity.getString("role");
                if("GBO".equals(targetUserRole) || "USER".equals(targetUserRole)) {
                    datastore.delete(key);
                    return Response.ok().entity("User successfully removed.").build();
                } else {
                    return Response.status(Response.Status.FORBIDDEN).entity("GA can only remove GBO or USER roles.").build();
                }
            }else if(userRoleLevel == 1 && request.loginUser.equals(request.targetUser)) {
               
                    datastore.delete(key);
                    return Response.ok().entity("User successfully removed.").build();
            }
        	return Response.status(Response.Status.FORBIDDEN).entity("GBO cannot remove users, User can only remove itself.").build();
        
        }else {
        	return Response.status(Response.Status.BAD_REQUEST).entity("User does not exist.").build();
        }
    }
    @POST
    @Path("/changePassword")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(PasswordChangeRequest request) {
    	Key key = keyFactory.newKey(request.loginUser);
        Entity user  = datastore.get(key);
        
        boolean isCurrentPasswordCorrect = user.getString("password").equals(request.currentPassword);
        if (!isCurrentPasswordCorrect) {
            return Response.status(Response.Status.FORBIDDEN).entity("Current password is incorrect.").build();
        }

        Entity updatedUser = Entity.newBuilder(user)
                .set("password",request.newPassword)
                .build();

        datastore.update(updatedUser);
        return Response.ok().entity("Password changed successfully.").build();
    }
    
    @POST
    @Path("/modifyUserAttributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modifyUserAttributes(UserAttributeUpdateRequest request) {
    	Key key = keyFactory.newKey(request.loginUser);
        Entity user  = datastore.get(key);
        if(request.loginRole.equals("USER")) {
        	if(request.loginUser.equals(request.targetUser) && request.telefone != null) {
        		Entity updatedUser = Entity.newBuilder(user)
                        .set("telefone",request.telefone)
                        .build();

                datastore.update(updatedUser);
                return Response.ok().entity("telefone changed successfully.").build();
        	}else {
        		return Response.status(Response.Status.FORBIDDEN).entity("An error occurred while updating user attributes. ").build();
        	}
        }

    	
        if (!isAuthorizedToUpdate(request.loginUser, request.loginRole, request.targetUser, request.role)) {
            return Response.status(Response.Status.FORBIDDEN).entity("You do not have permission to perform this operation.").build();
        }

        boolean updateResult = updateUserAttributes(request.targetUser, request.telefone, request.estado, request.role);
        
        if (updateResult) {
            return Response.ok().entity("User attributes updated successfully.").build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("An error occurred while updating user attributes.").build();
        }
    }
    
    @Path("/logout")
    public Response logoutUser() {
        
        NewCookie expiredCookie = new NewCookie("session::apdc", null, "/", null, null, 0, false);
        
        return Response.ok("Logged out successfully").cookie(expiredCookie).build();
    }

    private boolean isAuthorizedToUpdate(String loginUser, String loginRole, String targetUser, String newRole) {
    	Key key = keyFactory.newKey(loginUser);
        Entity user  = datastore.get(key);
        String targetRole = user.getString("role");
        int targetLevel = convertRole(targetRole);
    	if(loginUser.equals("GBO") ) {
        	if(targetRole.equals("USER") && convertRole(newRole)<2) {
        		return true;
        	}else {
        		return false;
        	}
        }else if(loginUser.equals("GA") ) {
        	if(targetLevel < 3 && convertRole(newRole)<3) {
        		return true;
        	}else {
        		return false;
        	}
        }    	
    	return "SU".equals(loginRole);
    }

    private boolean updateUserAttributes(String targetUser, String telefone, String estado, String role) {
    	Key key = keyFactory.newKey(targetUser);
        Entity user  = datastore.get(key);
        if(telefone != null) {
        	Entity updatedUser = Entity.newBuilder(user)
                    .set("telefone",telefone)
                    .build();

            datastore.update(updatedUser);
        }
        
        if(estado != null) {
        	Entity updatedUser = Entity.newBuilder(user)
                    .set("estado",estado)
                    .build();

            datastore.update(updatedUser);
        }
        
        if(role != null) {
        	Entity updatedUser = Entity.newBuilder(user)
                    .set("role",role)
                    .build();

            datastore.update(updatedUser);
        }
        
        return true;
    }
    
    private Response updateAccountStatus(Key key, String newStatus) {
        Entity updatedUser = Entity.newBuilder(key)
            .set("estado", newStatus)
            .build();
        datastore.update(updatedUser);
        return Response.ok().entity("Account status updated successfully.").build();
    }
    
    
    
    
    private static int convertRole(String role) {
		int result = 0;
		
		switch(role) {
			case "USER":
				result = 1;
				break;
			case "GBO":
				result = 2;
				break;
			case "GA":
				result = 3;
				break;
			case "SU":
				result = 4;
				break;
			default:
				result = 0;
				break;
		}
		return result;
	}
}
