#include "HealthThread.h"
#include "json\json.h"

CHealthThread::CHealthThread()
{
	m_pHelpServer = new CHelpServer();
}

CHealthThread::~CHealthThread()
{
}

VOID CHealthThread::InitHealthServer(DWORD dwPort) throw(std::exception)
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


DWORD CHealthThread::RecvDataFromManager(SOCKET ClientSock, std::string &refstrRecvData)
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

DWORD CHealthThread::ParseDataFromManager(std::string &refstrRecvMsg, ST_PROTO_ROOT *pProtoRoot)
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
	if (dwAction == E_PROTO_MM_KEEPALIVE) {
		ST_PROTO_MM_KEEPALIVE *pProtoCompleteReady = (ST_PROTO_MM_KEEPALIVE *)pProtoRoot;
		pProtoCompleteReady->dwAction = dwAction;
		pProtoCompleteReady->dwManagerNumber = jsonRecvData["number"].asInt();
	}
	else {
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

VOID CHealthThread::ChangeConnectionState(SOCKET ClientSock, ST_PROTO_ROOT *pProtoRoot)
{
	ST_PROTO_MM_KEEPALIVE *pProtoCompleteReady = (ST_PROTO_MM_KEEPALIVE *)pProtoRoot;

	for (unsigned i = 0; i < m_vecConnection.size(); i++) {
		SOCKET sNestedSock = m_vecConnection[i].stClientSocket.hClientSock;
		if (sNestedSock != ClientSock) {
			continue;
		}

		if (m_vecConnection[i].dwConnectionNumber == 0) {
			m_vecConnection[i].dwConnectionNumber = pProtoCompleteReady->dwManagerNumber;
		}

		m_vecConnection[i].stConnectionState.llTime = time(NULL);
		m_vecConnection[i].stConnectionState.dwState = E_CONN_ALIVE;
		break;
	}
}

VOID CHealthThread::CheckKeepAlive()
{
	for (unsigned i = 0; i < m_vecConnection.size(); i++) {
		time_t llCurrentTime = time(NULL);

		if (m_vecConnection[i].stConnectionState.llTime == 0) {
			continue;
		}

		if (llCurrentTime - m_vecConnection[i].stConnectionState.llTime <= 300) {
			continue;
		}

		m_vecConnection[i].stConnectionState.dwState = E_CONN_NOT_ALIVE;
		DebugLog("Manager connection is disconnected [%s]", m_vecConnection[i].stClientSocket.ClientAddress);
	}
}

DWORD CHealthThread::ProcessInterSectionTask(SOCKET ClientSock)
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
		return dwRet;
	}

	ChangeConnectionState(ClientSock, pProtoRoot);
	return E_RET_SUCCESS;
}

DWORD CHealthThread::ProcessCommunicationTask()
{
#define SELECT_TIMEOUT 0
	FD_SET stReadset, substReadset;

	FD_ZERO(&stReadset);
	FD_SET(m_stServerContext.stServerInfo.hServerSock, &stReadset);

	struct timeval stTimeVal;
	stTimeVal.tv_sec = 1;

	DWORD dwRet = E_RET_SUCCESS;
	BOOL bContinue = TRUE;

	DWORD dwAcceptCount = 0;
	while (bContinue) {
		CheckKeepAlive();
		substReadset = stReadset;

		int nRet;
		nRet = ::select(0, &stReadset, NULL, NULL, &stTimeVal);
		if (dwRet == SELECT_TIMEOUT) {
			continue;
		}
		else if (dwRet == SOCKET_ERROR) {
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

			m_vecConnection.push_back(stConnectionInfo);
			FD_SET(stConnectionInfo.stClientSocket.hClientSock, &stReadset);
			continue;
		}

		for (unsigned i = 0; m_vecConnection.size(); i++) {
			SOCKET ClientSock = m_vecConnection[i].stClientSocket.hClientSock;

			if (FD_ISSET(ClientSock, &substReadset)) {
				DWORD dwRet;
				dwRet = ProcessInterSectionTask(ClientSock);
				if (dwRet != E_RET_SUCCESS) {
					ErrorLog("Fail to process intersection task");
				}
			}
		}
	}

	return dwRet;
}

DWORD CHealthThread::StartThread(ST_THREADS_PARAM *pstThreadsParam)
{
	InitHealthServer(pstThreadsParam->dwPort);

	BOOL bContinue = TRUE;
	while (bContinue)
	{
		try
		{
			ProcessCommunicationTask();
		}
		catch (std::exception &e) {
			ErrorLog("%s", e.what());
		}

		// Wait for 1 second for a next step
		::Sleep(1000);
	}

	return E_RET_SUCCESS;
}
