import java.util.ArrayList;
import java.util.HashMap;

public class CLBCalculation {
	
	HashMap<String , Integer> map;
	ArrayList<LpMatchResult> lpMatchResult;
	ArrayList<ArrayList<ClientTrafficData>> initialUsersOfClouds;
	ArrayList<UserWeight> userWeightList;
	
	public CLBCalculation(ArrayList<UserWeight> userWeightList){
		//�켱���� �Ǵ��Ҷ� ���
		this.userWeightList = userWeightList;
		
		//Ư�� �����ǿ� �ִ� ������ �ִ� Ʈ���� ��
		//ex) 165.132.120.144�� �ִ� ������ �ִ� Ʈ���� ���� 500Gbyte�̴�
		map = new HashMap<String , Integer>();
		map.put("165.132.120.144", 300);
		map.put("165.132.123.73", 400);
		map.put("165.132.122.244", 500);
		map.put("165.132.122.245", 500);
	}
	
	//load balancing ���� �޼ҵ�
	public void lbMain(ArrayList<LpMatchResult> lpMatchRes){

		//LP calculation�� ��ġ ��� ����
		copyLpMatchResult(lpMatchRes);
		
		//process Ÿ���� �����ϴ� �޼ҵ� : Ʈ���� ���� ��� Ŭ���尡 �޾Ƶ��� �� �ִ� �������� �ƴ����� 3�ܰ�� ����
//		String processType = determineProcessType();
		
//		if(processType.equals("MINIMUM_TRAFFIC")){
			//LP�� �ϰ� ������. LB ���μ��� ���� ����
//		} else if (processType.equals("MEDIUM_TRAFFIC")){
			rematchForLoadBalancing();
//		} else if (processType.equals("MAXIMUM_TRAFFIC")){
		//����Ʈ���ȸ����ε� �̹� ��� �������ų�, ����Ʈ���ȸ����ε� �̹� ��� ��������
			//��ġ�� �κп� ���ؼ��� LB�Ѵ�? ���� ���� �ȵǴµ� �̹� �ְ��� ��õ��. --> �״�� �д�. �� �´µ�.
		//���� ��� �������� �ʾ�����	
			//1�������� rematchForLoadBalancing�� ���ؼ� ��� Ŭ������ ����Ʈ������ ���� �ִ�ġ�� ����������
			//�׷��� ���� ���� �κ��� lp������� ��õ���ִ� ������
//		} 
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
		
		String processType = null;
	
		int serverTotalTraffic = getTotalTraffic();			//�ش�ð��� �߻��� Ʈ���� �ѷ�
		int sumCloudsCapacity = sumCloudMaxCapacity();		//Ŭ������� ���� ���� Ʈ���� ���� ��
		int minCloudCapacity = getMinCloudCapacity();		//���� ���� Ʈ���� ���� ���� ���� Ŭ������ �뷮 �� 
		
		if(serverTotalTraffic <= minCloudCapacity){			//LP ����� ���� ���� ���� Ʈ���Ⱦ��� ���� ���� ���뷮�� ������ �ִ� Ŭ������ ������ ������, �� "�ش� �ð��� �߻��ϴ� Ʈ������ �ϳ��� Ŭ���尡 �� �����Ҽ�������" 
			processType = "MINIMUM_TRAFFIC";
		} else if(minCloudCapacity < serverTotalTraffic && serverTotalTraffic <= sumCloudsCapacity) {	//���� ��캸�� ũ��, ��� Ŭ������ ���� ���� �뷮�� ��ģ�� ���� ������
			processType = "MEDIUM_TRAFFIC";
		} else if(serverTotalTraffic > sumCloudsCapacity){	//��� Ŭ������ ���� ���� �뷮 ���� �� Ŭ��
			processType = "MAXIMUM_TRAFFIC";
		}
		
		return processType;
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
		
		serverList = getServerList();
		
		databaseInstance.disconnectBrokerDatabase();
		
		return serverList;
	}
	
