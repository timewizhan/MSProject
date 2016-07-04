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
	* Initialize DB Connection Queue
	*
	* @param :
		DWORD : Connection Number
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD InitDBCQueue(DWORD dwNumberOfConnection);

	/**
	* Destory DB Connection Queue
	*
	* @param :
		DWORD : Connection Number
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD DestoryDBCQueue(DWORD dwNumberOfConnection);

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
	CDBPSServer();
	~CDBPSServer();

	/**
	* Start Server
	*
	* @param :
		DWORD : Port
		DWORD : the number of connection
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StartServer(DWORD dwPort, DWORD dwNumberOfConnection);

	/**
	* Stop Server
	*
	* @param : None
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StopServer();
};

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