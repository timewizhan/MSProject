#include "BGBrokerThread.h"
#include "..\HelpTool\HelpTool.h"

CBGBrokerThread::CBGBrokerThread() 
{
}


DWORD CBGBrokerThread::InitSocket()
{
	DWORD dwRet;
	dwRet = ::WSAStartup(MAKEWORD(2, 2), &m_stServerInit.stWSAData);
	if (dwRet != 0) {
		ShowErrorWSAStartup(dwRet);
		return E_RET_FAIL;
	}

	m_stServerInit.hServerSock = ::WSASocket(AF_INET, SOCK_STREAM, IPPROTO_TCP, NULL, 0, WSA_FLAG_OVERLAPPED);
	if (m_stServerInit.hServerSock == INVALID_SOCKET) {
		dwRet = WSAGetLastError();
		ShowErrorWSASocket(dwRet);
		return E_RET_FAIL;
	}

	int nOptionValue = 1;
	int nResult;
	nResult = ::setsockopt(m_stServerInit.hServerSock, SOL_SOCKET, SO_REUSEADDR, (char *)&nOptionValue, sizeof(int));
	if (nResult == SOCKET_ERROR) {
		dwRet = WSAGetLastError();
		ShowErrorSetSockOpt(dwRet);
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

DWORD CBGBrokerThread::BindSocket()
{
	DWORD dwRet = E_RET_SUCCESS;
	std::vector<std::string> vecstrGetAddress;

	CHelpTool HelpTool;
	dwRet = HelpTool.GetAddressInfo(vecstrGetAddress);
	if (vecstrGetAddress.size() < 1 || dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to get address saved");
		return E_RET_FAIL;
	}

	// Port 6700 is fixed
	const DWORD dwPort = 6700;
	m_stServerInit.stServerAddrIn.sin_family = AF_INET;
	m_stServerInit.stServerAddrIn.sin_port = ::htons(static_cast<unsigned short>(dwPort));

	std::string strAddress = vecstrGetAddress[0].c_str();
	::inet_pton(AF_INET, strAddress.c_str(), &m_stServerInit.stServerAddrIn.sin_addr.s_addr);

	int nRet;
	DebugLog("Init Server Address : %s", vecstrGetAddress[0].c_str());
	DebugLog("Init Server Port : %d", dwPort);
	nRet = ::bind(m_stServerInit.hServerSock, (SOCKADDR *)&m_stServerInit.stServerAddrIn, sizeof(m_stServerInit.stServerAddrIn));
	if (nRet == SOCKET_ERROR) {
		ErrorLog("Fail to operate socket bind");
		return E_RET_FAIL;
	}

	// backlog value is set to SOMAXCONN
	// The backlog argument defines the maximum length to which the queue of pending connections for sockfd may grow
	nRet = ::listen(m_stServerInit.hServerSock, SOMAXCONN);
	if (nRet == SOCKET_ERROR) {
		ErrorLog("Fail to operate socket listen");
		return E_RET_FAIL;
	}

	return dwRet;
}

VOID CBGBrokerThread::AcceptBroker(ST_CLIENT_SOCKET &refstClientSocket) throw(std::exception)
{
	DWORD dwError;

	int nLengthOfClientSockIn = sizeof(refstClientSocket.stClientAddrIn);
	refstClientSocket.hClientSock = ::accept(m_stServerInit.hServerSock, (SOCKADDR *)&refstClientSocket.stClientAddrIn, &nLengthOfClientSockIn);
	if (refstClientSocket.hClientSock == INVALID_SOCKET) {
		dwError = WSAGetLastError();
		ErrorLog("Fail to get invalid broker socket");
		ShowErrorWSASocket(dwError);
		throw std::exception("Fail to accpet broker socket");
	}

	char szClientAddress[32] = { 0 };
	inet_ntop(AF_INET, (void *)&refstClientSocket.stClientAddrIn.sin_addr, (PSTR)szClientAddress, sizeof(szClientAddress));
	DebugLog("Success to connect from broker");
	DebugLog("Broker Socket : %d, Address : %s", refstClientSocket.hClientSock, szClientAddress);
}

DWORD CBGBrokerThread::ReadDataFromBroker(ST_CLIENT_SOCKET &refstClientSocket, std::string &refstrReqType)
{
	ST_BROKER_CONNECTION *pstBrokerConnection = new ST_BROKER_CONNECTION;

	::memset(pstBrokerConnection, 0x00, sizeof(ST_BROKER_CONNECTION));
	pstBrokerConnection->stWSABuf.buf = pstBrokerConnection->szBuf;
	pstBrokerConnection->stWSABuf.len = sizeof(pstBrokerConnection->szBuf);

	std::string strRecvData;

	DWORD dwRecv, nRet;
	DWORD dwFlags = 0;
	nRet = WSARecv(refstClientSocket.hClientSock,
		&(pstBrokerConnection->stWSABuf),
		1,
		&dwRecv,
		&dwFlags,
		pstBrokerConnection,
		NULL);
	if (nRet == SOCKET_ERROR) {
		DWORD dwRet = ::WSAGetLastError();
		if (dwRet != WSA_IO_PENDING) {
			ShowErrorWSASocket(dwRet);
			::closesocket(refstClientSocket.hClientSock);
			return E_RET_FAIL;
		}

		/*
			Working by async
		*/

		DWORD dwTransferred = 0;
		WSAGetOverlappedResult(refstClientSocket.hClientSock, pstBrokerConnection, &dwTransferred, true, &dwFlags);
		strRecvData = pstBrokerConnection->szBuf;
	}

	refstrReqType = strRecvData;
	::closesocket(refstClientSocket.hClientSock);
	return E_RET_SUCCESS;
}

DWORD CBGBrokerThread::ChangeReqTypeValue(std::string &refstrReqType)
{
	DWORD dwReqType = ::atoi(refstrReqType.c_str());
	SetLockBrokerReqType(dwReqType);

	return E_RET_SUCCESS;
}

VOID CBGBrokerThread::CommunicateWithBroker(ST_CLIENT_SOCKET &refstClientSocket)
{
	DWORD dwRet = E_RET_FAIL;

	std::string strReqType;
	dwRet = ReadDataFromBroker(refstClientSocket, strReqType);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to read data from broker");
	}

	dwRet = ChangeReqTypeValue(strReqType);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to send data to main thread");
	}
}

DWORD CBGBrokerThread::StartBrokerThread()
{
	DWORD dwRet = E_RET_FAIL;
	dwRet = InitSocket();
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}

	dwRet = BindSocket();
	if (dwRet != E_RET_SUCCESS) {
		return dwRet;
	}
	
	bool bContinue = true;
	while (bContinue) 
	{
		ST_CLIENT_SOCKET stClientSocket;
		try
		{
			AcceptBroker(stClientSocket);
			CommunicateWithBroker(stClientSocket);
		}
		catch (std::exception &e) {
			if (stClientSocket.hClientSock) {
				::closesocket(stClientSocket.hClientSock);
			}
			ErrorLog("%s", e.what());
			// Not stop
		}
	}
	
	return E_RET_SUCCESS;
}

