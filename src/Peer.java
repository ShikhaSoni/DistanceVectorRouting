import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Peer {
	static List<String> neighbors = new ArrayList<String>();
	private static List<Destination> routingTable = new CopyOnWriteArrayList<Destination>();
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;
	int port = 6999;
	String ownIP;

	public static void main(String[] args) {
		Peer me = new Peer();
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter 2 neighbors");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			neighbors.add(args[neighbor]);
		}
		me.establishConnection();
	}

	public void establishConnection() {
		for (String destination : neighbors) {
			try {
				ownIP = InetAddress.getLocalHost().toString();
				me_Client = new Socket(destination, port);
				System.out.println("Connected to " + destination);
				Destination dest = new Destination(destination, destination, 1);
				routingTable.add(dest);
				new StreamHandler(1, me_Client).start();
				new StreamHandler(2, me_Client).start();
			} catch (IOException e) {
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
				Destination dest = new Destination(s.getInetAddress().toString(), s.getInetAddress().toString(), 1);
				routingTable.add(dest);
				new StreamHandler(1, s).start();
				new StreamHandler(2, s).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void updateTable(Message table) {
		synchronized (routingTable) {
			for (int index = 0; index < table.routingTable.size(); index++) {
				if (table.routingTable.get(index).destinationIP.equals(ownIP)) {
					continue;
				}
				if (!routingTable.contains(table.routingTable.get(index))) {
					Destination d = new Destination(
							table.routingTable.get(index).destinationIP,
							table.source,
							table.routingTable.get(index).hopCount);
					System.out.println("New Destiantion added");
					routingTable.add(d);
				} else {
					for (int index1 = 0; index1 < routingTable.size(); index1++) {
						if (routingTable.get(index1).destinationIP.equals(table.routingTable.get(index).destinationIP) ){
							if (routingTable.get(index1).hopCount > table.routingTable
									.get(index).hopCount) {
								routingTable.get(index1).hopCount = table.routingTable
										.get(index).hopCount + 1;
							}
						}
					}
				}
			}
		}
	}

	public void printTable() {
		for (int index = 0; index < routingTable.size(); index++) {
			System.out.println("Destiantion: "
					+ routingTable.get(index).destinationIP);
			System.out.println("NextHop: " + routingTable.get(index).nextIP);
			System.out.println("HopCount: " + routingTable.get(index).hopCount);
			System.out.println();
		}
		System.out.println();
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
					System.out.println(rec.source + " sent an update");
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

				System.out.println("Sending a new update");
				Message send = new Message(routingTable, ownIP);
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
