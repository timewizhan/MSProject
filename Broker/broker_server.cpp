/*
 * broker_server.cpp
 *
 *  Created on: Jan 20, 2016
 *      Author: ms-dev
 */

#include "common.h"
#include "broker_server.h"
#include "data_queue.h"

CBrokerServer::CBrokerServer(){

	init_broker();
}

CBrokerServer::~CBrokerServer(){}

void CBrokerServer::init_broker(){

	printf("init_broker \n");

	init_thread();
	bridge_socket();
}

void CBrokerServer::init_thread(){

	printf("init_thread \n");

	pthread_t thread_for_queue;
	int thr_id = pthread_create(&thread_for_queue, NULL, preprocess_insert, &m_db);
}

void CBrokerServer::bridge_socket(){

	m_db.m_socket.comm_socket();
}




