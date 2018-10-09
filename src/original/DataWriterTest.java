package original;

import java.io.IOException;

public class DataWriterTest {

	public static void main(String[] args) throws IOException {
		
		DataWriter d = new DataWriter("F:/","OutputData2.txt");
		long test = 10;
		for(int i =0; i<1000 ; i++) {
			
			d.write(i + "Fuck you" +  " " +test + "\r\n");
			
			
			
		}
		
		d.afterWriting();
		
		System.out.println("So far so good");

	}

}
