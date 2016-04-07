#include "BotsThread.h"
#include "Protocol.h"
#include "json\json.h"

#include "MServerError.h"

#include "..\Queue\QueueBot.h"

CQueueBot<ST_BOT_CONNECTION>* CQueueBot<ST_BOT_CONNECTION>::m_QueueBot = nullptr;

CBotsThread::CBotsThread()
{
	m_pHelpServer = new CHelpServer();
	m_pHelpTool = new CHelpTool();
}

VOID CBotsThread::InitBotsServer(DWORD dwPort) throw(std::exception)
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

VOID CBotsThread::ClearConnection()
{
	for (unsigned i = 0; i < m_vecConnection.size(); i++) {
		::closesocket(m_vecConnection[i].hClientSock);
	}
}

DWORD CBotsThread::BuildSendMsg(std::string &refstrJsonData, ST_PROTO_ROOT *pProtoRoot)
{
	Json::Value jsonRoot;

	DWORD dwAction = pProtoRoot->dwAction;
	if (dwAction == E_PROTO_BOT_COMPLETE_READY) {
		ST_PROTO_BOT_COMPLETE_READY *pstProtoBotCompleteReady = (ST_PROTO_BOT_COMPLETE_READY *)pProtoRoot;
		jsonRoot["action"] = E_PROTO_INNER_COMPLETE_READY;
	}
	else if (dwAction == E_PROTO_BOT_COMMAND_START) {
		ST_PROTO_BOT_COMMAND_START *pstProtoBotCommandStart = (ST_PROTO_BOT_COMMAND_START *)pProtoRoot;
		jsonRoot["action"] = static_cast<int>(dwAction);
	}
	else {
		jsonRoot["action"] = E_PROTO_INNTER_UNKNOWN;
	}

	Json::StyledWriter jsonWriter;

	std::string strJsonData;
	strJsonData = jsonWriter.write(jsonRoot);
	refstrJsonData = strJsonData;

	return E_RET_SUCCESS;
}

DWORD CBotsThread::RecvDataFromBot(SOCKET ClientSock, std::string &refstrRecvData)
{
	char szBuf[128] = { 0 };

	DWORD dwRecvCount;
	dwRecvCount = ::recv(ClientSock, szBuf, sizeof(szBuf), 0);
	if (dwRecvCount < 1) {
		return E_RET_FAIL;
	}

	refstrRecvData = szBuf;
	return E_RET_SUCCESS;
}

DWORD CBotsThread::ParseDataFromBot(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
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
	if (dwAction == E_PROTO_BOT_COMPLETE_READY) {
		ST_PROTO_BOT_COMPLETE_READY *pProtoCompleteReady = (ST_PROTO_BOT_COMPLETE_READY *)pProtoRoot;
		pProtoCompleteReady->dwAction = dwAction;
		pProtoCompleteReady->dwPID = jsonRecvData["pid"].asInt();
	}
	else {
		// nothing 
		pProtoRoot->dwAction = dwAction;
	}

	return E_RET_SUCCESS;
}


VOID CBotsThread::PushDataToQueue(ST_PROTO_ROOT *pProtoRoot) 
{
	ST_PROTO_BOT_COMPLETE_READY *pstProtoBotCompleteReady = (ST_PROTO_BOT_COMPLETE_READY *)pProtoRoot;

	ST_BOT_CONNECTION stBotConnection;
	stBotConnection.dwBotPID;

	CQueueBot<ST_BOT_CONNECTION>::getInstance().pushQueueConnection(stBotConnection);
}

