package car.tp2.rest.clientFtp;



import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 */
public class ClientFtp {

    /**
     *
     */
    private FTPClient client;
    /**
     *
     */
    private String username;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String host;
    /**
     *
     */
    private int port;

    /**
     *
     * @param host
     * @param port
     * @param username
     * @param password
     */
    public ClientFtp(final String host, final int port, final String username, final String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.client = new FTPClient();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public boolean authenticate() throws IOException {
        this.client.connect(host, port);
        this.client.enterLocalPassiveMode();
        this.client.login(username, password);
        return this.client.isConnected();
    }

    /**
     * List Dir
     * @param path
     * @return
     * @throws IOException
     */
    public FTPFile[] list(final String path) throws IOException {
        FTPFile [] files;
        if(path.length()>=1){
            this.client.changeWorkingDirectory(path);
            files = this.client.listFiles();
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,this.client.getReplyString());
        }else{
            files = this.client.listFiles();
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,this.client.getReplyString());
        }

        return files;
    }


    /**
     * Delete a direction and file
     * @param name
     * @return
     * @throws IOException
     */
    public boolean delete(String name) throws IOException {

        String currentPath = this.client.printWorkingDirectory();
        String fileToDelete = currentPath+"/"+name;

        //check if it is file and delete it
        if (chckIsFile(this.client , fileToDelete)) {
            System.out.println("FILE");
            return this.deleteFile(this.client,fileToDelete);
        }

        //check if it is durectory and delete it
        if (checkIsDirectory(this.client,fileToDelete)) {
            System.out.println("DIR\n"+fileToDelete);
            return this.deleteDirectory(this.client,fileToDelete);
        }

        return false;

    }

    /**
     * create directory in the current path of server
     * @param path
     * @return true if the dir created
     * @throws IOException
     */
    public boolean mkd(final String path) throws IOException {
        if(path.length()==0){
            return false;
        }
        boolean created = this.client.makeDirectory(path);
        Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,this.client.getReplyString());

        return created;
    }
    /**
     * disconnect the FtpClient
     * @throws IOException
     */
    public void close() throws IOException {
        if (this.client != null && this.client.isConnected()) {
            this.client.logout();
            this.client.disconnect();
        }
    }

    /**
     * delete file
     * @param client
     * @param path
     * @return
     * @throws IOException
     */
    private boolean deleteFile(FTPClient client ,final String path) throws IOException {
        if(client.deleteFile(path)){
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,client.getReplyString());
            return true;
        }
        Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,client.getReplyString());
        return false;
    }

    /**
     * delete recurcive directory
     * @param client
     * @param path
     * @return
     * @throws IOException
     */
    private boolean deleteDirectory(FTPClient client ,final String path) throws IOException {

        FTPFile [] files = client.listFiles(path);
        System.out.println("DIR length : "+files.length);
        for(FTPFile file: files){
            if (file.isDirectory()){
                System.out.println("DIR to delete : "+path+"/"+file.getName());
                //client.removeDirectory(path+"/"+file.getName());
                deleteDirectory(client , path+"/"+file.getName());
            }

            if (file.isFile()){
                System.out.println("File to delete : "+path+"/"+file.getName());
                client.deleteFile(path+"/"+file.getName());

            }

        }

        if(this.client.removeDirectory(path)){
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,this.client.getReplyString());
            return true;
        }
        return false;
    }

    /**
     * check if the path is directory
     * @param client
     * @param path
     * @return boolean
     * @throws IOException
     */
    private boolean checkIsDirectory(FTPClient client , String path) throws IOException {
        String currentPath = client.printWorkingDirectory();
        this.client.changeWorkingDirectory(path);
        final int returnCode = this.client.getReplyCode();
        if (returnCode != 550) {
            this.client.changeWorkingDirectory(currentPath);
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"File "+path+" is directory");
            return true;
        }
        return false;
    }

    /**
     * check if the path is File
     * @param client
     * @param path
     * @return
     * @throws IOException
     */
    private boolean chckIsFile(FTPClient client , String path) throws IOException {
        InputStream inputStream = client.retrieveFileStream(path);
        int returnCode = this.client.getReplyCode();
        if (inputStream != null && returnCode != 550) {
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"File "+path+" is not directory");
            return true;
        }
        return false;
    }
}
