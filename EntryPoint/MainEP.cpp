/*
 * MainEP.cpp
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#include "Common.h"
#include "EP_Test4.h"

int main()
{
	EP_Test4 et;
	et.initEntryPoint();
	et.sendMessage();
}


