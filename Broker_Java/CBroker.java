/***********************************************************************************************
EntryPoint의 개수가 달라지면, 코드 상에서 변화가 생겨야 하는 부분
1. CLPCalculation의 static final int NUM_OF_EP = 3;
2. CLBCalculation의 생성자 부분에 IP와 Maximum Traffic 매칭 
3. broker_table DB에 locationIP 테이블 값 수정: 이 떄 여기에 있는 값은 실험하는 데이터센터의 개수와 정확히 일치/동일해야함
     예를 들어 데이터센터의 개수가 3개면 3개에 대한 정보만 있어야함
4. broker_table DB에 normalized_social_level_table 수정
5. CLPCalculation의 LP 수식에 가중치 적용하는 부분 수정
************************************************************************************************/

/**********************************************************************************************
실험할때 체크해야되는 부분
1. database 커넥션 생성하고 안끊는 부분 없는지
2. 하나의 루틴이 다돌고 데이터베이스 초기화 하는지 
   (locationIP의 ep 컬럼이 null이 되는지도 체크해야함) 
3. EP 새로 올리는 컴퓨터에 DB 접근 권한 (및 방화벽) 체크  

future work로 해야할까..
4. 각 User별로 특정 시간에 사용되는 예상 트래픽 양이 DB에 유지되고 있어야한다. 이 부분은 머신러닝을 통해서 학습시켜놓는게 좋을듯
   -> 봇은 실제로 사용한것과 동일하게 행동해야한다. 전체를 평균화 해서 매일 똑같이 행동하게 하면 안됨
**********************************************************************************************/

public class CBroker {
	
	public static void main(String [] args){
		
		System.out.println("Broker Start");
		
		ThreadPool.GetInstance().execute(new CLPCalculation());
		
		CNetworkServer cBroker = new CNetworkServer(); 
		cBroker.start();
	}
}
