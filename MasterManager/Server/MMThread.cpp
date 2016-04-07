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

VOID CMMThread::InitServerConnection()
{
	m_pHelpClient->InitClientSock(m_stClientContext, m_stServerAddr);
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

	return E_RET_SUCCESS;
}

DWORD CMMThread::BuildSendMsg(std::string &refstrJsonData, ST_PROTO_ROOT *pProtoRoot)
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
	else {
		// nothing 
		pProtoRoot->dwAction = dwAction;
	}

	return E_RET_SUCCESS;
}

DWORD CMMThread::ProcessPreTask(std::string &refstrSendData)
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

	dwRet = BuildSendMsg(refstrSendData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	delete pProtoRoot;
	return dwRet;
}

DWORD CMMThread::ProcessInnerTask(std::string &refstrSendData, std::string &refstrRecvData, ST_THREADS_PARAM *pstThreadsParam)
{
	DWORD dwWrittenSize;
	bool bSuccess;
	bSuccess = ::WriteFile(pstThreadsParam->stSharedMemInfo.hOnlyWritePipe, refstrSendData.c_str(), refstrSendData.size(), &dwWrittenSize, NULL);
	if (!bSuccess) {
		return E_RET_FAIL;
	}

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

DWORD CMMThread::ProcessPostTask(std::string &refstrRecvData)
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
	dwRet = BuildSendMsg(strSendData, pProtoRoot);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = SendToMMServer(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	delete pProtoRoot;
	return dwRet;
}


VOID CMMThread::ProcessCycleTask(ST_THREADS_PARAM *pstThreadsParam) throw(std::exception)
{
	DWORD dwRet;
	std::string strSendData;
	dwRet = ProcessPreTask(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("");
	}

	std::string strRecvData;
	dwRet = ProcessInnerTask(strSendData, strRecvData, pstThreadsParam);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("");
	}

	dwRet = ProcessPostTask(strRecvData);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("");
	}
}

DWORD CMMThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	InitServerConnection();

	BOOL bConnected = FALSE;
	BOOL bContinue = FALSE;
	while (bContinue) 
	{
		try
		{
			if (!bConnected) {
				bConnected = ConnectToServer();
			}

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

