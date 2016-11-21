import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SocialWeight {
	
	static Logger log = Logger.getLogger(SocialWeight.class.getName());
	
	public static void main(String args []){
		
		PropertyConfigurator.configure("log/log4j.properties");
		
		Database db = new Database();
		db.mySqlConnect();
		
		//CompleteUserId로 UserList (User ID, Location) 뽑아오기
		ArrayList<User> completeUserId = new ArrayList<User>();
		completeUserId = db.getCompletedUserIdList();
		
		System.out.println("logic 1 =============================================================");
		for(int i=0; i<completeUserId.size(); i++){
			System.out.println("[LOGIC 1][" + (i+1) + "/" + completeUserId.size() + "]");
			log.debug("CompleteUserId : " + completeUserId.get(i).getUserId() + "==========================================================================");
			
			//EP1: Washington, EP2: Texas, EP3: NewYork
			String [] cloudLocation = {"Washington", "Texas", "Newyork"};
			double [] arrSumWeightedDistance = new double [3];
			for(int k=0; k<3; k++){
				
				//SocialLevelPerUser로, 각 User의 친구들과 Portion 가지고 오기
				double sumWeightedDistance = 0.0;
				ArrayList<Friend> friendList = db.getFriendInSocialLevelPerUser(completeUserId.get(i).getUserId().toString());
				for(int j=0; j<friendList.size(); j++){
					log.debug("================> Friend ID : " + friendList.get(j).getUserId() + ", LOCATION : " + friendList.get(j).getLocation() + ", PORTION : " + friendList.get(j).getPortion());
					
					double dist = IDistanceCalculation.calculateDistance(friendList.get(j).getLocation().toString(), cloudLocation[k], db);
					double weightedDistance = dist * friendList.get(j).getPortion();
					log.debug("================> Friend LOCATION : " + friendList.get(j).getLocation() + ", Cloud LOCATION : " + cloudLocation[k] + ", DISTANCE : " + dist);
					log.debug("================> DISTANCE * PORTION : " + weightedDistance);
					
					sumWeightedDistance += weightedDistance;
				}
				log.debug("================> Sum Of DISTANCE * PORTION : " + sumWeightedDistance);
				
				arrSumWeightedDistance[k] = sumWeightedDistance;
			}
			log.debug("Array of Weighted Distance [EP1, EP2, EP3] : [" + arrSumWeightedDistance[0] + ", " + arrSumWeightedDistance[1] + ", " + arrSumWeightedDistance[2] + "]");
			
			//여기서 normalize 해야함
			double maxValue = getMax(arrSumWeightedDistance);
			double minValue = getMin(arrSumWeightedDistance);
			double normalizedValue = 0.0;
			log.debug("MaxValue, MinValue : " + maxValue + ", " + minValue);
			double [] normalizedCloudDistances = new double [3];
			for(int k=0; k<3; k++){
				normalizedValue = iNormFormula.normalize(arrSumWeightedDistance[k], maxValue, minValue);
				normalizedCloudDistances[k] = normalizedValue;
			}
			log.debug("Normalized Array of Weighted Distance [EP1, EP2, EP3] : [" + normalizedCloudDistances[0] + ", " + normalizedCloudDistances[1] + ", " + normalizedCloudDistances[2] + "]");
			
			//completeUserId 로 User ID 알아내서 Normalize 한 값 넣기
			db.insertSocialWeight(completeUserId.get(i).getUserId(), normalizedCloudDistances);
			log.debug("=============================================================================================");
		}
		
		db.mySqlDisconnect();
		System.out.println("=====================================================================");
		System.out.println("END Getting Social Weight Table");
		
	}
	
	static double getMax(double [] arrSumWeightedDistance){
		double max = 0.0;
		
		for(int i=0; i<arrSumWeightedDistance.length; i++){
			if(i==0){
				max = arrSumWeightedDistance[0];
			}
			if(max < arrSumWeightedDistance[i])
				max = arrSumWeightedDistance[i];
		}
		
		return max;
	}
	
	static double getMin(double [] arrSumWeightedDistance){
		double min = 0.0;
		
		for(int i=0; i<arrSumWeightedDistance.length; i++){
			if(i==0){
				min = arrSumWeightedDistance[0];
			}
			if(min > arrSumWeightedDistance[i])
				min = arrSumWeightedDistance[i];
		}
		
		return min;
	}
}
