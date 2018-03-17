package car.tp2.rest.clientFtp;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FtpUtils {

    List<String> fileList;
    List<String> dirList;

    public FtpUtils(){
        this.fileList = new ArrayList<String>();
        this.dirList = new ArrayList<String>();

    }
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
    public File dowloadSingleFile(final FTPClient ftpClient,
                                    String fileName, String remoteFilePath) throws IOException {
        //System.out.println(remoteFilePath+"     "+fileName);
//        fileName = fileName.substring(0,fileName.length()-2);
//        remoteFilePath = remoteFilePath.substring(0,remoteFilePath.length()-2);
        if(fileName.contains("/")){
            String [] splitName = fileName.split("/");
            fileName = splitName[splitName.length-1];
        }
        File downloadFile1 = new File(fileName);
        FileOutputStream fileDowloaded = new FileOutputStream(downloadFile1);
        try {
            ftpClient.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(4096);
            ftpClient.enterRemotePassiveMode();
            ftpClient.enterLocalPassiveMode();

            ftpClient.retrieveFile(remoteFilePath, fileDowloaded);
            System.out.println(ftpClient.getReplyCode());

        } catch (IOException e){
            fileDowloaded.close();
        }
        return downloadFile1;

    }


    /**
     * creat ZIP file from List of filePath to download it
     * @param client
     * @param SOURCE_FOLDER
     * @return
     * @throws IOException
     */
    public File dowloadFill(FTPClient client ,String SOURCE_FOLDER,String zipName) throws IOException {
        byte[] buffer = new byte[1024];

        try{

           // String zipFile = "testZip.zip";
            File zipFile = new File(zipName+".zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

//            for(String dir : this.dirList){
//                //ZIP file objects
//                System.out.println("dir==> "+dir);
//                ZipEntry ze= new ZipEntry(dir);
//                zos.putNextEntry(ze);
//
//            }


//
            for(String file : this.fileList){

                //ZIP file objects
                ZipEntry ze= new ZipEntry(file);
                zos.putNextEntry(ze);
                //Full Path of disatns File to dowload
                String pathFiledistant = SOURCE_FOLDER+File.separator + file;
                FileInputStream in = new FileInputStream(dowloadSingleFile(client,file,pathFiledistant));
                //Copy the file
                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
                System.out.println("File Added : " + file);
            }

            zos.closeEntry();
            zos.close();
            fos.close();


            System.out.println("Done");
            return zipFile;
        }catch(IOException ex){
            ex.printStackTrace();
            return null;
        }

    }


    /**
     * List a Directory with ist sub-Dir befor Download it
     * @param ftpClient
     * @param fixedPath
     * @param parentDir
     * @param currentDir
     * @param level
     * @throws IOException
     */
    public void listDirectory(FTPClient ftpClient, String fixedPath,String parentDir,
                              String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        if(!currentDir.equals(""))
             fixedPath += "/"+currentDir;
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".")
                        || currentFileName.equals("..")) {
                    // skip parent directory and directory itself
                    continue;
                }
                for (int i = 0; i < level; i++) {
                    //System.out.print("\t");
                }
                if (aFile.isDirectory()) {
                    if(!fixedPath.equals("")){
                        this.dirList.add(fixedPath+currentFileName+"/");
                    }
                    else {
                        this.dirList.add(currentFileName+"/");
                    }
                    listDirectory(ftpClient,fixedPath, dirToList, currentFileName, level + 1);
                } else {
                    if(!fixedPath.equals("")){
                        fileList.add(fixedPath+"/"+currentFileName);
                    }
                    else {
                        fileList.add(currentFileName);
                    }
                }
            }
        }
        fixedPath="";
    }

}
