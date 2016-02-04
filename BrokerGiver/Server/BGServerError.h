#ifndef _AY_SERVER_ERROR_
#define _AY_SERVER_ERROR_

#include "..\Common\Common.h"

VOID ShowErrorWSAStartup(DWORD dwErr);
VOID ShowErrorWSASocket(DWORD dwErr);
VOID ShowErrorSetSockOpt(DWORD dwErr);
VOID ShowErrorSend(DWORD dwErr);

#endif