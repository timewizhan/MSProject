import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class CNetworkBrokerGiver {

	private static final String BROKERGIVER_IP = "165.132.122.242";
	private static final int BROKERGIVER_PORT = 8888;
	static Socket BG_Socket = null;
	static BufferedOutputStream bos = null;
	
	static void initSocket(){
		try {
			BG_Socket = new Socket(BROKERGIVER_IP, BROKERGIVER_PORT);
			bos = new BufferedOutputStream(BG_Socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void sendStopMsg(){
		try {
			byte [] szSendStopMsg = new byte [2];
			szSendStopMsg[0] = '1';
			bos.write(szSendStopMsg);
			bos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void sendResumeMsg(){
		try {
			byte [] szSendStopMsg = new byte [2];
			szSendStopMsg[0] = '0';
			bos.write(szSendStopMsg);
			bos.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void closeSocket(){
		try {
			bos.close();
			BG_Socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
