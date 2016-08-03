import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CConnectionWrap implements Runnable{

	private Socket socket = null;
	
	public CConnectionWrap (Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			
			//java.io.OutputStream stream = socket.getOutputStream();
			//stream.write(null);
			
			// ������ �Է½�Ʈ���� ��´�.
			InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);	
		
            // �������κ��� ���� �����͸� ����Ѵ�.
            System.out.println("Ŭ���̾�Ʈ�� ���� ���� �޼��� : " + dis.readUTF());
            dis.close();
            
            /**
             * Monitoring ����� DB���� �о���� �κ�
             * socket.getInetAddress() ���� ��� IP �˾Ƴ��� �ű��� DB�� ���� 
             */
           // Counter.addRecvCompletedCount();
            Counter.GetInstance().addRecvCompletedCount();
            System.out.println(Counter.GetInstance().getRecvCompletedCount());
            
            //Monitoring ��� �� �޾Ҵٰ� EP���� �޼��� ����
            // ������ ��� ��Ʈ���� ��´�
 			OutputStream out = socket.getOutputStream();
 			DataOutputStream dos = new DataOutputStream(out);
 			
 			// ���� ����(remote socket)�� �����͸� ������
 			dos.writeUTF("[NOTICE] Test message1 from BROKER");
 			System.out.println("A message was sent");
 			
 			dos.close();
            
            System.out.println("������ �����մϴ�.");
            socket.close();
            
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
