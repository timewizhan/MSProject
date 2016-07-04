#ifndef _AY_WORKER_THREAD_
#define _AY_WORKER_THREAD_

#include "..\Common\Common.h"
#include "..\Common\BGLog.h"
#include "..\DataBase\DataBase.h"
#include "BGServerStruct.h"
#include "BGWorkerStruct.h"
#include "DBQueue.h"
#include "Protocol.h"

class CBGWorkerThread
{
	ST_WORKER_THREAD	m_stWorkerThread;

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
	* Return data from DB
	*
	* @param : None
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD GetDataFromDB();
	
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
	* Parse a data of request
	*
	* @param : 
		ST_RECV_DATA : received data
		ST_CLIENT_REQ : client request
	* @return : None
	* @exception : None
	*/
	void ParseReqData(ST_RECV_DATA &refstRecvData, ST_CLIENT_REQ &refstReqClient);

	/**
	* Extract a data from raw data (which is a data from db)
	*
	* @param : 
		ST_DB_RESULT : DB result
		ST_CLIENT_RES : client response data 
	* @return : None
	* @exception : None
	*/
	void ExtractResData(ST_DB_RESULT &refstDBResult, ST_CLIENT_RES &refstResClient);

	/**
	* make json string from a data of response
	*
	* @param : 
		ST_CLIENT_RES : client response data
		string :  data to send
	* @return : None
	* @exception : None
	*/
	void MakeJsonResData(ST_CLIENT_RES &refstResClient, std::string &refstrSendData);
	
	/**
	* request a result to DB using client's request
	*
	* @param : 
		ST_CLIENT_REQ : client request
		ST_DB_RESULT : db result
	* @return : None
	* @exception : None
	*/
	void RequestDataBase(ST_CLIENT_REQ &refstReqClient, ST_DB_RESULT &refstDBResult);
public:
	CBGWorkerThread();
	~CBGWorkerThread();

	/**
	* Configure client socket
	*
	* @param : 
		SOCKET : client socket
	* @return : None
	* @exception : None
	*/
	VOID SetClientSocket(SOCKET ClientSocket);

	/**
	* start worker thread
	*
	* @param : 
		char* : received buffer
		CBGLog* : log instance
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StartWorkerThread(char *pReceiveBuf, CBGLog *pBGLog);
};

#endif