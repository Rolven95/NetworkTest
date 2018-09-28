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

public class Sender {
	
    public void send(History history){        
        try {
        	DatagramSocket socket = new DatagramSocket();
        	int seq = 0;
        	String localIp = "192.168.202.20";
        	while(seq<1105) {
        		
            unicast_packet to_sent = new unicast_packet();
            to_sent.setSeq(seq);
            
            to_sent.setDeparture(System.currentTimeMillis());
            //System.out.println( "dep: " + System.currentTimeMillis());
            
            to_sent.setFrom(localIp);
            //System.out.println( localIp);
            history.insert_sent(to_sent);       
            byte[] buf = to_sent.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
            		InetAddress.getByName("10.0.2.15"), 9002); //192.168.202.191  192.168.109.1
            socket.send(packet);
            System.out.println( seq +" sent");
            seq++;
            }
            //socket.close();
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }
	public void receive_ACK(History history){
        try {
            System.out.println("Sender start");
            DatagramSocket ACK_socket = new DatagramSocket(9001);
            
            //long time_out = 0; 
            
            while(true){ 
            	//if(time_out == 0) time_out = System.currentTimeMillis();  //Initial received time
            	
            	byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                ACK_socket.receive(packet);
                byte[] data = packet.getData();
                unicast_packet arrival = new unicast_packet(); 
                arrival = arrival.bytes_to_packet(data);
                arrival.setArrival(System.currentTimeMillis());
                System.out.println("receved ACK of " + arrival.getSeq());
                history.insert_ACK(arrival);

            }
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    
    public static void main(String[] args) {

    	final History history = new History();
    	
    	new Thread(){
            @Override
            public void run() {
                Sender s = new Sender();
                long start_time = System.currentTimeMillis(); 
                s.send(history);
                while(true) {
                	if((System.currentTimeMillis()-start_time)>5000) {
                    	System.out.println("Loss Rate: " + (float)history.getLossRate() 
                    	+ " Average RTT: " + history.getAverageRTT()
                    	+ " RTTVar: " + history.getRTTVariance());
                    	break;
                    }
                } 
            }
        }.start();
        
        new Thread(){
            @Override
            public void run() {
            	Sender s = new Sender();
                s.receive_ACK(history);
            }
        }.start();
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
