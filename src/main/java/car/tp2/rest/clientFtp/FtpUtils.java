package car.tp2.rest.clientFtp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FtpUtils {

    /**
     * delete file
     * @param client
     *          an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param path
     * @return
     * @throws IOException
     *           if any network or IO error occurred.
     */
    protected boolean deleteFile(FTPClient client, final String path) throws IOException {
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
     *          an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param path
     * @return
     * @throws IOException
     *          if any network or IO error occurred.
     */
    protected boolean deleteDirectory(FTPClient client ,final String path) throws IOException {

        FTPFile[] files = client.listFiles(path);
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

        if(client.removeDirectory(path)){
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,client.getReplyString());
            return true;
        }
        return false;
    }

    /**
     * check if the path is directory
     * @param client
     *          an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param path
     * @return boolean
     * @throws IOException
     *          if any network or IO error occurred.
     */
    protected boolean checkIsDirectory(FTPClient client , String path) throws IOException {
        String currentPath = client.printWorkingDirectory();
        client.changeWorkingDirectory(path);
        final int returnCode = client.getReplyCode();
        if (returnCode != 550) {
            client.changeWorkingDirectory(currentPath);
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"File "+path+" is directory");
            return true;
        }
        return false;
    }

    /**
     * check if the path is File
     * @param client
     *          an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param path
     *          Path of the destination directory on the server.
     * @return
     * @throws IOException
     *          if any network or IO error occurred.
     */
    protected boolean chckIsFile(FTPClient client, String path) throws IOException {
        InputStream inputStream = client.retrieveFileStream(path);
        int returnCode = client.getReplyCode();
        if (inputStream != null && returnCode != 550) {
            Logger.getLogger(ClientFtp.class.getName()).log(Level.INFO,"File "+path+" is not directory");
            return true;
        }
        return false;
    }
    /**
     * Upload a whole directory (including its nested sub directories and files)
     * to a FTP server.
     *
     * @param ftpClient
     *            an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param remoteFile
     *            Path of the destination directory on the server.
     * @param localParentDir
     *            Path of the local directory being uploaded.
     * @param remoteParentDir
     *            Path of the parent directory of the current directory on the
     *            server (used by recursive calls).
     * @throws IOException
     *             if any network or IO error occurred.
     */
    public boolean uploadDirectory(FTPClient ftpClient,
                                   String remoteFile, String localParentDir, String remoteParentDir)
            throws IOException {

        boolean result = false;
        System.out.println("LISTING directory: " + localParentDir);

        File localDir = new File(localParentDir);
        File[] subFiles = localDir.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            //System.out.println("name===> "+localDir.getName());
//            boolean create = ftpClient.makeDirectory(localDir.getName());
//            ftpClient.changeWorkingDirectory(ftpClient.printWorkingDirectory()+"/"+localDir.getName());
            //TODO traiter les exeptions
            for (File item : subFiles) {
                String remoteFilePath="";
                remoteFilePath = remoteFile + "/" + remoteParentDir
                        + "/" + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteFile + "/" + item.getName();
                }




                if (item.isFile()) {
                    // upload the file
                    String localFilePath = item.getAbsolutePath();
                    System.out.println("About to upload the file: " + localFilePath);
                    boolean uploaded = uploadSingleFile(ftpClient,
                            localFilePath, remoteFilePath);
                    if (uploaded) {
                        System.out.println("UPLOADED a file to: "
                                + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT upload the file: "
                                + localFilePath);
                    }
                    result=uploaded;
                } else {
                    // create directory on the server
                    boolean created = ftpClient.makeDirectory(remoteFilePath);
                    if (created) {
                        System.out.println("CREATED the directory: "
                                + remoteFilePath);
                    } else {
                        System.out.println("COULD NOT create the directory: "
                                + remoteFilePath);
                    }

                    // upload the sub directory
                    String parent = remoteParentDir + "/" + item.getName();
                    if (remoteParentDir.equals("")) {
                        parent = item.getName();
                    }

                    localParentDir = item.getAbsolutePath();
                    uploadDirectory(ftpClient, remoteFile, localParentDir,
                            parent);
                }
            }

        }
      return result;
    }
    /**
     * Upload a single file to the FTP server.
     *
     * @param ftpClient
     *            an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param localFilePath
     *            Path of the file on local computer
     * @param remoteFilePath
     *            Path of the file on remote the server
     * @return true if the file was uploaded successfully, false otherwise
     * @throws IOException
     *             if any network or IO error occurred.
     */
    public boolean uploadSingleFile(FTPClient ftpClient,
                                           String localFilePath, String remoteFilePath) throws IOException {
        File localFile = new File(localFilePath);

        System.out.println(remoteFilePath+"     "+localFile.getPath());
        InputStream inputStream = new FileInputStream(localFile);
        try {
            ftpClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(4096);
            ftpClient.enterRemotePassiveMode();
            ftpClient.enterLocalPassiveMode();
            boolean resul=ftpClient.storeFile(remoteFilePath, inputStream);
            System.out.println(ftpClient.getReplyCode());
            return resul;
        } catch (IOException e){
            inputStream.close();
            return false;
        }
    }

    /**
     * download a single file to the FTP server.
     *
     * @param ftpClient
     *            an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param fileName
     *            Path of the file on local computer
     * @param remoteFilePath
     *            Path of the file on remote the server
     * @return true if the file was uploaded successfully, false otherwise
     * @throws IOException
     *             if any network or IO error occurred.
     */
    public File dowloadSingleFile(FTPClient ftpClient,
                                    String fileName, String remoteFilePath) throws IOException {

        System.out.println(remoteFilePath+"     "+fileName);
        File downloadFile1 = new File(fileName);
        boolean resul=false;
        FileOutputStream fileDowloaded = new FileOutputStream(downloadFile1);
        try {
            ftpClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(4096);
            ftpClient.enterRemotePassiveMode();
            ftpClient.enterLocalPassiveMode();

            resul=ftpClient.retrieveFile(remoteFilePath, fileDowloaded);

            System.out.println(ftpClient.getReplyCode());

        } catch (IOException e){
            fileDowloaded.close();
        }
        return downloadFile1;

    }
}
