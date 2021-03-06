package car.tp2.rest.clientFtp;


import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
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
     */
    private FtpUtils ftpUtils;
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
        this.ftpUtils = new FtpUtils();
    }

    /**
     *
     * @return
     * @throws IOException
     */
    public boolean authenticate() throws IOException {
        this.client.connect(host, port);
        this.client.enterLocalPassiveMode();
        this.client.enterRemotePassiveMode();
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
        if (this.ftpUtils.chckIsFile(this.client , fileToDelete)) {
            System.out.println("FILE");
            return this.ftpUtils.deleteFile(this.client,fileToDelete);
        }

        //check if it is durectory and delete it
        if (this.ftpUtils.checkIsDirectory(this.client,fileToDelete)) {
            System.out.println("DIR\n"+fileToDelete);
            return this.ftpUtils.deleteDirectory(this.client,fileToDelete);
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
     *
     * @param directoryName
     * @return
     * @throws IOException
     */
    public boolean stor(final String directoryName, InputStream received) throws IOException {

        String remoteFilePath = this.client.printWorkingDirectory() + "/" + directoryName;

        this.client.setBufferSize(4096);
        this.client.setFileType(FTP.BINARY_FILE_TYPE);
        this.client.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
        boolean storeFileResult = this.client.storeFile(remoteFilePath, received);

        if (!storeFileResult) {
            Logger.getLogger(ClientFtp.class.getName()).log(Level.WARNING, this.client.getReplyString());
            throw new IOException(this.client.getReplyString());
        }
        received.close();

        return  storeFileResult;
    }

    /**
     * donwload file from server
     * @param fileName
     * @return
     * @throws IOException
     */
    public File retr(final String fileName)throws IOException{
        File downloadedFile = null ;

        this.client.enterLocalPassiveMode();
        this.client.setFileType(FTP.BINARY_FILE_TYPE);
        this.client.setBufferSize(4096);

        final String remoteFilePath = this.client.printWorkingDirectory()+"/"+fileName;

        if(this.ftpUtils.checkIsDirectory(this.client,remoteFilePath)){
            this.ftpUtils.listDirectory(this.client,"",remoteFilePath,"",0);
            return this.ftpUtils.dowloadFill(this.client,remoteFilePath,fileName);
        }else {
            downloadedFile = this.ftpUtils.dowloadSingleFile(this.client,fileName,remoteFilePath);
        }

        return downloadedFile;
    }


}
