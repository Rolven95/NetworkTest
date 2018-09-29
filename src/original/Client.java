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
	public static final int serverListeningPort = 9001;
	public static String netIP = "192.168.202.20";
	
	public static void main(String[] args) throws Exception {

		new Thread() {
			@Override
			public void run() {
				PacketSender packetsender = new PacketSender();
				DatagramSocket connectionBuildSocket;
				try {
					connectionBuildSocket = new DatagramSocket();
				//netIP = getV4IP();
					System.out.println("netIP is : " + netIP);
					for (int i = 0; i < 10; i++) {
						packetsender.sendPacket(-1, 0, 0, 0, netIP, connectionBuildSocket, serverIP, serverListeningPort);
						try {
							Thread.sleep(100);
						} catch (Exception e) {
							System.exit(0);
						}
					}
					connectionBuildSocket.close();
					System.out.println("Req sent.");
				} catch (SocketException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}.start();
		
		new Thread() {
			@Override
			public void run() {
				Client r = new Client();
				r.clinetSendACK();
			}
		}.start();
	}
	public void clinetSendACK() {
		try {
			System.out.println("Client Starts, wait to send ACK");
			DatagramSocket socket = new DatagramSocket(9002); // Listening channel
			DatagramSocket ACK_socket = new DatagramSocket(); // Sending channel 9001
			while (true) {
				byte[] buf = new byte[2048];
				DatagramPacket packet = new DatagramPacket(buf, buf.length);
				socket.receive(packet);
				long cost1 = System.currentTimeMillis();
				byte[] data = packet.getData();
				unicast_packet arrival = new unicast_packet();
				arrival = arrival.bytes_to_packet(data);
				// System.out.println("receved " + arrival.getSeq()+ " dep = " +
				// arrival.getdeparture());
				arrival.setProcessing_cost(System.currentTimeMillis() - cost1);

				DatagramPacket to_sent_back = new DatagramPacket(arrival.toByteArray(), arrival.toByteArray().length,
						InetAddress.getByName(serverIP), 9001);

				ACK_socket.send(to_sent_back);
				System.out.println(arrival.getSeq() + " ACK sent");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static InetAddress getLocalHostLANAddress() throws Exception {
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							return inetAddr;
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
			// 如果没有发现 non-loopback地址.只能用最次选的方案
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			return jdkSuppliedAddress;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getV4IP() {
	String ip = "";
	String chinaz = "http://ip.chinaz.com/";
	String inputLine = "";
	String read = "";
	try {
			URL url = new URL(chinaz);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			while ((read = in.readLine()) != null) {
				inputLine += read;
			}
			System.out.println(inputLine);
	} catch (Exception e) {
		e.printStackTrace();
	}
	Pattern p = Pattern.compile("\\<strong class\\=\"red\">(.*?)\\<\\/strong>");
	Matcher m = p.matcher(inputLine);
	if(m.find()){
		String ipstr = m.group(1);
		System.out.println(ipstr);
	}
	return ip;
	}
		
}