#include "MMThread.h"
#include "MServerStruct.h"
#include "MServerError.h"

#include "Protocol.h"
#include "json\json.h"

CMMThread::CMMThread() :
m_pHelpClient(NULL)
{
	m_pHelpClient = new (std::nothrow) CHelpClient();
	if (!m_pHelpClient) {
		ErrorLog("Fail to allocate HelpClient memory");
	}
}

CMMThread::~CMMThread()
{
	if (m_pHelpClient) {
		delete m_pHelpClient;
	}
}

VOID CMMThread::InitServerConnection(ST_SERVER_ADDR &refstServerAddr)
{
	m_pHelpClient->InitClientSock(m_stClientContext, refstServerAddr);
}

BOOL CMMThread::ConnectToServer() 
{
#define SECOND 1000
	BOOL bContinue = TRUE;

	DWORD dwRet;
	DWORD dwTimeCount = 1;
	while (bContinue) {
		dwRet = m_pHelpClient->ConnectToServer(m_stClientContext);
		if (dwRet != E_RET_SUCCESS) {
			::Sleep(SECOND * dwTimeCount);

			if (dwTimeCount > 64)
				dwTimeCount = 1;

			dwTimeCount *= 2;
			continue;
		}
		bContinue = FALSE;
	}

	DebugLog("Success to create connection with MM Server");
	return TRUE;
}

