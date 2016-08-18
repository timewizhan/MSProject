import java.util.ArrayList;
import java.util.HashMap;

public class CLBCalculation {
	
	HashMap<String , Integer> map;
	
	public CLBCalculation(){
		
		//Ư�� �����ǿ� �ִ� ������ �ִ� Ʈ���� ��
		//ex) 165.132.120.144�� �ִ� ������ �ִ� Ʈ���� ���� 500Gbyte�̴�
		map = new HashMap<String , Integer>();
		map.put("165.132.120.144", 500);
		map.put("165.132.123.73", 500);
		map.put("165.132.122.244", 500);
		map.put("165.132.122.245", 500);
	}
	
	//load balancing ���� �޼ҵ�
	public void lbMain(){
		
		//process Ÿ���� �����ϴ� �޼ҵ� : Ʈ���� ���� ��� Ŭ���尡 �޾Ƶ��� �� �ִ� �������� �ƴ����� 3�ܰ�� ����
		String processType = determineProcessType();
		
		if(processType.equals("MINIMUM_TRAFFIC")){
			//LP�� �ϰ� ������. LB ���μ��� ���� ����
		} else if (processType.equals("MEDIUM_TRAFFIC")){
			rematchForLoadBalancing();
		} else if (processType.equals("MAXIMUM_TRAFFIC")){
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
	
		//"��� Ŭ���忡 ��Ī��" : ���� Ʈ���� ���� �ʰ��� Ŭ���� 
		��ġ ����� ��� ��̸���Ʈ�� ������ �����ٵ� �װ� ����, ���뷮�� �Ѿ���� �ȳѾ���� ����
		���� �Ѿ�� �� ���� ����ġ
		
		//"������" : �ش� ���� �ð� ���� ��뷮�� ���� ���� ��������
		//"�����" : �Ѹ��� �����Ͱ� �߻���ų �� �ִ� ���� Ʈ������ ����� ��, LP����� ���� ��Ī�� ��(�̰� Ʈ�������� ����ؾ���)���� ����� �����͸� ���� ���� Ʈ���� ���� �ȿ� ������ ����ϰ�, �׸�ŭ ��
		//"����" : �켱 ������ ���� �ű��, �������� ���� Ʈ������ ����ؼ� �ű�
		
	}
}
