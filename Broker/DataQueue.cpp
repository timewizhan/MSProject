#include "DataQueue.h"

CDataQueue* CDataQueue::m_cQueue = NULL;

CDataQueue::CDataQueue(){
	::InitializeCriticalSection(&m_stCriticalSection);
	//	m_mutex = PTHREAD_MUTEX_INITIALIZER;
}

CDataQueue::~CDataQueue(){
	::DeleteCriticalSection(&m_stCriticalSection);
	
}

::queue <ST_MONITORING_RESULT> CDataQueue::getQueue(){

	return m_deQueue;
} 

void CDataQueue::pushDataToQueue(ST_MONITORING_RESULT data){

//	printf("CDataQueue Push");
	::EnterCriticalSection(&m_stCriticalSection);
	m_deQueue.push(data);
//	printf("queue size : %d \n", m_deQueue.size());
	::LeaveCriticalSection(&m_stCriticalSection);
//	printf("~ CDataQuueue Push");
}

ST_MONITORING_RESULT CDataQueue::popDataFromQueue(){

//	printf("CDataQueue pop");
	::EnterCriticalSection(&m_stCriticalSection);
	ST_MONITORING_RESULT data = m_deQueue.front();
	m_deQueue.pop();
	::LeaveCriticalSection(&m_stCriticalSection);
//	printf("~ CDataQueue pop");

	return data;
}




