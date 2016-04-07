#ifndef _M_HELP_CLIENT_
#define _M_HELP_CLIENT_

#include "..\Common\Common.h"

#include "HelpTool.h"
#include "HelpCommStruct.h"

class CHelpClient
{
	CHelpTool			*m_pHelpTool;

public:
	CHelpClient();

	DWORD InitClientSock(ST_CLIENT_CONTEXT &refstServerContext, ST_SERVER_ADDR &refstServerAddr);
	DWORD ConnectToServer(ST_CLIENT_CONTEXT &refstServerContext);
	VOID CloseSock();
};


#endif