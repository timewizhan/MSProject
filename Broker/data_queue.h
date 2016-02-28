/*
 * data_queue.h
 *
 *  Created on: Feb 2, 2016
 *      Author: ms-dev
 */

#ifndef _QUEUE_
#define _QUEUE_

//#include "data_queue.h"
#include "common.h"
#include "socket.h"

class CDataQueue{
private:
	static CDataQueue*				m_pQueue;
	::deque <monitoring_result>		m_queue;
	CRITICAL_SECTION				m_stCriticalSection;

	CDataQueue();
	~CDataQueue();

public:

	static CDataQueue* getDataQueue(){
		if(m_pQueue == NULL){
			m_pQueue = new CDataQueue();
		}

		return m_pQueue;
	}

	::deque <monitoring_result> getQueue();
	void pushDataToQueue(monitoring_result data);
	monitoring_result popDataFromQueue();

};

//void* preprocess_insert(void *data);

#endif


