#ifndef _M_MM_THREAD_
#define _M_MM_THREAD_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpClient.h"

#include "ProtocolStruct.h"
#include "MServerStruct.h"

class CMMThread
{
	ST_CLIENT_CONTEXT	m_stClientContext;
	CHelpClient			*m_pHelpClient;

	VOID InitServerConnection(ST_SERVER_ADDR &refstServerAddr);
	BOOL ConnectToServer();

	DWORD BuildSendMsg(std::string &refstrJsonData, DWORD dwManagerNumber, ST_PROTO_ROOT *pProtoRoot);
	DWORD ParseDataFromMM(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);
	DWORD ParseDataFromBots(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);

	DWORD SendToMMServer(std::string &refstrSendMsg);
	DWORD RecvFromMMServer(std::string &refstrRecvMsg);


	DWORD ProcessPreTask(std::string &refstrSendData, DWORD dwManagerNumber);
	DWORD ProcessInnerTask(std::string &refstrSendData, std::string &refstrRecvData, ST_THREADS_PARAM &refstThreadsParam);
	DWORD ProcessPostTask(std::string &refstrRecvData, DWORD dwManagerNumber);

	VOID ProcessCycleTask(ST_THREADS_PARAM &refstThreadsParam);
public:
	CMMThread();
	~CMMThread();

	DWORD StartThread(ST_THREADS_PARAM *pstThreadsParam);
};


#endif