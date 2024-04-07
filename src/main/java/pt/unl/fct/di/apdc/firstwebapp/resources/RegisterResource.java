package pt.unl.fct.di.apdc.firstwebapp.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import pt.unl.fct.di.apdc.firstwebapp.util.UserData;

@Path("/api/register")
public class RegisterResource {

    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    public RegisterResource() {}
    
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doRegister(UserData data) {
    	if (data.getUsername() == null || data.getUsername().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Username is required\"}").build();
        }
    	if (data.getPassword() == null || data.getPassword().length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Password must be at least 8 characters long\"}").build();
        }
        if (data.getEmail() == null || !data.getEmail().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\": \"Invalid email address\"}").build();
        }

       //verifica o user
        Key usernameKey = userKeyFactory.newKey(data.getUsername());
        if (datastore.get(usernameKey) != null) {
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"Username already exists\"}").build();
        }
        
        // verifica o email
        Query<Entity> emailQuery = Query.newEntityQueryBuilder()
            .setKind("User")
            .setFilter(com.google.cloud.datastore.StructuredQuery.PropertyFilter.eq("email", data.getEmail()))
            .build();
        QueryResults<Entity> emailResults = datastore.run(emailQuery);
        if (emailResults.hasNext()) {
            return Response.status(Response.Status.CONFLICT).entity("{\"error\": \"Email already exists\"}").build();
        }

        // cira nobo entidade e guarda
        Entity newUser = Entity.newBuilder(usernameKey)
            .set("username", data.getUsername())
            .set("password", data.getPassword()) 
            .set("email", data.getEmail())
            .set("role", "USER") // default User
            .build();

        datastore.put(newUser);

        // registado
        return Response.status(Response.Status.CREATED).entity("{\"message\": \"User registered successfully\"}").build();
    }
}

