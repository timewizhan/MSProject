
public interface IDistanceCalculation {
	
	final static double M_PI = 3.14159265358979323846;
	
	public static double calculateDistance(String location1, String location2, Database db){
		
	//	CDatabase databaseInstance = new CDatabase();
	//	databaseInstance.connectBrokerDatabase();
		
		//location1, location2 위도 경도 값 할당
		CoordValue coordValue = null;
		coordValue = db.getLatitudeLongitude(location1);
		double location1_latitude = coordValue.getLatitude();
		double location1_longitude = coordValue.getLongitude();
		
		coordValue = db.getLatitudeLongitude(location2);
		double location2_latitude = coordValue.getLatitude();
		double location2_longitude = coordValue.getLongitude();
		
	//	databaseInstance.disconnectBrokerDatabase();
		
		//거리 계산
		double theta, dist;
		theta = location1_longitude - location2_longitude;
		dist = Math.sin(DegToRad(location1_latitude)) * Math.sin(DegToRad(location2_latitude)) + Math.cos(DegToRad(location1_latitude))
			* Math.cos(DegToRad(location2_latitude)) * Math.cos(DegToRad(theta));
		dist = Math.acos(dist);
		dist = RadToDeg(dist);

		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;    // 단위 mile 에서 km 변환.  
		dist = dist * 1000.0;      // 단위  km 에서 m 로 변환  

		return dist;
	}
	
	public static double DegToRad(double dDeg){
		
		return (double)(dDeg * M_PI / (double)180);
	}

	public static double RadToDeg(double dRad){

		return (double)(dRad * (double)180 / M_PI);
	}
}
