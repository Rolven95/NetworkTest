package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Sender {
    public void send(){        
        try {
            
        	DatagramSocket socket = new DatagramSocket();
        	int seq = 0;
        	while(seq<1105) {
        		
            unicast_packet to_sent = new unicast_packet();
            to_sent.setSeq(seq);
            
            to_sent.setDeparture(System.currentTimeMillis());
            System.out.println( "dep: " + System.currentTimeMillis());
            
            to_sent.setFrom(InetAddress.getLocalHost().getHostAddress().toString());	
                        
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
    public static void main(String[] args) {
    	new Thread(){
            @Override
            public void run() {
                Sender s = new Sender();
                s.send();
            }
        }.start();
    }
 
}
