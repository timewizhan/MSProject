#include "HelpServer.h"
#include "..\Server\MServerError.h"

CHelpServer::CHelpServer() :
m_pHelpTool(NULL)
{
	m_pHelpTool = new (std::nothrow) CHelpTool();
	if (!m_pHelpTool)
		ErrorLog("Fail to import HelpTool");
}

DWORD CHelpServer::InitServerSock(ST_SERVER_CONTEXT &refstServerContext)
{
	
	DWORD dwRet;
	WSADATA wsa;
	

	try
	{
		dwRet = ::WSAStartup(MAKEWORD(2, 2), &wsa);
		if (dwRet != 0) {
			ShowErrorWSAStartup(dwRet);
			return E_RET_FAIL;
		}
		refstServerContext.stServerInfo.hServerSock = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	}
	catch (std::exception &e) {
		printf("%s\n", e.what());
	}
	
	SOCKET Sock = NULL;
	/*if (Sock == INVALID_SOCKET) {
		dwRet = WSAGetLastError();
		ShowErrorWSASocket(dwRet);
		return E_RET_FAIL;
	}*/
	
	//refstServerContext.stServerInfo.hServerSock = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	//refstServerContext.stServerInfo.hServerSock = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	

	int nOptionValue = 1;
	int nResult;
	nResult = ::setsockopt(refstServerContext.stServerInfo.hServerSock, SOL_SOCKET, SO_REUSEADDR, (char *)&nOptionValue, sizeof(int));
	if (nResult == SOCKET_ERROR) {
		dwRet = WSAGetLastError();
		ShowErrorSetSockOpt(dwRet);
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

DWORD CHelpServer::InitServerBind(ST_SERVER_CONTEXT &refstServerContext, DWORD dwPort)
{
	DWORD dwRet = E_RET_SUCCESS;
	std::vector<std::string> vecstrGetAddress;
	dwRet = m_pHelpTool->GetAddressInfo(vecstrGetAddress);
	if (vecstrGetAddress.size() < 1 || dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to get address saved");
		return E_RET_FAIL;
	}

	/*
		before bind, listen is used, you have to set server socket about address, port, type
	*/
	refstServerContext.stServerInfo.stServerAddrIn.sin_family = AF_INET;
	refstServerContext.stServerInfo.stServerAddrIn.sin_port = ::htons((unsigned short)dwPort);

	std::string strAddress = vecstrGetAddress[0].c_str();
	::inet_pton(AF_INET, strAddress.c_str(), &refstServerContext.stServerInfo.stServerAddrIn.sin_addr.s_addr);

	int nRet;
	DebugLog("Init Server Address : %s", vecstrGetAddress[0].c_str());
	DebugLog("Init Server Port : %d", dwPort);
	nRet = ::bind(refstServerContext.stServerInfo.hServerSock, (SOCKADDR *)&refstServerContext.stServerInfo.stServerAddrIn, sizeof(refstServerContext.stServerInfo.stServerAddrIn));
	if (nRet == SOCKET_ERROR) {
		ErrorLog("Fail to operate socket bind");
		return E_RET_FAIL;
	}

	// backlog value is set to SOMAXCONN
	// The backlog argument defines the maximum length to which the queue of pending connections for sockfd may grow
	nRet = ::listen(refstServerContext.stServerInfo.hServerSock, SOMAXCONN);
	if (nRet == SOCKET_ERROR) {
		ErrorLog("Fail to operate socket listen");
		return E_RET_FAIL;
	}

	return dwRet;
}

DWORD CHelpServer::AcceptServer(ST_SERVER_CONTEXT &refstServerContext, ST_CLIENT_SOCKET &refstClientSock)
{
	int nLengthOfClientSockIn = sizeof(refstClientSock.stClientAddrIn);
	refstClientSock.hClientSock = ::accept(refstServerContext.stServerInfo.hServerSock, (SOCKADDR *)&refstClientSock.stClientAddrIn, &nLengthOfClientSockIn);
	if (refstClientSock.hClientSock == INVALID_SOCKET) {
		ErrorLog("Fail to get invalid client socket");
		return E_RET_FAIL;
	}

	char szClientAddress[32] = { 0 };
	inet_ntop(AF_INET, (void *)&refstClientSock.stClientAddrIn.sin_addr, (PSTR)szClientAddress, sizeof(szClientAddress));
	DebugLog("Connected Client Socket : %d, Address : %s", refstClientSock.hClientSock, szClientAddress);

	return E_RET_SUCCESS;
}