import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;


class StreamHandler extends Thread {
	InputStream is;
	ObjectInputStream ois;
	OutputStream os;
	ObjectOutputStream oos;
	Scanner myInput= new Scanner(System.in);
	boolean listening=true;

	public StreamHandler(Socket peer) {
		//get streams on separate threads
		try {
			is = peer.getInputStream();
			ois = new ObjectInputStream(is);
			os = peer.getOutputStream();
			oos = new ObjectOutputStream(os);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}

