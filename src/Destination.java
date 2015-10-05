import java.io.Serializable;


public class Destination implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String destinationIP, nextIP;
	int hopCount;
	String subnetMask;
	public Destination(String destinationIP, String nextIP, int hopCount){
		this.destinationIP=destinationIP;
		this.nextIP=nextIP;
		this.hopCount=hopCount;
		subnetMask="255.255.0.0";
	}
	public String toString(){
		return destinationIP;
	}
}
