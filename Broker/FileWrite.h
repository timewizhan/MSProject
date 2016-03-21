#ifndef _BROKERFILEWRITE_
#define _BROKERFILEWRITE_

#define _CRT_SECURE_NO_WARNINGS

#include "Common.h"

class CFileWrite {

public:
	ofstream m_DRResFile;
	ofstream m_WeightResFile;

	CFileWrite();
	~CFileWrite();

	void FileOpen();
	void InitDRFile();
	void InitWeightFile();
	void InputTimeIntoDRFile(ofstream &insDRResFile);
	void InputTimeIntoWeightFile();
	void WriteDRFile(char arrInputString[], ofstream &insDRResFile);
	void WriteDRFile(int sInputString, ofstream &insDRResFile);
	void WriteNewLine(ofstream &insDRResFile);
	void WriteWeightFile(string sInputString);
	void FileClose();
};

#endif