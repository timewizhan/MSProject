/*
 * data_queue.h
 *
 *  Created on: Feb 2, 2016
 *      Author: ms-dev
 */

#ifndef _QUEUE_
#define _QUEUE_

#include "common.h"

class CDataQueue{
private:
	static CDataQueue* m_pQueue;
	queue <monitoring_result> m_queue;
	pthread_mutex_t m_mutex;

	CDataQueue();
	~CDataQueue();

public:

	static CDataQueue* getDataQueue(){
		if(m_pQueue == NULL){
			m_pQueue = new CDataQueue();
		}

		return m_pQueue;
	}

	queue <monitoring_result> getQueue();
	void pushDataToQueue(monitoring_result data);
	monitoring_result popDataFromQueue();

};

#endif


