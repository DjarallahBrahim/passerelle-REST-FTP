package car.tp2.rest;


import car.tp2.rest.clientFtp.ClientFtp;
import org.apache.commons.net.ftp.FTPFile;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {
    /**
     * get annotation frome header
     */
    @Context
    private HttpHeaders headers;

    /**
     * get information about url
     */
    @Context
    private UriInfo context;
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!\n";
    }


    /**
     * Method handling HTTP GET requests for Listing directory. The returned object will be sent
     * to the client as "text/html" //TODO type of text/html.
     * @param path
     * @return
     * @throws IOException
     */
    @GET
    @Path("/myget/{Path:.*}")
    @Produces("text/html")
    public Response list(@PathParam("Path") String path) throws IOException {

        System.out.println("1");
        if (headers.getRequestHeader("authorization") == null) {
            Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Requires HTTP authentication!");
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Client did not use HTTP Authentification ");
            return response.build();
        }

        final String crepted = headers.getRequestHeader("authorization").get(0).substring("Basic ".length());

        DecodeBasicAuthenticator basicAuthenticator = new DecodeBasicAuthenticator(crepted);

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();

        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        FTPFile[] files = null;
        String htmlRespens = "<!DOCTYPE html>\n";
        htmlRespens += "<html>\n";
        htmlRespens += "<head>\n";
        htmlRespens += "</head>\n<body>\n";
        htmlRespens += "<h1>" + "</h1>\n";
        try {
            if (!client.authenticate()) {
                Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Wrong user name and password!");
                Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Wrong user name and password!");
                return response.header("Wrong user name and password!", "Wrong user name and password!").build();
            }
            files = client.list(path);
            System.out.println(path);
        } catch (IOException ex) {
            htmlRespens += "\t<h1>Error</h1>\n";
            htmlRespens += "\t<p>" + ex.getMessage() + "</p>\n";
            htmlRespens += "</body>\n";
            htmlRespens += "</html>\n";
            Response.ResponseBuilder response = Response.ok(htmlRespens, MediaType.TEXT_HTML);
            return response.build();
        } finally {
            client.close();
        }
        htmlRespens += "\t<h1>LIST " + path + "</h1>\n";
        for (FTPFile file : files) {
            htmlRespens += "\t\t\t<p>\n";
            if (file.isDirectory()) {
                htmlRespens += "<a";
                htmlRespens += " title = \"" + file.getName() + "\"";
                htmlRespens += " href = \"";
                if (path != null) {
                    htmlRespens += context.getAbsolutePath().getPath() + "/";
                    System.out.println("context = "+context.getAbsolutePath().getPath());
                }
                htmlRespens += file.getName() + "\">" + file.getName() + "</a>";
            } else {
                htmlRespens += "<a";
                htmlRespens += " title = \"" + file.getName() + "\"";
                htmlRespens += " href = \"/REST/api/file/";
                htmlRespens += file.getName() + "\">" + file.getName() + "</a>";
            }
            htmlRespens += "</p>\n";
            htmlRespens += "\t\t\t</p>\n";
        }
        htmlRespens += "</body>\n";
        htmlRespens += "</html>\n";
        Response.ResponseBuilder response = Response.ok(htmlRespens, MediaType.TEXT_HTML).status(Response.Status.OK);
        return response.build();
    }


    /**
     * Method handling HTTP GET requests for Deleting directory/file. The returned object will be sent
     * to the client as "TEXT_PLAIN" of MediaType type
     * @param fileName
     * @return
     * @throws IOException
     */
    @DELETE
    @Path("/delete/{param:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response mydelete(@PathParam("param") String fileName) throws IOException {
        System.out.println("2");
        if (headers.getRequestHeader("authorization") == null) {
            Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Requires HTTP authentication!");
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Client did not use HTTP Authentification ");
            return response.header("Www-authenticate", "Basic realm=\"rest\"").build();
        }

        final String crepted = headers.getRequestHeader("authorization").get(0).substring("Basic ".length());

        DecodeBasicAuthenticator basicAuthenticator = new DecodeBasicAuthenticator(crepted);

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Wrong user name and password!");
                Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Wrong user name and password!");
                return response.header(" ", " ").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

            String result = "File doesn't delete";
           if(client.delete(fileName)){
                result ="File deleted";
           }

            client.close();

        Response.ResponseBuilder response = Response.ok("Result: "+result, MediaType.TEXT_HTML).status(Response.Status.OK);
        return response.build();
        }

    /**
     * Method handling HTTP GET requests for Creating directory. The returned object will be sent
     * to the client as "TEXT_PLAIN" of MediaType type
     * @param fileName
     * @return
     * @throws IOException
     */
    @PUT
    @Path("mkd/{param:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response myMkd(@PathParam("param") String fileName) throws IOException {
        System.out.println("3");
        if (headers.getRequestHeader("authorization") == null) {
            Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Requires HTTP authentication!");
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Client did not use HTTP Authentification ");
            return response.header("Www-authenticate", "Basic realm=\"rest\"").build();
        }

        final String crepted = headers.getRequestHeader("authorization").get(0).substring("Basic ".length());

        DecodeBasicAuthenticator basicAuthenticator = new DecodeBasicAuthenticator(crepted);

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity("Wrong user name and password!");
                Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"Wrong user name and password!");
                return response.header(" ", " ").build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = "File doesn't created";
        if(client.mkd(fileName)){
            result ="File created";
        }

        client.close();

        Response.ResponseBuilder response = Response.ok("Result: "+result, MediaType.TEXT_HTML).status(Response.Status.OK);
        return response.build();

    }
}
