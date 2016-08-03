import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class CNetworkServer {
	
	private static final int PORT = 3333;
	
	public void start(ExecutorService threadPool){
		
		try	{
			
			ServerSocket serverSocket = new ServerSocket(PORT);

			while(true){
			
				Socket socket = serverSocket.accept();
				threadPool.execute(new CConnectionWrap(socket));
				
			//	serverSocket.close();
			}
			
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}
}