	public void rematchForLoadBalancing(){
		//lp calculation ����� ����, �� Ŭ���� ���� ��ġ�� ������ ������ 
		getMatchedUsersToClouds();
	/*	
		//test
		System.out.println();
		System.out.println("[before sorting]");
		for(int i=0; i<initialUsersOfClouds.size(); i++){
			if(initialUsersOfClouds.get(i) != null){
				
				ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
				for(int j=0; j<usersOfCloud.size(); j++){
					System.out.println("user id : " + usersOfCloud.get(j).getUserId()
							+ ", cloud No : " + usersOfCloud.get(j).getCloudNo()
							+ ", traffic : " + usersOfCloud.get(j).getUserTraffic());
				}
			}
		}
	*/	
		
		//Ʈ���� �翡 ���� �����ϱ�
		for(int i=0; i<initialUsersOfClouds.size();i++){
			sortUsersWithTraffic(initialUsersOfClouds.get(i));
		}
	
	
		//test
		System.out.println();
		System.out.println("[after sorting]");
		for(int i=0; i<initialUsersOfClouds.size(); i++){
			if(initialUsersOfClouds.get(i) != null){
				
				ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(i);
				for(int j=0; j<usersOfCloud.size(); j++){
					System.out.println("user id : " + usersOfCloud.get(j).getUserId()
							+ ", cloud No : " + usersOfCloud.get(j).getCloudNo()
							+ ", traffic : " + usersOfCloud.get(j).getUserTraffic());
				}
			}
		}
				
		//�ʰ��� Ŭ���尡 � ���� �ľ� //�ʰ��� Ʈ���� �뷮�� ������ ���
		System.out.println();
		System.out.println("[Server status]");
		
		ArrayList<ServerStatus> serverStateList = getCloudsStatus();
		ArrayList<ServerStatus> surplusServerList = new ArrayList<ServerStatus>();
		
		ArrayList<ClientTrafficData> extractedUsersList = new ArrayList<ClientTrafficData>(); 
		ArrayList<ClientTrafficData> extractedUsersFromCloud = new ArrayList<ClientTrafficData>();
		
		//�������� �޿� ���鼭, ���¸� Ȯ���Ѵ�. �� ������ �ƽø� Ʈ������ ������, ����Ǵ� Ʈ������ ������
		//�׷��� ����Ǵ� Ʈ������ �ƽø� Ʈ������ �ʰ��ϴ��� �ʰ����� �ʴ���
		for(int i=0; i<serverStateList.size(); i++){
			
			//test
			System.out.println("EP no.:" + serverStateList.get(i).getEpNo()
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
		
		// ��ġ�� ������ ���� �ֵ��� �ٸ� Ŭ���忡 ��ġ��Ų��
		rematchUsersToCloud(extractedUsersList, surplusServerList);
		
/////////////////////////////////////////////////////////////////////////////////////////////////		
		
		//"��� Ŭ���忡 ��Ī��" : ���� Ʈ���� ���� �ʰ��� Ŭ���� 
	/**
		��ġ ����� ��� ��̸���Ʈ�� ������ �����ٵ� �װ� ����, ���뷮�� �Ѿ���� �ȳѾ���� ����
		���� �Ѿ�� �� ���� ����ġ
	*/	
		//"������" : �ش� ���� �ð� ���� ��뷮�� ���� ���� ��������
		//"�����" : �Ѹ��� �����Ͱ� �߻���ų �� �ִ� ���� Ʈ������ ����� ��, LP����� ���� ��Ī�� ��(�̰� Ʈ�������� ����ؾ���)���� ����� �����͸� ���� ���� Ʈ���� ���� �ȿ� ������ ����ϰ�, �׸�ŭ ��
		//"����" : �켱 ������ ���� �ű��, �������� ���� Ʈ������ ����ؼ� �ű�
		
	}
	
	public ArrayList<ClientTrafficData> extractUsers(int epNo, int trafficGap){
		
		ArrayList<ClientTrafficData> usersOfCloud = initialUsersOfClouds.get(epNo-1);
		for(int j=0; j<usersOfCloud.size(); j++){
			asdfasdf
		}
	}
	
	public void rematchUsersToCloud(ArrayList<ClientTrafficData> extractedUsersList, ArrayList<ServerStatus> surplusServerList){
		
		ArrayList<ArrayList<ClientTrafficData>> rematchResult = initialUsersOfClouds;
		
		if(surplusServerList.size() <= 1){
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
			
		}else{
			
			//�� �������� �켱������ �ٸ���. ���� �� ������ ��ƾ ������ ó���Ǿ���
			for(int i=0; i<extractedUsersList.size(); i++){
				
				
				int numRemainClouds = surplusServerList.size();
				int priority = 1;
				
				while(numRemainClouds>1){
					
					//�켱���� 1. social + distance
					if(priority == 1){									
					
						//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
						String userId = extractedUsersList.get(i).getUserId();
						ArrayList<SurCloudWeight> priorWeight = getSurplusCloudWeight(surplusServerList, userId);
						
						//�ּ� �� ���ϱ�
						double minValue = getMinWeightValue();
						
						//���� �ּ� ���� ������ �ִ� Ŭ������� �����
						priorWeight = getCloudHavingMinWeight(minValue);
						
						//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
						numRemainClouds = priorWeight.size();
					
					//�켱���� 2. social	
					}else if(priority == 2){	
						
						//�� ����ڿ� ���� �׿� Ŭ������� ����ġ �� ����Ʈ ���ϱ�
						String userId = extractedUsersList.get(i).getUserId();
						ArrayList<SurCloudWeight> priorWeight = getSurplusCloudSocialWeight(surplusServerList, userId);
						
						//�ּ� �� ���ϱ�
						double minValue = getMinWeightValue();
						
						//���� �ּ� ���� ������ �ִ� Ŭ������� �����
						priorWeight = getCloudHavingMinWeight(minValue);
						
						//�ּ� ���� ������ �ִ� Ŭ���� ���� ���ϱ�
						numRemainClouds = priorWeight.size();
					
					//�켱���� 3. distance
					}else if(priority == 3){	
						//�ּ� �� ���ϱ�
						//���� ��� ���� ���ϱ�
						numRemainClouds = ���� ��� ��;
					
					//�켱���� 4. traffic
					}else if(priority == 4){
						//�ּ� �� ���ϱ�
						//���� ��� ���� ���ϱ�
						numRemainClouds = ���� ��� ��;
					
					//�켱���� 5. random
					}else if(priority == 5){
						
					}
					
					priority++;
				}
				
				
			}
		}
	}
	
	public ArrayList<SurCloudWeight> getCloudHavingMinWeight(double minValue){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		
		int iterCnt = priorWeight.size();
		for(int j=iterCnt; j>0; j--){
			if(minValue < priorWeight.get(j).getWeightValue()){
				priorWeight.remove(j);
			}
		}
		
		return priorWeight;
	}
	
	public ArrayList<SurCloudWeight> getSurplusCloudSocialWeight(ArrayList<ServerStatus> surplusServerList, String userId){
	
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		SurCloudWeight surCloudWeight = null;
		
		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();

		for(int j=0; j<surplusServerList.size(); j++){
		
			//normalized �� ��������
			int surplusEpNo = surplusServerList.get(j).getEpNo();
			double normalizedValue = databaseInstance.getNormalizedSocialWeightValue(userId, surplusEpNo);
			surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
		}
		
		databaseInstance.disconnectBrokerDatabase();
		
		return priorWeight;
	}
	
	public ArrayList<SurCloudWeight> getSurplusCloudWeight(ArrayList<ServerStatus> surplusServerList, String userId){
		
		ArrayList<SurCloudWeight> priorWeight = new ArrayList<SurCloudWeight>();
		
		SurCloudWeight surCloudWeight = null;
		
		for(int j=0; j<surplusServerList.size(); j++){
		
			//user index ã��
			int userIndex = getUserIndex(userId);
		
			//normalized �� ��������
			int surplusEpNo = surplusServerList.get(j).getEpNo();
			double normalizedValue = userWeightList.get(userIndex).getWeightValues()[surplusEpNo-1];
			surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
			
			//�߰�
			priorWeight.add(surCloudWeight);
		}
		
		return priorWeight;
	}
	
	public double getMinWeightValue(){
		double minValue = 0;
		asdfasdf
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
		for(int i=0; i<CLPCalculation.NUM_OF_EP; i++){
			initialUsersOfClouds.add(null);
		}
		
		ArrayList<ClientTrafficData> usersOfCloud = null;
		ClientTrafficData clientTrafficData = null; 
		int epNo = CLPCalculation.NUM_OF_EP;
		
		for(int j=0; j<CLPCalculation.NUM_OF_EP; j++){
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
