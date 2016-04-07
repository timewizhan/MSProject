#ifndef _PROTOCOL_STRUCT_
#define _PROTOCOL_STRUCT_

#include "..\Common\Common.h"

struct ST_PROTO_ROOT
{
	DWORD dwAction;
};

struct ST_PROTO_BOT_COMPLETE_READY : public ST_PROTO_ROOT
{
	DWORD dwPID;
};

struct ST_PROTO_BOT_COMMAND_START : public ST_PROTO_ROOT
{
};

struct ST_PROTO_MM_COMMAND_READY : public ST_PROTO_ROOT
{
	DWORD dwNumberOfBots;
};

struct ST_PROTO_MM_COMPLETE_READY : public ST_PROTO_ROOT
{
	DWORD dwManagerNumber;
};

struct ST_PROTO_MM_COMMAND_START : public ST_PROTO_ROOT
{
};

struct ST_PROTO_MM_COMMAND_STOP : public ST_PROTO_ROOT
{
};

struct ST_PROTO_MM_KEEPALIVE : public ST_PROTO_ROOT
{
	DWORD dwManagerNumber;
};


struct ST_PROTO_INNER_COMMAND_READY : public ST_PROTO_ROOT
{
	DWORD dwNumberOfBots;
};

struct ST_PROTO_INNER_COMPLETE_READY : public ST_PROTO_ROOT
{
};

struct ST_PROTO_INNER_COMMAND_START : public ST_PROTO_ROOT
{
};

struct ST_PROTO_INNER_COMMAND_STOP : public ST_PROTO_ROOT
{
};


#endif