#ifndef _M_HEALTH_THREAD_
#define _M_HEALTH_THREAD_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpServer.h"

#include "Protocol.h"
#include "ProtocolStruct.h"
#include "MMServerStruct.h"
#include "ConnectionStruct.h"

class CHealthThread
{
	ST_SERVER_CONTEXT	m_stServerContext;
	VecConnection		m_vecConnection;
	CHelpServer			*m_pHelpServer;

	VOID InitHealthServer(DWORD dwPort);
	VOID CheckKeepAlive();
	VOID ChangeConnectionState(SOCKET ClientSock, ST_PROTO_ROOT *pProtoRoot);

	DWORD RecvDataFromManager(SOCKET ClientSock, std::string &refstrRecvData);
	DWORD ParseDataFromManager(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);
	
	DWORD ProcessInterSectionTask(SOCKET ClientSock);
	DWORD ProcessCommunicationTask();

public:
	CHealthThread();
	~CHealthThread();

	DWORD StartThread(ST_THREADS_PARAM *pstThreadsParam);
};


#endif