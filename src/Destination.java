import java.io.Serializable;


public class Destination implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String destinationIP, nextIP;
	int hopCount;
	public Destination(String destinationIP, String nextIP, int hopCount){
		this.destinationIP=destinationIP;
		this.nextIP=nextIP;
		this.hopCount=hopCount;
	}
	public String toString(){
		return destinationIP;
	}
}
