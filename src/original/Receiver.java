package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Receiver {
	public void server(){
        try {
            System.out.println("Receiver starts");
        	DatagramSocket socket = new DatagramSocket(9002);
            DatagramSocket ACK_socket = new DatagramSocket();
            while(true){ 
                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                long cost1 = System.currentTimeMillis(); 
                byte[] data = packet.getData();
                unicast_packet arrival = new unicast_packet(); 
                arrival = arrival.bytes_to_packet(data);
                //System.out.println("receved " + arrival.getSeq()+ " dep = " + arrival.getdeparture());
                arrival.setProcessing_cost(System.currentTimeMillis() - cost1);
                
                DatagramPacket to_sent_back = new DatagramPacket(arrival.toByteArray(),
                		arrival.toByteArray().length, InetAddress.getByName(arrival.getFrom()), 9001);
               
                ACK_socket.send(to_sent_back);
                System.out.println( arrival.getSeq() +" ACK sent");
            }
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    public static void main(String[] args) {
        new Thread(){
            @Override
            public void run() {
                Receiver r = new Receiver();
                r.server();
            }
        }.start();
    }
}