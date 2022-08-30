public class TableRow {
    
    private String clientIP;
    private int clientPort;  

    private String natIP;
    private int natPort; // unique
    

    public TableRow(String clientIP, int clientPort, String natIP, int natPort) {
        this.clientIP = clientIP;
        this.clientPort = clientPort;
        this.natIP = natIP;
        this.natPort = natPort;
    }

    public String getClientIP() { return clientIP; }

    public int getClientPort() { return clientPort; }

    public String getNatIP() { return natIP; }

    public int getNatPort() { return natPort; }
}
