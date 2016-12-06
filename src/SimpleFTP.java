import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;

public class SimpleFTP {

    public SimpleFTP() { }

    public synchronized void connect(String host) throws IOException {
        connect(host, 21);
    }

    public synchronized void connect(String host, int port) throws IOException {
        connect(host, port, "anonymous", "anonymous");
    }

    public synchronized void connect(String host, int port, String user,
                                     String pass) throws IOException {
        if (socket != null) {
            throw new IOException("SimpleFTP is already connected. Disconnect first.");
        }
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));

        String response;

        do {
            response = readLine();
            System.out.println(response);
            if (response.startsWith("220 ")) {
                break;
            }
        } while (true);

        sendLine("USER " + user);
        System.out.println("Send: user " + user);

        response = readLine();
        System.out.println(response);

        sendLine("PASS " + pass);
        System.out.println("Send: pass " + pass);

        do {
            response = readLine();
            System.out.println(response);
            if (response.startsWith("230 ")) {
                break;
            }
        } while (true);

    }

    public synchronized void disconnect() throws IOException {
        try {
            sendLine("QUIT");
            System.out.println("Send: quit");
            String response = readLine();
            System.out.println(response);
        } finally {
            socket = null;
        }
    }

    public synchronized String pwd() throws IOException {
        sendLine("PWD");
        System.out.println("Send: pwd");

        String dir = null;
        String response = readLine();
            System.out.println(response);

        if (response.startsWith("257 ")) {
            int firstQuote = response.indexOf('\"');
            int secondQuote = response.indexOf('\"', firstQuote + 1);
            if (secondQuote > 0) {
                dir = response.substring(firstQuote + 1, secondQuote);
            }
        }

        return dir;
    }

    public synchronized boolean cwd(String dir) throws IOException {
        sendLine("CWD " + dir);
        System.out.println("Send: cwd");
        String response = readLine();
        System.out.println(response);

        return (response.startsWith("250 "));
    }

    public synchronized void help() throws IOException {
        sendLine("HELP");
        System.out.println("Send: help");
        String response;
        do {
            response = readLine();
            System.out.println(response);
            if (response.startsWith("214 ")) {
                break;
            }
        } while (true);
    }

    public synchronized void list() throws IOException {
        sendLine("PASV");
        System.out.println("Send: pasv");
        String response = readLine();
        System.out.println(response);
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: "
                        + response);
            }
        }

        sendLine("LIST");
        System.out.println("Send: " + "list");

        Socket dataSocket = new Socket(ip, port);

        response = readLine();
        System.out.println(response);

        reader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

        do {
            response = readLine();
            if (response == null) {
                dataSocket = null;
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                break;
            }
            System.out.println(response);
        } while (true);

        response = readLine();
        System.out.println(response);
    }

    public synchronized void retr(String file) throws IOException {
        sendLine("PASV");
        System.out.println("Send: pasv");
        String response = readLine();
        System.out.println(response);
        String ip = null;
        int port = -1;
        int opening = response.indexOf('(');
        int closing = response.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = response.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                ip = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                port = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
            } catch (Exception e) {
                throw new IOException("SimpleFTP received bad data link information: "
                        + response);
            }
        }

        sendLine("RETR " + file);
        System.out.println("Send: " + "retr " + file);

        Socket dataSocket = new Socket(ip, port);
        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
        BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));

        response = readLine();
        System.out.println(response);


        byte[] buffer = new byte[4096];
        int bytesRead = 0;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.close();
        input.close();

        response = readLine();
        System.out.println(response);
    }

    private void sendLine(String line) throws IOException {
        if (socket == null) {
            throw new IOException("SimpleFTP is not connected.");
        }
        try {
            writer.write(line + "\r\n");
            writer.flush();
            if (DEBUG) {
                System.out.println("> " + line);
            }
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    private String readLine() throws IOException {
        String line = reader.readLine();
        if (DEBUG) {
            System.out.println("< " + line);
        }
        return line;
    }


    private Socket socket = null;

    private BufferedReader reader = null;

    private BufferedWriter writer = null;

    private static boolean DEBUG = false;

}
