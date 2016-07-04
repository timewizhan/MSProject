#ifndef _DBPS_WORKER_THREAD_
#define _DBPS_WORKER_THREAD_

#include "..\Common\Common.h"
#include "..\DataBase\DataBase.h"
#include "DBPSServerStruct.h"
#include "DBPSWorkerStruct.h"
#include "Protocol.h"

class CDBPSWorkerThread
{
	ST_WORKER_THREAD	m_stWorkerThread;
	ST_DB_LOGIN_TOKEN	m_stDBLoginToken;


	/**
	* Send a data to client
	*
	* @param :
		string : data to send
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD SendDataToClient(std::string &refstrSendData);
	
	/**
	* Receive a data from client
	*
	* @param :
		ST_RECV_DATA : received data
		char* : received buffer
	* @return : None
	* @exception : None
	*/
	void ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf);

	/**
	* Make string from response data
	*
	* @param :
		ST_DB_RESULT : DB result
		string : data to send
	* @return : None
	* @exception : None
	*/
	void MakeSTRResData(ST_DB_RESULT &refstDBResult, std::string &refstrSendData);

	/**
	* request a result to DB using client's request
	*
	* @param :
		ST_CLIENT_REQ : client request
		ST_DB_RESULT : db result
	* @return : None
	* @exception : None
	*/
	void RequestDataBase(ST_RECV_DATA &refstRecvData, ST_DB_RESULT &refstDBResult);
public:
	CDBPSWorkerThread(SOCKET ClientSocket);
	~CDBPSWorkerThread();

	/**
	* start worker thread
	*
	* @param :
		char* : received buffer
		DWORD : Transferred bytes
	* @return :
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred);
};

#endif