import java.io.Serializable;

public class Paquet implements Serializable {
    
    // Ethernet frame important elements
    private String destinationMAC;
    private String sourceMAC;

    // IP packet/segment important elements
    private String sourceIP;
    private String destinationIP;
    private int sourcePort;
    private int destinationPort;

    // payload
    private String text;

    // extra
    private boolean isCommand;

    public Paquet(String destinationMAC, String sourceMAC, String sourceIP, String destinationIP, String text) {
        this.destinationMAC = destinationMAC;
        this.sourceMAC = sourceMAC;
        this.destinationIP = destinationIP;
        this.sourceIP = sourceIP;
        this.text = text;
        this.isCommand = false;
    }

    public Paquet(String text) {
        this.text = text;
        this.isCommand = true;
    }

    public String getDestinationMAC() { return destinationMAC; }

    public String getSourceMAC() { return sourceMAC; }

    public String getSourceIP() { return sourceIP; }

    public String getDestinationIP() { return destinationIP; }

    public String getText() { return text; }

    public boolean isCommand() { return isCommand; }
}
