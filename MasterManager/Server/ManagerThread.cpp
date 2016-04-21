#include "ManagerThread.h"
#include "Protocol.h"
#include "MMServerError.h"
#include "json\json.h"

#include "..\Queue\SharedCommand.h"

CManagerThread::CManagerThread() :
m_pHelpServer(NULL)
{
	m_pHelpServer = new (std::nothrow) CHelpServer();
	if (!m_pHelpServer)
		ErrorLog("Fail to import HelpTool");
}

CManagerThread::~CManagerThread()
{
	if (m_pHelpServer)
		delete m_pHelpServer;

	::WSACleanup();
}

VOID CManagerThread::InitManagerServer(DWORD dwPort) throw(std::exception)
{
	DWORD dwRet;
	dwRet = m_pHelpServer->InitServerSock(m_stServerContext);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to init bot server");
	}

	dwRet = m_pHelpServer->InitServerBind(m_stServerContext, dwPort);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to init server bind");
	}
}

DWORD CManagerThread::RecvDataFromManager(SOCKET ClientSock, std::string &refstrRecvData)
{
	char szBuf[128] = { 0 };

	DWORD dwRecvCount;
	dwRecvCount = ::recv(ClientSock, szBuf, sizeof(szBuf), 0);
	if (dwRecvCount == SOCKET_ERROR) {
		DebugLog("Manager Socket Error");
		return E_RET_FAIL;
	}
	DebugLog("Recv Datat From Manager [%s]", szBuf);

	refstrRecvData = szBuf;
	return E_RET_SUCCESS;
}

