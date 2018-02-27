package car.tp2.rest;

import sun.misc.BASE64Decoder;

import java.io.IOException;

/**
 * Decoding teh Basic authentication passed in the
 * authorization HTTP.
 *
 * @author Djarallah.B & Lamoot.R
 */
public class DecodeBasicAuthenticator {

    /**
     * encrypted authentication
     */
    private String deccrypted ;
    /**
     * decrypted user name
     */
    private String user;

    /**
     * decrypted user password
     */
    private String code;

    DecodeBasicAuthenticator(final String decrypted)  {

        try {
            this.deccrypted = new String(new BASE64Decoder().decodeBuffer(decrypted.split(":")[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("code 2& " + deccrypted);


    }

    /**
     * Returns the username
     *
     * @return The username
     */

    public String getUser() {
            final String user = this.deccrypted.split(":")[0];
            this.user = user;

        return user;

    }

    /**
     * Returns the user password
     *
     * @return The user password
     */

    public String getCode() {
            final String code = this.deccrypted.split(":")[1];
            this.code = code;

        return code;
    }
}
