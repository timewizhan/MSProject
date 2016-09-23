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
			// 소켓의 입력스트림을 얻는다.
			InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);	
		
            // 소켓으로부터 받은 데이터를 출력한다.
            log.info("	* Message from Entry Point : " + dis.readUTF());
            dis.close();
            
            /**
             * Monitoring 결과를 DB에서 읽어오는 부분
             * socket.getInetAddress() 으로 상대 IP 알아내서 거기의 DB에 접속 
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
