
public interface iNormFormula {

	public static double normalize(int elementValue, int maxValue, int minValue){
		
		double dRange = 0.0;
		
		if (maxValue == minValue){
			dRange = 1.0;
		
		}else {
			dRange = 1.0 / (maxValue - minValue);
		}
		
		double normalizedVal = (elementValue - minValue)*dRange;
		
		return normalizedVal;
	}
	
	public static double normalize(double elementValue, double maxValue, double minValue){
		
		double dRange = 0.0;
		
		if (maxValue == minValue){
			dRange = 1.0;
		
		}else {
			dRange = 1.0 / (maxValue - minValue);
		}
		
		double normalizedVal = (elementValue - minValue)*dRange;
		
		return normalizedVal;
	}
}
