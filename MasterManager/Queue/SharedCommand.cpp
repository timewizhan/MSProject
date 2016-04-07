#include "SharedCommand.h"

static ControlCommand g_ControlCommand;

void SetCommand(std::string &refstrCommand)
{
	g_ControlCommand = refstrCommand;
}

void GetCommand(std::string &refstrCommand)
{
	refstrCommand = g_ControlCommand;
}
