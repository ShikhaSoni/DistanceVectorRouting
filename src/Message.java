import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;


class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	String source;
	ConcurrentHashMap<String, Destination> routingTable;

	public Message(ConcurrentHashMap<String, Destination> neighbors, String source) {
		this.routingTable = neighbors;
		this.source = source;
	}
}