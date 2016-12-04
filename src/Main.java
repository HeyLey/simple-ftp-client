import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws IOException {

        SimpleFTP client = new SimpleFTP();
        client.connect("ftp.intel.com");
        client.help();
        client.pwd();
        client.list();
        client.cwd("images");
        client.pwd();
        client.list();
       // client.retr();
        client.disconnect();
    }

}
