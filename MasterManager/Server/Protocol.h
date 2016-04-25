#ifndef _PROTOCOL_
#define _PROTOCOL_

#include "..\Common\Common.h"

enum E_PROTO_BOT_ACTION
{
	E_PROTO_BOT_COMPLETE_READY = 1,
	E_PROTO_BOT_COMMAND_START,
	E_PROTO_BOT_COMPLETE_START,

	E_PROTO_BOT_UNKNOWN
};

enum E_PROTO_MM_ACTION
{
	E_PROTO_MM_COMMAND_READY = 1,
	E_PROTO_MM_COMPLETE_READY,
	E_PROTO_MM_COMPLETE_START,
	E_PROTO_MM_COMMAND_START,
	E_PROTO_MM_COMMAND_STOP,

	E_PROTO_MM_KEEPALIVE,
	E_PROTO_MM_UNKNOWN
};

enum E_PROTO_INNER_ACTION
{
	E_PROTO_INNER_COMMAND_READY = 1,
	E_PROTO_INNER_COMPLETE_READY,
	E_PROTO_INNER_COMMAND_START,
	E_PROTO_INNER_COMMAND_STOP,

	E_PROTO_INNTER_UNKNOWN
};
#endif