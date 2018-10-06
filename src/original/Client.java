package original;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;

import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

	public static final String serverIP = "13.233.122.179" ; //"13.233.125.32";
	//public static final String serverIP = "192.168.202.20" ;
	public static final int serverListeningPort = 9001;
	public static DatagramSocket connectionBuildSocket;
	public static int localPort; 
	public static String localIP;
	
	public static void main(String[] args) throws Exception {
		connectionBuildSocket = new DatagramSocket();          //local listening port
		localPort = connectionBuildSocket.getLocalPort();
		
		new Thread() {
			public void run() {
				
				
				for (int i = 0; i < 10; i++) {
					unicast_packet toSent = new unicast_packet();
					toSent.setSeq(-1);
					toSent.sendThisPacket(toSent, connectionBuildSocket, serverIP, serverListeningPort); 
				}
				//connectionBuildSocket.close();
				System.out.println("Req sent."); 
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				Client r = new Client();
				r.clinetReceiver();
			}
		}.start();
	}
		
		
	public void clinetReceiver() {
		try {
			System.out.println("Client Starts Listening at" 
					+ connectionBuildSocket.getLocalPort() + ", wait to send ACK");
			
			while (true) {
				byte[] buf = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				connectionBuildSocket.receive(packet);
				long cost1 = System.currentTimeMillis();
				byte[] data = packet.getData();
				unicast_packet arrival = new unicast_packet();
				arrival = arrival.bytes_to_packet(data);
				// System.out.println("receved " + arrival.getSeq()+ " dep = " +
				// arrival.getdeparture());
				arrival.setProcessing_cost(System.currentTimeMillis() - cost1);

				DatagramPacket to_sent_back = new DatagramPacket(arrival.toByteArray(), arrival.toByteArray().length,
						InetAddress.getByName(serverIP), 9001);

				connectionBuildSocket.send(to_sent_back);
				System.out.println(arrival.getSeq() + " ACK sent");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}