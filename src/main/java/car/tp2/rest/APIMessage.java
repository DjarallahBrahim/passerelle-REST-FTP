package car.tp2.rest;

/**
 * Class APIMessage
 * Contains all of the code and messages of the api rest
 *
 * @author Brahim DJARALLAH et Romain LAMOOT
 * @version 1.0
 */
public class APIMessage {

    /* Response send to user which not authentificated by HTTP header */
    public static String UNAUTHORIZED = "Requires HTTP authentication!";

    /* Message show in log when user is not authentificated by HTTP header */
    public static String LOG_UNAUTHORIZED = "Client did not use HTTP Authentification";

    /* Response send to user which not correctly authentificated by HTTP header */
    public static String UNAUTHORIZED_ERROR_USERPASS = "Wrong user name and password!";

    /* Message show in log when user is not correctly authentificated by HTTP header */
    public static String LOG_UNAUTHORIZED_ERROR_USERPASS = "Wrong user name and password!";

    public static String FILE_DELETED = "File deleted";

    public static String FILE_NOT_DELETED = "File doesn't delete";

    public static String FILE_CREATED = "File created";

    public static String FILE_NOT_CREATED = "File doesn't created";

    public static String FILE_NOT_UPLOAD = "File doesn't upload";

    public static String FILE_UPLOAD = "File uploaded";

    public static String FILE_NOT_DOWNLOADED = "File didn't dowloaded";

}
