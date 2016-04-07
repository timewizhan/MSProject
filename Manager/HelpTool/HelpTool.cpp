#include "HelpTool.h"

///////////////////////////////////////////////////////////////////////////////////////////////////////
CHelpTool::CHelpTool()
{
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CHelpTool::~CHelpTool()
{
}


///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD	CHelpTool::GetSystemInfo(SYSTEM_INFO &refstSystemInfo)
{
	::GetSystemInfo(&refstSystemInfo);
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
HANDLE	CHelpTool::CreateNewCompletionPort(DWORD dwNumberOfConcurrentThreads)
{
	/*
		if dwNumberOfConcurrentThread is 0, IOCompletion Port set number of max thread to operate concurrently itselt on your machine
	*/
	return ::CreateIoCompletionPort(INVALID_HANDLE_VALUE, NULL, 0, dwNumberOfConcurrentThreads);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
BOOL	CHelpTool::AssociateDeviceWithcompletionPort(HANDLE hDevice, HANDLE hCompletionPort, DWORD dwCompletionKey)
{
	HANDLE h = ::CreateIoCompletionPort(hDevice, hCompletionPort, dwCompletionKey, 0);
	return (h == hCompletionPort);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD	CHelpTool::GetAddressInfo(std::vector<std::string> &refvecstrAddress)
{
	char cHostName[256];
	int iRet;
	iRet = ::gethostname(cHostName, sizeof(cHostName));
	if (iRet == SOCKET_ERROR)
		return E_RET_FAIL;

	struct addrinfo hints, *res = NULL;
	::ZeroMemory(&hints, sizeof(hints));
	hints.ai_socktype		= SOCK_STREAM;
	hints.ai_family			= AF_INET;
	hints.ai_protocol		= IPPROTO_TCP;
	iRet = ::getaddrinfo(cHostName, "80", &hints, &res);
	if (iRet != 0)
		return E_RET_FAIL;

	struct addrinfo *pstAddrInfoNext = res;
	while (pstAddrInfoNext != NULL)
	{
		struct in_addr addr;
		addr.S_un = ((struct sockaddr_in *)(res->ai_addr))->sin_addr.S_un;

		char szBuf[128];
		inet_ntop(AF_INET, &addr.S_un, szBuf, sizeof(szBuf));
		std::string strAddress = szBuf;
		refvecstrAddress.push_back(strAddress);
		pstAddrInfoNext = pstAddrInfoNext->ai_next;
	}


	if (!refvecstrAddress.size())
		return E_RET_FAIL;
	
	::freeaddrinfo(res);
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD	CHelpTool::GetAllProcessID(VecPID &refVecPID)
{
	HANDLE hProcess;
	PROCESSENTRY32 pe32 = { 0 };

	hProcess = ::CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);
	pe32.dwSize = sizeof(PROCESSENTRY32);

	if (!Process32First(hProcess, &pe32)) {
		::CloseHandle(hProcess);
		return E_RET_FAIL;
	}

	do
	{
		refVecPID.push_back(pe32.th32ProcessID);
	} while (Process32Next(hProcess, &pe32));

	::CloseHandle(hProcess);
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD	CHelpTool::TerminateProcessByPID(DWORD dwPID)
{
	DWORD dwDesiredAccess = PROCESS_TERMINATE;
	BOOL  bInheritHandle = FALSE;
	HANDLE hProcess = ::OpenProcess(dwDesiredAccess, bInheritHandle, dwPID);
	if (hProcess == NULL) {
		return E_RET_FAIL;
	}

	UINT uExitCode = 1;
	BOOL result = ::TerminateProcess(hProcess, uExitCode);

	::CloseHandle(hProcess);
	return E_RET_SUCCESS;
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
DWORD	CHelpTool::TerminateProcessByALLPID(VecPID &refVecPID)
{
	for (unsigned i = 0; refVecPID.size(); i++) {
		TerminateProcessByPID(refVecPID[i]);
		::Sleep(500);
	}

	return E_RET_SUCCESS;
}

