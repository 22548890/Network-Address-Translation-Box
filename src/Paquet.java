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

    // note: 1 = dchp request, 2 = dchp reply
    private int type; // https://en.wikipedia.org/wiki/Internet_Control_Message_Protocol

    // payload
    private String text;

    /**
     * Constructor for this class
     * 
     * @param sourceMAC
     * @param destinationMAC
     * @param sourceIP
     * @param destinationIP
     * @param sourcePort
     * @param destinationPort
     * @param type
     * @param text
     */
    public Paquet(String sourceMAC, String destinationMAC, String sourceIP, String destinationIP, int sourcePort,
            int destinationPort, int type, String text) {
        this.sourceMAC = sourceMAC;
        this.destinationMAC = destinationMAC;
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.sourcePort = sourcePort;
        this.destinationPort = destinationPort;
        this.type = type;
        this.text = text;
    }

    public Paquet(String text) {
        this.text = text;
    }

    public void setSourceMAC(String mac) {
        this.sourceMAC = mac;
    }

    public void setDestinationMAC(String mac) {
        this.destinationMAC = mac;
    }

    public void setSourceIP(String ip) {
        this.sourceIP = ip;
    }

    public void setDestinationIP(String ip) {
        this.destinationIP = ip;
    }

    public void setSourcePort(int port) {
        this.sourcePort = port;
    }

    public void setDestinationPort(int port) {
        this.destinationPort = port;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSourceMAC() {
        return sourceMAC;
    }

    public String getDestinationMAC() {
        return destinationMAC;
    }

    public String getSourceIP() {
        return sourceIP;
    }

    public String getDestinationIP() {
        return destinationIP;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public int getType() {
        return type;
    }

    public String getTypeName() {
        switch (type) {
            case 0:
                return "ECHO_REPLY";
            case 1:
                return "DHCP_REPLY";
            case 2:
                return "DHCP_REQUEST";
            case 3:
                return "ARP_REPLY";
            case 4:
                return "ARP_REQUEST";
            case 5:
                return "TCP";
            case 6:
                return "UDP";
            case 7:
                return "ICMP";
            case 8:
                return "ECHO_REQUEST";
            default:
                return "ERROR";
        }
    }

    public String getText() {
        return text;
    }

}
