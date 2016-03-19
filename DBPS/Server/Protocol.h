#ifndef _PROTOCOL_
#define _PROTOCOL_

#include "..\Common\Common.h"

enum E_OP_TYPE
{
	E_OP_WRITE = 1,
	E_OP_READ,
	E_OP_REPLY,
	E_OP_SHARE,

	E_OP_UNKNOWN
};

struct ST_CLIENT_REQ
{
	int iType;
	std::string strSrc;
	std::string strDst;

	ST_CLIENT_REQ() : iType(E_OP_UNKNOWN) {}
};

struct ST_CLIENT_RES
{
	std::string strIPAddress;
};

#endif