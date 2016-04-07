#ifndef _M_BOTS_THREAD_
#define _M_BOTS_THREAD_

#include "..\Common\Common.h"
#include "..\HelpTool\HelpServer.h"

#include "ProtocolStruct.h"
#include "MMServerStruct.h"
#include "ConnectionStruct.h"
#include "..\Queue\SharedCommand.h"

class CManagerThread
{
	ST_SERVER_CONTEXT	m_stServerContext;
	VecConnection		m_vecConnection;
	CHelpServer			*m_pHelpServer;

	VOID InitManagerServer(DWORD dwPort);
	
	VOID ParseControlCommand(ControlCommand &refControlCommand, ST_PROTO_ROOT *pProtoRoot);
	DWORD ParseDataFromManager(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);
	DWORD ParseDataFromMMThread(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot);
	DWORD BuildSendMsg(std::string &refstrJsonData, ST_PROTO_ROOT *pProtoRoot);
	
	VOID ChangeConnectionState(SOCKET ClientSock, ST_PROTO_ROOT *pProtoRoot);
	VOID CheckControlCommand(ControlCommand &refControlCommand);
	VOID CheckAndOperateControlCommand();

	DWORD CheckAllReadyState();
	DWORD CheckAndOperateCompleteReady();

	DWORD SendToManager(std::string &refstrSendMsg, SOCKET sockManager);
	DWORD RecvDataFromManager(SOCKET ClientSock, std::string &refstrRecvData);
	DWORD BroadCastMsgToManager(std::string &refstrSendData);

	DWORD ProcessCommunicationTask(DWORD dwCountOfManager);
	DWORD ProcessInterSectionTask(SOCKET ClientSock);
public:
	CManagerThread();
	~CManagerThread();

	DWORD StartThread(ST_THREADS_PARAM *pstThreadsParam);
};


#endif