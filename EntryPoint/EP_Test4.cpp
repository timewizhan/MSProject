/*
 * EP_Test4.cpp
 *
 *  Created on: Jan 23, 2016
 *      Author: alphahacker
 */

#include "Common.h"
#include "database.h"
#include "EP_Test4.h"
#include "mysql.h"
//#include "socket.h"

EP_Test4::EP_Test4(){

	initEntryPoint();
}

EP_Test4::~EP_Test4(){}

void EP_Test4::initEntryPoint()
{

//	m_sock.init_socket();		//socket

	m_db.initDB();				//database
	m_db.extractData();
}

