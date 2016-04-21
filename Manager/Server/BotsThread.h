#ifndef _M_BOTS_THREAD_
#define _M_BOTS_THREAD_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpServer.h"

#include "ProtocolStruct.h"
#include "MServerStruct.h"

typedef std::vector<ST_CLIENT_SOCKET> VecConnection;

class CBotsThread
{
	ST_SERVER_CONTEXT	m_stServerContext;
	VecConnection		m_vecConnection;
	CHelpServer			*m_pHelpServer;
	CHelpTool			*m_pHelpTool;

	VOID InitBotsServer(DWORD dwPort);
	VOID PushDataToQueue(ST_PROTO_ROOT *pProtoRoot);
	VOID ClearConnection();
	VOID TerminateBot();

	DWORD ParseDataFromBot(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);
	DWORD ParseDataFromMMThread(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber);

	DWORD BuildSendMsg(std::string &refstrJsonData, ST_PROTO_ROOT *pProtoRoot);
	DWORD ExecuteBotGen(ST_THREADS_PARAM &refstThreadParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber);

	DWORD SendToMMThread(std::string &refstrSendData, ST_THREADS_PARAM &refstThreadParam);
	DWORD SendToBot(std::string &refstrSendMsg, SOCKET sockManager);
	DWORD RecvFromMMThread(std::string &refstrRecvData, ST_THREADS_PARAM &refstThreadParam);
	DWORD RecvDataFromBot(SOCKET ClientSock, std::string &refstrRecvData);
	DWORD BroadCastMsgToBot(std::string &refstrSendData);

	DWORD BranchByAction(ST_THREADS_PARAM &refstThreadParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber);

	DWORD ProcessPreTask(ST_THREADS_PARAM &refstThreadParam, DWORD &refdwBotNumber, DWORD &refdwAction);
	DWORD ProcessCommunicationTask(DWORD &refdwBotNumber);
	DWORD ProcessInterSectionTask(SOCKET ClientSock);
	DWORD ProcessPostTask(ST_THREADS_PARAM &refstThreadParam, DWORD &refdwAction);

	VOID ProcessCycleTask(ST_THREADS_PARAM &refstThreadParam);
public:
	CBotsThread();

	DWORD StartThread(ST_THREADS_PARAM *pstThreadsParam);
};


#endif