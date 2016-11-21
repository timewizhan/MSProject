
public interface IDistanceCalculation {
	
	final static double M_PI = 3.14159265358979323846;
	
	public static double calculateDistance(String location1, String location2, Database db){
		
	//	CDatabase databaseInstance = new CDatabase();
	//	databaseInstance.connectBrokerDatabase();
		
		//location1, location2 ���� �浵 �� �Ҵ�
		CoordValue coordValue = null;
		coordValue = db.getLatitudeLongitude(location1);
		double location1_latitude = coordValue.getLatitude();
		double location1_longitude = coordValue.getLongitude();
		
		coordValue = db.getLatitudeLongitude(location2);
		double location2_latitude = coordValue.getLatitude();
		double location2_longitude = coordValue.getLongitude();
		
	//	databaseInstance.disconnectBrokerDatabase();
		
		//�Ÿ� ���
		double theta, dist;
		theta = location1_longitude - location2_longitude;
		dist = Math.sin(DegToRad(location1_latitude)) * Math.sin(DegToRad(location2_latitude)) + Math.cos(DegToRad(location1_latitude))
			* Math.cos(DegToRad(location2_latitude)) * Math.cos(DegToRad(theta));
		dist = Math.acos(dist);
		dist = RadToDeg(dist);

		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;    // ���� mile ���� km ��ȯ.  
		dist = dist * 1000.0;      // ����  km ���� m �� ��ȯ  

		return dist;
	}
	
	public static double DegToRad(double dDeg){
		
		return (double)(dDeg * M_PI / (double)180);
	}

	public static double RadToDeg(double dRad){

		return (double)(dRad * (double)180 / M_PI);
	}
}
