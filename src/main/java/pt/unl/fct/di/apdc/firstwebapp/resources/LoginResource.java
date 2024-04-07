package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.AuthToken;
import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;


import com.google.gson.Gson;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;



@Path("/api/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private final Gson g = new Gson();
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	public LoginResource() {}
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogin(LoginData data) {
    	LOG.fine("Login attempt by user: " + data.username);
    	if(validateUser(data.getUsername(), data.getPassword())) { 
			AuthToken at = new AuthToken(data.username);
				return Response.ok(g.toJson(at)).build();
		}return Response.status(Status.FORBIDDEN).entity("Incorrect username or password.").build();
    }
    
    // tem de existir Key Kind User
    private boolean validateUser(String username, String password) {
        KeyFactory keyFactory = datastore.newKeyFactory().setKind("User");
        Key key = keyFactory.newKey(username);
        Entity user = datastore.get(key);
        return user != null && user.getString("password").equals(password);
    }
}
