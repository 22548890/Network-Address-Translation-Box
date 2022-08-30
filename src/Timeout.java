/**
 * Returns the current state of the player's stream.
 * 
 * @param player The player whose stream state is being requested.
 * @return The current state of the player's stream.
 */
public class Timeout implements Runnable {
    static final int TIMEOUT = 15 * (1000 * 60);// 15mins

    public Timeout() {

    }

    /**
     * The timeout thread that will be used to kill the server if it doesn't
     * shutdown properly.
     */
    @Override
    public void run() {

        try {
            Thread.sleep(TIMEOUT);
            System.out.println("----------------------------------------------------------------");
            System.err.println("EXPIRED TIMEOUT OF " + TIMEOUT + "ms.");
            System.out.println("----------------------------------------------------------------\n");
            System.exit(0);
        } catch (InterruptedException e) {
        }

    }

}
