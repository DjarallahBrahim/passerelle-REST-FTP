package car.tp2.rest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.ValidatableResponse;
import org.glassfish.grizzly.http.server.HttpServer;

import org.glassfish.grizzly.http.server.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static com.jayway.restassured.RestAssured.basic;
import static com.jayway.restassured.RestAssured.get;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MyResourceTest {

    private HttpServer server;
    private WebTarget target;

    private String username;
    private String password;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);


        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 9988;
        //RestAssured.authentication = basic("romain","");

        this.username = "brahimftp";
        this.password = "sam1";
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testCommandList()
    {
        System.out.println("------------ TEST LIST COMMAND ------------");
        // Test without authentification
        InputStream stream = RestAssured.get("/myapp/myresource/listHt/").asInputStream();
        assertEquals("Requires HTTP authentication!",getStringFromInputStream(stream));
        RestAssured.get("/myapp/myresource/listHt/").then().statusCode(401);

        // Test invalid authentification
//
//        com.jayway.restassured.response.Response response = RestAssured.given().auth().preemptive().basic("brahimftp", "mnnf").when().get("/myapp/myresource/listHt/");
//        stream = response.asInputStream();
//        assertEquals("<!DOCTYPE html><html><head></head><body><h1></h1>\t<h1>Error</h1>\t<p>Unable to determine system type - response: 530 Please login with USER and PASS.</p></body></html>",getStringFromInputStream(stream));


        // Test with authentification
        RestAssured.given().auth().preemptive().basic(this.username, this.password).when().get("/myapp/myresource/listHt/").then().statusCode(200);
        stream = RestAssured.given().auth().preemptive().basic(this.username, this.password).when().get("/myapp/myresource/listHt/").asInputStream();
        //TODO : assertEquals("LIST", stream);

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testMkdCommand()
    {
        System.out.println("------------ TEST MKD COMMAND ------------");
        // Without authentification
        RestAssured.put("/myapp/myresource/mkd/NewFolder/").then().statusCode(401);
        InputStream stream = RestAssured.put("/myapp/myresource/mkd/NewFolder/").asInputStream();
        assertEquals("Requires HTTP authentication!",getStringFromInputStream(stream));

        // With authentification
        // First mkdir NewFolder
        com.jayway.restassured.response.Response response = RestAssured.given().auth().preemptive().basic(this.username, this.password).when().put("/myapp/myresource/mkd/NewFolder/");

        assertEquals(200, response.getStatusCode());
        assertEquals("HTTP/1.1 200 OK", response.getStatusLine());

        stream = response.asInputStream();
        assertEquals("Result: File created", getStringFromInputStream(stream));

        // Second mkdir NewFolder (error because dir already exists)
        response = RestAssured.given().auth().preemptive().basic(this.username, this.password).when().put("/myapp/myresource/mkd/NewFolder/");
        stream = response.asInputStream();
        assertEquals("Result: File doesn't created", getStringFromInputStream(stream));

        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        System.out.println(statusLine);
    }


    @Test
    public void testMydeleteCommand()
    {
        System.out.println("------------ TEST DELETE COMMAND ------------");
        // Without authentification
        RestAssured.delete("/myapp/myresource/delete/NewFolder/").then().statusCode(401);
        InputStream stream = RestAssured.delete("/myapp/myresource/delete/NewFolder/").asInputStream();
        assertEquals("Requires HTTP authentication!",getStringFromInputStream(stream));

        // With authentification

        com.jayway.restassured.response.Response response = RestAssured.given().auth().preemptive().basic(this.username, this.password).when().delete("/myapp/myresource/delete/NewFolder/");
        assertEquals(200, response.getStatusCode());
        assertEquals("HTTP/1.1 200 OK", response.getStatusLine());

        stream = response.asInputStream();
        assertEquals("Result: File deleted", getStringFromInputStream(stream));

        response = RestAssured.given().auth().preemptive().basic(this.username, this.password).when().delete("/myapp/myresource/delete/NewFolder/");
        stream = response.asInputStream();
        assertEquals("Result: File doesn't delete", getStringFromInputStream(stream));

    }


    @AfterClass
    public static void afterClass()
    {
       assertTrue(1==1);
    }

    // convert InputStream to String
    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }
}
