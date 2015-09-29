import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Peer {
	static List<String> neighbors = new ArrayList<String>();
	ConcurrentHashMap<String, Destination> routingTable1= new ConcurrentHashMap<String, Destination>();
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;
	int port = 6999;
	String ownIP;

	public static void main(String[] args) {
		Peer me = new Peer();
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter at least 1 or at most 2 neighbors and their link weights");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			neighbors.add(args[neighbor]);
		}
		me.establishConnection();
	}

	public void establishConnection() {
		for (String destination : neighbors) {
			try {
				ownIP = InetAddress.getLocalHost().getHostAddress();
				//System.out.println("Own IP: "+ownIP);
				me_Client = new Socket(getIP(destination), port);
				System.out.println("Connected to " + me_Client.getInetAddress().getHostAddress());
				Destination dest = new Destination(getIP(destination), getIP(destination), getWeight(destination));
				routingTable1.put(getIP(destination), dest);
				new StreamHandler(1, me_Client).start();
				new StreamHandler(2, me_Client).start();
			} catch (IOException e) {
				/*System.out.println("Waiting for neighbors to become active");
				System.out.println(e);*/
				beServer();
			}
		}
	}

	private int getWeight(String destination) {
		return Integer.parseInt(destination.split(":")[1]);
	}

	private String getIP(String destination) {
		return destination.split(":")[0];
	}

	public void beServer() {
		Socket s;
		try {
			me_Server = new ServerSocket(port);
			while (true) {
				s = me_Server.accept();
				/*System.out.println("Recived connection from: "
						+ s.getInetAddress().toString());*/
				Destination dest = new Destination(s.getInetAddress().getHostAddress(), s.getInetAddress().getHostAddress(), myDistance(s.getInetAddress().getHostAddress()));
				routingTable1.put(s.getInetAddress().toString(), dest);
				new StreamHandler(1, s).start();
				new StreamHandler(2, s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateTable(Message table) {
		//1. check if ownIP is same as destinationIP
		//2. check if the IP does  not exist
		//check if count is smaller 
		//System.out.println("Updating Table");
		for(Entry<String, Destination> entry : table.routingTable.entrySet()){
			System.out.println("1");
			System.out.println(entry.getKey().toString() +" " +ownIP);
			if(entry.getKey().toString().equals(ownIP))
				continue;
			else{
				if(routingTable1.get(entry.getKey()) == null){
					System.out.println("2");
					Destination newDest= new Destination(entry.getValue().destinationIP, table.source, entry.getValue().hopCount+myDistance(table.source));
					routingTable1.put(entry.getValue().destinationIP, newDest);
				}
				else{
					System.out.println("3");
					if(routingTable1.get(entry.getKey()).hopCount>entry.getValue().hopCount){
						routingTable1.get(entry.getKey()).hopCount=entry.getValue().hopCount;
					}
				}
			}
		}
	}

	public void printTable() {
		//System.out.println("Printing Table");
		for(Entry<String, Destination> entry :routingTable1.entrySet()){
			System.out.println(" Destination "+entry.getValue().destinationIP);
			System.out.println("NextHop "+ entry.getValue().nextIP);
			System.out.println("Cost " +entry.getValue().hopCount);
		}
		System.out.println();
	}

	private int myDistance(String source) {
		for(String neighbors: neighbors){
			if(getIP(neighbors).equals(source)){
				return getWeight(neighbors);
			}
		}
		return 0;
	}

	class StreamHandler extends Thread {
		ObjectInputStream ois;
		ObjectOutputStream oos;
		boolean listening = true;
		int id;
		Socket socket;

		public void getIStream(Socket peer) {
			// get streams on separate threads
			try {
				ois = new ObjectInputStream(peer.getInputStream());
				while (true) {
					Message rec = (Message) ois.readObject();
				//	System.out.println(rec.source + " sent an update");
					updateTable(rec);
					printTable();
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		public void getOStream(Socket peer) {
			// get streams on separate threads
			try {
				oos = new ObjectOutputStream(peer.getOutputStream());
				// call after every one second

				//System.out.println("Sending a new update");
				Message send = new Message(routingTable1, ownIP);
				oos.writeObject(send);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public StreamHandler(int id, Socket socket) {
			this.id = id;
			this.socket = socket;
		}

		public void run() {
			if (id == 1)
				getIStream(socket);
			else
				getOStream(socket);
		}
	}
}
