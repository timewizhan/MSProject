#include "Broker.h"

CBroker::CBroker(){

	InitBroker();
}

void CBroker::InitBroker(){

//	printf("init_broker \n");

	while (1){
		InitThread();
		BridgeSocket(hThread);
	}
}

void CBroker::InitThread(){

//	printf("init_thread \n");

	hThread = (HANDLE)_beginthreadex(NULL, 0, PreprocessInsert, &m_cDatabase, NULL, NULL);
	if (!hThread)
	{
		printf(" Error Thread \r\n");
		return;
	}
}

void CBroker::BridgeSocket(HANDLE hThread){

	m_cDatabase.m_cSocket.CommSocket(hThread);
}

unsigned WINAPI PreprocessInsert(void *data){

//	printf("preprocess_insert \n");
	printf("[Thread Start] \n");
	CDatabase *l_db = (CDatabase *)data;

	int cnt = 0;
	int iECount = 0;
	ST_CCT stCCT;
	while (1){
	//	Sleep(1000);
	//	printf(".");
	//	if (cnt % 40 == 0){
	//		printf("\n");
	//	}

		if (!CDataQueue::getDataQueue()->getQueue().empty()){	//queue�� ������� ������
		
			ST_MONITORING_RESULT poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n - Preprocess of inserting data \n");
			printf("  - User: %s \n", poppedData.user);
		//	printf("cpu_util: %d, server-side traffic: %d \n", poppedData.cpu_util, poppedData.server_side_traffic);
		//	printf("user: %s, location: %s, timestamp: %d, user traffic: %d \n", poppedData.user, poppedData.location
		//		, poppedData.timestamp, poppedData.traffic);

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//�������� Ʈ���� ��ġ��
			if (!strcmp(poppedData.side_flag, "c")){

				if (!strcmp(poppedData.location, "NY")){

					stCCT.iNyTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);	//���� �κ� ���� �� �־��ֱ�

				}
				else if (!strcmp(poppedData.location, "BS")){

					stCCT.iBsTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);

				}
				else if (!strcmp(poppedData.location, "CHI")){

					stCCT.iChiTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "TEX")){

					stCCT.iTexTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "WA")){

					stCCT.iWhaTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "WHA")){

					stCCT.iWhaTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}




				else if (!strcmp(poppedData.location, "WASHINGTON")){

					stCCT.WASHINGTON_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MONTANA")){

					stCCT.MONTANA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NORTHDAKOTA")){

					stCCT.NORTHDAKOTA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "OREGON")){

					stCCT.OREGON_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "IDAHO")){

					stCCT.IDAHO_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "WYOMING")){

					stCCT.WYOMING_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "SOUTHDAKOTA")){

					stCCT.SOUTHDAKOTA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEBRASKA")){

					stCCT.NEBRASKA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "ALASKA")){

					stCCT.ALASKA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "CALIFORNIA")){

					stCCT.CALIFORNIA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEVADA")){

					stCCT.NEVADA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "UTAH")){

					stCCT.UTAH_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "COLORADO")){

					stCCT.COLORADO_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "KANSAS")){

					stCCT.KANSAS_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MISSOURI")){

					stCCT.MISSOURI_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "ARIZONA")){

					stCCT.ARIZONA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEWMEXICO")){

					stCCT.NEWMEXICO_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "TEXAS")){

					stCCT.TEXAS_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "OKLAHOMA")){

					stCCT.OKLAHOMA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "ARKANSAS")){

					stCCT.ARKANSAS_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "LOUISIANA")){

					stCCT.LOUISIANA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "HAWAII")){

					stCCT.HAWAII_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MINNESOTA")){

					stCCT.MINNESOTA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "WISCONSIN")){

					stCCT.WISCONSIN_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MICHIGAN")){

					stCCT.MICHIGAN_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "IOWA")){

					stCCT.IOWA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "ILLINOIS")){

					stCCT.ILLINOIS_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "INDIANA")){

					stCCT.INDIANA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "OHIO")){

					stCCT.OHIO_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "PENNSYLVANIA")){

					stCCT.PENNSYLVANIA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEWYORK")){

					stCCT.NEWYORK_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "VERMONT")){

					stCCT.VERMONT_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEWHAMPSHIRE")){

					stCCT.NEWHAMPSHIRE_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MAINE")){

					stCCT.MAINE_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MASSACHUSETTS")){

					stCCT.MASSACHUSETTS_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "RHODE")){

					stCCT.RHODE_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "CONNECTICUT")){

					stCCT.CONNECTICUT_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NEWJERSY")){

					stCCT.NEWJERSY_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "DELAWARE")){

					stCCT.DELAWARE_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MARYLAND")){

					stCCT.MARYLAND_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "KENTUCKY")){

					stCCT.KENTUCKY_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "WESTVIRGINIA")){

					stCCT.WESTVIRGINIA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "VIRGINIA")){

					stCCT.VIRGINIA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "TENNESSEE")){

					stCCT.TENNESSEE_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "NORTHCAROLINA")){

					stCCT.NORTHCAROLINA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "MISSISSIPPI")){

					stCCT.MISSISSIPPI_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "ALABAMA")){

					stCCT.ALABAMA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "GEORGIA")){

					stCCT.GEORGIA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "SOUTHCAROLINA")){

					stCCT.SOUTHCAROLINA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "FLORIDA")){

					stCCT.FLORIDA_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}
				else if (!strcmp(poppedData.location, "GUAM")){

					stCCT.GUAM_TRAFFIC += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);
				}











			}
			else if (!strcmp(poppedData.side_flag, "s")){

				l_db->InsertServerTable(poppedData.ep_num, poppedData.server_side_traffic, poppedData.cpu_util);		//Ŭ���̾�Ʈ �κ� ���� �� �־��ֱ�

			}
			else if (!strcmp(poppedData.side_flag, "e")){

				iECount++;

				if (iECount == 3){
					iECount = 0;
					break;
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		}
	}


	//���⼭ client-side traffic �� �־��ֱ�
	printf(" - Total Traffic \n");
	l_db->updateLocation(stCCT);

	return 0;
}


