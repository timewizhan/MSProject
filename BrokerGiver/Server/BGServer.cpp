#include "BGServer.h"
#include "BGServerError.h"
#include "BGWorkerThread.h"
#include "BGBrokerThread.h"
#include "BGSpinLock.h"
#include "DBQueue.h"
#include "..\Common\Log.h"
#include "..\Common\BGLog.h"

CDBQueue* g_pCDBQueue = nullptr;

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGServer::CBGServer() :
m_pHelpTool(NULL),
m_dwAcceptCount(0)
{
	/*
		Server initializer have role only for initializing variable
	*/
	::memset(&m_stServerInit, 0x00, sizeof(m_stServerInit));
	::memset(&m_stServerIOCPData, 0x00, sizeof(m_stServerIOCPData));
	::memset(&m_stServerWorkerThreads, 0x00, sizeof(m_stServerWorkerThreads));
	
	InitializeCriticalSection(&m_CriticalSection);

	m_pHelpTool = new CHelpTool();
	if (!m_pHelpTool) {
		ErrorLog("Fail to get Help Tool");
		return;
	}

	m_stDBLoginToken.strDatabaseName = "broker2";
	m_stDBLoginToken.strDatabaseIP = "127.0.0.1";
	m_stDBLoginToken.strPort = "3306";
	m_stDBLoginToken.strUserName = "root";
	m_stDBLoginToken.strPassword = "cclab";
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CBGServer::~CBGServer()
{
	if (m_pHelpTool)
		delete m_pHelpTool;

	DeleteCriticalSection(&m_CriticalSection);
	::closesocket(m_stServerInit.hServerSock);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitServerSock(DWORD dwPort)
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

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::AcceptServer(ST_CLIENT_SOCKET &refstClientSocket)
{
	int nLengthOfClientSockIn = sizeof(refstClientSocket.stClientAddrIn);
	refstClientSocket.hClientSock = ::accept(m_stServerInit.hServerSock, (SOCKADDR *)&refstClientSocket.stClientAddrIn, &nLengthOfClientSockIn);
	if (refstClientSocket.hClientSock == INVALID_SOCKET) {
		ErrorLog("Fail to get invalid client socket");
		return E_RET_FAIL;
	}
	m_dwAcceptCount++;

	char szClientAddress[32] = { 0 };
	inet_ntop(AF_INET, (void *)&refstClientSocket.stClientAddrIn.sin_addr, (PSTR)szClientAddress, sizeof(szClientAddress));
	DebugLog("Success to connect from client : [%d]", m_dwAcceptCount);
	DebugLog("Client Socket : %d, Address : %s", refstClientSocket.hClientSock, szClientAddress);

	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CBGServer::ListenToReplacement()
{
	DWORD dwReqType;
	bool bContinue = true;
	while (bContinue)
	{
		dwReqType = GetLockBrokerReqType();
		if (dwReqType == 1) {
			DebugLog("Currently, Broker replacement is now working");
			::Sleep(1000);
			continue;
		}
		bContinue = false;
		DebugLog("Currently, Broker replacement is now stopped");
	}
	
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::CompleteReadFromClient(ST_CLIENT_SOCKET &refstClientSocket)
{
	EnterCriticalSection(&m_CriticalSection);
	m_stServerStatus.dwNumberOfCurrentSocket++;
	
	ST_SERVER_CONNECTION *pstServerConnection = NULL;
	pstServerConnection = new ST_SERVER_CONNECTION;
	/*
		if ST_SERVER_CONNECTION is not initialized, 
		WSARecv return SOCKET_ERROR and cannot be operated.
	*/
	::memset(pstServerConnection, 0x00, sizeof(ST_SERVER_CONNECTION));
	pstServerConnection->ClientSock = refstClientSocket.hClientSock;
	pstServerConnection->stClientAddrIn = refstClientSocket.stClientAddrIn;
	pstServerConnection->stWSABuf.buf = pstServerConnection->szBuf;
	pstServerConnection->stWSABuf.len = sizeof(pstServerConnection->szBuf);

	BOOL bRet;
	bRet = m_pHelpTool->AssociateDeviceWithcompletionPort((HANDLE)refstClientSocket.hClientSock, 
		m_stServerIOCPData.hCompletionPort, 
		(DWORD)pstServerConnection);
	if (!bRet) {
		ErrorLog("Fail to associate with completionPort");
		return E_RET_FAIL;
	}

	DWORD dwRecv;
	DWORD dwFlags = 0;
	int nRet;
	nRet = WSARecv(pstServerConnection->ClientSock,
		&(pstServerConnection->stWSABuf),
		1, 
		&dwRecv, 
		&dwFlags, 
		&(pstServerConnection->stOverLapped),
		NULL);
	if (nRet == SOCKET_ERROR) {
		DWORD dwRet = ::WSAGetLastError();
		if (dwRet != WSA_IO_PENDING) {
			m_stServerStatus.dwNumberOfCurrentSocket--;
			::closesocket(pstServerConnection->ClientSock);
			::free(pstServerConnection);
		}
		
		/*
			following line is async
		*/
	}
	
	LeaveCriticalSection(&m_CriticalSection);
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitIOCompletionPort(DWORD dwNumberOfConcurrentThreads)
{
	m_stServerIOCPData.hCompletionPort = m_pHelpTool->CreateNewCompletionPort(dwNumberOfConcurrentThreads);
	if (!m_stServerIOCPData.hCompletionPort) {
		DWORD dwRet = GetLastError();
		ErrorLog("Fail to make completion port");
		return E_RET_FAIL;
	}
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitWorkerThread()
{
	SYSTEM_INFO stSystemInfo;
	DWORD dwRet = E_RET_SUCCESS;
	dwRet = m_pHelpTool->GetSystemInfo(stSystemInfo);
	if (dwRet != E_RET_SUCCESS) {
		ErrorLog("Fail to get SystemInfo");
		return dwRet;
	}

	DWORD dwNumberOfThread = stSystemInfo.dwNumberOfProcessors;
	m_stServerWorkerThreads.phWorkerThread = new HANDLE[dwNumberOfThread];
	m_stServerWorkerThreads.dwNumberOfThread = dwNumberOfThread;
	DWORD i;
	for (i = 0; i < dwNumberOfThread; i++) {
		HANDLE hThread;
		m_stServerIOCPData.dwThreadNumber = i;
		hThread = (HANDLE)_beginthreadex(NULL, 0, WorkerCompletionThread, (void *)&m_stServerIOCPData, 0, NULL);
		m_stServerWorkerThreads.phWorkerThread[i] = hThread;
		DebugLog("Thread [%d] is created", i);
		
		/*
			After making one thread, wait for one minute.
			because of exception abnormally.
		*/
		::Sleep(1000);
	}
	return dwRet;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitBrokerThread()
{
	m_stServerWorkerThreads.hBrokerThread = (HANDLE)_beginthreadex(NULL, 0, WorkerBrokerThread, NULL, 0, NULL);
	DebugLog("Broker Thread is created");

	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitDBCQueue(DWORD dwNumberOfConnection)
{
	extern CDBQueue* g_pCDBQueue;
	g_pCDBQueue = (new CDBQueue())->getQueueInstance();

	for (unsigned int i = 0; i < dwNumberOfConnection; i++) {
		ST_DBConnection stDBConnection;
		HANDLE hDataBase = CreateDBInstance(E_DB_MYSQL);
		ConnectToDB(hDataBase, m_stDBLoginToken);
		stDBConnection.hDataBase = hDataBase;

		DebugLog("Create DB Connection [%d]", i + 1);
		g_pCDBQueue->pushToQueue(stDBConnection);
		::Sleep(1000);
	}
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::InitServerValue(DWORD dwPort)
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
	m_stServerInit.stServerAddrIn.sin_family = AF_INET;
	m_stServerInit.stServerAddrIn.sin_port = ::htons((unsigned short)dwPort);

	std::string strAddress = vecstrGetAddress[0].c_str();
	//std::string strAddress = "165.132.120.160";
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

///////////////////////////////////////////////////////////////////////////////////////////////////////
VOID CBGServer::InitializeServer(DWORD dwPort, DWORD dwDBQueue) throw(std::exception)
{

	DWORD dwRet = E_RET_SUCCESS;
	dwRet = InitServerSock(dwPort);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to initailize Server Sock");
	}

	/*
		Create Completion Port for opertation IOCP.
		the variable 0 mean that IOCP set value itself.
	*/
	dwRet = InitIOCompletionPort(0);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to initailize Server Sock");
	}

	/*
		Make worker threads for operating IOCP
	*/
	dwRet = InitWorkerThread();
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to initailize Server Sock");
	}

	dwRet = InitBrokerThread();
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to initailize Broker Sock");
	}

	dwRet = InitServerValue(dwPort);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to configure server value");
	}

	dwRet = InitDBCQueue(dwDBQueue);
	if (dwRet != E_RET_SUCCESS) {
		throw std::exception("Fail to initialize DB Queue");
	}
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD CBGServer::StartServer(DWORD dwPort, DWORD dwDBQueue)
{
	DWORD dwRet = E_RET_SUCCESS;
	
	try
	{
		InitializeServer(dwPort, dwDBQueue);

		BOOL m_bStartServer = TRUE;
		while (m_bStartServer)
		{
			ListenToReplacement();

			ST_CLIENT_SOCKET stClientSocket;
			dwRet = AcceptServer(stClientSocket);
			if (dwRet != E_RET_SUCCESS) {
				continue;
			}

			dwRet = CompleteReadFromClient(stClientSocket);
			if (dwRet != E_RET_SUCCESS) {
				continue;
			}
		}
	}
	catch (std::exception &e)
	{
		/*
			Abnormally Exception
		*/
		ErrorLog("%s", e.what());
		return E_RET_FAIL;
	}

	/*
		All Thread is waiting for stopping their operation
	*/
	WaitForMultipleObjects(m_stServerWorkerThreads.dwNumberOfThread, m_stServerWorkerThreads.phWorkerThread, TRUE, INFINITE);
	WaitForSingleObject(m_stServerWorkerThreads.hBrokerThread, INFINITE);
	
	return dwRet;
}

/*
	Worker broker thread is used for receving data from broker
*/
///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerBrokerThread(void *pData)
{
	CBGBrokerThread *pBGBrokerThread;
	pBGBrokerThread = new CBGBrokerThread();

	try
	{
		pBGBrokerThread->StartBrokerThread();
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}
	return E_RET_SUCCESS;
}

/*
	Worker thread is operating regardless of main thread
*/
///////////////////////////////////////////////////////////////////////////////////////////////////////
unsigned int WINAPI WorkerCompletionThread(void *pIOCPData)
{
	ST_SERVER_IOCP_DATA *pstServerIOCPData = (ST_SERVER_IOCP_DATA *)pIOCPData;
	HANDLE hCompletionPort = pstServerIOCPData->hCompletionPort;
	ST_SERVER_CONNECTION *pstServerConnection = NULL, *pstKey = NULL;
	DWORD dwByteTransferred = 0;
	
	CBGLog BGLog(pstServerIOCPData->dwThreadNumber);
	CBGWorkerThread *pWorkerThread = new (std::nothrow) CBGWorkerThread();
	if (!pWorkerThread) {
		ErrorLog("[WorkerThread] Fail to allocate memory of thread");
		return E_RET_FAIL;
	}

	BOOL bThreadStart = TRUE;
	while (bThreadStart) {
		::GetQueuedCompletionStatus(hCompletionPort,
			&dwByteTransferred,
			(LPDWORD)&pstKey,
			(LPOVERLAPPED*)&pstServerConnection,
			INFINITE);

		if (dwByteTransferred == 0) {
			closesocket(pstServerConnection->ClientSock);
			::free(pstServerConnection);
			continue;
		}

		DWORD dwRet;
		DebugLog("Thread received data from client");
		pWorkerThread->SetClientSocket(pstServerConnection->ClientSock);
		dwRet = pWorkerThread->StartWorkerThread(pstServerConnection->szBuf, &BGLog);
		if (dwRet != E_RET_SUCCESS) {
			ErrorLog("Fail to operate StartWorkerThread");
			continue;
		}
		
		delete pstServerConnection;
		DebugLog("Success to operate StartWorkerThread");
	}

	delete pWorkerThread;
	return E_RET_SUCCESS;
}
