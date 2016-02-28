/*
* EP_Test4.h
*
*  Created on: Jan 23, 2016
*      Author: alphahacker
*/

#ifndef _ENTRYPOINT_
#define _ENTRYPOINT_
#include "mysql.h"
#include "database.h"
//#include "socket.h"

class EP_Test4{

public:

	//	CSocket m_sock;			//socket
	CDatabase m_db;			//database

	EP_Test4();
	~EP_Test4();

	void initEntryPoint();
};

#endif


