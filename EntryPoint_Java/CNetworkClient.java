import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import org.json.simple.JSONObject;

public class CNetworkClient {

	private static final String BROKER_IP = "165.132.122.243";
	private static final int BROKER_PORT = 3333;

	private static final String SERVICE_SERVER_IP = "127.0.0.1";
	private static final int SERVICE_SERVER_PORT = 7777;

//	public void start(){
	public int start(){

		System.out.println("Starting EntryPoint...");

		boolean isSuccessOfMonitoring = commServiceServer();
		if(isSuccessOfMonitoring){
			commBroker();
			return 1;
		} else {
			System.out.println("fail to store - end process");
			return 0;
		}
	}

	public boolean commServiceServer(){
		boolean isSuccessOfMonitoring = false;
		
		try {
			// 소켓을 생성하여 연결을 요청한다
			Socket socket = new Socket(SERVICE_SERVER_IP, SERVICE_SERVER_PORT);

			// 소켓의 출력 스트림을 얻는다
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream(), "UTF-8"));
			
			//Json 문자열 만들기
			JSONObject request = new JSONObject();
			request.put("TYPE", "5");

			String strRequest = request.toString(); 
			strRequest += "\r\n";
			
			// 원격 소켓(remote socket)에 데이터를 보낸다
			out.write(strRequest);
			out.newLine();
			out.flush();
			System.out.println("A JSON message was sent");

			// 소켓의 입력스트림을 얻는다
			InputStream in = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
			// 소켓으로부터 받은 데이터 확인
			if(reader.readLine().equals("store_complete")){
				System.out.println("success to accept store_complete message");
				isSuccessOfMonitoring = true;
			} else {
				System.out.println("fail to accept store_complete message");
				isSuccessOfMonitoring = false;
			}
			
			reader.close();
			in.close();
			
			out.close();
			socket.close();
			System.out.println("Connection was closed");

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return isSuccessOfMonitoring;
	}

	public void commBroker(){

		try {

			// 소켓을 생성하여 연결을 요청한다
			Socket socket = new Socket(BROKER_IP, BROKER_PORT);

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
			dos.writeUTF("Do extracting monitored data");
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
