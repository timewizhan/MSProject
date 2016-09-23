import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class CLBCalculation {
	
	static Logger log = Logger.getLogger(CBroker.class.getName());	
	
	HashMap<String , Integer> map;
	ArrayList<LpMatchResult> lpMatchResult;
	ArrayList<ArrayList<ClientTrafficData>> prevInitialUsersOfClouds;
	ArrayList<ArrayList<ClientTrafficData>> initialUsersOfClouds;
	ArrayList<UserWeight> userWeightList;
	
	public CLBCalculation(ArrayList<UserWeight> userWeightList){
		
		log.debug("[CLBCalculation constructor] - Start");
		
		//�켱���� �Ǵ��Ҷ� ���
		this.userWeightList = userWeightList;
		
		//Ư�� �����ǿ� �ִ� ������ �ִ� Ʈ���� ��
		//ex) 165.132.120.144�� �ִ� ������ �ִ� Ʈ���� ���� 500Gbyte�̴�
		map = new HashMap<String , Integer>();
		map.put("165.132.120.144", 100);
		map.put("165.132.123.73", 10000000);
		map.put("165.132.122.244", 10000000);
		map.put("165.132.122.245", 10000000);
		
		log.debug("	* set capacity limitation of each cloud (server) using HashMap");
		log.debug("	* current map.size() : " + map.size());
		
		log.debug("[CLBCalculation constructor] - End \r\n");
	}
	
	//load balancing ���� �޼ҵ�
	public void lbMain(ArrayList<LpMatchResult> lpMatchRes){

		log.info("[lbMain method] - Start");
		
		//LP calculation�� ��ġ ��� ����
		copyLpMatchResult(lpMatchRes);
		
		//process Ÿ���� �����ϴ� �޼ҵ� : Ʈ���� ���� ��� Ŭ���尡 �޾Ƶ��� �� �ִ� �������� �ƴ����� 3�ܰ�� ����
		String processType = determineProcessType();
		
		if(processType.equals("MINIMUM_TRAFFIC")){
			//LP�� �ϰ� ������. LB ���μ��� ���� ����
			log.debug("	* MINIMUM_TRAFFIC \r\n");
			getMatchedUsersToClouds();
			
		} else if (processType.equals("MEDIUM_TRAFFIC")){
			log.debug("	* MEDIUM_TRAFFIC \r\n");
			rematchForLoadBalancing(processType);
			
		} else if (processType.equals("MAXIMUM_TRAFFIC")){
			log.debug("	* MAXIMUM_TRAFFIC \r\n");
			
			//1. �̹� ��� Ŭ���尡 ����á��  --> �״�� �д�.
			//2. ���� ��� �������� �ʾ�����	 --> �����ִ� �κи� load balancing�ؼ� ä���. ��� ��������, �������� LP ����� ������.
			rematchForLoadBalancing(processType);
		}
		
		//broker giver database update
		updateBrokerGiver();
		
		//Resume BrokerGiver
		CNetworkBrokerGiver.initSocket();
		CNetworkBrokerGiver.sendResumeMsg();
		CNetworkBrokerGiver.closeSocket();
		
		//���̺� �ʱ�ȭ
		resetTables();
		
		log.info("[lbMain method] - End \r\n");
	}
	
	public void resetTables(){
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		String [] del_tables = {"server_table", "client_table", "norm_server_table"};
		String [] drop_tables = {"normalized_distance_table"};
 		
		for(int i=0; i<del_tables.length; i++){
			databaseInstance.deleteTable(del_tables[i]);
		}
		
		for(int i=0; i<drop_tables.length; i++){
			databaseInstance.dropTable(drop_tables[i]);
		//	System.out.println("dropping normalized_distance_table was done?");
		}

		databaseInstance.disconnectBrokerDatabase();
	}
	
	public void copyLpMatchResult(ArrayList<LpMatchResult> lpMatchRes){
		//ArrayList�� Deep Copy
		lpMatchResult = new ArrayList<LpMatchResult>();
		for (int i=0; i<lpMatchRes.size(); i++) {
			LpMatchResult initMatchResult = new LpMatchResult(lpMatchRes.get(i));
			lpMatchResult.add(initMatchResult);
		}
	}
	
	public String determineProcessType(){
		
		log.info("	[determineProcessType method] - Start");
		
		String processType = null;
	
		for(int epNo=1; epNo<=CBroker.NUM_OF_EP; epNo++){
			int eachServerTraffic = getEachServerTraffic(epNo);
			log.debug("		* " + "EP" + (int)(epNo) + " : " + eachServerTraffic);
		}
		int serverTotalTraffic = getTotalTraffic();			//�ش�ð��� �߻��� Ʈ���� �ѷ�
		log.debug("		* server total traffic : " + serverTotalTraffic);
		
		int sumCloudsCapacity = sumCloudMaxCapacity();		//Ŭ������� ���� ���� Ʈ���� ���� ��
		log.debug("		* total capacity of clouds : " + sumCloudsCapacity);
		
		int minCloudCapacity = getMinCloudCapacity();		//���� ���� Ʈ���� ���� ���� ���� Ŭ������ �뷮 �� 
		log.debug("		* minimum capacity of Clouds : " + minCloudCapacity);
		
		if(serverTotalTraffic <= minCloudCapacity){			//LP ����� ���� ���� ���� Ʈ���Ⱦ��� ���� ���� ���뷮�� ������ �ִ� Ŭ������ ������ ������, �� "�ش� �ð��� �߻��ϴ� Ʈ������ �ϳ��� Ŭ���尡 �� �����Ҽ�������" 
			processType = "MINIMUM_TRAFFIC";
		} else if(minCloudCapacity < serverTotalTraffic && serverTotalTraffic <= sumCloudsCapacity) {	//���� ��캸�� ũ��, ��� Ŭ������ ���� ���� �뷮�� ��ģ�� ���� ������
			processType = "MEDIUM_TRAFFIC";
		} else if(serverTotalTraffic > sumCloudsCapacity){	//��� Ŭ������ ���� ���� �뷮 ���� �� Ŭ��
			processType = "MAXIMUM_TRAFFIC";
		}
		
		log.info("	[determineProcessType method] - End");
		
		return processType;
	}
	
	public int getEachServerTraffic(int epNo){
		
		int amountOfTraffic = 0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		String ip = null;
		ip = getIpFromEpNo(epNo);
		amountOfTraffic = databaseInstance.getEachServerTraffic(ip);
		
		databaseInstance.disconnectBrokerDatabase();
		
		return amountOfTraffic;
	}
	
	public String getIpFromEpNo(int epNo){
		String ip = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		ip = databaseInstance.getIpWithEpNo(epNo);
		
		databaseInstance.disconnectBrokerDatabase();
		
		return ip;
	}
	
	public int getTotalTraffic(){
		
		int totalTraffic = 0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		totalTraffic = databaseInstance.getServerTotalTraffic();
		
		databaseInstance.disconnectBrokerDatabase();
		
		return totalTraffic;
	}
	
	public int getMinCloudCapacity(){
		
		ArrayList<String> serverList = getServerList();
		
		int MinCloudCapacity = 0;
		for(int i=0; i<serverList.size(); i++){
			int eachCloudMaxTraffic = map.get(serverList.get(i));
			if(i==0){
				MinCloudCapacity = eachCloudMaxTraffic;
			}else{
				if(eachCloudMaxTraffic < MinCloudCapacity){
					MinCloudCapacity = eachCloudMaxTraffic;
				}
			}
		}
		
		return MinCloudCapacity;
	}
	
	public int sumCloudMaxCapacity(){
		
		ArrayList<String> serverList = getServerList();
		
		int sumMaxTraffic = 0;
		for(int i=0; i<serverList.size(); i++){
			int eachCloudMaxTraffic = map.get(serverList.get(i));
			sumMaxTraffic += eachCloudMaxTraffic;
		}
		
		return sumMaxTraffic;
	}
	
	public ArrayList<String> getServerList(){
		
		ArrayList<String> serverList = new ArrayList<String>();
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		serverList = databaseInstance.getServerList();
		
		databaseInstance.disconnectBrokerDatabase();
		
		return serverList;
	}
	
	public void rematchForLoadBalancing(String processType){
		
		log.info("	[rematchForLoadBalancing method] - Start");
		
		//lp calculation ����� ����, �� Ŭ���� ���� ��ġ�� ������ ������ 
		getMatchedUsersToClouds();
		
		//Ʈ���� �翡 ���� �����ϱ�
		for(int i=0; i<initialUsersOfClouds.size();i++){
			sortUsersWithTraffic(initialUsersOfClouds.get(i));
		}
	
		
		//test
		log.debug("[after sorting]");
//		for(int i=0; i<initialUsersOfClouds.size(); i++){
//			if(initialUsersOfClouds.get(i) != null){
//				
//				ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
//				for(int j=0; j<usersOfCloud.size(); j++){
//					log.debug("user id : " + usersOfCloud.get(j).getUserId()
//							+ ", cloud No : " + usersOfCloud.get(j).getCloudNo()
//							+ ", traffic : " + usersOfCloud.get(j).getUserTraffic());
//				}
//			}
//		}
		
		
		//�ʰ��� Ŭ���尡 � ���� �ľ� //�ʰ��� Ʈ���� �뷮�� ������ ���
	//	System.out.println();
	//	System.out.println("[Server status]");
		
		ArrayList<ServerStatus> serverStateList = getCloudsStatus();
		ArrayList<ServerStatus> surplusServerList = new ArrayList<ServerStatus>();
		
		ArrayList<ClientTrafficData> extractedUsersList = new ArrayList<ClientTrafficData>(); 
		ArrayList<ClientTrafficData> extractedUsersFromCloud = new ArrayList<ClientTrafficData>();
		
		//�������� �޿� ���鼭, ���¸� Ȯ���Ѵ�. �� ������ �ƽø� Ʈ������ ������, ����Ǵ� Ʈ������ ������
		//�׷��� ����Ǵ� Ʈ������ �ƽø� Ʈ������ �ʰ��ϴ��� �ʰ����� �ʴ���
		log.info("	* before rematching process");
		for(int i=0; i<serverStateList.size(); i++){
			
			//test
			log.info("		EP no.:" + serverStateList.get(i).getEpNo()
					+ ", Maximum Traffic:" + serverStateList.get(i).getMaximumTraffic()
					+ ", Expected Traffic:" + serverStateList.get(i).getExpectedTraffic());

			int expectedTraffic = serverStateList.get(i).getExpectedTraffic();
			int maximumTraffic = serverStateList.get(i).getMaximumTraffic();
			
			if(expectedTraffic > maximumTraffic){
			
				// � EP����, �� �� ������ ����
				int trafficGap = expectedTraffic - maximumTraffic;
				int epNo = serverStateList.get(i).getEpNo();
				
				// trafficGap�� �����Ҷ����� �ֵ� ������
				extractedUsersFromCloud = extractUsers(epNo, trafficGap);
				// ������ ��� �ֵ��� ��Ƶδ� list�� �� cloud���� ������ �ֵ��� �߰�
				for(int j=0; j<extractedUsersFromCloud.size(); j++){
					extractedUsersList.add(extractedUsersFromCloud.get(j));
				}
			
			} else if (expectedTraffic < maximumTraffic){
				surplusServerList.add(serverStateList.get(i));
			
			} else {
				//expectedTraffic == maximumTraffic
					//do nothing
			}
		}
		
		if(surplusServerList.size() > 0){
		
			// ��ġ�� ������ ���� �ֵ��� �ٸ� Ŭ���忡 ��ġ��Ų��
			rematchUsersToCloud(extractedUsersList, surplusServerList, processType);
			
			log.info("	* after rematching process");
			
//			for(int i=0; i<initialUsersOfClouds.size(); i++){
//				if(initialUsersOfClouds.get(i) != null){
//					
//					ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
//					for(int j=0; j<usersOfCloud.size(); j++){
//						log.debug("user id : " + usersOfCloud.get(j).getUserId()
//								+ ", cloud No : " + usersOfCloud.get(j).getCloudNo()
//								+ ", traffic : " + usersOfCloud.get(j).getUserTraffic());
//					}
//				}
//			}
			
			serverStateList = getCloudsStatus();
			for(int i=0; i<serverStateList.size(); i++){
				//test
				log.info("		EP no.:" + serverStateList.get(i).getEpNo()
						+ ", Maximum Traffic:" + serverStateList.get(i).getMaximumTraffic()
						+ ", Expected Traffic:" + serverStateList.get(i).getExpectedTraffic());
			}
		
		//������, ������ Ŭ���尡 expected traffic�� �� ������ �� �ִ� ���¸� rematchUsersToCloud�� ������ �ʿ䰡 ����.
		} else {
			log.info("	* there's no surplus server - do not rematch process");
		
		}
		
		log.info("	[rematchForLoadBalancing method] - End");
		/////////////////////////////////////////////////////////////////////////////////////////////////		
		
		//"��� Ŭ���忡 ��Ī��" : ���� Ʈ���� ���� �ʰ��� Ŭ���� 
		//��ġ ����� ��� ��̸���Ʈ�� ������ �����ٵ� �װ� ����, ���뷮�� �Ѿ���� �ȳѾ���� ����
		//���� �Ѿ�� �� ���� ����ġ
		
		//"������" : �ش� ���� �ð� ���� ��뷮�� ���� ���� ��������
		//"�����" : �Ѹ��� �����Ͱ� �߻���ų �� �ִ� ���� Ʈ������ ����� ��, LP����� ���� ��Ī�� ��(�̰� Ʈ�������� ����ؾ���)���� ����� �����͸� ���� ���� Ʈ���� ���� �ȿ� ������ ����ϰ�, �׸�ŭ ��
		//"����" : �켱 ������ ���� �ű��, �������� ���� Ʈ������ ����ؼ� �ű�
	}
	
	public ArrayList<ClientTrafficData> extractUsers(int epNo, int trafficGap){
		
		ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(epNo-1);
		ArrayList<ClientTrafficData> extractedUsers = new ArrayList<ClientTrafficData>();
		
		int sumUsersTraffic = 0;
		for(int j=0; j<usersOfCloud.size(); j++){
			
			sumUsersTraffic += usersOfCloud.get(j).getUserTraffic();
			extractedUsers.add(usersOfCloud.get(j));
			
			if(sumUsersTraffic >= trafficGap)
				break;
		}
		
		return extractedUsers;
	}
	
	public void rematchUsersToCloud(ArrayList<ClientTrafficData> extractedUsersList, ArrayList<ServerStatus> surplusServerList, String processType){
		
		log.info("	[rematchUsersToCloud method] - Start");
		
		ArrayList<ArrayList<ClientTrafficData>> rematchResult = initialUsersOfClouds;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(surplusServerList.size() > 0 && surplusServerList.size() <= 1){
			log.debug("		0 < surplusServerList.size() <= 1");
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(processType.equals("MEDIUM_TRAFFIC")){
				log.debug("		MEDIUM_TRAFFIC");
				
				//����ڵ���, ���� �ϳ��� ���� Ŭ���忡 �� �����ִ´�.
				for(int i=0; i<extractedUsersList.size(); i++){

					//���� �ִ� ����Ʈ������ �����Ѵ�
					//����ڰ� ���ʿ� ��Ī�ƴ� EP��ȣ
					int initialEpNo = extractedUsersList.get(i).getCloudNo();

					//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
					String userId = extractedUsersList.get(i).getUserId();
					int userIndex = getUserIndex(initialEpNo, userId);

					//����Ʈ���� ����
					initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);

					//���ο� Ŭ���忡 �߰� �Ѵ�
					//���� �߰��� Ŭ���� EpNo ã��
					int rematchEpNo = surplusServerList.get(0).getEpNo();

					//����� EpNo ���� ������Ʈ
					extractedUsersList.get(i).setCloudNo(rematchEpNo);

					//�߰�
					initialUsersOfClouds.get(rematchEpNo-1).add(extractedUsersList.get(i));
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(processType.equals("MAXIMUM_TRAFFIC")){
				log.debug("		MAXIMUM_TRAFFIC");
				
				int trafficGap = surplusServerList.get(0).getMaximumTraffic() - surplusServerList.get(0).getExpectedTraffic();
				int i = 0;
				while(true){
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					trafficGap -= userTraffic;
					if(trafficGap<0)	break;
					
				//���� �ִ� ����Ʈ������ �����Ѵ�
					//����ڰ� ���ʿ� ��Ī�ƴ� EP��ȣ
					int initialEpNo = extractedUsersList.get(i).getCloudNo();
					
					//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
					String userId = extractedUsersList.get(i).getUserId();
					int userIndex = getUserIndex(initialEpNo, userId);
					
					//����Ʈ���� ����
					initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);
					
				//���ο� Ŭ���忡 �߰� �Ѵ�
					//���� �߰��� Ŭ���� EpNo ã��
					int rematchEpNo = surplusServerList.get(0).getEpNo();
					
					//����� EpNo ���� ������Ʈ
					extractedUsersList.get(i).setCloudNo(rematchEpNo);
					
					//�߰�
					initialUsersOfClouds.get(rematchEpNo-1).add(extractedUsersList.get(i));
					
					
					i++;
				}
			}
			
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}else if(surplusServerList.size() > 1){
			log.debug("		surplusServerList.size() > 1");
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(processType.equals("MEDIUM_TRAFFIC")){
				log.debug("		MEDIUM_TRAFFIC");
				
				//�� �������� �켱������ �ٸ���. ���� �� ������ ��ƾ ������ ó���Ǿ���
				for(int i=0; i<extractedUsersList.size(); i++){
					
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					ArrayList<SurCloudWeight> priorWeight = null;
					int numRemainClouds = surplusServerList.size();
					int priority = 1;

					while(numRemainClouds>1){
						
						//�켱���� 1. social + distance
						if(priority == 1){
						///*	
							log.debug("		Priority 1. social + distance");

							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
						//*/	
							//�켱���� 2. social	
						}else if(priority == 2){	
						///*	
							log.debug("		Priority 2. social");
							
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudSocialWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
						//*/	
							//�켱���� 3. distance
						}else if(priority == 3){	
						///*	
							log.debug("		Priority 3. distance");
							
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudDistanceWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
						//*/
							//�켱���� 4. traffic
						}else if(priority == 4){
							log.debug("		Priority 4. traffic");
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudTrafficWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
							
							//�켱���� 5. random
						}else if(priority == 5){
							log.debug("		Priority 5. random");
							log.debug("		Error!!! Not implemented yet!!!");
						//	System.out.println("random ��Ī�� ����");
						//	System.out.println("random ��Ī ���� ���� �ȵ�");
						}

						priority++;
					}

					if(numRemainClouds > 0){
						
						//���� �ִ� ����Ʈ������ �����Ѵ�
						//����ڰ� ���ʿ� ��Ī�ƴ� EP��ȣ
						int initialEpNo = extractedUsersList.get(i).getCloudNo();

						//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
						String userId = extractedUsersList.get(i).getUserId();
						int userIndex = getUserIndex(initialEpNo, userId);

						//����Ʈ���� ����
						initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);

						//���ο� Ŭ���忡 �߰� �Ѵ�
						//���� �߰��� Ŭ���� EpNo ã��
						int rematchEpNo = priorWeight.get(0).getEpNo();
						
						log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
						
						//remain traffic ����
						int rematchEpIndex = 0;
						for(int p=0; p<surplusServerList.size(); p++){
							if(surplusServerList.get(p).getEpNo() == rematchEpNo){
								rematchEpIndex = p;
								break;
							}
						}
						log.debug("			* rematchEpIndex (in surplusServerList ArrayList) : " + rematchEpIndex);
						
						double expectedTrafficRatio = surplusServerList.get(rematchEpIndex).getRatioOfTraffic(); 
						double expectedUserTraffic = userTraffic * expectedTrafficRatio;
						
						int changedRemainTraffic = surplusServerList.get(rematchEpIndex).getRemainTraffic() - (int)expectedUserTraffic;
						surplusServerList.get(rematchEpIndex).setRemainTraffic(changedRemainTraffic);
				//		System.out.println("  rematch ep no : " + rematchEpNo);
				//		System.out.println();
						
						//����� EpNo ���� ������Ʈ
						extractedUsersList.get(i).setCloudNo(rematchEpNo);

						//�߰�
						initialUsersOfClouds.get(rematchEpNo-1).add(extractedUsersList.get(i));
					}
					else if(numRemainClouds <= 0){
						//���������� ������ �� �ִ� Ŭ���� ������ 0�̸� �ƹ��͵� ���ϰ� ���� extractedUser�� �Ѿ��
					}
					
				}
				
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			} else if(processType.equals("MAXIMUM_TRAFFIC")){
				log.debug("		MAXIMUM_TRAFFIC");
				
				int trafficGap = 0;
				for(int i=0; i<surplusServerList.size(); i++){
					trafficGap += surplusServerList.get(i).getMaximumTraffic() - surplusServerList.get(i).getExpectedTraffic();
					int remainTraffic = surplusServerList.get(i).getMaximumTraffic() - surplusServerList.get(i).getExpectedTraffic();
					surplusServerList.get(i).setRemainTraffic(remainTraffic);
				}
				
				//�� �������� �켱������ �ٸ���. ���� �� ������ ��ƾ ������ ó���Ǿ���
				int i = 0;
				while (true) {
					
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					trafficGap -= userTraffic;
					if(trafficGap<0)	break;
				
					ArrayList<SurCloudWeight> priorWeight = null;
					int numRemainClouds = surplusServerList.size();
					int priority = 1;

					while(numRemainClouds>1){

						//�켱���� 1. social + distance
						if(priority == 1){
					//	/*	
							log.debug("		Priority 1. social + distance");
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
					//	*/
						//�켱���� 2. social	
						}else if(priority == 2){
					//	/*	
							log.debug("		Priority 2. social");
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudSocialWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
					//	*/
						//�켱���� 3. distance
						}else if(priority == 3){
					//	/*
							log.debug("		Priority 3. distance");
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudDistanceWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
					//	*/
						//�켱���� 4. traffic
						}else if(priority == 4){
							log.debug("		Priority 4. traffic");
							
							//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
							String userId = extractedUsersList.get(i).getUserId();
							priorWeight = getSurplusCloudTrafficWeight(surplusServerList, userId, userTraffic);

							//�ּ� �� ���ϱ�
							double minValue = getMinWeightValue(priorWeight);

							//���� �ּ� ���� ������ �ִ� Ŭ������� �����
							priorWeight = getCloudHavingMinWeight(minValue,priorWeight);

							//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
							numRemainClouds = priorWeight.size();
							log.debug("		after process, the number of remaining surplus clouds : " + numRemainClouds);
						//	System.out.println("[priority == 4], Number of remaining clouds : " + numRemainClouds);

						//�켱���� 5. random
						}else if(priority == 5){
							log.debug("		Priority 5. random");
							log.debug("		Error!!! Not implemented yet!!!");
						}

						priority++;
					}
					
					if(numRemainClouds > 0){
						
						//���� �ִ� ����Ʈ������ �����Ѵ�
						//����ڰ� ���ʿ� ��Ī�ƴ� EP��ȣ
						int initialEpNo = extractedUsersList.get(i).getCloudNo();

						//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
						String userId = extractedUsersList.get(i).getUserId();
						int userIndex = getUserIndex(initialEpNo, userId);

						//����Ʈ���� ����
						initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);

						//���ο� Ŭ���忡 �߰� �Ѵ�
						//���� �߰��� Ŭ���� EpNo ã��
						int rematchEpNo = priorWeight.get(0).getEpNo();
						
						log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
						
						//remain traffic ����
						int rematchEpIndex = 0;
						for(int p=0; p<surplusServerList.size(); p++){
							if(surplusServerList.get(p).getEpNo() == rematchEpNo){
								rematchEpIndex = p;
								break;
							}
						}
						log.debug("			* rematchEpIndex (in surplusServerList ArrayList) : " + rematchEpIndex);
						
						double expectedTrafficRatio = surplusServerList.get(rematchEpIndex).getRatioOfTraffic(); 
						double expectedUserTraffic = userTraffic * expectedTrafficRatio;
						
						int changedRemainTraffic = surplusServerList.get(rematchEpIndex).getRemainTraffic() - (int)expectedUserTraffic;
						surplusServerList.get(rematchEpIndex).setRemainTraffic(changedRemainTraffic);
						
						//����� EpNo ���� ������Ʈ
						extractedUsersList.get(i).setCloudNo(rematchEpNo);

						//�߰�
						initialUsersOfClouds.get(rematchEpNo-1).add(extractedUsersList.get(i));
					}
					else if(numRemainClouds <= 0){
						//���������� ������ �� �ִ� Ŭ���� ������ 0�̸� �ƹ��͵� ���ϰ� ���� extractedUser�� �Ѿ��
					}
					
					i++;
				}
			}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		} else { // surplusServerList.size() <= 0
			//do nothing : �� ��찡 �̹� ��� Ŭ���尡 ����á�� ������, LP ����� �״�� ������ �ó������� �ش��ϴ� ��
		}
		
		log.info("	[rematchUsersToCloud method] - End");
	}
	
	private void updateBrokerGiver(){
		log.info("	[updateBrokerGiver method] - Start");
		
		CDatabase databaseInstance = new CDatabase();	//brokerGiver database instance
		CDatabase databaseInstance2 = new CDatabase(); 	//broker database instance
		databaseInstance.connectBrokerGiverDatabase();
		
		/////////////////////////////////////////////////////////////
		//print log
		log.info(" 		* before update : number of user at each cloud");
		int epNo=0;
		String resultStr = "";
		for(int i=0; i<CBroker.NUM_OF_EP; i++){
			epNo = i+1;
			int numOfUsers = databaseInstance.getNumOfUserAtEachCloud(epNo);
			String epStr = "EP" + epNo;
			epStr += (":" + numOfUsers);
			resultStr += epStr + " ";
		}
		log.info("			" + resultStr);
		
		/////////////////////////////////////////////////////////////
		//update
		int basicCount = 0;
		int nullIdCount = 0;
		int skipCount = 0;
		int updateCount = 0;
		log.debug(" 		* Previous Matching HashMap size : " + CBroker.prevMatch.size());
		for(int i=0; i<initialUsersOfClouds.size(); i++){
			
			ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
			
			for(int j=0; j<usersOfCloud.size(); j++){
				
				String userId = usersOfCloud.get(j).getUserId();
				int cloudNo = usersOfCloud.get(j).getCloudNo();
				
				//prevMatch�� �ش� ���̵� ������
				if(!CBroker.prevMatch.containsKey(userId)){
				//	log.debug(" 			- no user id -> insert new id : " + userId);
					//���� �߰�
					CBroker.prevMatch.put(userId, cloudNo);
					
					//BrokerGiver update
					databaseInstance2.connectBrokerDatabase();
					String ip = databaseInstance2.getIpWithEpNo(cloudNo);
					String location = databaseInstance2.getLocationWithIp(ip);
					databaseInstance2.disconnectBrokerDatabase();
				
					databaseInstance.updateBrokerGiverTable(userId, cloudNo, ip, location);
					
					nullIdCount++;
					
				//prevMatch�� �ش� ���̵� ������
				} else {
					
					//prevMatch���� ������ �׳� �Ѿ��
					if(CBroker.prevMatch.get(userId) == cloudNo){
						//do nothing
					//	log.debug(" 			- same user id -> do nothing : " + userId);
						skipCount++;
						
					//prevMatch�� ������Ʈ�Ѵ�	
					//�ٸ��� BrokerGiver�������� ������Ʈ�Ѵ�
					} else {
					//	log.debug(" 			- same user id -> update broker giver table (UserId, CloudNo) : " + userId + ", " + cloudNo);
						
						//prevMatch update
						CBroker.prevMatch.put(userId, cloudNo);
						
						//BrokerGiver update
						databaseInstance2.connectBrokerDatabase();
						String ip = databaseInstance2.getIpWithEpNo(cloudNo);
						String location = databaseInstance2.getLocationWithIp(ip);
						databaseInstance2.disconnectBrokerDatabase();
					
						databaseInstance.updateBrokerGiverTable(userId, cloudNo, ip, location);
					//	System.out.println();
					//	System.out.println("update count : " + updateCount + " ");
						updateCount++;
					}
				}
			}
		//	System.out.print("(" + basicCount + ") ");
		}
		log.debug(" 			- null id count (so, put into the HashMap) : " + nullIdCount);
		log.debug(" 			- skip count (cuz, same with the existing match) : " + skipCount);
		log.debug(" 			- update count (cuz, different with the existing match): " + updateCount);
		log.debug(" 		* After Matching HashMap size : " + CBroker.prevMatch.size());
		
		/////////////////////////////////////////////////////////////
		//print log
		log.info("		* after update : number of user at each cloud");
		epNo=0;
		resultStr = "";
		for(int i=0; i<CBroker.NUM_OF_EP; i++){
			epNo = i+1;
			int numOfUsers = databaseInstance.getNumOfUserAtEachCloud(epNo);
			String epStr = "EP" + epNo;
			epStr += (":" + numOfUsers);
			resultStr += epStr + " ";
		}
		log.info("			" + resultStr);
		
		databaseInstance.disconnectBrokerGiverDatabase();
		
		log.info("	[updateBrokerGiver method] - End");
	}
	
	public ArrayList<SurCloudWeight> getCloudHavingMinWeight(double minValue, ArrayList<SurCloudWeight> priorWeight){
		
	//	ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		
		int iterCnt = priorWeight.size();
		for(int j=iterCnt; j>0; j--){
			if(minValue < priorWeight.get(j-1).getWeightValue()){
				priorWeight.remove(j-1);
			}
		}
		
		return priorWeight;
	}
		
	public ArrayList<SurCloudWeight> getSurplusCloudTrafficWeight(ArrayList<ServerStatus> surplusServerList, String userId, int userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
		
			//�ش� ������ ���� �뷮 ����
			int remainTraffic = surplusServerList.get(j).getMaximumTraffic() - surplusServerList.get(j).getExpectedTraffic();
			surplusServerList.get(j).setRemainTraffic(remainTraffic);
			double expectedTrafficRatio = surplusServerList.get(j).getRatioOfTraffic(); 
			double expectedUserTraffic = userTraffic * expectedTrafficRatio;
			
		//	log.debug("				* user id : " + userId);
		//	log.debug("				* remain traffic : " + remainTraffic);
		//	log.debug("				* user traffic : " + expectedUserTraffic);
			
			//���࿡ �ش� ������ ���� Ʈ���� ���� �����ϴ°���, ���� Ʈ���� ��� ������ ���ԵǴ� �Ÿ� ��� ���� �ƴϸ� break; 
			if(remainTraffic >= expectedUserTraffic){
				//normalized �� ��������
				int surplusEpNo = surplusServerList.get(j).getEpNo();
				double normalizedValue = databaseInstance.getNormalizedTrafficWeightValue(surplusEpNo);
				surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
				priorWeight.add(surCloudWeight);
			}
		}
		
		databaseInstance.disconnectBrokerDatabase();
		
		return priorWeight;
	}
	
	public ArrayList<SurCloudWeight> getSurplusCloudDistanceWeight(ArrayList<ServerStatus> surplusServerList, String userId, int userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
		
			//�ش� ������ ���� �뷮 ����
			int remainTraffic = surplusServerList.get(j).getMaximumTraffic() - surplusServerList.get(j).getExpectedTraffic();
			surplusServerList.get(j).setRemainTraffic(remainTraffic);
			double expectedTrafficRatio = surplusServerList.get(j).getRatioOfTraffic(); 
			double expectedUserTraffic = userTraffic * expectedTrafficRatio;
			
		//	log.debug("  * user id : " + userId);
		//	log.debug("  * remain traffic : " + remainTraffic);
		//	log.debug("  * user traffic : " + expectedUserTraffic);
			
			//���࿡ �ش� ������ ���� Ʈ���� ���� �����ϴ°���, ���� Ʈ���� ��� ������ ���ԵǴ� �Ÿ� ��� ���� �ƴϸ� break; 
			if(remainTraffic >= expectedUserTraffic){
				//normalized �� ��������
				int surplusEpNo = surplusServerList.get(j).getEpNo();
				double normalizedValue = databaseInstance.getNormalizedDistanceWeightValue(userId, surplusEpNo);
				surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
				priorWeight.add(surCloudWeight);
			}
		}
		
		databaseInstance.disconnectBrokerDatabase();
		
		return priorWeight;
	}
	
	public ArrayList<SurCloudWeight> getSurplusCloudSocialWeight(ArrayList<ServerStatus> surplusServerList, String userId, int userTraffic){
	
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
		
			//�ش� ������ ���� �뷮 ����
			int remainTraffic = surplusServerList.get(j).getMaximumTraffic() - surplusServerList.get(j).getExpectedTraffic();
			surplusServerList.get(j).setRemainTraffic(remainTraffic);
			double expectedTrafficRatio = surplusServerList.get(j).getRatioOfTraffic(); 
			double expectedUserTraffic = userTraffic * expectedTrafficRatio;
			
		//	log.debug("  * user id : " + userId);
		//	log.debug("  * remain traffic : " + remainTraffic);
		//	log.debug("  * user traffic : " + expectedUserTraffic);
			
			//���࿡ �ش� ������ ���� Ʈ���� ���� �����ϴ°���, ���� Ʈ���� ��� ������ ���ԵǴ� �Ÿ� ��� ���� �ƴϸ� break; 
			if(remainTraffic >= expectedUserTraffic){
				//normalized �� ��������
				int surplusEpNo = surplusServerList.get(j).getEpNo();
				double normalizedValue = databaseInstance.getNormalizedSocialWeightValue(userId, surplusEpNo);
				surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
				priorWeight.add(surCloudWeight);
			}
		}
		
		databaseInstance.disconnectBrokerDatabase();
		
		return priorWeight;
	}

	public ArrayList<SurCloudWeight> getSurplusCloudWeight(ArrayList<ServerStatus> surplusServerList, String userId, int userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		log.debug(" -- getSurplusCloudWeight method");
		for(int j=0; j<surplusServerList.size(); j++){
			System.out.println();		
			//user index ã��
			int userIndex = getUserIndex(userId);
			
			//�ش� ������ ���� �뷮 ����
			int remainTraffic = surplusServerList.get(j).getMaximumTraffic() - surplusServerList.get(j).getExpectedTraffic();
			surplusServerList.get(j).setRemainTraffic(remainTraffic);
			//int remainTraffic = surplusServerList.get(j).getRemainTraffic();
			double expectedTrafficRatio = surplusServerList.get(j).getRatioOfTraffic(); 
			double expectedUserTraffic = userTraffic * expectedTrafficRatio;
			
	//		log.debug("  * user id : " + userId);
	//		log.debug("  * remain traffic : " + remainTraffic);
	//		log.debug("  * user traffic : " + expectedUserTraffic);
			
			//���࿡ �ش� ������ ���� Ʈ���� ���� �����ϴ°���, ���� Ʈ���� ��� ������ ���ԵǴ� �Ÿ� ��� ���� �ƴϸ� break; 
			if(remainTraffic >= expectedUserTraffic){
				
				//normalized �� ��������
				int surplusEpNo = surplusServerList.get(j).getEpNo();
				
				log.debug("  -- into rematch process, ep no: " + surplusEpNo);
				
				double normalizedValue = userWeightList.get(userIndex).getWeightValues()[surplusEpNo-1];
				surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
				
				//�߰�
				priorWeight.add(surCloudWeight);
				
				log.debug("     number of remain servers: " + priorWeight.size());
				
			}else{
				
			}
			
		}
		
		return priorWeight;
	}
	
	public double getMinWeightValue(ArrayList<SurCloudWeight> priorWeight){
		double minValue = 0;
		
		for(int i=0; i<priorWeight.size(); i++){
		
			if(i==0){
				minValue = priorWeight.get(i).weightValue;
			}else{
				if(minValue > priorWeight.get(i).weightValue){
					minValue = priorWeight.get(i).weightValue;
				}
			}
		}
		
		return minValue;
	}
	
	public int getUserIndex(String userId){
		int userIndex = 0;
		
		for(int i=0; i<userWeightList.size(); i++){
			if(userId.equals(userWeightList.get(i).getUser())){
				userIndex = i;
				break;
			}
		}
		
		return userIndex;
	}
	
	public int getUserIndex(int initialEpNo, String userId){
		
		int userIndex = 0;
		ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(initialEpNo-1);
		for(int j=0; j<usersOfCloud.size(); j++){
			if(usersOfCloud.get(j).getUserId().equals(userId)){
				userIndex = j;
				break;
			}
		}
		
		return userIndex;
	}
	
	public ArrayList<ServerStatus> getCloudsStatus(){
	
		ArrayList<ServerStatus> serverStateList = new ArrayList<ServerStatus>();
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		serverStateList = databaseInstance.getServersStatus(map, initialUsersOfClouds);
		
		databaseInstance.disconnectBrokerDatabase();
		
		return serverStateList;
	}
	
	public void getMatchedUsersToClouds(){
		
		initialUsersOfClouds = new ArrayList<ArrayList<ClientTrafficData>>();
		for(int i=0; i<CBroker.NUM_OF_EP; i++){
			initialUsersOfClouds.add(null);
		}
		
		ArrayList<ClientTrafficData> usersOfCloud = null;
		ClientTrafficData clientTrafficData = null; 
		int epNo = CBroker.NUM_OF_EP;
		
		for(int j=0; j<CBroker.NUM_OF_EP; j++){
			usersOfCloud = new ArrayList<ClientTrafficData>();
			if(epNo > 0){
				
				for(int i=0; i<lpMatchResult.size(); i++){
					if(epNo == lpMatchResult.get(i).getCloudNo()){
						clientTrafficData = new ClientTrafficData();
						clientTrafficData.setUserId(lpMatchResult.get(i).getUserId());
						clientTrafficData.setUserTraffic(getUserTraffic(lpMatchResult.get(i).getUserId()));
						clientTrafficData.setCloudNo(lpMatchResult.get(i).getCloudNo());
						usersOfCloud.add(clientTrafficData);
					}
				}
				initialUsersOfClouds.remove(epNo-1);
				initialUsersOfClouds.add(epNo-1, usersOfCloud);
				epNo--;
				
			}
		}
	}
	
	public int getUserTraffic(String userId){
		
		int traffic=0;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		
		traffic = databaseInstance.getUserTraffic(userId);
		
		databaseInstance.disconnectBrokerDatabase();
		
		return traffic;
	}
	
	public void sortUsersWithTraffic(ArrayList<ClientTrafficData> S){
		//	public void sortUsersWithTraffic(ArrayList<ClientTrafficData> usersOfCloud){

		//quick sort
		if (S.size() < 2) return; // Nothing needs to be done if S has zero or one element)

		// Divide: If S has at least two elements, select a specific element x from S, which is called the pivot.
		ArrayList<ClientTrafficData> L = new ArrayList<ClientTrafficData>(); // L, storing the elements in S less than pivot
		ArrayList<ClientTrafficData> E = new ArrayList<ClientTrafficData>(); // E, storing the elements in S equal to pivot
		ArrayList<ClientTrafficData> G = new ArrayList<ClientTrafficData>(); // G, storing the elements in S greater than pivot
		int pivot = (int)S.get(S.size() - 1).getUserTraffic();
	
		E.add(S.get(S.size() - 1));
		S.remove(S.size() - 1);
		
		while (S.size() > 0) {
			if ((int)S.get(0).getUserTraffic() < pivot) {
				L.add(S.get(0));
			} else if ((int)S.get(0).getUserTraffic() == pivot) {
				E.add(S.get(0));
			} else {
				G.add(S.get(0));
			}
			S.remove(0);
		}

		// Conquer: Recursively sort sequences L and G.
		sortUsersWithTraffic(L);
		sortUsersWithTraffic(G);

		// Combine: Put back the element into S in order by first inserting the elements of L, then those of E, and finally those of G.
		for (int i = 0; i < L.size(); i++) {
			S.add(L.get(i));
		}
		for (int i = 0; i < E.size(); i++) {
			S.add(E.get(i));
		}
		for (int i = 0; i < G.size(); i++) {
			S.add(G.get(i));
		}
	}
}
