#ifndef _AY_HELP_TOOL_
#define _AY_HELP_TOOL_

#include "..\Common\Common.h"

class CHelpTool
{
public:
	CHelpTool();
	~CHelpTool();

	DWORD	GetSystemInfo(SYSTEM_INFO &refstSystemInfo);
	DWORD	GetAddressInfo(std::vector<std::string> &refvecstrAddress);
	HANDLE	CreateNewCompletionPort(DWORD dwNumberOfConcurrentThreads = 0);
	BOOL	AssociateDeviceWithcompletionPort(HANDLE hDevice, HANDLE hCompletionPort, DWORD dwCompletionKey);
};

#endif