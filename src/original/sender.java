package original;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class sender {
    public void send(){        
        try {
            
        	DatagramSocket socket = new DatagramSocket();
        	while(true) {
            String text = "test";
            byte[] buf = text.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getLocalHost(), 9002);
            socket.send(packet);
            System.out.println("sent");
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
                sender s = new sender();
                s.send();
            }
        }.start();
    }

}
