/*
 * data_queue.cpp
 *
 *  Created on: Feb 2, 2016
 *      Author: ms-dev
 */

#include "data_queue.h"
#include "common.h"
#include "broker_server.h"


CDataQueue* CDataQueue::m_pQueue = NULL;

CDataQueue::CDataQueue(){
	::InitializeCriticalSection(&m_stCriticalSection);
//	m_mutex = PTHREAD_MUTEX_INITIALIZER;
}

CDataQueue::~CDataQueue(){
	::DeleteCriticalSection(&m_stCriticalSection);
}

/*static CDataQueue* CDataQueue::getDataQueue(){
	if(m_pQueue == NULL){
		m_pQueue = new CDataQueue();
	}

	return m_pQueue;
}*/

::deque <monitoring_result> CDataQueue::getQueue(){

	return m_queue;
}

void CDataQueue::pushDataToQueue(monitoring_result data){

	::EnterCriticalSection(&m_stCriticalSection);
	m_queue.push_back(data);
	::LeaveCriticalSection(&m_stCriticalSection);

}	

monitoring_result CDataQueue::popDataFromQueue(){

	::EnterCriticalSection(&m_stCriticalSection);
	monitoring_result data = m_queue.front();
	m_queue.pop_front();
	::LeaveCriticalSection(&m_stCriticalSection);

	return data;
}




