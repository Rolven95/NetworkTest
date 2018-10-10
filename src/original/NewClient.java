package original;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewClient {
	public static final String serverIP = "192.168.1.105" ; //"13.233.125.32";
	public static final int serverPort = 9001;
	public static final int packetLength = 512;
	public static DatagramSocket clientSocket;
	public static int localPort; 
	public static String localIP;

	public static boolean reqSentFlag = false;
	public static boolean oneWayMode = true;
	//public static boolean reqSentFlag = false;
	public static void main(String[] args) throws Exception {
		
		clientSocket = new DatagramSocket(9002);
		localPort = clientSocket.getLocalPort();
		localIP = InetAddress.getLocalHost().getHostAddress().toString();
		
		ExecutorService exec = Executors.newCachedThreadPool(); 
		Thread thread1=new Thread(new ClientReciever());
		Thread thread2=new Thread(new ClientSender());
		
		exec.execute(thread1);
		exec.execute(thread2);
		exec.shutdown();
	}
		
	static class ClientReciever implements Runnable{
		@Override
		public void run() {
			try {
				System.out.println("Clilent listener Online");
				System.out.println("Client listening at: "+ localIP 
									+ " : "+ localPort);
				while(true) {
					byte[] buf = new byte[packetLength]; // The maxium size of UDP is 65507, 视线中
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					clientSocket.receive(packet);
				
					byte[] data = packet.getData();
					unicast_packet arrival = new unicast_packet(); 
					arrival = arrival.bytes_to_packet(data);
			
					if(arrival.getType() == -2) {//服务端收到req 返回打洞尝试
						oneWayMode = false; 
						System.out.println("client in duplex mode");
						for(int i = 0 ;i < 5 ; i++){
							//byte[] buf = new byte[packetLength];
							unicast_packet to_sent = new unicast_packet(-2);
							//System.out.println("clent type = " + to_sent.getType());
							buf = to_sent.toByteArray();
							System.out.println("dup notif sent to  " + serverIP +":" + serverPort);
							DatagramPacket tosent = new DatagramPacket(buf, buf.length,
									InetAddress.getByName(serverIP), serverPort); //192.168.202.191  192.168.109.1
							clientSocket.send(tosent);
						}
					}else if(arrival.getType() == 0) {//收到Data 返回ACK
						oneWayMode = false; 
						//System.out.println("client enter duplex mode");
						unicast_packet to_sent = arrival;
						arrival.seType(1);
						arrival.setArrival(System.currentTimeMillis());
						buf = to_sent.toByteArray();
						DatagramPacket tosent = new DatagramPacket(buf, buf.length,
								InetAddress.getByName(serverIP), serverPort); //192.168.202.191  192.168.109.1
						//Thread.sleep(2000); 
						clientSocket.send(tosent);
						System.out.println(arrival.getSeq() + " ACK sent back");
					} else {
						System.out.println("recieved a shit");
						
					}
				}
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static class ClientSender implements Runnable{
		@Override
		public void run() {
			try {
				if (!reqSentFlag) {
					for(int i = 0 ;i < 5 ; i++){
						byte[] buf = new byte[packetLength];
						unicast_packet to_sent = new unicast_packet(-1, -1);
						//System.out.println("clent type = " + to_sent.getType());
						buf = to_sent.toByteArray();
						System.out.println("req sent to " + serverIP +" at " + serverPort);
						DatagramPacket tosent = new DatagramPacket(buf, buf.length,
							InetAddress.getByName(serverIP), serverPort); //192.168.202.191  192.168.109.1
						clientSocket.send(tosent);
					}
				}
				
				Thread.sleep(2000);	
				
				if(oneWayMode) {
					System.out.println("Client sender on one way mode, start send shit to server");
					for(int i = 0 ;i<10000 ; i++){
						byte[] buf = new byte[packetLength];
						unicast_packet to_sent = new unicast_packet(i,0);
						to_sent.setDeparture(System.currentTimeMillis());
						buf = to_sent.toByteArray();
						System.out.println(i + " sent to " + serverIP +" at " + serverPort);
						DatagramPacket tosent = new DatagramPacket(buf, buf.length,
							InetAddress.getByName(serverIP), serverPort); //192.168.202.191  192.168.109.1
						clientSocket.send(tosent);
						//Thread.sleep(10);
					}
				}
				
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}
}
