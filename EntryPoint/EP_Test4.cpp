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

	CSocket cBrokerSocket;
	cBrokerSocket.init_socket();

	//SNS server�� ����ϴ� �κ�
	printf("[Init Socket with SNS Server] \n");
	CSocket cSNSServerSocket1;
	cSNSServerSocket1.InitSocketWithSNSServer();

	//1. ����� ��� �����϶�� �޼��� ������ string : {"TYPE" : "5"}
	printf("[Sending STORE Command Signal to SNS Server] \n");
	cSNSServerSocket1.SendStoreCmdMessage();

	//2. ����Ϸ��ߴٰ� �޼��� �ޱ�
	printf("[Receving STORE Complete Signal from SNS Server] \n");
	cSNSServerSocket1.RecvStoreCompleteMessage();
	
	cSNSServerSocket1.CloseSNSServerSocket();

	//3. ����͸� ��� �̾Ƽ� Broker���� ����
	printf("[Sending Monitoring Data to Broker] \n");
	m_db.extractData(cBrokerSocket);

	//4. ��Ī ��� �޾Ƽ� EP�� �ִ� DB�� ����
	printf("[Receving and Storing Matching Data] \n");
	m_db.StoreData(cBrokerSocket);

	//5. ��Ī ��� ���� �Ϸ� �޼��� ���� 
	printf("[Sending Storing Matching Result Complete Signal to SNS Server] \n");
	CSocket cSNSServerSocket2;
	cSNSServerSocket2.InitSocketWithSNSServer();
	cSNSServerSocket2.SendMatchStoreCompleteMessage();

	//6. SNS������ ���� Data Replacement �Ϸ� �޼��� �ޱ�
	printf("[Receving Data Replacement Complete Signal from SNS Server] \n");
	cSNSServerSocket2.RecvDRCompleteMessage();
	cSNSServerSocket2.CloseSNSServerSocket();

	//8. Broker���� DR �Ϸ� �޼��� ������
	//	printf("[Sending Data Replacement Complete Signal to Broker] \n");
	//	cBrokerSocket.SendDRCompleteMessage();

	//9. 1�ð����� EP�� ���ÿ� ���ư��� �ϱ� ���� ����ȭ �޼��� �ٽ� �ޱ�
	//	printf("[Receving Sync Signal from Broker] \n");
	//	cBrokerSocket.RecvSyncMessage();

	//10. server_side_monitor, client_side_monitor, match_result_table �����
	m_db.DeleteTables();

	printf("[Close Databases] \n");
	m_db.CloseDB();

	//7. ���� �ݱ� 
	printf("[Close Sockets] \n");
//	cSNSServerSocket.CloseSNSServerSocket();
	cBrokerSocket.CloseBrokerSocket();
}

