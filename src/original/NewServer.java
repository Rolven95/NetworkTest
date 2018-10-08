package original;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import original.DemonThread.Daemon;
import original.DemonThread.Task;


public class NewServer{
	
	public static boolean connectedToClientFlag; 
	public static boolean trySendBackFlag; 
	public static boolean oneWayTestFlag; 
	public static boolean sendThreadFlag; 
	public static boolean receiveThreadFlag; 
	
	public static String reqFromIP;
	public static int reqFromPort; 
	public static History history;
	public static DatagramSocket serverRecieveSocket;
	public static void main(String[] args) throws SocketException {
		
		connectedToClientFlag = false; 
		trySendBackFlag = false; 
		oneWayTestFlag = true; 
		sendThreadFlag = false; 
		receiveThreadFlag = false; 
		
		reqFromIP = "";
		reqFromPort = 0; 
		history = new History();
		serverRecieveSocket = new DatagramSocket(9001);
		
		RecieveThread task1=new RecieveThread("RecieverOne", 999); //TODO
		
		Thread thread1=new Thread(task1);
		
		SendThread SendThread =new SendThread("SendThread", 999);
		
		Thread thread2=new Thread(SendThread);
		
		thread1.start();
		thread2.start();
		//Daemon daemon=new Daemon(Thread1, 999);
		//Thread daemoThread=new Thread(daemon);
		
		
		//daemoThread.setDaemon(true);
		
		//daemoThread.start();
	}
	

	static class RecieveThread implements Runnable{
		
		private String name;
		private int time;
		public RecieveThread(String s,int t) {
			name=s;
			time=t;
		}
		public int getTime(){
			return time;
		}
		public void run () {
			try {//------------------------------------------main course of this thread. 
				Thread.sleep(1000);	
				System.out.println("Server Listening Starts");
				
				while(true){
					byte[] buf = new byte[65507]; // The maxium size of UDP
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					serverRecieveSocket.receive(packet);    
					// packet.getSocketAddress()
					byte[] data = packet.getData();
					unicast_packet arrival = new unicast_packet(0,0,0,0,""); 
					arrival = arrival.bytes_to_packet(data);  
					arrival.setArrival(System.currentTimeMillis()); 
					if(arrival.getSeq() == -1 && !connectedToClientFlag) {
						System.out.println("Req received");
						connectedToClientFlag = true; 
						reqFromIP = packet.getAddress().toString().substring(1); // ???????
						reqFromPort = packet.getPort();
						System.out.println("reqIP: " + reqFromIP + 
		                	" reqFromPort: " + reqFromPort );
						trySendBackFlag = true;    // 收到请求 尝试打洞
						continue;
					}else if(arrival.getSeq() >=0 && connectedToClientFlag && !oneWayTestFlag) { //收到包 双向模式 
						System.out.println("receved ACK of " + arrival.getSeq());
						history.insert_ACK(arrival);      
							
					}else if(arrival.getSeq() >=0 && connectedToClientFlag && oneWayTestFlag){ // 收到包 单向模式
						history.insert_oneWayHistory(arrival);
					}else if(arrival.getSeq() == -2){ // 打洞成功
						oneWayTestFlag = false; //开启双向测试模式
						trySendBackFlag = false; // 停止打洞尝试
					}
				}	
			}catch(InterruptedException | IOException e){
					System.out.println(name+" is interrupted, calculating data");
					//TODO 
					//add data analysis
					return; //注意这里如果不return的话，线程还会继续执行，所以任务超时后在这里处理结果然后返回
			}
		}
	}
	
	static class SendThread implements Runnable{
		private String name;
		private int time;
		public SendThread(String s,int t){
			name=s;
			time=t;
		}
		public int getTime(){
			return time;
		}
		public void run() {
				try {//------------------------------------------main course of this thread. 
					
					while (true) { // 此线程不需关闭
						
						int uselessCounter = 0;
						while (uselessCounter < 100 && trySendBackFlag){ // 尝试打洞回复客户端
							unicast_packet to_sent = new unicast_packet(-2 ,System.currentTimeMillis(),0,0,"");
							byte[] buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName(reqFromIP), reqFromPort); 
				        	uselessCounter ++ ;
				        	Thread.sleep(5);
						}
						
						Thread.sleep(2000); // 等待客户端回应

					if (!oneWayTestFlag && connectedToClientFlag) {
						System.out.println("One way mode is off, try to send packets");
				        int seq = 0;
				        while(seq < 1000) {
				        	unicast_packet to_sent = new unicast_packet(seq,System.currentTimeMillis(),0,0,"");
				        	history.insert_sent(to_sent);       
				        	byte[] buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName(reqFromIP), reqFromPort); //192.168.202.191  192.168.109.1
				        	serverRecieveSocket.send(packet);
				        	System.out.println( seq +" sent to "+reqFromIP + " " + reqFromPort);
				        	seq++;
						}
				      }
					}
					//------------------------------------------ end of this thread. 
				}catch(InterruptedException | IOException e){
					System.out.println(name+" is interrupted");
					return; //注意这里如果不return的话，线程还会继续执行，所以任务超时后在这里处理结果然后返回
				}
		}
	}
	
	static class Daemon implements Runnable{
		List<Runnable> tasks=new ArrayList<Runnable>();
		private Thread thread;
		private int time; // The runtime of the monitored thread. But at this point only one thread could be fucked
		
		public Daemon(Thread r,int t) {
			thread=r;time=t;
		}
		public void addTask(Runnable r){
				tasks.add(r);
		}
		
		@Override
		public void run() {
			while(true){
				try{
					Thread.sleep(time*1000);
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
					thread.interrupt();
			}
		}
	}
}
