#ifndef _AY_SERVER_
#define _AY_SERVER_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpTool.h"
#include "..\DataBase\DataBase.h"
#include "BGServerStruct.h"

class CBGServer
{
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

	/**
	* Initialize Socket
	*
	* @param : 
		DWORD : Port
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitServerSock(DWORD dwPort);

	/**
	* Initialize I/O Completion Port
	*
	* @param : 
		DWORD : Thread number
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitIOCompletionPort(DWORD dwNumberOfConcurrentThreads);

	/**
	* Initialize Worker Thread
	*
	* @param : None
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitWorkerThread();

	/**
	* Initialize Broker Thread
	*
	* @param : None
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitBrokerThread();

	/**
	* Initialize DB Connection Queue
	*
	* @param : 
		DOWRD : Connection Number
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitDBCQueue(DWORD dwNumberOfConnection);

	/**
	* Initialize Server
	*
	* @param : 
		DWORD : Port
		DWORD : DB Queue
	* @return : None
	* @exception : None
	*/
	VOID InitializeServer(DWORD dwPort, DWORD dwDBQueue);

	/*
		InitServerValue method have internel method
		neccesary method (bind, listen)
	*/

	/**
	* Initialize Server Value
	*
	* @param : 
		DWORD : Port
	* @return : None
	* @exception : None
	*/
	DWORD InitServerValue(DWORD dwPort);

	/**
	* Accept Socket
	*
	* @param : 
		ST_CLIENT_SOCKET : Client Socket
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD AcceptServer(ST_CLIENT_SOCKET &refstClientSocket);

	/**
	* Server wait for a command to replace from other server
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	VOID ListenToReplacement();

	/**
	* Complete to read data from client
	*
	* @param : 
		ST_CLIENT_SOCKET : Client Socket
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD CompleteReadFromClient(ST_CLIENT_SOCKET &refstClientSocket);

public:
	CBGServer();
	~CBGServer();

	/**
	* Start Server
	*
	* @param : 
		DWORD : Port
		DWORD : BackLog
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StartServer(DWORD dwPort, DWORD dwBackLog);
};

/**
* Main fuction of Broker thread
*
* @param : 
	VOID* : Broker Thread Data
* @return : 
	DWORD : E_RET_TYPE
* @exception : None
*/
unsigned int WINAPI WorkerBrokerThread(void *pData);

/**
* Main function of completion thread
*
* @param : 
	VOID* : IOCP Data
* @return : 
	DWORD : E_RET_TYPE
* @exception : None
*/
unsigned int WINAPI WorkerCompletionThread(void *pIOCPData);

#endif