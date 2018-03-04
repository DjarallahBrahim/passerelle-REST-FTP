package car.tp2.rest.JWT;

import car.tp2.rest.JWT.utils.KeyGenerator;
import car.tp2.rest.JWT.utils.SimpleKeyGenerator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


import javax.inject.Inject;
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.TypedQuery;
//import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

/**
 * @author Djarallah Brahim
 *         --
 */
@Path("/users")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
//@Transactional
public class UserEndpoint {

    // ======================================
    // =          Injection Points          =
    // ======================================

    @Context
    private UriInfo uriInfo;


    private KeyGenerator keyGenerator;

//    @PersistenceContext
//    private EntityManager em;

    // ======================================
    // =          Business methods          =
    // ======================================

    @POST
    @Path("/login")
    @Consumes(APPLICATION_FORM_URLENCODED)
    public Response authenticateUser(@QueryParam("login") String login,
                                     @QueryParam("password") String password) {

        try {

            System.out.println("#### login/password : " + login + "/" + password);

            // Authenticate the user using the credentials provided
            authenticate(login, password);

            // Issue a token for the user
            String token = issueToken(login);

            // Return the token on the response
            return Response.ok().header(AUTHORIZATION, "Bearer " + token).build();

        } catch (Exception e) {
            return Response.status(UNAUTHORIZED).build();
        }
    }

    private void authenticate(String login, String password) {
        //TODO ADD USER DATA BASE !
    }

    private String issueToken(String login) {
        Key key = new SimpleKeyGenerator().generateKey();
        String jwtToken = Jwts.builder()
                .setSubject(login)
                .setIssuer(uriInfo.getAbsolutePath().toString())
                .setIssuedAt(new Date())
                .setExpiration(toDate(LocalDateTime.now().plusMinutes(1)))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();
        System.out.println("#### generating token for a key : " + jwtToken + " - " + key);
        return jwtToken;

    }


    // ======================================
    // =          Private methods           =
    // ======================================

    private Date toDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}

