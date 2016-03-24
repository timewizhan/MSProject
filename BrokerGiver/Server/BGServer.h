#ifndef _AY_SERVER_
#define _AY_SERVER_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpTool.h"
#include "BGServerStruct.h"

class CBGServer
{
	BOOL	m_bStartServer;
	DWORD	m_dwAcceptCount;

	ST_SERVER_INIT			m_stServerInit;
	ST_SERVER_IOCP_DATA		m_stServerIOCPData;
	ST_SERVER_WORKER_THREAD m_stServerWorkerThreads;
	ST_SERVER_STATUS		m_stServerStatus;

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
	DWORD InitBrokerThread();

	/*
		InitServerValue method have internel method
		neccesary method (bind, listen)
	*/
	DWORD InitServerValue(DWORD dwPort);
	DWORD AcceptServer(ST_CLIENT_SOCKET &refstClientSocket);
	VOID ListenToReplacement();
	DWORD CompleteReadFromClient(ST_CLIENT_SOCKET &refstClientSocket);

public:
	CBGServer();
	~CBGServer();

	DWORD StartServer(DWORD dwPort, DWORD dwBackLog);
	DWORD StopServer();
};

unsigned int WINAPI WorkerBrokerThread(void *pData);
unsigned int WINAPI WorkerCompletionThread(void *pIOCPData);

#endif