DWORD CMMThread::SendToMMServer(std::string &refstrSendMsg)
{
	int nSizeOfData = refstrSendMsg.size();
	int nSent = 0, nRet;
	BOOL bContinue = TRUE;
	while (bContinue) {
		nRet = send(m_stClientContext.stServerInfo.hServerSock, refstrSendMsg.c_str() + nSent, nSizeOfData - nSent, 0);
		if (nSent == SOCKET_ERROR) {
			int nRet = WSAGetLastError();
			ShowErrorSend(nRet);
			bContinue = FALSE;
			continue;
		}
		else if (nRet == nSizeOfData) {
			DebugLog("Success to send data to MM Server");
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

DWORD CMMThread::RecvFromMMServer(std::string &refstrRecvMsg)
{
	char szBuf[256] = { 0 };

	int nRecv;
	nRecv = ::recv(m_stClientContext.stServerInfo.hServerSock, szBuf, sizeof(szBuf), 0);
	if (nRecv == SOCKET_ERROR) {
		ErrorLog("Fail to recv data from client");
		return E_RET_FAIL;
	}

	refstrRecvMsg = szBuf;
	DebugLog("Receive From MM Server [%s]", refstrRecvMsg.c_str());

	return E_RET_SUCCESS;
}

DWORD CMMThread::BuildSendMsg(std::string &refstrJsonData, DWORD dwManagerNumber, ST_PROTO_ROOT *pProtoRoot)
{
	Json::Value jsonRoot;

	DWORD dwAction = pProtoRoot->dwAction;
	if (dwAction == E_PROTO_MM_COMMAND_READY) {
		ST_PROTO_INNER_COMMAND_READY *pProtoCommandReady = (ST_PROTO_INNER_COMMAND_READY *)pProtoRoot;

		jsonRoot["action"] = E_PROTO_INNER_COMMAND_READY;
		jsonRoot["number"] = static_cast<int>(pProtoCommandReady->dwNumberOfBots);
	}
	else if (dwAction == E_PROTO_MM_COMMAND_START) {
		ST_PROTO_INNER_COMMAND_START *pProtoCommandStart = (ST_PROTO_INNER_COMMAND_START *)pProtoRoot;

		jsonRoot["action"] = E_PROTO_INNER_COMMAND_START;
	}
	else if (dwAction == E_PROTO_MM_COMMAND_STOP) {
		ST_PROTO_INNER_COMMAND_STOP *pProtoCommandStop = (ST_PROTO_INNER_COMMAND_STOP *)pProtoRoot;

		jsonRoot["action"] = E_PROTO_INNER_COMMAND_STOP;
	}
	// From Bot Thread
	else if (dwAction == E_PROTO_INNER_COMPLETE_READY) {
		ST_PROTO_INNER_COMPLETE_READY *pProtoCommandCompleteReady = (ST_PROTO_INNER_COMPLETE_READY *)pProtoRoot;

		jsonRoot["action"] = E_PROTO_MM_COMPLETE_READY;
		jsonRoot["number"] = static_cast<int>(dwManagerNumber);
	}
	else if (dwAction == E_PROTO_BOT_COMPLETE_START) {
		ST_PROTO_BOT_COMPLETE_START *pProtoCommandCompleteStart = (ST_PROTO_BOT_COMPLETE_START *)pProtoRoot;

		jsonRoot["action"] = E_PROTO_MM_COMPLETE_START;
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

DWORD CMMThread::ParseDataFromMM(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
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
	if (dwAction == E_PROTO_MM_COMMAND_READY) {
		ST_PROTO_MM_COMMAND_READY *pProtoCommandReady = (ST_PROTO_MM_COMMAND_READY *)pProtoRoot;
		pProtoCommandReady->dwAction = dwAction;
		pProtoCommandReady->dwNumberOfBots = jsonRecvData["number"].asInt();
	}
	else if (dwAction == E_PROTO_MM_COMMAND_START) {
		ST_PROTO_MM_COMMAND_START *pProtoCommandStart = (ST_PROTO_MM_COMMAND_START *)pProtoRoot;
		pProtoCommandStart->dwAction = dwAction;
	}
	else {
		// nothing 
		pProtoRoot->dwAction = dwAction;
	}

	return E_RET_SUCCESS;
}

DWORD CMMThread::ParseDataFromBots(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
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
	if (dwAction == E_PROTO_INNER_COMPLETE_READY) {
		ST_PROTO_INNER_COMPLETE_READY *pProtoCommandReady = (ST_PROTO_INNER_COMPLETE_READY *)pProtoRoot;
		pProtoCommandReady->dwAction = dwAction;
	}
	else if (dwAction == E_PROTO_BOT_COMPLETE_START) {
		ST_PROTO_BOT_COMPLETE_START *pProtoCommandStart = (ST_PROTO_BOT_COMPLETE_START *)pProtoRoot;
		pProtoCommandStart->dwAction = dwAction;
	}
	else {
		// nothing 
		pProtoRoot->dwAction = dwAction;
	}

	return E_RET_SUCCESS;
}

DWORD CMMThread::ProcessPreTask(std::string &refstrSendData, DWORD dwManagerNumber)
{
	DWORD dwRet = E_RET_FAIL;

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	std::string strRecvMsg;
	dwRet = RecvFromMMServer(strRecvMsg);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = ParseDataFromMM(strRecvMsg, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = BuildSendMsg(refstrSendData, dwManagerNumber, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	return dwRet;
}

DWORD CMMThread::ProcessInnerTask(std::string &refstrSendData, std::string &refstrRecvData, ST_THREADS_PARAM &refstThreadsParam)
{
	DWORD dwWrittenSize;
	BOOL bSuccess;

	bSuccess = ::WriteFile(refstThreadsParam.stSharedMemInfo.hOnlyWritePipe, refstrSendData.c_str(), refstrSendData.size(), &dwWrittenSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

	DebugLog("Send to Bot thread By Pipe [%s]", refstrSendData.c_str());
	::FlushFileBuffers(refstThreadsParam.stSharedMemInfo.hOnlyWritePipe);

	DWORD dwReadSize;
	char szRecvData[128] = { 0 };
	bSuccess = ::ReadFile(refstThreadsParam.stSharedMemInfo.hOnlyReadPipe, szRecvData, sizeof(szRecvData), &dwReadSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}
	
	refstrRecvData = szRecvData;
	refstrRecvData.resize(dwReadSize);
	DebugLog("Receive from Bot thread By Pipe [%s]", refstrRecvData.c_str());

	return E_RET_SUCCESS;
}

DWORD CMMThread::ProcessPostTask(std::string &refstrRecvData, DWORD dwManagerNumber)
{
	DWORD dwRet = E_RET_FAIL;

	ST_PROTO_ROOT *pProtoRoot = new (std::nothrow) ST_PROTO_ROOT();
	if (!pProtoRoot) {
		return dwRet;
	}

	dwRet = ParseDataFromBots(refstrRecvData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	std::string strSendData;
	dwRet = BuildSendMsg(strSendData, dwManagerNumber, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = SendToMMServer(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	return dwRet;
}


VOID CMMThread::ProcessCycleTask(ST_THREADS_PARAM &refstThreadsParam)
{
	DebugLog("Working ProcessPreTask");
	DWORD dwRet;
	std::string strSendData;
	dwRet = ProcessPreTask(strSendData, refstThreadsParam.dwManagerNumber);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPreTask");
	}

	DebugLog("Working ProcessInnerTask");

	std::string strRecvData;
	dwRet = ProcessInnerTask(strSendData, strRecvData, refstThreadsParam);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessInnerTask");
	}

	DebugLog("Working ProcessPostTask");
	dwRet = ProcessPostTask(strRecvData, refstThreadsParam.dwManagerNumber);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPostTask");
	}
}

DWORD CMMThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	ST_THREADS_PARAM stThreadParam = *pstThreadsParam;

	ST_SERVER_ADDR stServerAddr;
	stServerAddr.strIPAddress = stThreadParam.strIPAddress;
	stServerAddr.dwPort = stThreadParam.dwPort;

	InitServerConnection(stServerAddr);

	BOOL bConnected = FALSE;
	BOOL bContinue = TRUE;
	while (bContinue) 
	{
		try
		{
			if (!bConnected) {
				bConnected = ConnectToServer();
			}

			ProcessCycleTask(stThreadParam);
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());
			::WSACleanup();
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}
	
	::WSACleanup();
	return E_RET_SUCCESS;
}

