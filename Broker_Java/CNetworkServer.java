import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class CNetworkServer {
	
	private static final int PORT = 3333;

	public void start(){

		try	{
			ServerSocket serverSocket = new ServerSocket(PORT);
	
			while(true){
				Socket socket = serverSocket.accept();
				ThreadPool.GetInstance().execute(new CConnectionWrap(socket));
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
}
