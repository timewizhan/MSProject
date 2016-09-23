import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;

public class CNetworkServer {
	
	static Logger log = Logger.getLogger(CBroker.class.getName());
	private static final int PORT = 3333;

	public void start(){

		try	{
			ServerSocket serverSocket = new ServerSocket(PORT);
			
			while(true){
				Socket socket = serverSocket.accept();
			//	log.info("# EP SOCKET CONNECTION ACCEPTED");
				ThreadPool.GetInstance().execute(new CConnectionWrap(socket));
			}
		} catch (IOException e) {
			log.error("CNetworkServer.start() Error!", e);
		}
	
	}
	
}
