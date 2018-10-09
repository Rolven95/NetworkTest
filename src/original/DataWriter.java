package original;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
	
	
	
public class DataWriter {
			public static BufferedOutputStream Buff = null;
			public static FileOutputStream fileStrm = null;
			public static String path = "";
			public static String fileName = "";
			public static File file;
			
			public DataWriter(String pa , String fi) throws IOException{
				
				path = pa;
				fileName = fi;
				file = new File(path+fileName);
				if(!file.exists()){
					file.createNewFile();
					System.out.println("New file created");
				}else {
					System.out.println("File already exist");
				}
				fileStrm = new FileOutputStream(file, true);
				dataWriter("Test\r\n");
			}
			public static boolean dataWriter( String c) throws IOException {
	            Buff = new BufferedOutputStream(fileStrm);
	            Buff.write(c.getBytes()); 
	            Buff.flush();
	            Buff.close();
				return false;
			}
	}