DWORD CBotsThread::ProcessInterSectionTask(SOCKET ClientSock)
{
	std::string strRecvData;

	DWORD dwRet;
	dwRet = RecvDataFromBot(ClientSock, strRecvData);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	dwRet = ParseDataFromBot(strRecvData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	PushDataToQueue(pProtoRoot);
	return E_RET_SUCCESS;
}

DWORD CBotsThread::SendToMMThread(std::string &refstrSendData, ST_THREADS_PARAM *pstThreadsParam)
{
	DWORD dwWrittenSize;
	BOOL bSuccess;
	bSuccess = ::WriteFile(pstThreadsParam->stSharedMemInfo.hOnlyWritePipe, refstrSendData.c_str(), refstrSendData.size(), &dwWrittenSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

DWORD CBotsThread::RecvFromMMThread(std::string &refstrRecvData, ST_THREADS_PARAM *pstThreadsParam)
{
	BOOL bSuccess;
	DWORD dwReadSize;
	char szRecvData[128] = { 0 };
	bSuccess = ::ReadFile(pstThreadsParam->stSharedMemInfo.hOnlyReadPipe, szRecvData, sizeof(szRecvData), &dwReadSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

	refstrRecvData = szRecvData;
	refstrRecvData.resize(dwReadSize);

	return E_RET_SUCCESS;
}

DWORD CBotsThread::ParseDataFromMMThread(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
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

DWORD CBotsThread::ExecuteBotGen(ST_THREADS_PARAM *pstThreadsParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber)
{
	if (pProtoRoot->dwAction != E_PROTO_INNER_COMMAND_READY) {
		return E_RET_SUCCESS;
	}

	ST_PROTO_INNER_COMMAND_READY *pstProtoInnerCommandReady = (ST_PROTO_INNER_COMMAND_READY *)pProtoRoot;
	std::string strParam = std::to_string(pstProtoInnerCommandReady->dwNumberOfBots);
	::ShellExecuteA(NULL, "open", pstThreadsParam->strFilePath.c_str(), strParam.c_str(), NULL, SW_SHOW);

	refdwBotNumber = pstProtoInnerCommandReady->dwNumberOfBots;
	return E_RET_SUCCESS;
}

DWORD CBotsThread::BroadCastMsgToBot(std::string &refstrSendData)
{
	DWORD dwTotalConnection = m_vecConnection.size();
	DWORD dwSucceedCount = 0;
	DWORD dwRet;
	for (unsigned i = 0; i < dwTotalConnection; i++) {
		SOCKET sockManager = m_vecConnection[i].hClientSock;
		dwRet = SendToBot(refstrSendData, sockManager);
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

DWORD CBotsThread::SendToBot(std::string &refstrSendMsg, SOCKET sockManager)
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

VOID CBotsThread::TerminateBot()
{
	std::vector<DWORD> vecPID;

	DWORD dwSize = CQueueBot<ST_BOT_CONNECTION>::getInstance().getQueueSize();
	for (unsigned i = 0; i < dwSize; i++) {
		ST_BOT_CONNECTION stBotConnection;
		CQueueBot<ST_BOT_CONNECTION>::getInstance().popQueueConnection(stBotConnection);

		if (stBotConnection.dwBotPID == 0)
			continue;
		
		vecPID.push_back(stBotConnection.dwBotPID);
	}

	m_pHelpTool->TerminateProcessByALLPID(vecPID);
}

DWORD CBotsThread::BranchByAction(ST_THREADS_PARAM *pstThreadsParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber)
{
	DWORD dwRet = E_RET_FAIL;

	DWORD dwAction = pProtoRoot->dwAction;
	if (dwAction == E_PROTO_INNER_COMMAND_READY) {
		// If the action is E_PROTO_INNER_COMMAND_READY, this thread execute bot generator
		// otherwise, pass it
		dwRet = ExecuteBotGen(pstThreadsParam, pProtoRoot, refdwBotNumber);
		if (dwRet != E_RET_SUCCESS) {
			return dwRet;
		}

		dwRet = ProcessCommunicationTask(refdwBotNumber);
		if (dwRet != E_RET_SUCCESS) {
			return dwRet;
		}
	}
	else if (dwAction == E_PROTO_INNER_COMMAND_START) {
		std::string strSendData;
		dwRet = BuildSendMsg(strSendData, pProtoRoot);
		if (dwRet != E_RET_SUCCESS) {
			return dwRet;
		}
		dwRet = BroadCastMsgToBot(strSendData);
		if (dwRet != E_RET_SUCCESS) {
			return dwRet;
		}
	}
	else if (dwAction == E_PROTO_INNER_COMMAND_STOP) {
		TerminateBot();
	}
	else {
		return dwRet;
	}

	return dwRet;
}

DWORD CBotsThread::ProcessPreTask(ST_THREADS_PARAM *pstThreadsParam, DWORD &refdwBotNumber)
{
	DWORD dwRet = E_RET_FAIL;

	std::string strRecvMsg;
	dwRet = RecvFromMMThread(strRecvMsg, pstThreadsParam);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	dwRet = ParseDataFromMMThread(strRecvMsg, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = BranchByAction(pstThreadsParam, pProtoRoot, refdwBotNumber);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	delete pProtoRoot;
	return dwRet;
}

DWORD CBotsThread::ProcessCommunicationTask(DWORD &refdwBotNumber)
{
	FD_SET stReadset, substReadset;

	FD_ZERO(&stReadset);
	FD_SET(m_stServerContext.stServerInfo.hServerSock, &stReadset);

	DWORD dwRet = E_RET_SUCCESS;
	BOOL bContinue = TRUE;
	while (bContinue) {
		substReadset = stReadset;
		::select(0, &stReadset, NULL, NULL, NULL);

		if (FD_ISSET(m_stServerContext.stServerInfo.hServerSock, &substReadset)) {
			ST_CLIENT_SOCKET stClientSocket;
			m_pHelpServer->AcceptServer(m_stServerContext, stClientSocket);
			m_vecConnection.push_back(stClientSocket);

			FD_SET(stClientSocket.hClientSock, &stReadset);
			continue;
		}

		for (unsigned i = 0; m_vecConnection.size(); i++) {
			SOCKET ClientSock = m_vecConnection[i].hClientSock;

			if (FD_ISSET(ClientSock, &substReadset)) {
				DWORD dwRet;
				dwRet = ProcessInterSectionTask(ClientSock);
				if (dwRet != E_RET_SUCCESS) {
					ErrorLog("Fail to process intersection task");
				}
			}
		}
		
		if (CQueueBot<ST_BOT_CONNECTION>::getInstance().getQueueSize() == refdwBotNumber) {
			FD_ZERO(&stReadset);
			ClearConnection();
			bContinue = FALSE;
			continue;
		}
	}

	return dwRet;
}


DWORD CBotsThread::ProcessPostTask(ST_THREADS_PARAM *pstThreadsParam)
{
	DWORD dwRet = E_RET_FAIL;

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	ST_PROTO_BOT_COMPLETE_READY *pstProtoBotCompleteReady = (ST_PROTO_BOT_COMPLETE_READY *)pProtoRoot;
	pstProtoBotCompleteReady->dwAction = E_PROTO_BOT_COMPLETE_READY;

	std::string strSendData;
	dwRet = BuildSendMsg(strSendData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = SendToMMThread(strSendData, pstThreadsParam);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	delete pProtoRoot;
	return dwRet;
}

VOID CBotsThread::ProcessCycleTask(ST_THREADS_PARAM *pstThreadsParam) throw(std::exception)
{
	DWORD dwRet;

	DWORD dwBotNumber;
	dwRet = ProcessPreTask(pstThreadsParam, dwBotNumber);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPreTask");
	}

	dwRet = ProcessPostTask(pstThreadsParam);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPostTask");
	}
}

DWORD CBotsThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	InitBotsServer(pstThreadsParam->dwPort);

	BOOL bContinue = TRUE;
	while (bContinue)
	{
		try
		{
			ProcessCycleTask(pstThreadsParam);
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}

	return E_RET_SUCCESS;
}
