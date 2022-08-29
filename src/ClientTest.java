import java.io.*;
import java.net.*;
import java.util.*;

public class ClientTest {
    public static void main(String[] args) {
        try {
            //
            // Create a connection to the server socket on the server application
            //

            InetAddress host = InetAddress.getLocalHost();
            Socket socket = new Socket(host.getHostName(), 1234);

            //
            // Send a message to the client application
            //
            DataOutputStream dos = new DataOutputStream(
                    socket.getOutputStream());

            DataInputStream dis = new DataInputStream(System.in);

            String line = "";
            while (!line.equals("bye")) {
                try {
                    line = dis.readLine();
                    dos.writeUTF(line);
                    dos.flush();
                    socket.close();
                } catch (IOException ioe) {
                    System.out.println("Sending error: " + ioe.getMessage());
                }
            }

            dis.close();
            dos.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}