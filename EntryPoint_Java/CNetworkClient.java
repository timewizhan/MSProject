import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class CNetworkClient {

	private static final String IP = "127.0.0.1";
	private static final int PORT = 3333;
	
	public void start(){
		
		System.out.println("Starting EntryPoint...");
		try {
			
			// 소켓을 생성하여 연결을 요청한다
			Socket socket = new Socket(IP, PORT);
			
			// 소켓의 입력스트림을 얻는다
			//InputStream in = socket.getInputStream();
			//DataInputStream dis = new DataInputStream(in);
			
			// 소켓으로부터 받은 데이터를 출력한다
			//System.out.println("Print a message from the server : " + dis.readUTF());
			//System.out.println("Closing connection...");
		
			// 소켓의 출력 스트림을 얻는다
			OutputStream out = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			
			// 원격 소켓(remote socket)에 데이터를 보낸다
			dos.writeUTF("[NOTICE] Test message1 from BROKER");
			System.out.println("A message was sent");
			
			//dis.close();
			dos.close();
			socket.close();
			System.out.println("Connection was closed");
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
