#ifndef _BG_EXCEPTION_
#define _BG_EXCEPTION_

#include "..\Common\Common.h"

class JSONException : public std::exception
{
	std::string m_strErrMsg;
public:
	JSONException();
	JSONException(std::string strErrMsg);
	JSONException(const char *pstrErrMsg);
	~JSONException();

	virtual const char * what() const override;
};

class DBException : public std::exception
{
	std::string m_strErrMsg;
public:
	DBException();
	DBException(std::string strErrMsg);
	DBException(const char *pstrErrMsg);
	~DBException();

	virtual const char * what() const override;
};


#endif