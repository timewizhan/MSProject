#include "Broker.h"

CBroker::CBroker(){

	CDataQueue::getDataQueue();
	InitBroker();

}

void CBroker::InitBroker(){

//	CFileWrite cFileWrite;
	ofstream insDRResFile("Data_Replacement_Result.txt");
	ofstream insWeightResFile("Weight_Result.txt");
	
//	cFileWrite.FileOpen();
	while (1){
		printf("[Start While Statement] \n");
		if (!CDataQueue::getDataQueue()->getQueue().empty())
		{
			printf("Warning! DataQueue is not empty. \n");
			break;
		}

		m_cDatabase.InitDB();

		InitThread();
		BridgeSocket(hThread, insDRResFile, insWeightResFile);
		
		m_cDatabase.CloseDB();
	}
}

void CBroker::InitThread(){

	hThread = (HANDLE)_beginthreadex(NULL, 0, PreprocessInsert, &m_cDatabase, NULL, NULL);
	if (!hThread)
	{
		printf(" Error Thread \r\n");
		return;
	}
}

void CBroker::BridgeSocket(HANDLE hThread, ofstream &insDRResFile, ofstream &insWeightResFile){

	m_cDatabase.m_cSocket.CommSocket(hThread, insDRResFile, insWeightResFile);
}

unsigned WINAPI PreprocessInsert(void *data){

	printf("[Thread Start] \n");
	CDatabase *l_db = (CDatabase *)data;

	int cnt = 0;
	int iECount = 0;
	ST_CCT stCCT;
	while (1){
	
//		if (!CDataQueue::getDataQueue()->getQueue().empty()){	//queue가 비어있지 않으면
		if (!CDataQueue::getDataQueue()->isEmpty()){	//queue가 비어있지 않으면
		
			ST_MONITORING_RESULT poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

			printf("\n - Preprocess of inserting data \n");
			printf("  - User: %s, Flag: %s \n", poppedData.user, poppedData.side_flag);
	
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//지역별로 트래픽 합치기
			if (!strcmp(poppedData.side_flag, "c")){

				if (!strcmp(poppedData.location, "NY")){

					stCCT.iNyTraffic += poppedData.traffic;
					l_db->InsertClientTable(poppedData.user, poppedData.location, poppedData.timestamp, poppedData.traffic);	//서버 부분 빼고 다 넣어주기

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

				l_db->InsertServerTable(poppedData.ep_num, poppedData.server_side_traffic, poppedData.cpu_util);		//클라이언트 부분 빼고 다 넣어주기

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


	//여기서 client-side traffic 값 넣어주기
	printf(" - Total Traffic \n");
	l_db->updateLocation(stCCT);

	return 0;
}


