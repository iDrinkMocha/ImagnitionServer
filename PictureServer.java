import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class PictureServer {

    static final int PORT = 12345;
    static int filecount = 0;

    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
        	System.out.println("Initialised");
            serverSocket = new ServerSocket(PORT);
            //serverSocket.setSoTimeout(10000);
        } catch (IOException e) {
            e.printStackTrace();

        }
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            // new thread for a client
            new EchoThread(socket).start();
            System.out.println(filecount);
            filecount++;
        }
    }
}