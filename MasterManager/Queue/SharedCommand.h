#ifndef _SHARED_COMMAND_
#define _SHARED_COMMAND_

#include "..\Common\Common.h"

/*
	Not use sync.
	Because Command string is only written by control thread.
	And Manager Thread can use the Command string (not write)
*/

typedef std::string ControlCommand;
void SetCommand(std::string &refstrCommand);
void GetCommand(std::string &refstrCommand);


#endif