DWORD CManagerThread::ParseDataFromManager(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
{
	Json::Reader jsonReader;
	Json::Value jsonRecvData;

	bool bParseRet;
	bParseRet = jsonReader.parse(refstrRecvMsg, jsonRecvData);
	if (!bParseRet) {
		return E_RET_FAIL;
	}

	DWORD dwAction;
	dwAction = jsonRecvData["action"].asInt();
	if (dwAction == E_PROTO_MM_COMPLETE_READY) {
		ST_PROTO_MM_COMPLETE_READY *pProtoCompleteReady = (ST_PROTO_MM_COMPLETE_READY *)pProtoRoot;
		pProtoCompleteReady->dwAction = dwAction;
		pProtoCompleteReady->dwManagerNumber = jsonRecvData["number"].asInt();
	}
	else if (dwAction == E_PROTO_MM_COMPLETE_START) {
		ST_PROTO_MM_COMPLETE_START *pProtoCompleteStart = (ST_PROTO_MM_COMPLETE_START *)pProtoRoot;
		pProtoCompleteStart->dwAction = dwAction;
	}
	else {
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}


VOID CManagerThread::ChangeConnectionState(SOCKET ClientSock, ST_PROTO_ROOT *pProtoRoot)
{
	DWORD dwAction = pProtoRoot->dwAction;
	for (unsigned i = 0; i < m_vecConnection.size(); i++) {
		SOCKET sNestedSock = m_vecConnection[i].stClientSocket.hClientSock;
		if (sNestedSock != ClientSock) {
			continue;
		}

		if (dwAction == E_PROTO_MM_COMPLETE_READY) {
			ST_PROTO_MM_COMPLETE_READY *pProtoCompleteReady = (ST_PROTO_MM_COMPLETE_READY *)pProtoRoot;
			if (m_vecConnection[i].dwConnectionNumber == 0) {
				m_vecConnection[i].dwConnectionNumber = pProtoCompleteReady->dwManagerNumber;
			}

			m_vecConnection[i].stConnectionState.dwState = E_CONN_COMPLETE_READY;
		}
		else if (dwAction == E_PROTO_MM_COMPLETE_START) {
			m_vecConnection[i].stConnectionState.dwState = E_CONN_COMPLETE_START;
		}

		break;
	}
}

DWORD CManagerThread::ProcessInterSectionTask(SOCKET ClientSock)
{
	std::string strRecvData;

	DWORD dwRet;
	dwRet = RecvDataFromManager(ClientSock, strRecvData);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	dwRet = ParseDataFromManager(strRecvData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Cannot parse data of Manager");
		return dwRet;
	}

	ChangeConnectionState(ClientSock, pProtoRoot);
	return E_RET_SUCCESS;
}

DWORD CManagerThread::ParseDataFromMMThread(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
{
	Json::Reader jsonReader;
	Json::Value jsonRecvData;

	bool bParseRet;
	bParseRet = jsonReader.parse(refstrRecvMsg, jsonRecvData);
	if (!bParseRet) {
		return E_RET_FAIL;
	}

	DWORD dwAction;
	dwAction = jsonRecvData["action"].asInt();
	if (dwAction == E_PROTO_INNER_COMMAND_READY) {
		ST_PROTO_INNER_COMMAND_READY *pProtoCommandReady = (ST_PROTO_INNER_COMMAND_READY *)pProtoRoot;
		pProtoCommandReady->dwAction = dwAction;
		pProtoCommandReady->dwNumberOfBots = jsonRecvData["number"].asInt();
	}
	else if (dwAction == E_PROTO_INNER_COMMAND_START) {
		ST_PROTO_INNER_COMMAND_START *pProtoCommandStart = (ST_PROTO_INNER_COMMAND_START *)pProtoRoot;
		pProtoCommandStart->dwAction = dwAction;
	}
	else if (dwAction == E_PROTO_INNER_COMMAND_STOP) {
		ST_PROTO_INNER_COMMAND_STOP *pProtoCommandStop = (ST_PROTO_INNER_COMMAND_STOP *)pProtoRoot;
		pProtoCommandStop->dwAction = dwAction;
	}
	else {
		// nothing 
		pProtoRoot->dwAction = dwAction;
	}

	return E_RET_SUCCESS;
}

VOID CManagerThread::ParseControlCommand(ControlCommand &refControlCommand, ST_PROTO_ROOT *pProtoRoot)
{
	DWORD dwTotalSize = refControlCommand.size();

	/*
		Command Msg
		Start -> start 500
		Stop -> stop
	*/

	DWORD dwPos;
	dwPos = refControlCommand.find_first_of(" ");
	if (dwPos == std::string::npos) {
		ST_PROTO_MM_COMMAND_STOP *pstProtoMMCommandStop = (ST_PROTO_MM_COMMAND_STOP *)pProtoRoot;
		pstProtoMMCommandStop->dwAction = E_PROTO_MM_COMMAND_STOP;
		return;
	}

	std::string strFirstArg = refControlCommand.substr(0, dwPos);
	std::string strSecondArg = refControlCommand.substr(dwPos, dwTotalSize - dwPos);

	ST_PROTO_MM_COMMAND_READY *pstProtoMMCommandReady = (ST_PROTO_MM_COMMAND_READY *)pProtoRoot;
	pstProtoMMCommandReady->dwAction = E_PROTO_MM_COMMAND_READY;
	pstProtoMMCommandReady->dwNumberOfBots = ::atoi(strSecondArg.c_str());
}

VOID CManagerThread::CheckControlCommand(ControlCommand &refControlCommand)
{
	GetCommand(refControlCommand);

	// ControlCommand is initialized
	ControlCommand controlCommand = "";
	SetCommand(controlCommand);
}

DWORD CManagerThread::BuildSendMsg(std::string &refstrJsonData, ST_PROTO_ROOT *pProtoRoot)
{
	Json::Value jsonRoot;

	DWORD dwAction = pProtoRoot->dwAction;
	if (dwAction == E_PROTO_MM_COMMAND_READY) {
		ST_PROTO_MM_COMMAND_READY *pstProtoMMCommandReady = (ST_PROTO_MM_COMMAND_READY *)pProtoRoot;

		jsonRoot["action"] = static_cast<int>(dwAction);
		jsonRoot["number"] = static_cast<int>(pstProtoMMCommandReady->dwNumberOfBots);
	}
	else if (dwAction == E_PROTO_MM_COMMAND_STOP) {
		ST_PROTO_MM_COMMAND_STOP *pstProtoMMCommandStop = (ST_PROTO_MM_COMMAND_STOP *)pProtoRoot;

		jsonRoot["action"] = static_cast<int>(dwAction);
	}
	else if (dwAction == E_PROTO_MM_COMPLETE_READY) {
		ST_PROTO_MM_COMPLETE_READY *pstProtoMMCompleteReady = (ST_PROTO_MM_COMPLETE_READY *)pProtoRoot;

		jsonRoot["action"] = static_cast<int>(dwAction);
	}
	else if (dwAction == E_PROTO_MM_COMMAND_START) {
		ST_PROTO_MM_COMMAND_START *pstProtoMMCompleteReady = (ST_PROTO_MM_COMMAND_START *)pProtoRoot;

		jsonRoot["action"] = static_cast<int>(dwAction);
	}
	else {
		jsonRoot["action"] = E_PROTO_MM_UNKNOWN;
	}

	Json::StyledWriter jsonWriter;

	std::string strJsonData;
	strJsonData = jsonWriter.write(jsonRoot);
	refstrJsonData = strJsonData;

	return E_RET_SUCCESS;
}

DWORD CManagerThread::SendToManager(std::string &refstrSendMsg, SOCKET sockManager)
{
	int nSizeOfData = refstrSendMsg.size();
	int nSent = 0, nRet;
	BOOL bContinue = TRUE;
	while (bContinue) {
		nRet = send(sockManager, refstrSendMsg.c_str() + nSent, nSizeOfData - nSent, 0);
		if (nSent == SOCKET_ERROR) {
			int nRet = WSAGetLastError();
			ShowErrorSend(nRet);
			bContinue = FALSE;
			continue;
		}
		else if (nRet == nSizeOfData) {
			DebugLog("Success to send data to client");
			bContinue = FALSE;
			continue;
		}

		/*
			if all data is not sent to client yet, continue to send to rest of data
		*/
		nSent = nRet;
	}
	return E_RET_SUCCESS;
}

DWORD CManagerThread::BroadCastMsgToManager(std::string &refstrSendData)
{
	DWORD dwTotalConnection = m_vecConnection.size();
	DWORD dwSucceedCount = 0;
	DWORD dwRet;
	for (unsigned i = 0; i < dwTotalConnection; i++) {
		SOCKET sockManager = m_vecConnection[i].stClientSocket.hClientSock;
		dwRet = SendToManager(refstrSendData, sockManager);
		if (dwRet != E_RET_SUCCESS) {
			continue;
		}
		dwSucceedCount++;
	}

	if (dwSucceedCount != dwTotalConnection) {
		ErrorLog("Fail to broardcast to all manager [%d/%d]", dwSucceedCount, dwTotalConnection);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

VOID CManagerThread::CheckAndOperateControlCommand()
{
	ControlCommand controlCommand;
	CheckControlCommand(controlCommand);
	if (controlCommand.size() < 1) {
		return;
	}

	DebugLog("Request ready command to all manager");
	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return;
	}

	ParseControlCommand(controlCommand, pProtoRoot);

	std::string strSendData;
	BuildSendMsg(strSendData, pProtoRoot);

	DWORD dwRet;
	dwRet = BroadCastMsgToManager(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		return;
	}
}

DWORD CManagerThread::CheckAllReadyState()
{
	DWORD dwTotalConnection = m_vecConnection.size();
	DWORD dwCountOfReady = 0;
	for (unsigned i = 0; i < dwTotalConnection; i++) {
		if (m_vecConnection[i].stConnectionState.dwState != E_CONN_COMPLETE_READY) {
			continue;
		}
		dwCountOfReady++;
	}

	if (dwTotalConnection != dwCountOfReady) {
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CManagerThread::CheckAndOperateCompleteReady()
{
	DWORD dwRet;
	dwRet = CheckAllReadyState();
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	ST_PROTO_MM_COMPLETE_READY *pstProtoMMCompleteReady = (ST_PROTO_MM_COMPLETE_READY *)pProtoRoot;
	pstProtoMMCompleteReady->dwAction = E_PROTO_MM_COMPLETE_READY;

	std::string strSendData;
	BuildSendMsg(strSendData, pProtoRoot);

	dwRet = BroadCastMsgToManager(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	return dwRet;
}

DWORD CManagerThread::BroadCastStartCommand()
{
	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return E_RET_FAIL;
	}

	DWORD dwTotalConnection = m_vecConnection.size();
	for (unsigned i = 0; i < dwTotalConnection; i++) {
		m_vecConnection[i].stConnectionState.dwState = E_CONN_BROADCAST_SEND;
	}

	pProtoRoot->dwAction = E_PROTO_MM_COMMAND_START;

	std::string strSendData;
	BuildSendMsg(strSendData, pProtoRoot);

	DWORD dwRet;
	dwRet = BroadCastMsgToManager(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

VOID CManagerThread::DeleteSelectedConnection(DWORD dwSelectedValue)
{
	VecConnection::iterator iterVec;
	DWORD dwCurrent = 0;
	for (iterVec = m_vecConnection.begin(); iterVec != m_vecConnection.end(); iterVec++, dwCurrent++) {
		if (dwCurrent != dwSelectedValue) {
			continue;
		}
		m_vecConnection.erase(iterVec);
		DebugLog("Delete Manager Connection [%d]", m_vecConnection.size());
	}
}

DWORD CManagerThread::ProcessCommunicationTask(DWORD dwCountOfManager)
{
#define SELECT_TIMEOUT 0
	FD_SET stReadset, substReadset;

	FD_ZERO(&stReadset);
	FD_SET(m_stServerContext.stServerInfo.hServerSock, &stReadset);

	struct timeval stTimeVal;
	stTimeVal.tv_sec = 10;
	stTimeVal.tv_usec = 0;

	DWORD dwRet = E_RET_SUCCESS;
	BOOL bContinue = TRUE;

	while (bContinue) {
		// Waiting for all manager connection
		if (dwCountOfManager == m_vecConnection.size()) {
			CheckAndOperateControlCommand();

			dwRet = CheckAllReadyState();
			if (dwRet == E_RET_SUCCESS) {
				DebugLog("Broadcast to all manager for start command");
				BroadCastStartCommand();
			}
		}

		substReadset = stReadset;

		int nRet;
		nRet = ::select(0, &substReadset, NULL, NULL, &stTimeVal);
		if (nRet == SELECT_TIMEOUT) {
			continue;
		}
		else if (nRet == SOCKET_ERROR) {
			DWORD dwRet = ::GetLastError();
			ErrorLog("Fail to operate select function [%d]", dwRet);
			continue;
		}

		if (FD_ISSET(m_stServerContext.stServerInfo.hServerSock, &substReadset)) {
			ST_CONNECTION_INFO stConnectionInfo;
			dwRet = m_pHelpServer->AcceptServer(m_stServerContext, stConnectionInfo.stClientSocket);
			if (dwRet != E_RET_SUCCESS) {
				ErrorLog("Fail to accept manager connection");
				continue;
			}
			DebugLog("Manager Connection is created");

			m_vecConnection.push_back(stConnectionInfo);
			FD_SET(stConnectionInfo.stClientSocket.hClientSock, &stReadset);

			continue;
		}

		for (unsigned i = 0; i < m_vecConnection.size(); i++) {
			SOCKET ClientSock = m_vecConnection[i].stClientSocket.hClientSock;

			if (FD_ISSET(ClientSock, &substReadset)) {
				DWORD dwRet;
				dwRet = ProcessInterSectionTask(ClientSock);
				if (dwRet != E_RET_SUCCESS) {
					FD_CLR(ClientSock, &substReadset);
					DeleteSelectedConnection(i);
					break;
				}
			}
		}
	}

	return dwRet;
}

DWORD CManagerThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	DWORD dwCountOfManager = pstThreadsParam->dwCountOfManager;

	InitManagerServer(pstThreadsParam->dwPort);

	BOOL bContinue = TRUE;
	while (bContinue)
	{
		try
		{
			ProcessCommunicationTask(dwCountOfManager);
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}

	return E_RET_SUCCESS;
}

