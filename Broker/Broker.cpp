#include "Broker.h"

CBroker::CBroker(){

	InitBroker();
}

void CBroker::InitBroker(){

	printf("init_broker \n");

	InitThread();
	BridgeSocket();
}

void CBroker::InitThread(){

	printf("init_thread \n");

	hThread = (HANDLE)_beginthreadex(NULL, 0, PreprocessInsert, &m_cDatabase, NULL, NULL);
	if (!hThread)
	{
		printf(" Error Thread \r\n");
		return;
	}
}

void CBroker::BridgeSocket(){

	m_cDatabase.m_cSocket.CommSocket();
}

unsigned WINAPI PreprocessInsert(void *data){

	printf("preprocess_insert \n");
	CDatabase *l_db = (CDatabase *)data;

	int l_ny_traffic = 0;
	int l_bs_traffic = 0;
	int l_chi_traffic = 0;

	int cnt = 0;
	while (1){
		cnt++;
		if (cnt % 31 == 0){ printf("\n"); }
		printf(".");

		if (!CDataQueue::getDataQueue()->getQueue().empty()){															//queue�� ������� ������
			//�̺κп� ť�� ����ִ°ɷ� üũ�ϸ� �ȵǰ�,
			//EP�� ���� �����͸� �� �޾Ҵٴ� �޼����� ���� �޾ƾ���

			ST_MONITORING_RESULT poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n[read test in preprocess of inserting data] \n");
			printf("EP: %d, Side: %s \n", poppedData.ep_num, poppedData.side_flag);
			printf("cpu_util: %d, server-side traffic: %d \n", poppedData.cpu_util, poppedData.server_side_traffic);
			printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", poppedData.user, poppedData.location
				, poppedData.timestamp, poppedData.traffic);

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�������� Ʈ���� ��ġ��
			if (!strcmp(poppedData.side_flag, "c")){

				if (!strcmp(poppedData.location, "NY")){

					l_ny_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
						, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);	//���� �κ� ���� �� �־��ֱ�

				}
				else if (!strcmp(poppedData.location, "BS")){

					l_bs_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
						, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);

				}
				else if (!strcmp(poppedData.location, "CHI")){

					l_chi_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
						, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);
				}

			}
			else if (!strcmp(poppedData.side_flag, "s")){

				l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
					, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);		//Ŭ���̾�Ʈ �κ� ���� �� �־��ֱ�

			}
			else if (!strcmp(poppedData.side_flag, "e")){

				break;
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}

		::Sleep(1000);
	}


	//���⼭ client-side traffic �� �־��ֱ�
	printf("Total traffic test: NY TT = %d, BS TT = %d, CHI TT = %d", l_ny_traffic, l_bs_traffic, l_chi_traffic);
	l_db->updateLocation(l_ny_traffic, l_bs_traffic, l_chi_traffic);


	// ������ ���� ����ȭ �ϴ� �Լ� ȣ��

	//	���⼭ LP �˰����� ȣ���ؾ��ҵ�.

	return 0;
}


