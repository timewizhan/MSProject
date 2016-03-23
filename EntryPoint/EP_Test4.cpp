/*
* EP_Test4.cpp
*
*  Created on: Jan 23, 2016
*      Author: alphahacker
*/

#include "Common.h"
#include "database.h"
#include "EP_Test4.h"
#include "mysql.h"
//#include "socket.h"

EP_Test4::EP_Test4(){

	initEntryPoint();
}

EP_Test4::~EP_Test4(){}

void EP_Test4::initEntryPoint()
{
	printf("[Init Database] \n");
	//	m_sock.init_socket();		//socket
	m_db.initDB();				//database

	
	//SNS server와 통신하는 부분
	printf("[Init Socket with SNS Server] \n");
	CSocket cSNSServerSocket;
	cSNSServerSocket.InitSocketWithSNSServer();

	//1. 모니터 결과 저장하라고 메세지 보내기 string : {"TYPE" : "5"}
	printf("[Sending STORE Command Signal to SNS Server] \n");
	cSNSServerSocket.SendStoreCmdMessage();

	//2. 저장완료했다고 메세지 받기
	printf("[Receving STORE Complete Signal from SNS Server] \n");
	cSNSServerSocket.RecvStoreCompleteMessage();

	//3. 모니터링 결과 뽑아서 Broker에게 전송
	printf("[Sending Monitoring Data to Broker] \n");
	m_db.extractData();

	//4. 매칭 결과 받아서 EP에 있는 DB에 저장
	printf("[Receving and Storing Matching Data] \n");
	m_db.StoreData();
	
	//5. 매칭 결과 저장 완료 메세지 전송 
	printf("[Sending Storing Matching Result Complete Signal to SNS Server] \n");
	cSNSServerSocket.SendMatchStoreCompleteMessage();
	
	//6. SNS서버로 부터 Data Replacement 완료 메세지 받기
	printf("[Receving Data Replacement Complete Signal from SNS Server] \n");
	cSNSServerSocket.RecvDRCompleteMessage();

	//7. 소켓 닫기 
//	cSNSServerSocket.CloseSNSServerSocket();

	//8. Broker에게 DR 완료 메세지 보내기
	printf("[Sending Data Replacement Complete Signal to Broker] \n");
	CSocket cBrokerSocket;
	cBrokerSocket.SendDRCompleteMessage();

	//9. 1시간마다 EP가 동시에 돌아가게 하기 위해 동기화 메세지 다시 받기
	printf("[Receving Sync Signal from Broker] \n");
	cBrokerSocket.RecvSyncMessage();

	printf("[Close Databases] \n");
	m_db.CloseDB();

	//7. 소켓 닫기 
	printf("[Close Sockets] \n");
	cSNSServerSocket.CloseSNSServerSocket();
	cBrokerSocket.CloseBrokerSocket();
}

