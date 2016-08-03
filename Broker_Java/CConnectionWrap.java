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
			
			// 소켓의 입력스트림을 얻는다.
			InputStream in = socket.getInputStream();
            DataInputStream dis = new DataInputStream(in);	
		
            // 소켓으로부터 받은 데이터를 출력한다.
            System.out.println("클라이언트로 부터 받은 메세지 : " + dis.readUTF());
            dis.close();
            
            /**
             * Monitoring 결과를 DB에서 읽어오는 부분
             * socket.getInetAddress() 으로 상대 IP 알아내서 거기의 DB에 접속 
             */
           // Counter.addRecvCompletedCount();
            Counter.GetInstance().addRecvCompletedCount();
            System.out.println(Counter.GetInstance().getRecvCompletedCount());
            
            //Monitoring 결과 다 받았다고 EP에게 메세지 전송
            // 소켓의 출력 스트림을 얻는다
 			OutputStream out = socket.getOutputStream();
 			DataOutputStream dos = new DataOutputStream(out);
 			
 			// 원격 소켓(remote socket)에 데이터를 보낸다
 			dos.writeUTF("[NOTICE] Test message1 from BROKER");
 			System.out.println("A message was sent");
 			
 			dos.close();
            
            System.out.println("연결을 종료합니다.");
            socket.close();
            
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
