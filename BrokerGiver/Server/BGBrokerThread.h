#ifndef _BG_BROKER_THREAD_
#define _BG_BROKER_THREAD_

#include "..\Common\Common.h"
#include "BGServerStruct.h"
#include "BGServerError.h"
#include "BGSpinLock.h"

#define MAX_CLIENT_MSG_BUF 1024
struct ST_BROKER_CONNECTION : public OVERLAPPED
{
	WSABUF	stWSABuf;
	char	szBuf[MAX_CLIENT_MSG_BUF];
};

class CBGBrokerThread
{
	ST_SERVER_INIT m_stServerInit;

	DWORD InitSocket();
	DWORD BindSocket();
	VOID AcceptBroker(ST_CLIENT_SOCKET &refstClientSocket) throw(std::exception);

	DWORD ReadDataFromBroker(ST_CLIENT_SOCKET &refstClientSocket, std::string &refstrReqType);
	DWORD ChangeReqTypeValue(std::string &refstrReqType);
	VOID CommunicateWithBroker(ST_CLIENT_SOCKET &refstClientSocket) throw(std::exception);
public:
	CBGBrokerThread();

	DWORD StartBrokerThread();
};


#endif