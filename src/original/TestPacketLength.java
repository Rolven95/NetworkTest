package original;

import java.net.UnknownHostException;

public class TestPacketLength {
	
	
	public static int packetLength =2048;
	
	

	public static void main(String[] args) throws UnknownHostException {
		
		String testText = "Piece of shit22222222222222222";
		
		unicast_packet testpacket = new unicast_packet(); 
		
		byte[] buf = new byte[packetLength];
		
		buf = testText.getBytes();
		//int 
		System.out.println(testpacket.toByteArray(testpacket.getType()).length);
		
	}
 
}
