#include "HealthThread.h"
#include "MServerStruct.h"
#include "MServerError.h"

#include "Protocol.h"
#include "json\json.h"

CHealthThread::CHealthThread() :
m_pHelpClient(NULL)
{
	m_pHelpClient = new (std::nothrow) CHelpClient();
	if (!m_pHelpClient) {
		ErrorLog("Fail to allocate HelpClient memory");
	}
}

CHealthThread::~CHealthThread()
{
	if (m_pHelpClient) {
		delete m_pHelpClient;
	}
}

VOID CHealthThread::InitServerConnection(ST_SERVER_ADDR &refstServerAddr)
{
	m_pHelpClient->InitClientSock(m_stClientContext, refstServerAddr);
}

VOID CHealthThread::CloseConnection()
{
	::closesocket(m_stClientContext.stServerInfo.hServerSock);
}

BOOL CHealthThread::ConnectToServer()
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

	DebugLog("Success to create connection with MM Health Server");
	return TRUE;
}

DWORD CHealthThread::SendToMMServer(std::string &refstrSendMsg)
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
			//DebugLog("Success to send data to MM Health Check Server");
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

DWORD CHealthThread::BuildSendMsg(std::string &refstrJsonData, DWORD dwManagerNumber)
{
	Json::Value jsonRoot;

	jsonRoot["action"] = E_PROTO_MM_KEEPALIVE;
	jsonRoot["number"] = static_cast<int>(dwManagerNumber);

	Json::StyledWriter jsonWriter;

	std::string strJsonData;
	strJsonData = jsonWriter.write(jsonRoot);
	refstrJsonData = strJsonData;

	return E_RET_SUCCESS;
}

DWORD CHealthThread::ProcessPreTask(std::string &refstrSendData, DWORD dwManagerNumber)
{
	DWORD dwRet = E_RET_FAIL;

	dwRet = BuildSendMsg(refstrSendData, dwManagerNumber);
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	return dwRet;
}

DWORD CHealthThread::ProcessPostTask(std::string &refstrRecvData)
{
	DWORD dwRet = E_RET_FAIL;

	dwRet = SendToMMServer(refstrRecvData);
	if (dwRet != E_RET_SUCCESS) {
		DebugLog("Fail to send data to MM Health Check Server");
		return dwRet;
	}

	return dwRet;
}


VOID CHealthThread::ProcessCycleTask(DWORD dwManagerNumber)
{
	DWORD dwRet;
	std::string strSendData;
	dwRet = ProcessPreTask(strSendData, dwManagerNumber);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPreTask");
	}

	dwRet = ProcessPostTask(strSendData);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to operate ProcessPostTask");
	}
}

DWORD CHealthThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	ST_SERVER_ADDR stServerAddr;
	stServerAddr.strIPAddress = pstThreadsParam->strIPAddress;
	stServerAddr.dwPort = pstThreadsParam->dwPort;

	DWORD dwManagerNumber = pstThreadsParam->dwManagerNumber;
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

			ProcessCycleTask(dwManagerNumber);
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());

			CloseConnection();
			bConnected = FALSE;
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}

	return E_RET_SUCCESS;
}

