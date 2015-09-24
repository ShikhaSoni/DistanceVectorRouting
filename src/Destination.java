
public class Destination {
	int s=10;

	String destinationIP, nextIP, subnetMask;
	int hopCount, port;
	public Destination(String destinationIP, String nextIP, int port, int hopCount){
		this.port=port;
		this.destinationIP=destinationIP;
		this.nextIP=nextIP;
		//this.subnetMask= subnetMask;
		this.hopCount=hopCount;
	}
}
