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

	//	m_sock.init_socket();		//socket
	m_db.initDB();				//database

	
	//SNS server�� ����ϴ� �κ�
	CSocket cSNSServerSocket;
	cSNSServerSocket.InitSocketWithSNSServer();

	//1. ����� ��� �����϶�� �޼��� ������ string : {"TYPE" : "5"}
	cSNSServerSocket.SendStoreCmdMessage();

	//2. ����Ϸ��ߴٰ� �޼��� �ޱ�
	cSNSServerSocket.RecvStoreCompleteMessage();

	//3. ����͸� ��� �̾Ƽ� Broker���� ����
	m_db.extractData();

	//4. ��Ī ��� �޾Ƽ� EP�� �ִ� DB�� ����
	m_db.StoreData();
	
	//5. ��Ī ��� ���� �Ϸ� �޼��� ���� 
	cSNSServerSocket.SendMatchStoreCompleteMessage();
	
	//6. SNS������ ���� Data Replacement �Ϸ� �޼��� �ޱ�
	cSNSServerSocket.RecvDRCompleteMessage();

	//7. ���� �ݱ� 
//	cSNSServerSocket.CloseSNSServerSocket();

	//8. Broker���� DR �Ϸ� �޼��� ������
	CSocket cBrokerSocket;
	cBrokerSocket.SendDRCompleteMessage();

	//9. 1�ð����� EP�� ���ÿ� ���ư��� �ϱ� ���� ����ȭ �޼��� �ٽ� �ޱ�
	cBrokerSocket.RecvSyncMessage();

	m_db.CloseDB();

	//7. ���� �ݱ� 
	cSNSServerSocket.CloseSNSServerSocket();
	cBrokerSocket.CloseBrokerSocket();
}

