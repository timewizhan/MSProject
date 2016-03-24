#ifndef _DBPS_SERVER_
#define _DBPS_SERVER_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpTool.h"
#include "..\DataBase\DataBase.h"
#include "DBPSServerStruct.h"

class CDBPSServer
{
	BOOL	m_bStartServer;
	DWORD	m_dwAcceptCount;

	ST_SERVER_INIT			m_stServerInit;
	ST_SERVER_IOCP_DATA		m_stServerIOCPData;
	ST_SERVER_WORKER_THREAD m_stServerWorkerThreads;
	ST_SERVER_STATUS		m_stServerStatus;
	ST_DB_LOGIN_TOKEN		m_stDBLoginToken;

	CRITICAL_SECTION		m_CriticalSection;
	std::vector<ST_SERVER_CONNECTION>	m_vecstServerConnection;

	/*
		HelpTool is about system method
		ex) Getsystem, GetAddress etc..
	*/
	CHelpTool	*m_pHelpTool;

	DWORD InitServerSock(DWORD dwPort);
	DWORD InitIOCompletionPort(DWORD dwNumberOfConcurrentThreads);
	DWORD InitWorkerThread();
	DWORD InitDBCQueue(DWORD dwNumberOfConnection);
	DWORD DestoryDBCQueue(DWORD dwNumberOfConnection);

	/*
		InitServerValue method have internel method
		neccesary method (bind, listen)
	*/
	DWORD InitServerValue(DWORD dwPort);
	DWORD AcceptServer(ST_CLIENT_SOCKET &refstClientSocket);
	DWORD CompleteReadFromClient(ST_CLIENT_SOCKET &refstClientSocket);

public:
	CDBPSServer();
	~CDBPSServer();

	DWORD StartServer(DWORD dwPort, DWORD dwNumberOfConnection);
	DWORD StopServer();
};

unsigned int WINAPI WorkerCompletionThread(void *pIOCPData);

#endif