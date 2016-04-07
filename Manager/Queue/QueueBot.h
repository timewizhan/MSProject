#ifndef _QUEUE_BOT_
#define _QUEUE_BOT_

#include "..\Common\Common.h"
#include "..\Server\MServerStruct.h"

template <typename T>
class CQueueBot;

template <>

class CQueueBot<ST_BOT_CONNECTION>
{
	typedef std::vector<ST_BOT_CONNECTION> VecBotConnection;
	VecBotConnection m_VecBotConnection;

	static CQueueBot<ST_BOT_CONNECTION> *m_QueueBot;
	CQueueBot(){}
public:

	static CQueueBot& getInstance() {
		if (!m_QueueBot) {
			m_QueueBot = new CQueueBot();
		}
		return *m_QueueBot;
	}

	void pushQueueConnection(ST_BOT_CONNECTION &refstBotConnection) {
		m_VecBotConnection.push_back(refstBotConnection);
	}

	void popQueueConnection(ST_BOT_CONNECTION &refstBotConnection) {
		if (m_VecBotConnection.size() < 1) {
			return;
		}

		ST_BOT_CONNECTION stBotConnection;
		stBotConnection = m_VecBotConnection.front();
		m_VecBotConnection.pop_back();

		refstBotConnection = stBotConnection;
	}

	DWORD getQueueSize() {
		return m_VecBotConnection.size();
	}
};

#endif 