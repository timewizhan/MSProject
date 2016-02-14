#ifndef _AY_WORKER_THREAD_
#define _AY_WORKER_THREAD_

#include "..\Common\Common.h"
#include "BGServerStruct.h"
#include "BGWorkerStruct.h"

class CBGWorkerThread
{
	ST_WORKER_THREAD	m_stWorkerThread;

	DWORD SendDataToClient(std::string &refstrSendData);
	void ReceiveDataFromClient(ST_RECV_DATA &refstRecvData, char *pReceiveBuf);
	void ParseReceivedData(ST_RECV_DATA &refstRecvData, ST_REQ_CLIENT &refstReqClient);
public:
	CBGWorkerThread(SOCKET ClientSocket);
	~CBGWorkerThread();

	DWORD StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred);
};

#endif