package pt.unl.fct.di.apdc.firstwebapp.resources;


import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import pt.unl.fct.di.apdc.firstwebapp.util.LoginData;
import pt.unl.fct.di.apdc.firstwebapp.util.SignatureUtils;

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
     
    
}
