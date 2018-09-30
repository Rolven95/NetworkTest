package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import original.Server.History;

public class PacketSender {
	
	public unicast_packet sendPacket(int seq, long dep, long arr, long cost, String localIP, DatagramSocket socket, String targetIP, int port){
		
		try {
            unicast_packet to_sent = new unicast_packet();
            
            to_sent.setSeq(seq);
            to_sent.setDeparture(System.currentTimeMillis());
            to_sent.setArrival(arr);
            to_sent.setProcessing_cost(cost);
            to_sent.setFrom(localIP);
            
            byte[] buf = to_sent.toByteArray();
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
            		InetAddress.getByName(targetIP), port); 
            
            socket.send(packet);
            System.out.println( seq +" sent");
        	return to_sent;
        	
        } catch (Exception e) {            
            e.printStackTrace();
        }
        return null;
    }
	
	
}
