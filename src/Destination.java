/**
 * @author Shikha Soni
 * This class is the Destination class that stores every information about the destination
 */
import java.io.Serializable;

public class Destination implements Serializable {
	private static final long serialVersionUID = 1L;

	String destinationIP, nextIP;
	int hopCount;

	/**
	 * Constructor to pass the IP, next hop, and hopcount
	 * @param destinationIP
	 * @param nextIP
	 * @param hopCount
	 */
	public Destination(String destinationIP, String nextIP, int hopCount) {
		this.destinationIP = destinationIP;
		this.nextIP = nextIP;
		this.hopCount = hopCount;
	}

	public String toString() {
		return destinationIP;
	}
}
