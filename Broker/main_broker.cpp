/*
 * main_broker.cpp
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#include "common.h"
#include "broker_server.h"

int main(){

	CBrokerServer server;
	server.initThread();

	return 0;
}
