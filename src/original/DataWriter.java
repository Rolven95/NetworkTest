package original;

	import java.io.FileOutputStream;
	import java.io.FileInputStream;
	import java.io.IOException;
	import java.io.InputStreamReader;
	import java.io.OutputStreamWriter;
	
	
	
public class DataWriter {
	
		public static void main(String[] args) throws IOException {
			
			String file="F:/stream.txt";
			String charSet="UTF-8";
			//д�ַ�ת�����ֽ���
			FileOutputStream fileWriter=new FileOutputStream(file);
			OutputStreamWriter writer=new OutputStreamWriter(fileWriter, charSet);
			
			try {
				writer.write("���������ַ���1");
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				writer.close();
			}
			
			//��ȡ�ֽ�ת�����ַ�
			FileInputStream fileInputStream=new FileInputStream(file);
			InputStreamReader reader=new InputStreamReader(fileInputStream, charSet);
			StringBuilder builder=new StringBuilder();
			char [] buf=new char[64];
			int count=0;
			try {
				while ((count = reader.read(buf)) != -1) {
					builder.append(buf,0,count);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				reader.close();
			}
			System.out.println(builder.toString());
		}
	}

