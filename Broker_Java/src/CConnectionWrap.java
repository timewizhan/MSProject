import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

public class CConnectionWrap implements Runnable{

	static Logger log = Logger.getLogger(CBroker.class.getName());
	private Socket socket = null;
	
	public CConnectionWrap (Socket socket){
		this.socket = socket;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			// ������ �Է½�Ʈ���� ��´�.
			InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);	
		
            // �������κ��� ���� �����͸� ����Ѵ�.
            log.info("	* Message from Entry Point : " + dis.readUTF());
            dis.close();
            
            /**
             * Monitoring ����� DB���� �о���� �κ�
             * socket.getInetAddress() ���� ��� IP �˾Ƴ��� �ű��� DB�� ���� 
             */
            CDatabase databaseInstance = new CDatabase();
            databaseInstance.connectEntryPointDatabase(socket);
            databaseInstance.extractServerMonitoredResult();
            databaseInstance.extractClientMonitoredResult();
            databaseInstance.disconnectEntryPointDatabase();

            Counter.GetInstance().addRecvCompletedCount();
            log.info("	* Number of Entry Point Receving Data Completely : " + Counter.GetInstance().getRecvCompletedCount());
            
        //  log.info("# EP SOCKET CONNECTION CLOSED");
            socket.close();
            
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
