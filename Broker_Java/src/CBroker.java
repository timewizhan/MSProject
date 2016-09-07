import org.apache.log4j.Logger;
import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PropertyConfigurator;

/***********************************************************************************************
EntryPoint�� ������ �޶�����, �ڵ� �󿡼� ��ȭ�� ���ܾ� �ϴ� �κ�
1. Main�� static final int NUM_OF_EP = 3;
2. CLBCalculation�� ������ �κп� IP�� Maximum Traffic ��Ī 
3. broker_table DB�� locationIP ���̺� �� ����: �� �� ���⿡ �ִ� ���� �����ϴ� �����ͼ����� ������ ��Ȯ�� ��ġ/�����ؾ���
     ���� ��� �����ͼ����� ������ 3���� 3���� ���� ������ �־����
4. broker_table DB�� normalized_social_level_table ����
5. CLPCalculation�� LP ���Ŀ� ����ġ �����ϴ� �κ� ����
************************************************************************************************/

/**********************************************************************************************
�����Ҷ� üũ�ؾߵǴ� �κ�
1. database Ŀ�ؼ� �����ϰ� �Ȳ��� �κ� ������
2. �ϳ��� ��ƾ�� �ٵ��� �����ͺ��̽� �ʱ�ȭ �ϴ��� 
   (locationIP�� ep �÷��� null�� �Ǵ����� üũ�ؾ��� <-- locationIP ���̺��� �׻� üũ�ؾ���) 
3. EP ���� �ø��� ��ǻ�Ϳ� DB ���� ���� (�� ��ȭ��) üũ 
4. BrokerGiver ���̺� User�� Access �ؾ��ϴ� Port �� ����� ������ 

future work�� �ؾ��ұ�..
4. �� User���� Ư�� �ð��� ���Ǵ� ���� Ʈ���� ���� DB�� �����ǰ� �־���Ѵ�. �� �κ��� �ӽŷ����� ���ؼ� �н����ѳ��°� ������
   -> ���� ������ ����ѰͰ� �����ϰ� �ൿ�ؾ��Ѵ�. ��ü�� ���ȭ �ؼ� ���� �Ȱ��� �ൿ�ϰ� �ϸ� �ȵ�
**********************************************************************************************/

public class CBroker {

	static Logger log = Logger.getLogger(CBroker.class.getName());		//initiate logger
	static final int NUM_OF_EP = 3; 									//set number of clouds
	
	public static void main(String [] args){
		
			PropertyConfigurator.configure("log/log4j.properties");
			log.info("========================================================================================================");
			log.info("============================================= BROKER START =============================================");
			log.info("========================================================================================================\r\n");
		//	log.debug("end \r\n");
			
			ThreadPool.GetInstance().execute(new CLPCalculation());
			CNetworkServer cBroker = new CNetworkServer(); 
			cBroker.start();
	}
}
