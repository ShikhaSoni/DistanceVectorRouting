/**
 * Message class that is the packet to be sent and received with the source that is sending it
 */
import java.io.Serializable;
import java.util.Hashtable;

class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	String source;
	Hashtable<String, Destination> routingTable;

	/**
	 * Constructor to hold the source and routing table
	 * @param neighbors
	 * @param source
	 */
	public Message(Hashtable<String, Destination> neighbors, String source) {
		this.routingTable = neighbors;
		this.source = source;
	}
}