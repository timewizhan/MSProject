#ifndef _COMMON_
#define _COMMON_

#define FD_SETSIZE      1024

#pragma comment(lib, "ws2_32.lib")

#include <WinSock2.h>
#include <WS2tcpip.h>
#include <Windows.h>

#include <TlHelp32.h>
#include <tchar.h>
#include <process.h>

#include <direct.h>

#include <string>
#include <vector>
#include <map>

/*
	inner unitest
*/

#include <assert.h>

/*
	User defined
*/

#include "Type.h"
#include "Log.h"


#endif