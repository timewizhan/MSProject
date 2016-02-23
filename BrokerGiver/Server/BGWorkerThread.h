#ifndef _AY_WORKER_THREAD_
#define _AY_WORKER_THREAD_

#include "..\Common\Common.h"
#include "..\DataBase\DataBase.h"
#include "BGServerStruct.h"
#include "BGWorkerStruct.h"
#include "Protocol.h"

class CBGWorkerThread
{
	ST_WORKER_THREAD	m_stWorkerThread;
	ST_DB_LOGIN_TOKEN	m_stDBLoginToken;

	DWORD SendDataToClient(std::string &refstrSendData);
	DWORD GetDataFromDB();
	
	void ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf);
	void ParseReqData(ST_RECV_DATA &refstRecvData, ST_CLIENT_REQ &refstReqClient);
	void ExtractResData(ST_DB_RESULT &refstDBResult, ST_CLIENT_RES &refstResClient);
	void MakeJsonResData(ST_CLIENT_RES &refstResClient, std::string &refstrSendData);
	void RequestDataBase(ST_CLIENT_REQ &refstReqClient, ST_DB_RESULT &refstDBResult);
public:
	CBGWorkerThread(SOCKET ClientSocket);
	~CBGWorkerThread();

	DWORD StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred);
};

#endif