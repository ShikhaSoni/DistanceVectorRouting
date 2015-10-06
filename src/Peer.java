/**
 * @author Shikha Soni
 * Distance Vector Routing RIP v2
 * 
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Peer extends Thread {
	List<String> neighbors = new ArrayList<String>();
	Hashtable<String, Destination> routingTable = new Hashtable<String, Destination>();
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;
	int port = 5999;
	String ownIP;

	/**
	 * Main method to start the Peer
	 * 
	 * @param args
	 *            Command Line arguements take the information of the neighbor
	 *            as "IPAddress:linkcost"
	 */
	public static void main(String[] args) {
		Peer me = new Peer();
		try {
			me.ownIP = InetAddress.getLocalHost().getHostAddress();
			System.out.println(InetAddress.getLocalHost().toString());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (args.length > 2 || args.length == 0) {
			System.out
					.println("Enter at least 1 or at most 2 neighbors and their link weights");
		}

		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			me.neighbors.add(args[neighbor]);
		}
		me.establishConnection();
	}

	/**
	 * EstablishConnection is used to connect to the specified neighbors in the
	 * arraylist If the connection is not successful, the the same peer wait for
	 * the neighbors to connect using beServer
	 * 
	 */
	public void establishConnection() {
		int count = 0;
		for (String destination : neighbors) {
			try {
				me_Client = new Socket(getIP(destination), port);
				System.out.println("Connecting to  " + getIP(destination));
				Destination dest = new Destination(getIP(destination),
						getIP(destination), getWeight(destination));
				routingTable.put(getIP(destination), dest);
				new StreamHandler(1, me_Client).start();
				new StreamHandler(2, me_Client).start();
			} catch (IOException e) {
				System.out.println(destination + " not active");
				count++;
				continue;
			}
		}
		if (count > 0) {
			beServer();
		}
	}

	/**
	 * This method separates the IP and link cost taken from the command line
	 * arguments
	 * 
	 * @param destination
	 *            The String of the form IP:Linkcost
	 * @return the Weigt is extracted from the string and returned back
	 */
	private int getWeight(String destination) {
		return Integer.parseInt(destination.split(":")[1]);
	}

	/**
	 * This method extracts the IP out of the string
	 * 
	 * @param destination
	 *            IP:Linkcost
	 * @return it returns the IP from the given string
	 */
	private String getIP(String destination) {
		return destination.split(":")[0];
	}

	/**
	 * Executed when one or more neighbors are inactive
	 */
	public void beServer() {
		Socket s;
		try {
			me_Server = new ServerSocket(port);
			while (true) {
				s = me_Server.accept();
				System.out.println("Recived connection from: "
						+ s.getInetAddress().getHostAddress());
				Destination dest = new Destination(s.getInetAddress()
						.getHostAddress(), s.getInetAddress().getHostAddress(),
						myDistance(s.getInetAddress().getHostAddress()));
				routingTable.put(s.getInetAddress().getHostAddress(), dest);
				new StreamHandler(1, s).start();
				new StreamHandler(2, s).start();
			}
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * The function updateTable takes in the packet received from the neighbor
	 * 
	 * @param table
	 *            the packet received The three conditions are checked: 1. check
	 *            if ownIP is same as destinationIP 2. check if the IP does not
	 *            exist 3. check if count is smaller
	 */
	public void updateTable(Message table) {

		for (Entry<String, Destination> entry : table.routingTable.entrySet()) {
			if (entry.getKey().toString().equals(ownIP))
				continue;
			else {
				if (routingTable.get(entry.getKey()) == null) {
					Destination newDest = new Destination(
							entry.getValue().destinationIP, table.source,
							entry.getValue().hopCount
									+ myDistance(table.source));
					routingTable.put(entry.getValue().destinationIP, newDest);
				} else {
					if ((routingTable.get(entry.getKey()).hopCount) > (entry
							.getValue().hopCount) + myDistance(table.source)) {
						routingTable.get(entry.getKey()).hopCount = entry
								.getValue().hopCount + myDistance(table.source);
						routingTable.get(entry.getKey()).nextIP = table.source;
					}
				}
			}
		}
	}

	/**
	 * printTable prints the routing table of the Peer after the the update from
	 * neighbor is received
	 */
	public synchronized void printTable() {
		System.out
				.println("******************************************************************");
		for (Entry<String, Destination> entry : routingTable.entrySet()) {
			System.out.println(getPrefix(entry.getValue().destinationIP)
					+ "		255.255.255.0" + "		" + entry.getValue().nextIP + "		"
					+ entry.getValue().hopCount);
		}
		System.out
				.println("******************************************************************");
		System.out.println();
	}

	/**
	 * This method returns back the network prefix from the subnet mask and the
	 * IP
	 * 
	 * @param IP
	 *            The network prefix is extracted from the IP
	 * @return returns the network prefix
	 */
	public String getPrefix(String IP) {
		String[] s = IP.split("\\.");
		String one = Integer.toString(Integer.parseInt(s[0]) & 255);
		String two = Integer.toString(Integer.parseInt(s[1]) & 255);
		String three = Integer.toString(Integer.parseInt(s[2]) & 255);
		String four = Integer.toString(Integer.parseInt(s[3]) & 0);
		return one + "." + two + "." + three + "." + four;
	}

	/**
	 * myDistance returns the local neighbor link weight of the source
	 * 
	 * @param source
	 * @return returns the weight
	 */
	private int myDistance(String source) {
		for (String neighbors : neighbors) {
			if (getIP(neighbors).equals(source)) {
				return getWeight(neighbors);
			}
		}
		return 0;
	}

	/**
	 * This class is written to handle the connections and make the streams on
	 * separate threads
	 * 
	 * @author Shikha Soni
	 *
	 */
	class StreamHandler extends Thread {
		ObjectInputStream ois;
		ObjectOutputStream oos;
		boolean listening = true;
		int id;
		Socket socket;

		/**
		 * The constructor takes the choice of stream to be made
		 * 
		 * @param id
		 * @param socket
		 */
		public StreamHandler(int id, Socket socket) {
			this.id = id;
			this.socket = socket;
		}

		/**
		 * The input stream is made here and the objects are read from this
		 * stream
		 * 
		 * @param peer
		 *            The socket of the neighbor
		 */
		public void getIStream(Socket peer) {
			// get streams on separate threads
			try {
				ois = new ObjectInputStream(peer.getInputStream());
				while (true) {
					Message rec = (Message) ois.readObject();
					updateTable(rec);
					printTable();
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println(peer.getInetAddress().getHostAddress()
						+ "just shut down");
				remove(peer.getInetAddress().getHostAddress().toString());
			}
		}

		/**
		 * This function makes the output stream
		 * 
		 * @param peer
		 *            The socket of the neighbor
		 */
		public void getOStream(Socket peer) {
			// get streams on separate threads
			try {
				oos = new ObjectOutputStream(peer.getOutputStream());
				// call after every one second
				while (true) {
					Thread.sleep(5000);
					// make a new object
					Hashtable<String, Destination> newTable = makeNew(peer
							.getInetAddress().getHostAddress());
					Message send = new Message(newTable, ownIP);
					oos.writeObject(send);
					oos.flush();
					oos.reset();
				}
			} catch (IOException | InterruptedException e) {
				System.out.println(peer.getInetAddress().getHostAddress()
						+ "just shut down");
				System.out.println(e);
				remove(peer.getInetAddress().getHostAddress().toString());
			}
		}

		/**
		 * the run method to create the threads
		 */
		public void run() {
			if (id == 1)
				getIStream(socket);
			else
				getOStream(socket);
		}

		public Hashtable<String, Destination> makeNew(String ip) {
			Hashtable<String, Destination> tempTable = new Hashtable<>();
			for (Entry<String, Destination> entry : routingTable.entrySet()) {
				if (!entry.getValue().nextIP.equals(ip)) {
					tempTable.put(entry.getKey(), entry.getValue());
				}
			}
			return tempTable;
		}
		public void remove(String IP) {
			Iterator<String> it = routingTable.keySet().iterator();
			while (it.hasNext()) {
				String s = routingTable.get(it.next()).destinationIP;
				if (IP.equals(s)) {
					it.remove();
				}
			}
		}
	}
}
