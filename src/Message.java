import java.io.Serializable;
import java.util.List;


class Message implements Serializable{

	private static final long serialVersionUID = 1L;
	String source;
	private List<Destination> routingTable;

	public Message(List<Destination> neighbors, String source) {
		this.routingTable = neighbors;
		this.source = source;
	}
}