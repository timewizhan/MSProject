#ifndef _AY_WORKER_THREAD_
#define _AY_WORKER_THREAD_

#include "..\Common\Common.h"
#include "BGServerStruct.h"

class CBGWorkerThread
{
	ST_WORKER_THREAD	m_stWorkerThread;

	DWORD SendDataToClient(std::string &refstrSendData);
public:
	CBGWorkerThread(SOCKET ClientSocket);
	~CBGWorkerThread();

	DWORD StartWorkerThread(char *pReceiveBuf, DWORD dwByteTransferred);
};

#endif