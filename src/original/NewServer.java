package original;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewServer{
	public static final int packetLength = 512;
	public static boolean connectedToClientFlag; 
	public static boolean trySendBackFlag; 
	public static boolean oneWayTestFlag; 
	public static boolean sendThreadFlag; 
	public static boolean receiveThreadFlag; 
	public static boolean oneWayTimeOutFlag;
	public static String reqFromIP;
	public static int reqFromPort; 
	public static History history;
	public static DatagramSocket serverRecieveSocket;
	public static DataWriter dataWriter;
	public static int connectionID;
	public static String temStr = "";
	public static void main(String[] args) throws IOException{
		try {
		connectedToClientFlag = false; 
		trySendBackFlag = false; 
		oneWayTestFlag = true;  // mode switch flag 
		oneWayTimeOutFlag = false;
		sendThreadFlag = false; 
		receiveThreadFlag = false; 
		
		reqFromIP = "";
		
		reqFromPort = 0; 
		connectionID = 0;
		history = new History();
		
		dataWriter = new DataWriter("F:/","OutputData003.txt");
		
		serverRecieveSocket = new DatagramSocket(9001);
		
		ExecutorService exec = Executors.newCachedThreadPool(); 
		
		Thread thread1=new Thread(new RecieveThread("RecieverOne", 999));
		Thread thread2=new Thread(new SendThread("SendThread", 999));
		
		exec.execute(thread1);
		exec.execute(thread2);
		exec.shutdown();
		
		Daemon daemon=new Daemon();
		Thread daemoThread=new Thread(daemon);
		daemoThread.setDaemon(true);
		daemoThread.start();
		
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	static class RecieveThread implements Runnable{
		private String name;                       //Useless 
		private int time;
		public RecieveThread(String s,int t) { //Useless 
			name=s;
			time=t;
		}
		public int getTime(){ //Useless 
			return time;
		}
		public void run () {
			try {//------------------------------------------main course of this thread. 
				Thread.sleep(100);	
				System.out.println("Server Listening at port: " + serverRecieveSocket.getLocalPort());
				
				while(true){
					byte[] buf = new byte[packetLength]; // The maximum size of UDP
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					serverRecieveSocket.receive(packet);    
					// packet.getSocketAddress()
					byte[] data = packet.getData();
					unicast_packet arrival = new unicast_packet(); 
					arrival = arrival.bytes_to_packet(data);  
					arrival.setArrival(System.currentTimeMillis()); 
					
					if(arrival.getType() == -1 && !connectedToClientFlag) {//�յ�req
						System.out.println("Req received and set connectedToClientFlag");
						connectedToClientFlag = true; 
						reqFromIP = packet.getAddress().toString().substring(1); // ???????
						reqFromPort = packet.getPort();
						System.out.println("reqIP: " + reqFromIP + 
		                	" reqFromPort: " + reqFromPort );
						trySendBackFlag = true;    //�յ����� ���Դ�
						//;continue
					}else if(arrival.getType() == 1 && connectedToClientFlag && !oneWayTestFlag) { //˫��ack 
						System.out.println("receved ACK of " + arrival.getSeq());//
						arrival.setNakArrival(System.currentTimeMillis());
						history.insert_ACK(arrival);
					}else if(arrival.getType() == 0 && connectedToClientFlag){ //����ģʽ
						System.out.println(arrival.getSeq() + "recieved (one way mode)");
						oneWayTimeOutFlag = true ;
						//arrival.setArrival(Systemcurrenttime);
						history.insert_oneWayHistory(arrival);
					}else if(arrival.getType() == -2){ // �򶴳ɹ�
						oneWayTestFlag = false; //����˫�����ģʽ
						trySendBackFlag = false; //ֹͣ�򶴳���
					}else {
						System.out.println(" one packet ignored");
						System.out.println(" Server ignored: " + arrival.getSeq() + " Type: " +arrival.getType()
											+ " conntionFlag: " + connectedToClientFlag
											+ " onewayFlag: " +  oneWayTestFlag);
					}
				}
			}catch(InterruptedException | IOException e){
					System.out.println(name+" is interrupted");
					
					return; //ע�����������return�Ļ����̻߳������ִ�У���������ʱ�������ﴦ����Ȼ�󷵻�
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
				try {
					while(true){//���̲߳���ر�
						int uselessCounter = 0;
						while (uselessCounter < 20 && trySendBackFlag){ // ���Դ򶴻ظ��ͻ���
							unicast_packet to_sent = new unicast_packet(-2);
							byte[] buf = new byte[packetLength];
							buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName(reqFromIP), reqFromPort); 
				        	System.out.println("Trying to reply to:"+InetAddress.getByName(reqFromIP) 
				        	+ " at "+ reqFromPort);
				        	serverRecieveSocket.send(packet);//**************************************
				        	uselessCounter ++ ;
				        	Thread.sleep(2);
						}
						trySendBackFlag = false;
						Thread.sleep(1500);//�ȴ��ͻ��˻�Ӧ
						
					if (!oneWayTestFlag && connectedToClientFlag) {//�򶴳ɹ���˫��ģʽ����ʼ����
						System.out.println("Server: Sender in dup mode");
						Thread.sleep(5000);
				        int seq = 0;
				        while(seq < 1000) {
				        	unicast_packet to_sent = new unicast_packet(seq,System.currentTimeMillis(),0,0,"",0);
				        	history.insert_sent(to_sent);       
				        	byte[] buf = to_sent.toByteArray();
				        	DatagramPacket packet = new DatagramPacket(buf, buf.length,
				            	InetAddress.getByName(reqFromIP), reqFromPort); //192.168.202.191  192.168.109.1
				        	serverRecieveSocket.send(packet);
				        	System.out.println( seq +" sent to "+reqFromIP + " " + reqFromPort);
				        	seq++;
						}
				        System.out.println("Server dup mode finished. Waiting 10s");
				        Thread.sleep(10000); //˫���������� �ȴ�����
				        connectedToClientFlag = false;
				        oneWayTestFlag = true;
				        
				        //TODO
				        for (int i =0 ; i < history.ACK_history.size(); i++) {
				        	dataWriter.write(connectionID + " " //id 
				        			 + 1 + " " 				//mode
				        			 + history.ACK_history.get(i).getdeparture() + " "
				        			 + history.ACK_history.get(i).getArrival() + " "
				        			 + history.ACK_history.get(i).getNakArrival() + " "
				        			 + history.ACK_history.get(i).getSeq() + " "
				        			 + "\r\n");
				        }
				        
				        history.ACK_history.clear();
				        history.sent_history.clear();
				        history.oneWay_history.clear(); //just in case
				        System.out.println("Server: Data output done, history cleared, connection id:" + connectionID);
				        connectionID ++;
				      }else {
				    	  System.out.println("Sender enter one way mode");
				      }
					}
				}catch(InterruptedException | IOException e){
					System.out.println(name+" is interrupted");
					return; 
				}
		}
	}
	
	static class Daemon implements Runnable{
		@Override
		public void run() {
			while(true){
				try{
					System.out.println("Deamon online");
					while(true) {
						Thread.sleep(1000);
						System.out.println("Deamon idling");
						if(connectedToClientFlag && oneWayTestFlag &&!trySendBackFlag) { // connection built
							System.out.println("Deamon find one way mode is on");
							Thread.sleep(3000); // wait some time
							while(oneWayTimeOutFlag) { // check one way mode flag 
								System.out.println("Deamon: oneway running");
								oneWayTimeOutFlag = false;
								Thread.sleep(1000); // time out
							}
							System.out.println("Deamon�� Server oneway timeout!!!!");
							oneWayTimeOutFlag = false ;
							connectedToClientFlag = false;
							//TODO
							System.out.println("Server: Oneway mode ended, One way timeout, connection break"
									+ ", start data analyzing");
					        for (int i =0 ; i < history.oneWay_history.size(); i++) {
					        	dataWriter.write(connectionID + " " //id 
					        			 + 0 + " " 				//mode
					        			 + history.oneWay_history.get(i).getdeparture() + " "
					        			 + history.oneWay_history.get(i).getArrival() + " "
					        			 + history.oneWay_history.get(i).getSeq() + " "
					        			 + "\r\n");
					        }
					        //dataWriter.afterWriting();
					        connectionID ++;
							//history.clearOneWayHistory();
							history.ACK_history.clear();
							history.sent_history.clear();
							history.oneWay_history.clear();
							
							System.out.println("Server: Data output done, all history cleared, connection id:" + connectionID);
						}
					}					
				}catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
				System.out.println("Deamon dead");	
				//thread.interrupt();
				return;
			}
		}
	}
}
