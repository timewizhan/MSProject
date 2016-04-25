#include "BGException.h"

JSONException::JSONException() {}
JSONException::JSONException(std::string strErrMsg) 
{
	m_strErrMsg = strErrMsg;
}
JSONException::JSONException(const char *pstrErrMsg) 
{
	m_strErrMsg = pstrErrMsg;
}
JSONException::~JSONException() 
{
}

const char * JSONException::what() const
{
	return m_strErrMsg.c_str();
}


DBException::DBException() {}
DBException::DBException(std::string strErrMsg) 
{
	m_strErrMsg = strErrMsg;
}

DBException::DBException(const char *pstrErrMsg) 
{
	m_strErrMsg = pstrErrMsg;
}

DBException::~DBException() 
{
}

const char * DBException::what() const
{
	return m_strErrMsg.c_str();
}
