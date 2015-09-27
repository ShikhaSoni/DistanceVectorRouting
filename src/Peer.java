import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Peer {
	static List<Destination> neighbors = new ArrayList<Destination>();
	static List<Destination> routingTable = new ArrayList<Destination>();
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;
	int port = 7999;
	String ownIP;

	public static void main(String[] args) {
		Peer me = new Peer();
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter 2 neighbors");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			Destination n = me.fillNeighbor(args[neighbor]);
			neighbors.add(n);
			routingTable.add(n);
		}
		me.establishConnection();
	}

	private Destination fillNeighbor(String IP) {
		Destination neighbor = new Destination(IP, IP, 1);
		return neighbor;
	}

	public void establishConnection() {
		for (Destination destination : neighbors) {
			try {
				ownIP = InetAddress.getLocalHost().toString();
				me_Client = new Socket(destination.destinationIP, port);
				System.out.println("Connected to "+destination.destinationIP);
				new StreamHandler(1, me_Client).start();
				new StreamHandler(2, me_Client).start();
			} catch (IOException/* | ClassNotFoundException */e) {
				System.out.println("Waiting for neighbors to become active");
				System.out.println(e);
				// since the host is not active become a server and wait for
				// connections
				beServer(port);
			}
		}
	}

	public void beServer(int port) {
		Socket s;
		try {
			me_Server = new ServerSocket(port);
			while (true) {
				s = me_Server.accept();
				System.out.println("Recived connection from: "
						+ s.getInetAddress().toString());
				new StreamHandler(1, s).start();
				new StreamHandler(2, s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void updateTable(Message table){
		
	}
	class StreamHandler extends Thread {
		ObjectInputStream ois;
		ObjectOutputStream oos;
		boolean listening=true;
		int id;
		Socket socket;

		public void getIStream(Socket peer) {
			//get streams on separate threads
			try {
				ois = new ObjectInputStream(peer.getInputStream());
				Message rec=(Message)ois.readObject();
				System.out.println(rec.source);
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		public  void getOStream(Socket peer) {
			//get streams on separate threads
			try {
				oos = new ObjectOutputStream( peer.getOutputStream());
				Message m = new Message(routingTable, ownIP);
				oos.writeObject(m);
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
		public StreamHandler(int id, Socket socket){
			this.id=id;
			this.socket=socket;
		}
		public void run(){
			if(id==1)
				getIStream(socket);
			else
				getOStream(socket);
		}
	}
}
