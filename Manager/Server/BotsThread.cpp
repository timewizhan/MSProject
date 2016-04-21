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

VOID CBotsThread::InitBotsServer(DWORD dwPort)
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
	else if (dwAction == E_PROTO_BOT_COMPLETE_START) {
		ST_PROTO_BOT_COMPLETE_START *pstProtoBotCompleteStart = (ST_PROTO_BOT_COMPLETE_START *)pProtoRoot;
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
	stBotConnection.dwBotPID = pstProtoBotCompleteReady->dwPID;

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

DWORD CBotsThread::SendToMMThread(std::string &refstrSendData, ST_THREADS_PARAM &refstThreadParam)
{
	DWORD dwWrittenSize;
	BOOL bSuccess;
	bSuccess = ::WriteFile(refstThreadParam.stSharedMemInfo.hOnlyWritePipe, refstrSendData.c_str(), refstrSendData.size(), &dwWrittenSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

	DebugLog("Send to Bot thread By Pipe [%s]", refstrSendData.c_str());
	::FlushFileBuffers(refstThreadParam.stSharedMemInfo.hOnlyWritePipe);

	return E_RET_SUCCESS;
}

DWORD CBotsThread::RecvFromMMThread(std::string &refstrRecvData, ST_THREADS_PARAM &refstThreadParam)
{
	BOOL bSuccess;
	DWORD dwReadSize;
	char szRecvData[128] = { 0 };
	bSuccess = ::ReadFile(refstThreadParam.stSharedMemInfo.hOnlyReadPipe, szRecvData, sizeof(szRecvData), &dwReadSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

	refstrRecvData = szRecvData;
	refstrRecvData.resize(dwReadSize);
	DebugLog("Receive from MM thread By Pipe [%s]", refstrRecvData.c_str());

	return E_RET_SUCCESS;
}

DWORD CBotsThread::ParseDataFromMMThread(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber)
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
		refdwBotNumber = pProtoCommandReady->dwNumberOfBots;
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

DWORD CBotsThread::ExecuteBotGen(ST_THREADS_PARAM &refstThreadParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber)
{
	if (pProtoRoot->dwAction != E_PROTO_INNER_COMMAND_READY) {
		return E_RET_SUCCESS;
	}

	ST_PROTO_INNER_COMMAND_READY *pstProtoInnerCommandReady = (ST_PROTO_INNER_COMMAND_READY *)pProtoRoot;
	std::string strParam = std::to_string(refstThreadParam.dwManagerNumber) + " " + std::to_string(pstProtoInnerCommandReady->dwNumberOfBots);

	std::string strFullFilePath = refstThreadParam.strFilePath.c_str();
	DWORD dwLastPos = strFullFilePath.find_last_of("\\");
	
	std::string strWorkingDir = strFullFilePath.substr(0, dwLastPos);
	std::string strFileName = strFullFilePath.substr(dwLastPos + 1, strFullFilePath.size() - dwLastPos);

	_chdir(strWorkingDir.c_str());

	DebugLog("Execute Bot Generator : Working Dir : [%s]", strWorkingDir.c_str());
	DebugLog("Execute Bot Generator : File Name : [%s]", strFileName.c_str());
	DebugLog("Execute Bot Generator : Param : [%s]", strParam.c_str());
	::ShellExecuteA(NULL, "open", strFileName.c_str(), strParam.c_str(), strWorkingDir.c_str(), SW_SHOW);

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
			DebugLog("Success to send data to bot");
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

DWORD CBotsThread::BranchByAction(ST_THREADS_PARAM &refstThreadParam, ST_PROTO_ROOT *pProtoRoot, DWORD &refdwBotNumber)
{
	DWORD dwRet = E_RET_FAIL;

	DWORD dwAction = pProtoRoot->dwAction;
	if (dwAction == E_PROTO_INNER_COMMAND_READY) {
		// If the action is E_PROTO_INNER_COMMAND_READY, this thread execute bot generator
		// otherwise, pass it
		dwRet = ExecuteBotGen(refstThreadParam, pProtoRoot, refdwBotNumber);
		if (dwRet != E_RET_SUCCESS) {
			return dwRet;
		}

		DebugLog("Wait for Bot connection");
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

		DebugLog("Send start command to Bots [%s]", strSendData.c_str());

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

DWORD CBotsThread::ProcessPreTask(ST_THREADS_PARAM &refstThreadParam, DWORD &refdwBotNumber, DWORD &refdwAction)
{
	DWORD dwRet = E_RET_FAIL;

	std::string strRecvMsg;
	dwRet = RecvFromMMThread(strRecvMsg, refstThreadParam);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}
	
	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	dwRet = ParseDataFromMMThread(strRecvMsg, pProtoRoot, refdwBotNumber);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	refdwAction = pProtoRoot->dwAction;
	dwRet = BranchByAction(refstThreadParam, pProtoRoot, refdwBotNumber);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	return dwRet;
}

DWORD CBotsThread::ProcessCommunicationTask(DWORD &refdwBotNumber)
{
	FD_SET stReadset, substReadset;

	FD_ZERO(&stReadset);
	FD_SET(m_stServerContext.stServerInfo.hServerSock, &stReadset);

	DWORD dwRet = E_RET_SUCCESS;
	DWORD dwConnection = 0;
	BOOL bContinue = TRUE;
	while (bContinue) {
		substReadset = stReadset;
		::select(0, &substReadset, NULL, NULL, NULL);

		if (FD_ISSET(m_stServerContext.stServerInfo.hServerSock, &substReadset)) {
			ST_CLIENT_SOCKET stClientSocket;
			dwRet = m_pHelpServer->AcceptServer(m_stServerContext, stClientSocket);
			if (dwRet != E_RET_SUCCESS) {
				ErrorLog("Fail to accept manager connection");
				continue;
			}
			dwConnection++;

			DebugLog("Bot Connection is created [%d]", dwConnection);

			m_vecConnection.push_back(stClientSocket);
			FD_SET(stClientSocket.hClientSock, &stReadset);
			continue;
		} 

		for (unsigned i = 0; i < m_vecConnection.size(); i++) {
			SOCKET ClientSock = m_vecConnection[i].hClientSock;

			if (FD_ISSET(ClientSock, &substReadset)) {
				DWORD dwRet;
				dwRet = ProcessInterSectionTask(ClientSock);
				if (dwRet != E_RET_SUCCESS) {
					ErrorLog("Fail to process intersection task");
				}
				DebugLog("QueueBot Size is [%d]", CQueueBot<ST_BOT_CONNECTION>::getInstance().getQueueSize());
			}
		}
		
		if (CQueueBot<ST_BOT_CONNECTION>::getInstance().getQueueSize() >= refdwBotNumber) {
			DebugLog("Requested Bots have sent message to Bot Thread");

			FD_ZERO(&stReadset);
			bContinue = FALSE;
			continue;
		}
	}

	return dwRet;
}


DWORD CBotsThread::ProcessPostTask(ST_THREADS_PARAM &refstThreadParam, DWORD &refdwAction)
{
	DWORD dwRet = E_RET_FAIL;

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	if (refdwAction == E_PROTO_INNER_COMMAND_READY) {
		ST_PROTO_BOT_COMPLETE_READY *pstProtoBotCompleteReady = (ST_PROTO_BOT_COMPLETE_READY *)pProtoRoot;
		pstProtoBotCompleteReady->dwAction = E_PROTO_BOT_COMPLETE_READY;
	}
	else if (refdwAction == E_PROTO_INNER_COMMAND_START) {
		ST_PROTO_BOT_COMPLETE_START *pstProtoBotCompleteStart = (ST_PROTO_BOT_COMPLETE_START *)pProtoRoot;
		pstProtoBotCompleteStart->dwAction = E_PROTO_BOT_COMPLETE_START;
	}

	std::string strSendData;
	dwRet = BuildSendMsg(strSendData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = SendToMMThread(strSendData, refstThreadParam);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	//delete pProtoRoot;
	return dwRet;
}

VOID CBotsThread::ProcessCycleTask(ST_THREADS_PARAM &refstThreadParam)
{
	DWORD dwRet;

	DWORD dwBotNumber;
	DWORD dwAction;
	dwRet = ProcessPreTask(refstThreadParam, dwBotNumber, dwAction);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPreTask");
	}

	dwRet = ProcessPostTask(refstThreadParam, dwAction);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPostTask");
	}
}

DWORD CBotsThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	ST_THREADS_PARAM stThreadParam = *pstThreadsParam;

	InitBotsServer(stThreadParam.dwPort);

	BOOL bContinue = TRUE;
	while (bContinue)
	{
		try
		{
			ProcessCycleTask(stThreadParam);
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}

	return E_RET_SUCCESS;
}
