#ifndef _DBPS_SERVER_ERROR_
#define _DBPS_SERVER_ERROR_

#include "..\Common\Common.h"

/**
* Show error message according to a number of error
*
* @param :
	DWORD : Error Type
* @return : None
* @exception : None
*/
VOID ShowErrorWSAStartup(DWORD dwErr);
VOID ShowErrorWSASocket(DWORD dwErr);
VOID ShowErrorSetSockOpt(DWORD dwErr);
VOID ShowErrorSend(DWORD dwErr);

#endif