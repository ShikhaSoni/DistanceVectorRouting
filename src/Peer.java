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
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;

	public static void main(String[] args) {
		Peer me= new Peer();
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter 2 neighbors");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			Destination n= me.fillNeighbor(args[neighbor]);
			neighbors.add(n);
		}
		me.establishConnection();
	}

	private Destination fillNeighbor(String IP) {
		Destination neighbor = new Destination(parseIP(IP), parseIP(IP), 1, parsePort(IP));
		return neighbor;
	}

	public void establishConnection() {// do it on diff threads
		for (Destination destination : neighbors) {

			try {

				me_Client = new Socket(destination.destinationIP , destination.port);
				ObjectInputStream ois = new ObjectInputStream(
						me_Client.getInputStream());
				ObjectOutputStream oos = new ObjectOutputStream(
						me_Client.getOutputStream());
				/*
				 * ObjectInputStream ois = new StreamHandler()
				 * .getIStream(me_Client); ObjectOutputStream oos = new
				 * StreamHandler() .getOStream(me_Client);
				 */
				Message send = new Message("Hi");
				oos.writeObject(send);
				Message rec = (Message) ois.readObject();
				System.out.println(rec.source);
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Neighbors not active or unreachable");
				System.out.println("Waiting for them to become active");
				// since the host is not active become a server and wait for
				// connections
				beServer(destination.port);
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
				/*ObjectOutputStream oos = new StreamHandler().getOStream(s);
				ObjectInputStream ois = new StreamHandler().getIStream(s);
				*/
				ObjectOutputStream oos = new ObjectOutputStream(
				s.getOutputStream()); ObjectInputStream ois = new
				ObjectInputStream( s.getInputStream());
				Message rec = (Message) ois.readObject();
				System.out.println(rec.source);
				Message send = new Message("Wassup");
				oos.writeObject(send);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readObject(Message rec) {
		System.out.println(rec.source);
	}

	public String parseIP(String IP) {
		return IP.split(":")[0];

	}

	public int parsePort(String IP) {
		return Integer.parseInt(IP.split(":")[1]);

	}
}
