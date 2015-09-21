
public class Destination {
	int s=10;

	String destinationIP, nextIP, subnetMask;
	int hopCount;
	public Destination(String destinationIP, String nextIP, String subnetMask, int hopCount){
		this.destinationIP=destinationIP;
		this.nextIP=nextIP;
		this.subnetMask= subnetMask;
		this.hopCount=hopCount;
	}
}
