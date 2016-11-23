import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class CLBCalculation {
	
	static Logger log = Logger.getLogger(CBroker.class.getName());	
	
	HashMap<String , Long> map;
	ArrayList<LpMatchResult> lpMatchResult;
//	ArrayList<ArrayList<ClientTrafficData>> prevInitialUsersOfClouds;
	ArrayList<ArrayList<ClientTrafficData>> initialUsersOfClouds;
	ArrayList<UserWeight> userWeightList;
	
	public CLBCalculation(ArrayList<UserWeight> userWeightList){
		
		log.debug("[CLBCalculation constructor] - Start");
		
		//�켱���� �Ǵ��Ҷ� ���
		this.userWeightList = userWeightList;
		
		//Ư�� �����ǿ� �ִ� ������ �ִ� Ʈ���� ��
		//ex) 165.132.120.144�� �ִ� ������ �ִ� Ʈ���� ���� 500Gbyte�̴�
		map = new HashMap<String , Long>();
		map.put("165.132.123.73", (long)38000000);
		map.put("165.132.122.244", (long)200000000);
		map.put("165.132.122.245", (long)200000000);
		
		log.debug("	* set capacity limitation of each cloud (server) using HashMap");
		log.debug("	* current map.size() : " + map.size());
		
		initialUsersOfClouds = new ArrayList<ArrayList<ClientTrafficData>> ();
		
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
		
			CBroker.isFirstMinimum = false;
			CBroker.isFirstMedium = true;		
			CBroker.isFirstMaximum = true;
			 
		} else if (processType.equals("MEDIUM_TRAFFIC")){
			log.debug("	* MEDIUM_TRAFFIC \r\n");
			rematchForLoadBalancing(processType);
		
			CBroker.isFirstMinimum = true;
			CBroker.isFirstMedium = false;
			CBroker.isFirstMaximum = true;
			
		} else if (processType.equals("MAXIMUM_TRAFFIC")){
			log.debug("	* MAXIMUM_TRAFFIC \r\n");
			
			//1. �̹� ��� Ŭ���尡 ����á��  --> �״�� �д�.
			//2. ���� ��� �������� �ʾ�����	 --> �����ִ� �κи� load balancing�ؼ� ä���. ��� ��������, �������� LP ����� ������.
			rematchForLoadBalancing(processType);
			
			CBroker.isFirstMinimum = true;
			CBroker.isFirstMedium = true;
			CBroker.isFirstMaximum = false;
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
		
		databaseInstance.setPrivServerTrafficZero();

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
		
		double sumCloudsCapacity = sumCloudMaxCapacity();		//Ŭ������� ���� ���� Ʈ���� ���� ��
		log.debug("		* total capacity of clouds : " + sumCloudsCapacity);
		
		double minCloudCapacity = getMinCloudCapacity();		//���� ���� Ʈ���� ���� ���� ���� Ŭ������ �뷮 �� 
		log.debug("		* minimum capacity of Clouds : " + minCloudCapacity);
		
		if(serverTotalTraffic <= minCloudCapacity){			//LP ����� ���� ���� ���� Ʈ���Ⱦ��� ���� ���� ���뷮�� ������ �ִ� Ŭ������ ������ ������, �� "�ش� �ð��� �߻��ϴ� Ʈ������ �ϳ��� Ŭ���尡 �� �����Ҽ�������" 
			processType = "MINIMUM_TRAFFIC";
		//	isFirstMinimum
		
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
	
	public double getMinCloudCapacity(){
		
		ArrayList<String> serverList = getServerList();
		
		double MinCloudCapacity = 0;
		for(int i=0; i<serverList.size(); i++){
			double eachCloudMaxTraffic = map.get(serverList.get(i));
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
			double eachCloudMaxTraffic = map.get(serverList.get(i));
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
	
	boolean checkFirstMedium(){
	
		boolean isFirst = false;
		
		if(CBroker.isFirstMedium){
			isFirst = true;
		} else {
			isFirst = false;
		}
		
		return isFirst;
	}

	void initialRematchProcess(String processType){
	
		//lp calculation ����� ����, �� Ŭ���� ���� ��ġ�� ������ ������ 
		getMatchedUsersToClouds();
		commonRematchProcess(processType);
		
	}
	
	void commonRematchProcess(String processType){
	/**
	 * ���⼭ ���ʹ� ���� �и�	
	 */
		//Ʈ���� �翡 ���� �����ϱ�
		for(int i=0; i<initialUsersOfClouds.size();i++){
			sortUsersWithTraffic(initialUsersOfClouds.get(i));
		}
 
		//�ʰ��� Ŭ���尡 � ���� �ľ� //�ʰ��� Ʈ���� �뷮�� ������ ���
		ArrayList<ServerStatus> serverStateList = getCloudsStatus();
		ArrayList<ServerStatus> surplusServerList = new ArrayList<ServerStatus>();

		ArrayList<ClientTrafficData> extractedUsersList = new ArrayList<ClientTrafficData>(); 
		ArrayList<ClientTrafficData> extractedUsersFromCloud = new ArrayList<ClientTrafficData>();

		//�������� �޿� ���鼭, ���¸� Ȯ���Ѵ�. �� ������ �ƽø� Ʈ������ ������, ����Ǵ� Ʈ������ ������
		//�׷��� ����Ǵ� Ʈ������ �ƽø� Ʈ������ �ʰ��ϴ��� �ʰ����� �ʴ���
		log.info("	* before rematching process");
		for(int i=0; i<serverStateList.size(); i++){

			//print server status
			log.info("		EP no.:" + serverStateList.get(i).getEpNo() + ", Maximum Traffic:" + serverStateList.get(i).getMaximumTraffic()
							+ ", Current Traffic:" + serverStateList.get(i).getCurrentTraffic() + ", (Expected Traffic:" + serverStateList.get(i).getExpectedTraffic()
							+ ",) Remain Traffic:" + serverStateList.get(i).getRemainTraffic());
			
			long expectedTraffic = 0;
			long maximumTraffic = 0;
			
			if(CBroker.isFirstMedium){
				expectedTraffic = serverStateList.get(i).getExpectedTraffic();
				maximumTraffic = serverStateList.get(i).getMaximumTraffic();
				
				if(expectedTraffic > maximumTraffic){
	
					// � EP����, �� �� ������ ����
					long trafficGap = expectedTraffic - maximumTraffic;
					int epNo = serverStateList.get(i).getEpNo();
	
					// trafficGap�� �����Ҷ����� �ֵ� ������
					if(initialUsersOfClouds.get(epNo-1) != null){
						extractedUsersFromCloud = extractUsers(epNo, trafficGap);
					}
					
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
			//is NOT first Medium
			} else {
				expectedTraffic = serverStateList.get(i).getCurrentTraffic();
				maximumTraffic = serverStateList.get(i).getMaximumTraffic();
				
				if(expectedTraffic > maximumTraffic){
					
					// � EP����, �� �� ������ ����
					long trafficGap = expectedTraffic - maximumTraffic;
					int epNo = serverStateList.get(i).getEpNo();
	 
					// trafficGap�� �����Ҷ����� �ֵ� ������
					if(initialUsersOfClouds.get(epNo-1) != null){
						extractedUsersFromCloud = extractUsers(epNo, trafficGap);
					}	
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
		}

		if(surplusServerList.size() > 0){

			// ��ġ�� ������ ���� �ֵ��� �ٸ� Ŭ���忡 ��ġ��Ų��
			if(extractedUsersList.size() > 0){
				rematchUsersToCloud(extractedUsersList, serverStateList, surplusServerList, processType);
			} else {
				log.debug("	* there's no extracted users list");
			}

			log.info("	* after rematching process");
			serverStateList = getCloudsStatus();
			for(int i=0; i<serverStateList.size(); i++){
				//test
				log.info("		EP no.:" + serverStateList.get(i).getEpNo()
						+ ", Maximum Traffic:" + serverStateList.get(i).getMaximumTraffic()
						+ ", Current Traffic:" + serverStateList.get(i).getCurrentTraffic()
						+ ", (Expected Traffic:" + serverStateList.get(i).getExpectedTraffic()
						+ ",) Remain Traffic:" + serverStateList.get(i).getRemainTraffic());
			}

			//������, ������ Ŭ���尡 expected traffic�� �� ������ �� �ִ� ���¸� rematchUsersToCloud�� ������ �ʿ䰡 ����.
		} else {
			log.info("	* there's no surplus server - do not rematch process");

		}
		
		//initialUsersOfClouds ��  prevInitialUsersOfClouds �� Deep copy
		DeepCopyUsersOfClouds();
		 
		log.info("	[rematchForLoadBalancing method] - End");
	}
	
	void DeepCopyUsersOfClouds() {
		
		CBroker.prevInitialUsersOfClouds = new ArrayList<ArrayList<ClientTrafficData>>();
		ArrayList<ArrayList<ClientTrafficData>> test_list = new ArrayList<ArrayList<ClientTrafficData>>();
		ArrayList<ClientTrafficData> usersOfCloud = new ArrayList<ClientTrafficData>();
		ArrayList<ClientTrafficData> copiedUsersOfCloud = new ArrayList<ClientTrafficData>();
		ArrayList<ClientTrafficData> prevUsersOfCloud = new ArrayList<ClientTrafficData>();
		
		for(int i=0; i<initialUsersOfClouds.size(); i++){
			 
			usersOfCloud = initialUsersOfClouds.get(i);
			copiedUsersOfCloud = new ArrayList<ClientTrafficData>();
			for(int j=0; j<usersOfCloud.size(); j++){
				ClientTrafficData eachClientData = new ClientTrafficData(usersOfCloud.get(j));
				copiedUsersOfCloud.add(eachClientData);
			//	prevUsersOfCloud.add(new ClientTrafficData(usersOfCloud.get(j)));
			}
			CBroker.prevInitialUsersOfClouds.add(copiedUsersOfCloud);
			test_list.add(copiedUsersOfCloud);
		}
		
	}
	
	boolean checkCapacity() {
		boolean isEnoughCapacity = true;
		ArrayList<ServerStatus> serverStateList = getCloudsStatus();
		
		for(int i=0; i<serverStateList.size(); i++){
			if(serverStateList.get(i).getCurrentTraffic() > serverStateList.get(i).getMaximumTraffic()){
				isEnoughCapacity = false;
				break;
			}
		}
		
		return isEnoughCapacity;
	}

	public void rematchForLoadBalancing(String processType){
		
		log.info("	[rematchForLoadBalancing method] - Start");
		
		if(processType.equals("MEDIUM_TRAFFIC")) {
			
			//(���ӵ��� ����) ������ Medium traffic ���μ��� ���� �ƴ��� �˻�
		//	boolean isFirstMedium = checkFirstMedium();
			
		//	if(isFirstMedium) {
			if(CBroker.isFirstMedium) {
				log.debug("is First!");
				initialRematchProcess("MEDIUM_TRAFFIC");
			 
			} else {
				log.debug("is NOT First!");
				// prevInitialUsrsOfClouds�� initialUsersOfClouds�� �����Ѵ�
				initialUsersOfClouds = CBroker.prevInitialUsersOfClouds;
				 
				// �� cloud �� capacity�� �����ϴ��� ���ϴ��� �˻�
				boolean isEnoughCapacity = checkCapacity();
				if(isEnoughCapacity){ // i) capacity�� ��� ������ ��
					//do nothing
				
				} else { // ii) capacity�� ��� �������� ���� ��
					
					// getUserTraffic �޼ҵ带 �̿��ؼ� �� ����ڵ��� �̹� �ð� traffic�� ������Ʈ �Ѵ�
					for(int i=0; i<initialUsersOfClouds.size(); i++){
					
						ArrayList<ClientTrafficData> eachCloudUsers = new ArrayList<ClientTrafficData>();
						eachCloudUsers = initialUsersOfClouds.get(i);
						
						for(int j=0; j<eachCloudUsers.size(); j++){
						
							int userTraffic = 0;
							String userId = null;
							
							userId = eachCloudUsers.get(j).getUserId();
							userTraffic = getUserTraffic(userId);
							
							eachCloudUsers.get(j).setUserTraffic(userTraffic);
						}
					}
					
					commonRematchProcess(processType);
				}
			}
			
		} else if(processType.equals("MAXIMUM_TRAFFIC")) {
			
		}
	}
	
	public ArrayList<ClientTrafficData> extractUsers(int epNo, long trafficGap){
		
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
	
	public void rematchUsersToCloud(ArrayList<ClientTrafficData> extractedUsersList, ArrayList<ServerStatus> serverStateList, ArrayList<ServerStatus> surplusServerList, String processType){
		
		log.info("	[rematchUsersToCloud method] - Start");
		
		ArrayList<ArrayList<ClientTrafficData>> rematchResult = initialUsersOfClouds;
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if(surplusServerList.size() > 0 && surplusServerList.size() <= 1){
			log.debug("		0 < surplusServerList.size() <= 1");
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(processType.equals("MEDIUM_TRAFFIC")){
				log.debug("		MEDIUM_TRAFFIC");
				
		//		double trafficGap = surplusServerList.get(0).getMaximumTraffic() - surplusServerList.get(0).getExpectedTraffic();
				long trafficGap = surplusServerList.get(0).getMaximumTraffic() - surplusServerList.get(0).getCurrentTraffic();
				
				//����ڵ���, ���� �ϳ��� ���� Ŭ���忡 �� �����ִ´�.
				for(int i=0; i<extractedUsersList.size(); i++){
					
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					
				//	trafficGap -= (ServerStatus.ratioOfTraffic * userTraffic);
					trafficGap -= userTraffic;
				//	log.debug(" 	* user id : " + extractedUsersList.get(i).getUserId());
				//	log.debug(" 	* Server traffic ratio : " + ServerStatus.ratioOfTraffic);
				//	log.debug(" 	* expected user traffic : " + (ServerStatus.ratioOfTraffic * userTraffic));
				//	log.debug(" 	* traffic gap : " + trafficGap);
					
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
					
					log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if(processType.equals("MAXIMUM_TRAFFIC")){
				log.debug("		MAXIMUM_TRAFFIC");
				
			//	double trafficGap = surplusServerList.get(0).getMaximumTraffic() - surplusServerList.get(0).getExpectedTraffic();
				long trafficGap = surplusServerList.get(0).getMaximumTraffic() - surplusServerList.get(0).getCurrentTraffic();
				int i = 0;
				while(true){
					
					if(i >= extractedUsersList.size())
						break;
					
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					
				//	trafficGap -= (ServerStatus.ratioOfTraffic * userTraffic);
					trafficGap -= userTraffic;
				//	log.debug(" 	* user id : " + extractedUsersList.get(i).getUserId());
				//	log.debug(" 	* Server traffic ratio : " + ServerStatus.ratioOfTraffic);
				//	log.debug(" 	* expected user traffic : " + (ServerStatus.ratioOfTraffic * userTraffic));
				//	log.debug(" 	* traffic gap : " + trafficGap);
					
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
					
				//	log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
				}
			}
			
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}else if(surplusServerList.size() > 1){
			log.debug("		surplusServerList.size() > 1");
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(processType.equals("MEDIUM_TRAFFIC")){
				log.debug("		* traffic type : * MEDIUM_TRAFFIC \n");
				
				//�� �������� �켱������ �ٸ���. ���� �� ������ ��ƾ ������ ó���Ǿ���
				for(int i=0; i<extractedUsersList.size(); i++){
					
					long userTraffic = extractedUsersList.get(i).getUserTraffic();
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
						//*/	
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
						//*/	
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
						
						int initialIndex = 0;
						for(int k=0; k<serverStateList.size(); k++){
							if(initialEpNo == serverStateList.get(k).getEpNo()){
								initialIndex = k;
							}
						}
						
						//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
						String userId = extractedUsersList.get(i).getUserId();
						int userIndex = getUserIndex(initialEpNo, userId);

						//����Ʈ���� ����
						initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);

						
						
						
						//������ �־��� Ŭ������ Remain Traffic ����//////////////////////////////////////////////////////////////////////
						log.debug("			* Remain traffic, before changing (at initial ep) : " + serverStateList.get(initialIndex).getRemainTraffic());
					//	double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
						double expectedUserTraffic = userTraffic;
						long changedRemainTraffic = serverStateList.get(initialIndex).getRemainTraffic() + (long)expectedUserTraffic;
						serverStateList.get(initialIndex).setRemainTraffic(changedRemainTraffic);
						log.debug("			* Remain traffic, after changing (at initial ep): " + serverStateList.get(initialIndex).getRemainTraffic());
						////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
						//���ο� Ŭ���忡 �߰� �Ѵ�, ���� �߰��� Ŭ���� EpNo ã��
						int rematchEpNo = priorWeight.get(0).getEpNo();
						log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
					
						
						
						
						//remain traffic ���� ///////////////////////////////////////////////////////////////////////////////////////
						int rematchEpIndex = 0;
						for(int p=0; p<serverStateList.size(); p++){
							if(serverStateList.get(p).getEpNo() == rematchEpNo){
								rematchEpIndex = p;
								break;
							}
						}
						log.debug("			* Remain traffic, before changing (at rematch ep): " + serverStateList.get(rematchEpIndex).getRemainTraffic());
						changedRemainTraffic = serverStateList.get(rematchEpIndex).getRemainTraffic() - (long)expectedUserTraffic;
						serverStateList.get(rematchEpIndex).setRemainTraffic(changedRemainTraffic);
						log.debug("			* Remain traffic, after changing (at rematch ep): " + serverStateList.get(rematchEpIndex).getRemainTraffic());
						////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
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
				log.debug("		* traffic type : MAXIMUM_TRAFFIC \n");
				
				long trafficGap = 0;
				for(int i=0; i<surplusServerList.size(); i++){
				//	trafficGap += (surplusServerList.get(i).getMaximumTraffic() - surplusServerList.get(i).getExpectedTraffic());
					trafficGap += (surplusServerList.get(i).getMaximumTraffic() - surplusServerList.get(i).getCurrentTraffic());
					long remainTraffic = surplusServerList.get(i).getMaximumTraffic() - surplusServerList.get(i).getCurrentTraffic();
					surplusServerList.get(i).setRemainTraffic(remainTraffic);
				}
				
				//�� �������� �켱������ �ٸ���. ���� �� ������ ��ƾ ������ ó���Ǿ���
				int i = 0;
				while (true) {
	
					if(i >= extractedUsersList.size())
						break;
					
					int userTraffic = extractedUsersList.get(i).getUserTraffic();
					
				//	trafficGap -= (ServerStatus.ratioOfTraffic * userTraffic);
				//	log.debug(" 	* user id : " + extractedUsersList.get(i).getUserId());
				//	log.debug(" 	* Server traffic ratio : " + ServerStatus.ratioOfTraffic);
				//	log.debug(" 	* expected user traffic : " + (ServerStatus.ratioOfTraffic * userTraffic));
				//	log.debug(" 	* traffic gap : " + trafficGap);
					
				//	if((trafficGap - ServerStatus.ratioOfTraffic * userTraffic) < 0)	break;
					if((trafficGap - userTraffic) < 0)	break;
					
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
							log.debug("			* after process, the number of remaining surplus clouds : " + numRemainClouds);
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
							log.debug("			* after process, the number of remaining surplus clouds : " + numRemainClouds);
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
							log.debug("			* after process, the number of remaining surplus clouds : " + numRemainClouds);
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
							log.debug("			* after process, the number of remaining surplus clouds : " + numRemainClouds);
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
						
						int initialIndex = 0;
						for(int k=0; k<serverStateList.size(); k++){
							if(initialEpNo == serverStateList.get(k).getEpNo()){
								initialIndex = k;
							}
						}
						
						//�ش� ����ڰ� �����ִ� ����Ʈ�� ���° �ε����� �־����� �˻�
						String userId = extractedUsersList.get(i).getUserId();
						int userIndex = getUserIndex(initialEpNo, userId);

						//����Ʈ���� ����
						initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);

						
						
						
						//������ �־��� Ŭ������ Remain Traffic ����//////////////////////////////////////////////////////////////////////
						log.debug("			* Remain traffic, before changing (at initial ep) : " + serverStateList.get(initialIndex).getRemainTraffic());
					//	double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
						double expectedUserTraffic = userTraffic;
						long changedRemainTraffic = serverStateList.get(initialIndex).getRemainTraffic() + (long)expectedUserTraffic;
						serverStateList.get(initialIndex).setRemainTraffic(changedRemainTraffic);
						log.debug("			* Remain traffic, after changing (at initial ep): " + serverStateList.get(initialIndex).getRemainTraffic());
						////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
						//���ο� Ŭ���忡 �߰� �Ѵ�, ���� �߰��� Ŭ���� EpNo ã��
						int rematchEpNo = priorWeight.get(0).getEpNo();
						log.debug("			* transferred user id : " + userId + " " + initialEpNo + " -> " + rematchEpNo);
					
						
						
						
						//remain traffic ���� ///////////////////////////////////////////////////////////////////////////////////////
						int rematchEpIndex = 0;
						for(int p=0; p<serverStateList.size(); p++){
							if(serverStateList.get(p).getEpNo() == rematchEpNo){
								rematchEpIndex = p;
								break;
							}
						}
						log.debug("			* Remain traffic, before changing (at rematch ep): " + serverStateList.get(rematchEpIndex).getRemainTraffic());
						changedRemainTraffic = serverStateList.get(rematchEpIndex).getRemainTraffic() - (int)expectedUserTraffic;
						serverStateList.get(rematchEpIndex).setRemainTraffic(changedRemainTraffic);
						log.debug("			* Remain traffic, after changing (at rematch ep): " + serverStateList.get(rematchEpIndex).getRemainTraffic());
						////////////////////////////////////////////////////////////////////////////////////////////////////////////
						
						
						
						
						//traffic gap ����
					//	trafficGap -= (ServerStatus.ratioOfTraffic * userTraffic);
						trafficGap -= userTraffic;
						log.debug(" 	* traffic gap : " + trafficGap);
						
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
		int prevCloudNo = 0;
//		log.debug(" 		* Previous Matching HashMap size : " + CBroker.prevMatch.size());
		for(int i=0; i<initialUsersOfClouds.size(); i++){
			
			ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
			
			for(int j=0; j<usersOfCloud.size(); j++){
				
				String userId = usersOfCloud.get(j).getUserId();
				int cloudNo = usersOfCloud.get(j).getCloudNo();
				
				//Broker ���� �ش� Ŭ������ IP�� Location ������ ����
				databaseInstance2.connectBrokerDatabase();
				String ip = databaseInstance2.getIpWithEpNo(cloudNo);
				String location = databaseInstance2.getLocationWithIp(ip);
				databaseInstance2.disconnectBrokerDatabase();
				
				//BrokerGiver���� �ش� User�� ���� CloudNo ������ ����
				prevCloudNo = databaseInstance.getEpNo(userId);
				if(prevCloudNo != cloudNo){
					log.debug(" 			- same user id -> update broker giver table (UserId, PrevCloudNo, CloudNo) : " + userId + ", " + prevCloudNo + ", " + cloudNo);
				//	databaseInstance.updateBrokerGiverTable(userId, cloudNo, ip, location);
					
					updateCount++;
				} else {
				}
				databaseInstance.updateBrokerGiverTable(userId, cloudNo, ip, location);
			}
		//	System.out.print("(" + basicCount + ") ");
		}
	//	log.debug(" 			- null id count (so, put into the HashMap) : " + nullIdCount);
	//	log.debug(" 			- skip count (cuz, same with the existing match) : " + skipCount);
		log.debug(" 			- update count (cuz, different with the existing match): " + updateCount);
	//	log.debug(" 		* After Matching HashMap size : " + CBroker.prevMatch.size());
		
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
		
	public ArrayList<SurCloudWeight> getSurplusCloudTrafficWeight(ArrayList<ServerStatus> surplusServerList, String userId, long userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
			
			//�ش� ������ ���� �뷮 ����
			log.debug("			* ep number : " + surplusServerList.get(j).getEpNo());
			long remainTraffic = surplusServerList.get(j).getRemainTraffic();
			log.debug("			* remain traffic : " + remainTraffic);
			double expectedUserTraffic = userTraffic;
	//		double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
			log.debug("			* expected user traffic : " + expectedUserTraffic);
			
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
	
	public ArrayList<SurCloudWeight> getSurplusCloudDistanceWeight(ArrayList<ServerStatus> surplusServerList, String userId, long userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
			
			//�ش� ������ ���� �뷮 ����
			log.debug("			* ep number : " + surplusServerList.get(j).getEpNo());
			long remainTraffic = surplusServerList.get(j).getRemainTraffic();
			log.debug("			* remain traffic : " + remainTraffic);
		//	double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
			double expectedUserTraffic = userTraffic;
			log.debug("			* expected user traffic : " + expectedUserTraffic);
			
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
	
	public ArrayList<SurCloudWeight> getSurplusCloudSocialWeight(ArrayList<ServerStatus> surplusServerList, String userId, long userTraffic){
	
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
			
			//�ش� ������ ���� �뷮 ����
			log.debug("			* ep number : " + surplusServerList.get(j).getEpNo());
			long remainTraffic = surplusServerList.get(j).getRemainTraffic();
			log.debug("			* remain traffic : " + remainTraffic);
			double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
			log.debug("			* expected user traffic : " + expectedUserTraffic);
			
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

	public ArrayList<SurCloudWeight> getSurplusCloudWeight(ArrayList<ServerStatus> surplusServerList, String userId, long userTraffic){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;

		for(int j=0; j<surplusServerList.size(); j++){
		
			//user index ã��
			int userIndex = getUserIndex(userId);
			
			//�ش� ������ ���� �뷮 ����
			log.debug("			* ep number : " + surplusServerList.get(j).getEpNo());
			long remainTraffic = surplusServerList.get(j).getRemainTraffic();
			log.debug("			* remain traffic : " + remainTraffic);
			double expectedUserTraffic = userTraffic;
		//	double expectedUserTraffic = userTraffic * ServerStatus.ratioOfTraffic;
			log.debug("			* expected user traffic : " + expectedUserTraffic);
			
			//���࿡ �ش� ������ ���� Ʈ���� ���� �����ϴ°���, ���� Ʈ���� ��� ������ ���ԵǴ� �Ÿ� ��� ���� �ƴϸ� break; 
			if(remainTraffic >= expectedUserTraffic){
				
				//normalized �� ��������
				int surplusEpNo = surplusServerList.get(j).getEpNo();
				double normalizedValue = userWeightList.get(userIndex).getWeightValues()[surplusEpNo-1];
				surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
				
				//�߰�
				priorWeight.add(surCloudWeight);
				
			//	log.debug("				=> number of remain servers: " + priorWeight.size() + "\n");
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
