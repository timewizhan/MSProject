#ifndef _BG_EXCEPTION_
#define _BG_EXCEPTION_

#include "..\Common\Common.h"

class JSONException : public std::exception
{
	std::string m_strErrMsg;
public:
	/**
	* Default Constructor
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	JSONException();

	/**
	* Default Constructor
	*
	* @param : 
		string : Error message
	* @return : None
	* @exception : None
	*/
	JSONException(std::string strErrMsg);

	/**
	* Default Constructor
	*
	* @param : 
		const char : Error message
	* @return : None
	* @exception : None
	*/
	JSONException(const char *pstrErrMsg);

	/**
	* Default Destructor
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	~JSONException();

	virtual const char * what() const override;
};

class DBException : public std::exception
{
	std::string m_strErrMsg;
public:
	/**
	* Default Constructor
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	DBException();

	/**
	* Default Constructor
	*
	* @param :
	string : Error message
	* @return : None
	* @exception : None
	*/
	DBException(std::string strErrMsg);

	/**
	* Default Constructor
	*
	* @param :
	const char : Error message
	* @return : None
	* @exception : None
	*/
	DBException(const char *pstrErrMsg);

	/**
	* Default Destructor
	*
	* @param : None
	* @return : None
	* @exception : None
	*/
	~DBException();

	virtual const char * what() const override;
};


#endif