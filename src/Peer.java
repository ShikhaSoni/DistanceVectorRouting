import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Peer {
	static List<String> neighbors = new ArrayList<String>();
	static int port = 8999;
	Socket me_Client;
	InetAddress server;
	ServerSocket me_Server;

	public static void main(String[] args) {
		if (args.length > 2 || args.length == 0) {
			System.out.println("Enter 2 neighbors");
		}
		for (int neighbor = 0; neighbor < args.length; neighbor++) {
			neighbors.add(args[neighbor]);
		}
		new Peer().establishConnection();
	}

	public void establishConnection() {
		for (String IP : neighbors) {
			try {
				me_Client = new Socket(IP, port);
				new StreamHandler(me_Client);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println("Neighbors not active or unreachable");
				System.out.println("Waiting for them to become active");
				// since the host is not active become a server and wait for
				// connections
				beServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void beServer() {
		try {
			me_Server = new ServerSocket(port);
			while(true){
				new StreamHandler(me_Server.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readObject(Message rec) {
		System.out.println(rec.source);
		
	}
}
