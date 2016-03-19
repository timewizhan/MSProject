#include "DataQueue.h"

CDataQueue* CDataQueue::m_cQueue = NULL;

CDataQueue::CDataQueue(){
	::InitializeCriticalSection(&m_stCriticalSection);
	//	m_mutex = PTHREAD_MUTEX_INITIALIZER;
}

CDataQueue::~CDataQueue(){
	::DeleteCriticalSection(&m_stCriticalSection);
}

::deque <ST_MONITORING_RESULT> CDataQueue::getQueue(){

	return m_deQueue;
}

void CDataQueue::pushDataToQueue(ST_MONITORING_RESULT data){

	::EnterCriticalSection(&m_stCriticalSection);
	m_deQueue.push_back(data);
	::LeaveCriticalSection(&m_stCriticalSection);
}

ST_MONITORING_RESULT CDataQueue::popDataFromQueue(){

	::EnterCriticalSection(&m_stCriticalSection);
	ST_MONITORING_RESULT data = m_deQueue.front();
	m_deQueue.pop_front();
	::LeaveCriticalSection(&m_stCriticalSection);

	return data;
}




