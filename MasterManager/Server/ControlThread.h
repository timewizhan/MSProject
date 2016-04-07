#ifndef _MM_CONTROL_THREAD_
#define _MM_CONTROL_THREAD_

#include "..\Common\Common.h"

class CMMControlThread
{
	VOID InputCommand();
	VOID Usage();

	DWORD CheckInputData(std::string &refstrInputData, DWORD dwType);
public:
	CMMControlThread();
	~CMMControlThread();

	DWORD StartThread();
};


#endif