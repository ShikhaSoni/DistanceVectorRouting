import java.io.Serializable;


class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String source;

	public Message(/* ArrayList<Destination> routingTable, */String source) {
		// this.routingTable = routingTable;
		this.source = source;
	}
}