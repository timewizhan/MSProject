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

		if (!CDataQueue::getDataQueue()->getQueue().empty()){															//queue가 비어있지 않으면
			//이부분에 큐가 비어있는걸로 체크하면 안되고,
			//EP로 부터 데이터를 다 받았다는 메세지를 따로 받아야함

			ST_MONITORING_RESULT poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n[read test in preprocess of inserting data] \n");
			printf("EP: %d, Side: %s \n", poppedData.ep_num, poppedData.side_flag);
			printf("cpu_util: %d, server-side traffic: %d \n", poppedData.cpu_util, poppedData.server_side_traffic);
			printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", poppedData.user, poppedData.location
				, poppedData.timestamp, poppedData.traffic);

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//지역별로 트래픽 합치기
			if (!strcmp(poppedData.side_flag, "c")){

				if (!strcmp(poppedData.location, "NY")){

					l_ny_traffic += poppedData.traffic;
					l_db->insertData(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic
						, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);	//서버 부분 빼고 다 넣어주기

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
					, poppedData.server_side_traffic, poppedData.cpu_util, poppedData.ep_num, poppedData.side_flag);		//클라이언트 부분 빼고 다 넣어주기

			}
			else if (!strcmp(poppedData.side_flag, "e")){

				break;
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}

		::Sleep(1000);
	}


	//여기서 client-side traffic 값 넣어주기
	printf("Total traffic test: NY TT = %d, BS TT = %d, CHI TT = %d", l_ny_traffic, l_bs_traffic, l_chi_traffic);
	l_db->updateLocation(l_ny_traffic, l_bs_traffic, l_chi_traffic);


	// 데이터 값들 정규화 하는 함수 호출

	//	여기서 LP 알고리즘을 호출해야할듯.

	return 0;
}


