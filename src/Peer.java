import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class Peer extends Thread{
	List<String> neighbors = new ArrayList<String>();
	ConcurrentHashMap<String, Destination> routingTable1= new ConcurrentHashMap<String, Destination>();
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;
	int port = 5999;
	String ownIP;

	public static void main(String[] args) {
		Peer me = new Peer();
		try {
			me.ownIP = InetAddress.getLocalHost().getHostAddress();
			System.out.println(InetAddress.getLocalHost().toString());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter at least 1 or at most 2 neighbors and their link weights");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			me.neighbors.add(args[neighbor]);
		}
		me.establishConnection();
	}
	public void establishConnection() {
		System.out.println("here");
		int count=0;
		for(String destination:neighbors){
			try {
				me_Client = new Socket(getIP(destination), port);
				System.out.println("Connecting to  "+getIP(destination));
				System.out.println("Weight of the destination: "+  getWeight(destination));
				Destination dest = new Destination(getIP(destination), getIP(destination), getWeight(destination));
				routingTable1.put(getIP(destination), dest);
				new StreamHandler(1, me_Client).start();
				new StreamHandler(2, me_Client).start();
			} catch (IOException e) {
				System.out.println(destination+" not active" + e);
				count++;
				continue;
			}
		}
		if(count>0){
			beServer();
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
				System.out.println("Recived connection from: "
						+ s.getInetAddress().getHostAddress());
				Destination dest = new Destination(s.getInetAddress().getHostAddress(), s.getInetAddress().getHostAddress(), myDistance(s.getInetAddress().getHostAddress()));
				routingTable1.put(s.getInetAddress().getHostAddress(), dest);
				new StreamHandler(1, s).start();
				new StreamHandler(2, s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public String checkIP(String IP){
		if(IP.charAt(0)=='/'){
			return IP.substring(1);
		}
		return IP;
	}

	public void updateTable(Message table) {
		//1. check if ownIP is same as destinationIP
		//2. check if the IP does  not exist
		//check if count is smaller 
		//System.out.println("Updating Table");
		for(Entry<String, Destination> entry : table.routingTable.entrySet()){
			if(entry.getKey().toString().equals(ownIP))
				continue;
			else{
				if(routingTable1.get(entry.getKey()) == null){
					System.out.println("New Destination added");
					Destination newDest= new Destination(entry.getValue().destinationIP, table.source, entry.getValue().hopCount+myDistance(table.source));
					routingTable1.put(entry.getValue().destinationIP, newDest);
				}
				else{
					System.out.println("hopcount comparison");
					System.out.println(routingTable1.get(entry.getKey()).hopCount+" : "+ (entry.getValue().hopCount)+myDistance(table.source));
					if((routingTable1.get(entry.getKey()).hopCount)>(entry.getValue().hopCount)+myDistance(table.source)){
						System.out.println("hopcount less received");
						routingTable1.get(entry.getKey()).hopCount=entry.getValue().hopCount+myDistance(table.source);
						routingTable1.get(entry.getKey()).nextIP=table.source;
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
			System.out.println("");
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
					System.out.println("Update received with table size "+rec.routingTable.size());
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
