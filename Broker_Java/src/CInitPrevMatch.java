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
		
		// (1) Ŭ���̾�Ʈ ���̺�(client_table)�� ����ִ� user_id �ܾ�ͼ�, (2) ep number = 2 �� ���� (2: newyork)
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
