package original;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	
	
	public static void main(String[] args){
		try {
		connectedToClientFlag = false; 
		trySendBackFlag = false; 
		oneWayTestFlag = true; 
		sendThreadFlag = false; 
		receiveThreadFlag = false; 
		
		reqFromIP = "";
		reqFromPort = 0; 
		history = new History();
		
			serverRecieveSocket = new DatagramSocket(9001);
		
		
		ExecutorService exec = Executors.newCachedThreadPool(); 
		Thread thread1=new Thread(new RecieveThread("RecieverOne", 999));
		Thread thread2=new Thread(new SendThread("SendThread", 999));
		
		exec.execute(thread1);
		exec.execute(thread2);
		exec.shutdown();
		
		//Daemon daemon=new Daemon(thread1, 3);
		//Thread daemoThread=new Thread(daemon);
		//daemoThread.setDaemon(true);
		//daemoThread.start();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				Thread.sleep(100);	
				System.out.println("Server Listening at port: " + serverRecieveSocket.getLocalPort());
				
				while(true){
					byte[] buf = new byte[2048]; // The maximum size of UDP
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					serverRecieveSocket.receive(packet);    
					// packet.getSocketAddress()
					byte[] data = packet.getData();
					unicast_packet arrival = new unicast_packet(); 
					arrival = arrival.bytes_to_packet(data);  
					arrival.setArrival(System.currentTimeMillis()); 
					
					if(arrival.getType() == -1 && !connectedToClientFlag) {//未建立连接收到req
						System.out.println("Req received and set connectedToClientFlag");
						connectedToClientFlag = true; 
						reqFromIP = packet.getAddress().toString().substring(1); // ???????
						reqFromPort = packet.getPort();
						System.out.println("reqIP: " + reqFromIP + 
		                	" reqFromPort: " + reqFromPort );
						trySendBackFlag = true;    //收到请求 尝试打洞
						//;continue
					}else if(arrival.getType() == 1 && connectedToClientFlag && !oneWayTestFlag) { //双收ack 
						System.out.println("receved ACK of " + arrival.getSeq());
						history.insert_ACK(arrival);      
							
					}else if(arrival.getType() == 0 && connectedToClientFlag){ //单向模式
						history.insert_oneWayHistory(arrival);
					}else if(arrival.getType() == -2){ // 打洞成功
						oneWayTestFlag = false; //开启双向测试模式
						trySendBackFlag = false; //停止打洞尝试
					}else {
						
						System.out.println("one packet ignored");
						
					}
				}	
			}catch(InterruptedException | IOException e){
					System.out.println(name+" is interrupted");
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
						while (uselessCounter < 20 && trySendBackFlag){ // 尝试打洞回复客户端
							
							unicast_packet to_sent = new unicast_packet(-2);
							
							byte[] buf = new byte[2048];

							buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName("192.168.202.20"), reqFromPort); 
				        	System.out.println("Trying to reply to:"+InetAddress.getByName(reqFromIP) 
				        	+ " at "+ reqFromPort);
				        	serverRecieveSocket.send(packet);
				        	uselessCounter ++ ;
				        	Thread.sleep(2);
						}
						trySendBackFlag = false;
						
						Thread.sleep(1500); // 等待客户端回应

					if (!oneWayTestFlag && connectedToClientFlag) { // 打洞成功，双向模式
						System.out.println("One way mode is off, try to send packets");
				        int seq = 0;
				        while(seq < 1000) {
				        	unicast_packet to_sent = new unicast_packet(seq,System.currentTimeMillis(),0,0,"",0);
				        	history.insert_sent(to_sent);       
				        	byte[] buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName(reqFromIP), 9002); //192.168.202.191  192.168.109.1
				        	serverRecieveSocket.send(packet);
				        	System.out.println( seq +" sent to "+reqFromIP + " " + reqFromPort);
				        	seq++;
						}
				        connectedToClientFlag = false;
				        oneWayTestFlag = true;
				      }else {
				    	  
				    	  System.out.println("enter one way mode");
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
				System.out.println("trying to stop thread");	
				thread.interrupt();
				return;
			}
		}
	}
}
