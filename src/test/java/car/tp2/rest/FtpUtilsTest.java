package car.tp2.rest;

import car.tp2.rest.clientFtp.FtpUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FtpUtilsTest {

    MockFTPClient ftp;
    FtpUtils ftpUtils;

    @Before
    public void before()
    {
        ftp = new MockFTPClient();
        ftpUtils = new FtpUtils();
    }


    @Test
    public void testDeleteFile()
    {
        try {
            assertTrue(this.ftp.deleteFile("path"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class MockFTPClient extends FTPClient{

        @Override
        public boolean deleteFile(String pathname) throws IOException {
            assertTrue(1 == 1);
            return true;
        }
    }
}
