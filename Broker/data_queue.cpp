/*
 * data_queue.cpp
 *
 *  Created on: Feb 2, 2016
 *      Author: ms-dev
 */

#include "common.h"
#include "broker_server.h"
#include "data_queue.h"

CDataQueue* CDataQueue::m_pQueue = NULL;

CDataQueue::CDataQueue(){

	m_mutex = PTHREAD_MUTEX_INITIALIZER;
}

CDataQueue::~CDataQueue(){

}

/*static CDataQueue* CDataQueue::getDataQueue(){
	if(m_pQueue == NULL){
		m_pQueue = new CDataQueue();
	}

	return m_pQueue;
}*/

queue <monitoring_result> CDataQueue::getQueue(){

	return m_queue;
}

void CDataQueue::pushDataToQueue(monitoring_result data){

	pthread_mutex_lock(&m_mutex);
	m_queue.push(data);
	pthread_mutex_unlock(&m_mutex);
}

monitoring_result CDataQueue::popDataFromQueue(){

	pthread_mutex_lock(&m_mutex);
	monitoring_result data = m_queue.front();
	m_queue.pop();
	pthread_mutex_unlock(&m_mutex);

	return data;
}




