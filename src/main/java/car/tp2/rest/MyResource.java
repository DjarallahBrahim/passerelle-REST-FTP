package car.tp2.rest;


import car.tp2.rest.clientFtp.ClientFtp;
import org.apache.commons.net.ftp.FTPFile;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * get annotation from header
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


    @POST
    @Path("/login")
    public String login(@FormParam("username") String username, @FormParam("password") String password)
    {

        //return username + " -- " + password;

       return HtmlScript.LOGIN_SCRIPT;

    }


    /**
     * Method handling HTTP GET requests for Listing directory. The returned object will be sent
     * to the client as "text/html" //TODO type of text/html.
     * @param path
     * @return
     * @throws IOException
     */
    @GET
    @Path("/listHt/{Path:.*}")
    @Produces("text/html")
    public Response list(@PathParam("Path") String path) throws IOException {

        System.out.println("1");
        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();


        ClientFtp client = new ClientFtp("localhost", 21, username, password);
        System.out.println(username);
        System.out.println(password);


        FTPFile[] files = null;
        String htmlRespens = "<!DOCTYPE html>\n";
        htmlRespens += "<html>\n";
        htmlRespens += "<head>\n";
        htmlRespens += HtmlScript.LINK_CSS;
        htmlRespens += "</head>\n<body>\n";
        htmlRespens += "<h1>" + "</h1>\n";
        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
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
     * Method handling HTTP GET requests for Listing directory. The returned object will be sent
     * to the client as "APPLICATION_JSON" //TODO type of text/html.
     * @param path
     * @return
     * @throws IOException
     */
    @GET
    @Path("/listJs/{Path:.*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listJson(@PathParam("Path") String path) throws IOException {

        System.out.println("1");
        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();

        ClientFtp client = new ClientFtp("localhost", 21, username,password);

        FTPFile[] files = null;
        String JsonRespens = "[";

        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
            }
            files = client.list(path);
            System.out.println(path);
        } catch (IOException ex) {
            JsonRespens += "An error occured]";
            Response.ResponseBuilder response = Response.ok(JsonRespens, MediaType.TEXT_HTML);
            return response.build();
        } finally {
            client.close();
        }

        for (FTPFile file : files) {
            JsonRespens += "{";
            JsonRespens += "\"name\" : " +"\""+ file.getName() +"\"" + ", ";
            JsonRespens += "\"size\" :" +"\""+ file.getSize() +"\""+ ", ";
            JsonRespens += "\"type\" : " + "\""+(file.isDirectory() ? "directory" : "file")+"\"";
            JsonRespens += " }, ";


        }

        JsonRespens += "]";
        JsonRespens = JsonRespens.substring(0,JsonRespens.length()-4)+"}]";
        System.out.println(JsonRespens);
        Response.ResponseBuilder response = Response.status(Response.Status.OK)
                .entity(JsonRespens);
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
        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

            String result = APIMessage.FILE_NOT_DELETED;
           if(client.delete(fileName)){
                result = APIMessage.FILE_DELETED;
           }

            client.close();

        Response.ResponseBuilder response = Response.ok("Result: " + result, MediaType.TEXT_HTML).status(Response.Status.OK);
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
        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = APIMessage.FILE_NOT_CREATED;
        if(client.mkd(fileName)){
            result = APIMessage.FILE_CREATED;
        }

        client.close();

        Response.ResponseBuilder response = Response.ok("Result: " + result, MediaType.TEXT_HTML).status(Response.Status.OK);
        return response.build();
    }

    @PUT
    @Consumes({MediaType.MULTIPART_FORM_DATA, MediaType.APPLICATION_OCTET_STREAM})
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/file/{path:.*}")
    /**
     * Creates or overwrites a file on the server.
     *
     * @param pathFile Path of the file
     * @param received file input stream
     */
    public Response stor(@PathParam("path") String pathFile , InputStream received) throws IOException {
        System.out.println("4");

        if (pathFile == null || pathFile.isEmpty()) {
            Response.ResponseBuilder response = Response.status(400); //Bad request
            return response.entity("failure: the new file must be given a name!").build();
        }

        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("X");

        String result = APIMessage.FILE_NOT_UPLOAD;

        if(client.stor(pathFile, received)){
            result = APIMessage.FILE_UPLOAD;
        }

        client.close();

        Response.ResponseBuilder response = Response.ok("Result: " + result, MediaType.TEXT_HTML).status(Response.Status.OK);
        return response.build();

    }

    /**
     * Method handling HTTP GET requests for Download FILE /directory. The returned object will be sent
     * to the client as "TEXT_PLAIN" of MediaType type
     * @param path
     * @return
     * @throws IOException
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/downloadDir/{path: .*}")
    public Response get(@PathParam("path") String path) throws IOException {
        System.out.println("4");

        DecodeBasicAuthenticator basicAuthenticator = getAuthentification();
        if(basicAuthenticator == null)
        {
            return requireHTTPAuthenticationResponse();
        }

        String username = basicAuthenticator.getUser();
        String password = basicAuthenticator.getCode();
        ClientFtp client = new ClientFtp("localhost", 21, username, password);

        try {
            if (!client.authenticate()) {
                return errorAuthentificationUserPassResponse();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = null;
        file = client.retr(path);
        if(file!=null){
            Response.ResponseBuilder response = Response.ok((Object) file);
            response.header("Content-Disposition", "attachement; filename = "+ file.getName());
            return response.build();
        }else {
            Response.ResponseBuilder response = Response.ok(path + APIMessage.FILE_NOT_DOWNLOADED, MediaType.TEXT_HTML).status(Response.Status.OK);
            return response.build();
        }
    }

    /**
     * Get the basic authentification in http request header
     * @return DecodeBasicAuthenticator if http request header contains authentification. Null otherwise
     */
    private DecodeBasicAuthenticator getAuthentification()
    {
        if (headers.getRequestHeader("authorization") != null) {
            final String created = headers.getRequestHeader("authorization").get(0).substring("Basic ".length());
            return  new DecodeBasicAuthenticator(created);
        }
        return null;
    }

    /**
     * Build response to signal the api need http authentification
     * @return response header
     */
    private Response requireHTTPAuthenticationResponse(){
        Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity(APIMessage.UNAUTHORIZED);
        Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,APIMessage.LOG_UNAUTHORIZED);
        return response.header("Www-authenticate", "Basic realm=\"rest\"").build();
    }

    /**
     * Build response to signal the authentification is incorrect (user or password incorrect)
     * @return response header
     */
    private Response errorAuthentificationUserPassResponse() {
        Response.ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED).entity(APIMessage.UNAUTHORIZED_ERROR_USERPASS);
        Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,APIMessage.LOG_UNAUTHORIZED_ERROR_USERPASS);
        return response.header(APIMessage.UNAUTHORIZED_ERROR_USERPASS, APIMessage.UNAUTHORIZED_ERROR_USERPASS).build();
    }

}
