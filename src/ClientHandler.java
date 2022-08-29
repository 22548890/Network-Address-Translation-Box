import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream ous;
    
    /** 
     * Constructor for this handler
     * @param socket Is the socket that the client connects to
     */
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.ous = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());

            clientHandlers.add(this);
        } catch (IOException e){
            closeEverything();
        }
    }

    @Override
    public void run() {
        while (socket.isConnected()) {
            
        }
    }

    public void closeEverything() {
        clientHandlers.remove(this);
        try {
            if (ois != null) {
                ois.close();
            }
        } catch (IOException e) {}
        try {
            if (ous != null) {
                ous.close();
            }
        } catch (IOException e) {}
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {}
    }
}
