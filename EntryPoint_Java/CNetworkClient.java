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
			
			// ������ �����Ͽ� ������ ��û�Ѵ�
			Socket socket = new Socket(IP, PORT);
			
			// ������ �Է½�Ʈ���� ��´�
			//InputStream in = socket.getInputStream();
			//DataInputStream dis = new DataInputStream(in);
			
			// �������κ��� ���� �����͸� ����Ѵ�
			//System.out.println("Print a message from the server : " + dis.readUTF());
			//System.out.println("Closing connection...");
		
			// ������ ��� ��Ʈ���� ��´�
			OutputStream out = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(out);
			
			// ���� ����(remote socket)�� �����͸� ������
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
