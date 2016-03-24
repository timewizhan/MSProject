#ifndef _BG_SPINLOCK_
#define _BG_SPINLOCK_

#include "..\Common\Common.h"

static LONG lBrokerReqType = 0;
inline VOID SetLockBrokerReqType(DWORD dwReqType)
{
	DWORD dwValue = 1;
	// if lBrokerReqType is 0, replacement is stopped
	if (lBrokerReqType == 0) {
		while (lBrokerReqType == 0) {
			lBrokerReqType = ::InterlockedIncrement(&dwValue);
			if (lBrokerReqType != 0) {
				break;
			}
			::Sleep(10);
			continue;
		}
	}
	// if lBrokerReqType is 1 or more, replacement is started
	else {
		while (lBrokerReqType == 0) {
			lBrokerReqType = ::InterlockedDecrement(&dwValue);
			if (lBrokerReqType != 1) {
				break;
			}
			::Sleep(10);
			continue;
		}
	}
}

inline DWORD GetLockBrokerReqType()
{
	return lBrokerReqType;
}

#endif