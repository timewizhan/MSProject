import java.beans.PropertyVetoException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.SQLException;

import org.json.simple.JSONObject;

import Type.opType;
import Type.userType;
import Wrapper.coordInfo;

public class WorkerRunnable implements Runnable {
    private Socket mClientSocket = null;
    private coordInfo mCoord = null;

    public WorkerRunnable(Socket clientSocket, coordInfo coord) {
    	this.mClientSocket = clientSocket;
        this.mCoord = coord;        
    }

    public void run() {
        JSONObject request  = Utility.msgParser(this.mClientSocket);
              
        try {        	
        	int result = operationHandler(request);
			String response = Utility.msgGenerator(result);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					this.mClientSocket.getOutputStream(), "UTF-8"));	
			
			Thread.sleep(Utility.calRTT(this.mCoord, (String) request.get("LOC")));
			
			out.write(response);
			out.newLine();
			out.flush();			

			mClientSocket.close();
			System.out.println(Utility.getTime() + mCoord.getServerLoc() + " handled a request from " + "[" + request.get("SRC") + "]");
		} catch (PropertyVetoException e) {			
			e.printStackTrace();
		} catch (SQLException e) {			
			e.printStackTrace();
		} catch (IOException e) {			
			e.printStackTrace();
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
    }
    
    private int operationHandler(JSONObject request) throws PropertyVetoException, SQLException, IOException, InterruptedException {		
		int uid = -1;
		int reqSize = request.toString().length();
		int res = 0;		
						
		int reqType = Integer.parseInt((String) request.get("TYPE"));	
		
		String src = (String) request.get("SRC");		
		String dst = (String) request.get("DST");
		String loc = (String) request.get("LOC");
		String msg = (String) request.get("MSG");
		
		switch (reqType) {                                                                                                                                                                                                      
		case opType.tweet:				
			uid = DBConnection.isThere(src, userType.resident, loc);			
			res = DBConnection.writeStatus(uid, msg, reqSize);			
			break;
		case opType.read:
			uid = DBConnection.isThere(src, userType.visitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, opType.num_read);
			break; 
		case opType.reply:
			uid = DBConnection.isThere(src, userType.visitor, loc);			
			res = DBConnection.writeReply(uid, dst, msg, reqSize, opType.num_read);
			break;
		case opType.retweet:
			uid = DBConnection.isThere(src, userType.visitor, loc);
			res = DBConnection.readStatus(uid, dst, reqSize, opType.num_share);
			break;
		case opType.monitor:
			// stop cpu util monitoring
			// cal average cpu util
			// store client_side_monitor
			// store server_side_monitor
			int server_side_traffic = DBConnection.storeClientMonitor();
						
			break;
		default:
			System.out.println("[ERROR] Invalid Operation Type: " + reqType);			
			break;
		}
		return res;
	}
}
