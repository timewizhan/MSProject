#ifndef _M_HELP_SERVER_
#define _M_HELP_SERVER_

#include "..\Common\Common.h"

#include "HelpTool.h"
#include "HelpCommStruct.h"

class CHelpServer
{
	CHelpTool			*m_pHelpTool;

public:
	CHelpServer();

	DWORD InitServerSock(ST_SERVER_CONTEXT &refstServerContext);

	/*
		InitServerValue method have internel method
		neccesary method (bind, listen)
	*/
	DWORD InitServerBind(ST_SERVER_CONTEXT &refstServerContext, DWORD dwPort);
	DWORD AcceptServer(ST_SERVER_CONTEXT &refstServerContext, ST_CLIENT_SOCKET &refstClientSock);
};


#endif