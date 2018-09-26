package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

public class Sender {
	
    public void send(History history){        
        try {
        	DatagramSocket socket = new DatagramSocket();
        	int seq = 0;
        	while(seq<1105) {
        		
            unicast_packet to_sent = new unicast_packet();
            to_sent.setSeq(seq);
            
            to_sent.setDeparture(System.currentTimeMillis());
            System.out.println( "dep: " + System.currentTimeMillis());
            
            to_sent.setFrom(InetAddress.getLocalHost().getHostAddress().toString());	
            history.insert_sent(to_sent);       
            
            byte[] buf = to_sent.toByteArray();
            
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9002);
            socket.send(packet);
            System.out.println( seq +" sent");
            seq++;
            }
            //socket.close();
        } catch (Exception e) {            
            e.printStackTrace();
        }
    }

    
    public void receive_NAK(History history){
        try {
            System.out.println("Sender start");
            DatagramSocket NAK_socket = new DatagramSocket(9001);
            
            //long time_out = 0; 
            
            while(true){ 
            	//if(time_out == 0) time_out = System.currentTimeMillis();  //Initial received time
            	
            	byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                NAK_socket.receive(packet);
                byte[] data = packet.getData();
                unicast_packet arrival = new unicast_packet(); 
                arrival = arrival.bytes_to_packet(data);
                arrival.setArrival(System.currentTimeMillis());
                System.out.println("receved NAK of " + arrival.getSeq());
                history.insert_NAK(arrival);

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
                s.receive_NAK(history);
            }
        }.start();
    }
    
    static class History {
    	ArrayList <unicast_packet> sent_history = new ArrayList<unicast_packet>(); 
    	
    	ArrayList <unicast_packet> NAK_history = new ArrayList<unicast_packet>(); 
    	
    	
    	//ArrayList<Integer> list1=new ArrayList<Integer>()

    	public synchronized void insert_sent(unicast_packet to_insert ) {
    		
    		sent_history.add(to_insert);

    	}
    	public synchronized void insert_NAK(unicast_packet to_insert ) {
    		
    		NAK_history.add(to_insert);

    	}
    	
    	public float getLossRate () {
    		float result; 
    		int total = sent_history.size();
    		int received = NAK_history.size(); 
    		result = (total - received)/total; 
    		//return (double)( (double)sent_history.size() - (double)NAK_history.size()) / (double)sent_history.size(); 
    	 return (float) (total - received)/total;
    	}
    	
    	public float getAverageRTT () {
    		float totalRTT = 0 ; 
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.NAK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.NAK_history.get(q).getSeq() ) {
    					
    					totalRTT +=  this.NAK_history.get(q).getArrival() 
    								- this.NAK_history.get(q).getProcessing_cost() 
    								- this.NAK_history.get(q).getdeparture();
    				}
    			}
    		}
    		return (float)totalRTT/this.NAK_history.size(); 
    	}
    	
    	public float getRTTVariance () {
    		float averageRTT = this.getAverageRTT(); 
    		float bigfuck = 0;
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.NAK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.NAK_history.get(q).getSeq() ) {
    					
    					bigfuck +=  Math.pow((this.NAK_history.get(q).getArrival() 
    							- this.NAK_history.get(q).getProcessing_cost() 
    							- this.NAK_history.get(q).getdeparture()-averageRTT),2);
    				}
    			}
    		}
    		return bigfuck/(this.NAK_history.size()-1); 
    	}
    }
 
}
