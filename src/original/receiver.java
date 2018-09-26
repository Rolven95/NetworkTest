package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class receiver {
	public void server(){
        try {
            System.out.println("start===================start");
            DatagramSocket socket = new DatagramSocket(9002);
            while(true){ 
                byte[] buf = new byte[2048];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                String msg = new String(data, 0, packet.getLength());
                System.out.println(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            
        }
    }
    
    public static void main(String[] args) {
        new Thread(){
            @Override
            public void run() {
                receiver r = new receiver();
                r.server();
            }
        }.start();
    }


}