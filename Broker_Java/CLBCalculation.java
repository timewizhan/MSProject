import java.util.ArrayList;
import java.util.HashMap;

public class CLBCalculation {
	
	HashMap<String , Integer> map;
	
	public CLBCalculation(){
		
		//특정 아이피에 있는 서버의 최대 트래픽 값
		//ex) 165.132.120.144에 있는 서버의 최대 트래픽 양은 500Gbyte이다
		map = new HashMap<String , Integer>();
		map.put("165.132.120.144", 500);
		map.put("165.132.123.73", 500);
		map.put("165.132.122.244", 500);
		map.put("165.132.122.245", 500);
	}
	
	//load balancing 메인 메소드
	public void lbMain(){
		
		//process 타입을 결정하는 메소드 : 트래픽 양이 모든 클라우드가 받아들일 수 있는 정도인지 아닌지를 3단계로 나눔
		String processType = determineProcessType();
		
		if(processType.equals("MINIMUM_TRAFFIC")){
			//LP만 하고 끝낸다. LB 프로세스 동작 안함
		} else if (processType.equals("MEDIUM_TRAFFIC")){
			rematchForLoadBalancing();
		} else if (processType.equals("MAXIMUM_TRAFFIC")){
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
	
		//"어느 클라우드에 매칭된" : 가용 트래픽 보다 초과된 클라우드 
		매치 결과를 디비나 어레이리스트로 가지고 있을텐데 그걸 보고, 가용량을 넘어서는지 안넘어서는지 구분
		많이 넘어서는 놈 부터 리매치
		
		//"누구를" : 해당 단위 시간 동안 사용량이 가장 적은 유저부터
		//"몇명을" : 한명의 데이터가 발생시킬 수 있는 예상 트래픽을 계산한 후, LP결과에 의해 매칭된 것(이걸 트래픽으로 계산해야함)에서 몇명의 데이터를 빼야 가용 트래픽 범위 안에 들어서는지 계산하고, 그만큼 뺌
		//"어디로" : 우선 순위에 따라 옮기되, 목적지의 가용 트래픽을 고려해서 옮김
		
	}
}
