import java.io.Serializable;
import java.util.Hashtable;

class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	String source;
	Hashtable<String, Destination> routingTable;

	public Message(Hashtable<String, Destination> neighbors, String source) {
		this.routingTable = neighbors;
		this.source = source;
	}
}