import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class NewDataAnalyzer {
	static Logger log = Logger.getLogger(NewDataAnalyzer.class.getName());
	public static void main(String args[]){
		PropertyConfigurator.configure("log/log4j.properties");
		
		//DB Connection
		Database db = new Database();
		db.mySqlConnect();
		
		//ArrayList�� ��� User�� ����Ʈ�� ����
		ArrayList<User> users = new ArrayList<User>();
		//UserProperty ���̺� �ִ� ������ ����Ʈ �̾ƿ���
		users = db.getUserListFromUserProperty();
		
		//UserProperty ���̺��� ����ڵ� ����Ʈ �ҷ��´�
		ArrayList<UserProperty> properties = new ArrayList<UserProperty>();
		
		//logic1 - UserProperty�� �ִ� ���� ���̵�, ���, Ʈ�� ���� ������ ������ �´�.
		System.out.println("logic 1 =============================================================");
		//2000���� ����
		int maxUserSize = 2000;
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + maxUserSize + "]");
			
			if(maxUserSize < 0){
				break;
			} else {
				//Ʈ�� ������ 20�� �̻� 301�� ������ ������ ���� ���� UserProperty ���̺� �ִ´�. 
				if(users.get(i).getNumOfTweet() >= 20 && users.get(i).getNumOfTweet() <= 301){
					properties = db.getUserProperties(users.get(i).getUserId());
					db.insertNewUserProperty(properties);
					
					maxUserSize--;
				}
			}
		}
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
	/*	
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<users.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + users.size() + "]");
			
			//Ʈ�� ������ 20�� �̻� 301�� ������ ������ ���� ���� UserProperty ���̺� �ִ´�. 
			if(users.get(i).getNumOfTweet() >= 20 && users.get(i).getNumOfTweet() <= 301){
				properties = db.getUserProperties(users.get(i).getUserId());
				db.insertNewUserProperty(properties);
			}
		}
		
		System.out.println("=====================================================================");
		System.out.println("END Data Analyzer");
	*/	
		//DB Disconnection
		db.mySqlDisconnect();
	}
}
