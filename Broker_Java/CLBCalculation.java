import java.util.ArrayList;
import java.util.HashMap;

public class CLBCalculation {
	
	HashMap<String , Integer> map;
	ArrayList<LpMatchResult> lpMatchResult;
	ArrayList<ArrayList<ClientTrafficData>> initialUsersOfClouds;
	ArrayList<UserWeight> userWeightList;
	
	public CLBCalculation(ArrayList<UserWeight> userWeightList){
		//우선순위 판단할때 사용
		this.userWeightList = userWeightList;
		
		//특정 아이피에 있는 서버의 최대 트래픽 값
		//ex) 165.132.120.144에 있는 서버의 최대 트래픽 양은 500Gbyte이다
		map = new HashMap<String , Integer>();
		map.put("165.132.120.144", 300);
		map.put("165.132.123.73", 400);
		map.put("165.132.122.244", 500);
		map.put("165.132.122.245", 500);
	}
	
	//load balancing 메인 메소드
	public void lbMain(ArrayList<LpMatchResult> lpMatchRes){

		//LP calculation의 매치 결과 저장
		copyLpMatchResult(lpMatchRes);
		
		//process 타입을 결정하는 메소드 : 트래픽 양이 모든 클라우드가 받아들일 수 있는 정도인지 아닌지를 3단계로 나눔
//		String processType = determineProcessType();
		
//		if(processType.equals("MINIMUM_TRAFFIC")){
			//LP만 하고 끝낸다. LB 프로세스 동작 안함
//		} else if (processType.equals("MEDIUM_TRAFFIC")){
			rematchForLoadBalancing();
//		} else if (processType.equals("MAXIMUM_TRAFFIC")){
		//예상트래픽만으로도 이미 모두 가득차거나, 예상트래픽만으로도 이미 모두 넘쳤을때
			//넘치는 부분에 대해서만 LB한다? ㄴㄴ 말이 안되는듯 이미 최고의 추천임. --> 그대로 둔다. 가 맞는듯.
		//아직 모두 가득차진 않았을때	
			//1차적으론 rematchForLoadBalancing을 통해서 모든 클라우드의 예상트래픽이 가용 최대치에 맞춰져야함
			//그러고 나서 남은 부분은 lp결과에서 추천해주는 곳으로
//		} 
	}
	
	public void copyLpMatchResult(ArrayList<LpMatchResult> lpMatchRes){
		//ArrayList의 Deep Copy
		lpMatchResult = new ArrayList<LpMatchResult>();
		for (int i=0; i<lpMatchRes.size(); i++) {
			LpMatchResult initMatchResult = new LpMatchResult(lpMatchRes.get(i));
			lpMatchResult.add(initMatchResult);
		}
	}
	
