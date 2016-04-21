#ifndef _M_HEALTH_THREAD_
#define _M_HEALTH_THREAD_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpClient.h"

#include "ProtocolStruct.h"
#include "MServerStruct.h"

class CHealthThread
{
	ST_CLIENT_CONTEXT	m_stClientContext;
	CHelpClient			*m_pHelpClient;

	VOID InitServerConnection(ST_SERVER_ADDR &refstServerAddr);
	VOID CloseConnection();
	BOOL ConnectToServer();

	DWORD BuildSendMsg(std::string &refstrJsonData, DWORD dwManagerNumber);
	
	DWORD SendToMMServer(std::string &refstrSendMsg);

	DWORD ProcessPreTask(std::string &refstrSendData, DWORD dwManagerNumber);
	DWORD ProcessPostTask(std::string &refstrRecvData);

	VOID ProcessCycleTask(DWORD dwManagerNumber);
public:
	CHealthThread();
	~CHealthThread();

	DWORD StartThread(ST_THREADS_PARAM *pstThreadsParam);
};


#endif