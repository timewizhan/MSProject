#include "DBQueue.h"
#include "..\Common\Log.h"

/////////////////////////////////////////////////////////////////////////////////////////////////////
CDBQueue::CDBQueue()
{
	InitializeCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
CDBQueue::~CDBQueue()
{
	DeleteCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBQueue::pushToQueue(const ST_DBConnection &refstDBConnection)
{
	EnterCriticalSection(&m_CriticalSection);

	try
	{
		m_queueDBConnection.push(refstDBConnection);
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}


	LeaveCriticalSection(&m_CriticalSection);
}

///////////////////////////////////////////////////////////////////////////////////////////////////////
void CDBQueue::popFromQueue(ST_DBConnection &refstDBConnection)
{
	EnterCriticalSection(&m_CriticalSection);

	try
	{
		ST_DBConnection stDBConnection;
		if (m_queueDBConnection.size() != 0) {
			stDBConnection = m_queueDBConnection.front();
			m_queueDBConnection.pop();
		}
		refstDBConnection = stDBConnection;
	}
	catch (std::exception &e) {
		ErrorLog("%s", e.what());
	}

	LeaveCriticalSection(&m_CriticalSection);
}