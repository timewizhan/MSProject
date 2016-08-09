
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class CNormalizeDistance implements Callable{

	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		System.out.println("normalization of distance start");

		CDatabase databaseInstance = new CDatabase();
		databaseInstance.connectBrokerDatabase();
		ArrayList<ServerData> alServerData = databaseInstance.extractLocation();
		
		Iterator it = alServerData.iterator();
		while(it.hasNext()){
			ServerData serverData = (ServerData) it.next();
		
			double normalizedValue = 0.0;
			normalizedValue = iNormFormula.normalize(serverData.getServerTraffic(), ServerData.maxServerTraffic, ServerData.minServerTraffic);
			
			databaseInstance.insertNormServerData(serverData.getEpAddr(), normalizedValue);
		}
		
		System.out.println("normalization of distance was done");

		return null;
		
		
		///////////////////////////////////////
		
		sQuery = "select * from client_table";
		vector <client_data> vecDataList_client = databaseInstance.extractClientData(sQuery);

		vector <int> vec_distance_list;
		for (int i = 0; i < vecDataList_client.size(); i++){
			string sEachLocation = vecDataList_client.at(i).sLocation;
			
			//���⿡ ������ �����ͼ��Ϳ� �Ÿ���� �Լ�
			//EP1(Washington : 38.9071923, -77.0368707)
			//EP2(Texas		 : 31.9685988, -99.9018131)
			//EP3(Newyork	 : 40.7127837, -74.0059413)

			coord_value stUserCoordValue = databaseInstance.ExtractCoordValue(sEachLocation);

//			printf("[%s] stUserCoordValue: lat- %f, lon- %f \n", sEachLocation.c_str(), stUserCoordValue.latitude, stUserCoordValue.longitude);

			double iDistFromEP1 = CalculateDistEp1(stUserCoordValue); //EP1���� �Ÿ�
			double iDistFromEP2 = CalculateDistEp2(stUserCoordValue); //EP2���� �Ÿ�
			double iDistFromEP3 = CalculateDistEp3(stUserCoordValue); //EP3���� �Ÿ�
			
//			printf("location: %s, EP1 Dist: %f, EP2 Dist: %f, EP3 Dist: %f \n"
//				, sEachLocation.c_str(), iDistFromEP1, iDistFromEP2, iDistFromEP3);

			double arrDists[3] = {iDistFromEP1, iDistFromEP2, iDistFromEP3};

			//���� �߿��� �ִ밪 �ּҰ� ���ϴ� �Լ� ȣ��
			double iMaxValue = FindMaxDist(arrDists);
			double iMinValue = FindMinDist(arrDists);

			//normalize
			dRange = 0.0;
			if (iMaxValue == iMinValue){
				dRange = 1.0;
			}
			else {
				dRange = 1.0 / (iMaxValue - iMinValue);
			}

			for (int i = 0; i < sizeof(arrDists)/sizeof(double); i++){

				double dNormalizedVal = (arrDists[i] - iMinValue)*dRange;
				arrDists[i] = dNormalizedVal;
			}

			//���⼭ ��ֶ������� �� ���̺� ������Ʈ.. 
			databaseInstance.InsertNormDistTable(vecDataList_client.at(i).sUser, arrDists[0], arrDists[1], arrDists[2]);
		}

		databaseInstance.CloseDB();
		databaseInstance.~CDatabase();
	}

}