	public String determineProcessType(){
		
		String processType = null;
	
		int serverTotalTraffic = getTotalTraffic();			//해당시간에 발생한 트래픽 총량
		int sumCloudsCapacity = sumCloudMaxCapacity();		//클라우드들의 수용 가능 트래픽 양의 합
		int minCloudCapacity = getMinCloudCapacity();		//수용 가능 트래픽 양이 가장 작은 클라우드의 용량 값 
		
		if(serverTotalTraffic <= minCloudCapacity){			//LP 결과에 의해 나온 예상 트래픽양이 가장 작은 수용량을 가지고 있는 클라우드의 값보다 작을때, 즉 "해당 시간에 발생하는 트래픽을 하나의 클라우드가 다 감당할수있을때" 
			processType = "MINIMUM_TRAFFIC";
		} else if(minCloudCapacity < serverTotalTraffic && serverTotalTraffic <= sumCloudsCapacity) {	//앞의 경우보단 크고, 모든 클라우드의 수용 가능 용량을 합친거 보단 작을때
			processType = "MEDIUM_TRAFFIC";
		} else if(serverTotalTraffic > sumCloudsCapacity){	//모든 클라우드의 수용 가능 용량 보다 더 클때
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
		//lp calculation 결과에 따라, 각 클라우드 별로 매치된 유저들 모으기 
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
		
		//트래픽 양에 따라 소팅하기
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
				
		//초과한 클라우드가 어떤 건지 파악 //초과한 트래픽 용량이 얼마인지 계산
		System.out.println();
		System.out.println("[Server status]");
		
		ArrayList<ServerStatus> serverStateList = getCloudsStatus();
		ArrayList<ServerStatus> surplusServerList = new ArrayList<ServerStatus>();
		
		ArrayList<ClientTrafficData> extractedUsersList = new ArrayList<ClientTrafficData>(); 
		ArrayList<ClientTrafficData> extractedUsersFromCloud = new ArrayList<ClientTrafficData>();
		
		//서버들을 쭈욱 보면서, 상태를 확인한다. 각 서버의 맥시멈 트래픽은 얼마인지, 예상되는 트래픽은 얼마인지
		//그래서 예상되는 트래픽이 맥시멈 트래픽을 초과하는지 초과하지 않는지
		for(int i=0; i<serverStateList.size(); i++){
			
			//test
			System.out.println("EP no.:" + serverStateList.get(i).getEpNo()
					+ ", Maximum Traffic:" + serverStateList.get(i).getMaximumTraffic()
					+ ", Expected Traffic:" + serverStateList.get(i).getExpectedTraffic());

			int expectedTraffic = serverStateList.get(i).getExpectedTraffic();
			int maximumTraffic = serverStateList.get(i).getMaximumTraffic();
			
			if(expectedTraffic > maximumTraffic){
			
				// 어떤 EP에서, 몇 명 빼올지 결정
				int trafficGap = expectedTraffic - maximumTraffic;
				int epNo = serverStateList.get(i).getEpNo();
				
				// trafficGap을 만족할때까지 애들 빼오기
				extractedUsersFromCloud = extractUsers(epNo, trafficGap);
				// 빼내온 모든 애들을 모아두는 list에 각 cloud에서 빼내온 애들을 추가
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
		
		// 넘치는 곳에서 빼온 애들을 다른 클라우드에 매치시킨다
		rematchUsersToCloud(extractedUsersList, surplusServerList);
		
/////////////////////////////////////////////////////////////////////////////////////////////////		
		
		//"어느 클라우드에 매칭된" : 가용 트래픽 보다 초과된 클라우드 
	/**
		매치 결과를 디비나 어레이리스트로 가지고 있을텐데 그걸 보고, 가용량을 넘어서는지 안넘어서는지 구분
		많이 넘어서는 놈 부터 리매치
	*/	
		//"누구를" : 해당 단위 시간 동안 사용량이 가장 적은 유저부터
		//"몇명을" : 한명의 데이터가 발생시킬 수 있는 예상 트래픽을 계산한 후, LP결과에 의해 매칭된 것(이걸 트래픽으로 계산해야함)에서 몇명의 데이터를 빼야 가용 트래픽 범위 안에 들어서는지 계산하고, 그만큼 뺌
		//"어디로" : 우선 순위에 따라 옮기되, 목적지의 가용 트래픽을 고려해서 옮김
		
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
			//사용자들을, 저기 하나의 남는 클라우드에 다 때려넣는다.
			for(int i=0; i<extractedUsersList.size(); i++){

			//원래 있던 리스트에서는 제거한다
				//사용자가 최초에 매칭됐던 EP번호
				int initialEpNo = extractedUsersList.get(i).getCloudNo();
			
				//해당 사용자가 원래있던 리스트의 몇번째 인덱스에 있었는지 검색
				String userId = extractedUsersList.get(i).getUserId();
				int userIndex = getUserIndex(initialEpNo, userId);
				
				//리스트에서 제거
				initialUsersOfClouds.get(initialEpNo-1).remove(userIndex);
				
			//새로운 클라우드에 추가 한다
				//새로 추가할 클라우드 EpNo 찾기
				int rematchEpNo = surplusServerList.get(0).getEpNo();
				
				//사용자 EpNo 정보 업데이트
				extractedUsersList.get(i).setCloudNo(rematchEpNo);
				
				//추가
				initialUsersOfClouds.get(rematchEpNo-1).add(extractedUsersList.get(i));
			}
			
		}else{
			
			//각 유저마다 우선순위가 다르다. 따라서 각 유저의 루틴 내에서 처리되야함
			for(int i=0; i<extractedUsersList.size(); i++){
				
				
				int numRemainClouds = surplusServerList.size();
				int priority = 1;
				
				while(numRemainClouds>1){
					
					//우선순위 1. social + distance
					if(priority == 1){									
					
						//각 사용자에 대한 잉여 클라우드들의 가중치 값 리스트 구하기
						String userId = extractedUsersList.get(i).getUserId();
						ArrayList<SurCloudWeight> priorWeight = getSurplusCloudWeight(surplusServerList, userId);
						
						//최소 값 구하기
						double minValue = getMinWeightValue();
						
						//같은 최소 값을 가지고 있는 클라우드들이 어떤건지
						priorWeight = getCloudHavingMinWeight(minValue);
						
						//최소 값을 가지고 있는 클라우드 개수 구하기
						numRemainClouds = priorWeight.size();
					
					//우선순위 2. social	
					}else if(priority == 2){	
						
						//각 사용자에 대한 잉여 클라우드들의 가중치 값 리스트 구하기
						String userId = extractedUsersList.get(i).getUserId();
						ArrayList<SurCloudWeight> priorWeight = getSurplusCloudSocialWeight(surplusServerList, userId);
						
						//최소 값 구하기
						double minValue = getMinWeightValue();
						
						//같은 최소 값을 가지고 있는 클라우드들이 어떤건지
						priorWeight = getCloudHavingMinWeight(minValue);
						
						//최소 값을 가지고 있는 클라우드 개수 구하기
						numRemainClouds = priorWeight.size();
					
					//우선순위 3. distance
					}else if(priority == 3){	
						//최소 값 구하기
						//같은 요소 개수 구하기
						numRemainClouds = 같은 요소 수;
					
					//우선순위 4. traffic
					}else if(priority == 4){
						//최소 값 구하기
						//같은 요소 개수 구하기
						numRemainClouds = 같은 요소 수;
					
					//우선순위 5. random
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
		
			//normalized 값 가져오기
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
		
			//user index 찾기
			int userIndex = getUserIndex(userId);
		
			//normalized 값 가져오기
			int surplusEpNo = surplusServerList.get(j).getEpNo();
			double normalizedValue = userWeightList.get(userIndex).getWeightValues()[surplusEpNo-1];
			surCloudWeight = new SurCloudWeight(surplusEpNo, normalizedValue);
			
			//추가
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
