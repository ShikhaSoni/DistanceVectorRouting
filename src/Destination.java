import java.io.Serializable;


public class Destination implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int s=10;

	String destinationIP, nextIP, subnetMask;
	int hopCount;
	public Destination(String destinationIP, String nextIP, int hopCount){
		this.destinationIP=destinationIP;
		this.nextIP=nextIP;
		//this.subnetMask= subnetMask;
		this.hopCount=hopCount;
	}
	public String toString(){
		return destinationIP;
	}
}
