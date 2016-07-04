#ifndef _AY_HELP_TOOL_
#define _AY_HELP_TOOL_

#include "..\Common\Common.h"

class CHelpTool
{
public:
	CHelpTool();
	~CHelpTool();

	/**
	* Return a current system information
	*
	* @param :
		SYSTEM_INFO : system info
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD	GetSystemInfo(SYSTEM_INFO &refstSystemInfo);

	/**
	* Return a current address
	*
	* @param :
		vector<string> : vector of address string 
	* @return : 
		DWORD : E_RET_TYPE
	* @exception : None
	*/
	DWORD	GetAddressInfo(std::vector<std::string> &refvecstrAddress);

	/**
	* Create new completion port
	*
	* @param :
		DWORD : number of threads
	* @return : 
		HANDLE : IOCP Handle
	* @exception : None
	*/
	HANDLE	CreateNewCompletionPort(DWORD dwNumberOfConcurrentThreads = 0);

	/**
	* Associate a device with completion port
	*
	* @param :
		HANDLE : Device
		HANDLE : Completion port
		DWORD : Completion key
	* @return : 
		BOOL : Result
	* @exception : None
	*/
	BOOL	AssociateDeviceWithcompletionPort(HANDLE hDevice, HANDLE hCompletionPort, DWORD dwCompletionKey);
};

#endif