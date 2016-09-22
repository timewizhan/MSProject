import java.util.ArrayList;
import java.util.concurrent.Callable;
import org.apache.log4j.*;

public class CInitPrevMatch implements Callable {

	static Logger log = Logger.getLogger(CBroker.class.getName());
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		log.debug("Initialize previous match - start");
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		// (1) 클라이언트 테이블(client_table)에 들어있는 user_id 긁어와서, (2) ep number = 2 로 셋팅 (2: newyork)
		// (1)
		ArrayList<ClientData> userIdList = new ArrayList<ClientData>();
		userIdList = databaseInstance.getUserList();

		databaseInstance.disconnectBrokerDatabase();

		// (2)
		for(int i=0; i<userIdList.size(); i++){
			
			String userId = userIdList.get(i).getUserID();
			int epNo = 2;
			
			CBroker.prevMatch.put(userId, epNo);
		}
		log.debug("	Number of users : " + userIdList.size());
		log.debug("	Previous match size : " + CBroker.prevMatch.size());
		log.debug("Initialize previous match - end");
		return null;
	}
}
