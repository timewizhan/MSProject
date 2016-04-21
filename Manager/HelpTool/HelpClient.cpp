#include "HelpClient.h"

#include "..\Server\MServerError.h"

CHelpClient::CHelpClient() :
m_pHelpTool(NULL)
{
	m_pHelpTool = new (std::nothrow) CHelpTool();
	if (!m_pHelpTool)
		ErrorLog("Fail to import HelpTool");
}

DWORD CHelpClient::InitClientSock(ST_CLIENT_CONTEXT &refstServerContext, ST_SERVER_ADDR &refstServerAddr)
{
	DWORD dwRet;
	dwRet = ::WSAStartup(MAKEWORD(2, 2), &refstServerContext.stWSAData);
	if (dwRet != 0) {
		ShowErrorWSAStartup(dwRet);
		return E_RET_FAIL;
	}

	refstServerContext.stServerInfo.hServerSock = ::socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (refstServerContext.stServerInfo.hServerSock == INVALID_SOCKET) {
		dwRet = WSAGetLastError();
		ShowErrorWSASocket(dwRet);
		return E_RET_FAIL;
	}

	refstServerContext.stServerInfo.sockAddr.sin_family = AF_INET;
	::inet_pton(AF_INET, refstServerAddr.strIPAddress.c_str(), (void *)&refstServerContext.stServerInfo.sockAddr.sin_addr);
	refstServerContext.stServerInfo.sockAddr.sin_port = htons(static_cast<u_short>(refstServerAddr.dwPort));
	
	return E_RET_SUCCESS;
}

DWORD CHelpClient::ConnectToServer(ST_CLIENT_CONTEXT &refstServerContext)
{
	DWORD dwRet;
	dwRet = ::connect(refstServerContext.stServerInfo.hServerSock, (SOCKADDR *)&refstServerContext.stServerInfo.sockAddr, sizeof(refstServerContext.stServerInfo.sockAddr));
	if (dwRet != SOCKET_ERROR) {
		dwRet = ::WSAGetLastError();
		return E_RET_FAIL;
	}

	return E_RET_SUCCESS;
}

VOID CHelpClient::CloseSock()
{
	::WSACleanup();
}