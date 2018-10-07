package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

public class Server {
	
	public static boolean reqFlag = false; 
	public static String reqFromIP = "";
	public static int reqFromPort = 0;
	//public static final String localIp ="13.233.125.32";
			//"13.233.125.32";
	
	public static void main(String[] args) {
		
    	final History history = new History();
    	//-------------------------------------------------------------------Thread 1 
    	new Thread(){
            @Override
            public void run() {
                Server s = new Server();
                while(true) {
                	if(reqFlag) {
                		System.out.println("start sending");
                		s.send(history);
                		break;
                	}else {
                		try {
        					Thread.sleep(1000);
        				} catch (Exception e) {
        					System.exit(0);
        				}
                		System.out.println("req == false");
                		continue;
                	}
                }
            }
        }.start();
        //------------------------------------------------------------------Thread 2
        new Thread(){
            @Override
            public void run() {
            	Server s = new Server();
                s.receive_packet(history);
            }
        }.start();
    }
		//------------------------------------------------------------------Thread 3
    public void send(History history){
        try {
        	DatagramSocket socket = new DatagramSocket();
        	int seq = 0;
        	//String tempLocalIP = localIp;
        	try {
				Thread.sleep(1000);
			} catch (Exception e) {
				System.exit(0);
			}
        	while(seq < 1000) {
        		unicast_packet to_sent = new unicast_packet(0,0,0,0,"");
        		to_sent.setSeq(seq);
        		to_sent.setDeparture(System.currentTimeMillis());
        		to_sent.setFrom("0");
        		//System.out.println( localIp);
        		history.insert_sent(to_sent);       
        		byte[] buf = to_sent.toByteArray();
        		DatagramPacket packet = new DatagramPacket(buf, buf.length,
            		InetAddress.getByName(reqFromIP), reqFromPort); //192.168.202.191  192.168.109.1
        		socket.send(packet);
        		System.out.println( seq +" sent to "+reqFromIP + " " + reqFromPort);
        		seq++;
        		try {
					Thread.sleep(100);
				} catch (Exception e) {
					System.exit(0);
				}
            }
        	socket.close();
        	long start_time = System.currentTimeMillis(); 
 
        	System.out.println("Sending Finished, Servise Stopped for Data Recoding");
        	while(true) {
            	if((System.currentTimeMillis()-start_time)>10000) {
                	System.out.println("Loss Rate: " + (float)history.getLossRate() 
                	+ " Average RTT: " + history.getAverageRTT()
                	+ " RTTVar: " + history.getRTTVariance());
                	break;
                }
            }
        	//reqFlag = false; 
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }
	public void receive_packet(History history){
        try {
            System.out.println("Server Listening Starts");
            DatagramSocket serverListeningSocket = new DatagramSocket(9001);
            while(true){
            	byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverListeningSocket.receive(packet);    
               // packet.getSocketAddress()
                byte[] data = packet.getData();
                unicast_packet arrival = new unicast_packet(0,0,0,0,""); 
                arrival = arrival.bytes_to_packet(data);       //Already put received packet in :"arrival"
                arrival.setArrival(System.currentTimeMillis());
                
                if(arrival.getSeq() == -1 && !reqFlag) {
                	System.out.println("Req received");
                	reqFlag = true; 
                	//reqFromIP = arrival.getFrom();
                	reqFromIP = packet.getAddress().toString().substring(1); // ??????????????????????????????????????????????????????????????
                			//.getHostAddress();
                	reqFromPort = packet.getPort();
                	System.out.println("reqIP: " + reqFromIP + 
                			" reqFromPort: " + reqFromPort );
                	continue;
                }else if(arrival.getSeq() != -1 && reqFlag) {
                	System.out.println("receved ACK of " + arrival.getSeq());
                	history.insert_ACK(arrival);            	
                }else {
                	continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
    static class History {
    	ArrayList <unicast_packet> sent_history = new ArrayList<unicast_packet>(); 
    	ArrayList <unicast_packet> ACK_history = new ArrayList<unicast_packet>(); 
    	//ArrayList<Integer> list1=new ArrayList<Integer>()
    	
    	public synchronized void insert_sent(unicast_packet to_insert ) {
    		sent_history.add(to_insert);
    	}
    	
    	public synchronized void insert_ACK(unicast_packet to_insert ) {	
    		ACK_history.add(to_insert);
    	}
    	
    	public float getLossRate () {
    		float result; 
    		int total = sent_history.size();
    		int received = ACK_history.size(); 
    		result = (total - received)/total; 
    		//return (double)( (double)sent_history.size() - (double)ACK_history.size()) / (double)sent_history.size(); 
    	 return (float) (total - received)/total;
    	}
    	
    	public float getAverageRTT () {
    		float totalRTT = 0 ; 
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.ACK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.ACK_history.get(q).getSeq() ) {
    					
    					totalRTT +=  this.ACK_history.get(q).getArrival() 
    								- this.ACK_history.get(q).getProcessing_cost() 
    								- this.ACK_history.get(q).getdeparture();
    				}
    			}
    		}
    		return (float)totalRTT/this.ACK_history.size(); 
    	}
    	
    	public float getRTTVariance () {
    		float averageRTT = this.getAverageRTT(); 
    		float bigfuck = 0;
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.ACK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.ACK_history.get(q).getSeq() ) {
    					
    					bigfuck +=  Math.pow((this.ACK_history.get(q).getArrival() 
    							- this.ACK_history.get(q).getProcessing_cost() 
    							- this.ACK_history.get(q).getdeparture()-averageRTT),2);
    				}
    			}
    		}
    		return bigfuck/(this.ACK_history.size()-1); 
    	}
    }
 
}
