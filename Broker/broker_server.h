/*
 * main_broker.h
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#ifndef _BROKER_
#define _BROKER_

#include "common.h"
#include "broker_server.h"
#include "database.h"

unsigned WINAPI preprocess_insert(void *data);

class CBrokerServer{

public:
		CDatabase	 m_db;

		CBrokerServer();
		~CBrokerServer();

		void init_broker();
		void init_thread();
		void bridge_socket();

};

#endif

