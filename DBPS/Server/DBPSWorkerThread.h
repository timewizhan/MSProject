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

	DWORD SendDataToClient(std::string &refstrSendData);
	
	void ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf);
	void MakeJsonResData(ST_DB_RESULT &refstDBResult, std::string &refstrSendData);
	void RequestDataBase(ST_RECV_DATA &refstRecvData, ST_DB_RESULT &refstDBResult);
public:
	CDBPSWorkerThread(SOCKET ClientSocket);
	~CDBPSWorkerThread();

	DWORD StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred);
};

#endif