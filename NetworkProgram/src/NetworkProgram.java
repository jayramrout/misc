import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class NetworkProgram {
	public static void main(String[] args) {
		while (true) {
			String log = new Date() + " Online: "
					+ ((testInet("google.com")) || (testInet("amazon.com")));
			//System.out.println(log);
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new FileWriter("NetworkLogTime.log", true)));
				out.println(log);
				out.close();
			} catch (IOException localIOException) {
			}
			try {
				Thread.sleep(60000L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean testInet(String site) {
		Socket sock = new Socket();
		InetSocketAddress addr = new InetSocketAddress(site, 80);
		try {
			sock.connect(addr, 3000);
			return true;
		} catch (IOException e) {
			return false;
		} finally {
			try {
				sock.close();
			} catch (IOException localIOException3) {
			}
		}
	}
}