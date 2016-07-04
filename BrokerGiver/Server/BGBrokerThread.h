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

	/**
	* Initialize Socket
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	DWORD InitSocket();

	/**
	* Bind Socket
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	DWORD BindSocket();

	/**
	* Accept Socket
	*
	* @param : 
		ST_CLIENT_SOCKET : Client Socket
	* @return : None
	* @exception : 
		standard exception
	*/
	VOID AcceptBroker(ST_CLIENT_SOCKET &refstClientSocket) throw(std::exception);

	/**
	* Read data that broker sent
	*
	* @param : 
		ST_CLIENT_SOCKET : Client Socket
		string : Request type
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD ReadDataFromBroker(ST_CLIENT_SOCKET &refstClientSocket, std::string &refstrReqType);

	/**
	* Change request type to value
	*
	* @param : 
		string : Request type
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD ChangeReqTypeValue(std::string &refstrReqType);

	/**
	* Communicate with Broker
	*
	* @param : 
		ST_CLIENT_SOCKET : Client Socket
	* @return : None
	* @exception : 
		standard exception
	*/
	VOID CommunicateWithBroker(ST_CLIENT_SOCKET &refstClientSocket) throw(std::exception);
public:
	CBGBrokerThread();

	/**
	* Start a broker thread
	*
	* @param : None
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD StartBrokerThread();
};


#endif