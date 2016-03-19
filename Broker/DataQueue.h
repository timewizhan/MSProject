#ifndef _QUEUE_
#define _QUEUE_

#include "SocketStruct.h"
#include "Common.h"

class CDataQueue{
private:
	static CDataQueue*					m_cQueue;
	deque <ST_MONITORING_RESULT>		m_deQueue;
//	queue <ST_MONITORING_RESULT>		m_deQueue;
	CRITICAL_SECTION					m_stCriticalSection;

	CDataQueue();
	~CDataQueue();

public:
	static CDataQueue* getDataQueue(){
		if (m_cQueue == NULL){
			m_cQueue = new CDataQueue();
		}

		return m_cQueue;
	}

	deque <ST_MONITORING_RESULT> getQueue();
	void pushDataToQueue(ST_MONITORING_RESULT data);
	ST_MONITORING_RESULT popDataFromQueue();

};


#endif


