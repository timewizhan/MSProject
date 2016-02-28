/*
 * broker_server.cpp
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#include "data_queue.h"
#include "common.h"
#include "broker_server.h"


CBrokerServer::CBrokerServer(){

	init_broker();
}

CBrokerServer::~CBrokerServer(){}

void CBrokerServer::init_broker(){

	printf("init_broker \n");

	init_thread();
	bridge_socket();
}

void CBrokerServer::init_thread(){

	printf("init_thread \n");

	HANDLE hThread = (HANDLE)_beginthreadex(NULL, 0, preprocess_insert, &m_db, CREATE_SUSPENDED, NULL);
	if (!hThread)
	{
		printf(" Error Thread \r\n");
		return;
	}

//	pthread_t thread_for_queue;
//	int thr_id = pthread_create(&thread_for_queue, NULL, preprocess_insert, &m_db);
}

void CBrokerServer::bridge_socket(){

	m_db.m_socket.comm_socket();
}

unsigned WINAPI preprocess_insert(void *data){

	printf("preprocess_insert \n");
	CDatabase *l_db = (CDatabase *)data;

	int l_ny_traffic = 0;
	int l_bs_traffic = 0;
	int l_chi_traffic = 0;

	while (1){

		if (!CDataQueue::getDataQueue()->getQueue().empty()){															//queue�� ������� ������

			monitoring_result poppedData = CDataQueue::getDataQueue()->popDataFromQueue();

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

		::Sleep(1);
	}


	//���⼭ client-side traffic �� �־��ֱ�
	printf("Total traffic test: NY TT = %d, BS TT = %d, CHI TT = %d", l_ny_traffic, l_bs_traffic, l_chi_traffic);
	l_db->updateLocation(l_ny_traffic, l_bs_traffic, l_chi_traffic);


	//	���⼭ LP �˰����� ȣ���ؾ��ҵ�.


	return 0;
}


