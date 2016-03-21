#include "FileWrite.h"

CFileWrite::CFileWrite(){
//	FileOpen();
}

CFileWrite::~CFileWrite(){

}

void CFileWrite::FileOpen(){
	m_DRResFile.open("Data_Replacement_Result.txt");
	m_WeightResFile.open("Weight_Result.txt");
}

void CFileWrite::InitDRFile(){
	m_DRResFile << "========== Data Replacement ==========" << endl;
}

void CFileWrite::InitWeightFile(){
	m_WeightResFile << "========== Weight and Factors Value ==========" << endl;
}

void CFileWrite::InputTimeIntoDRFile(ofstream &insDRResFile){
	struct tm *t;
	time_t timer;

	timer = time(NULL);
	t = localtime(&timer);

	insDRResFile << endl;
	insDRResFile << "Data Replacement time : " << t->tm_year + 1900 << "�� " << t->tm_mon + 1 << "�� " << t->tm_mday << "�� " << t->tm_hour << "�� " << t->tm_min << "�� " << t->tm_sec << "�� " << endl;
	insDRResFile << endl;
}

void CFileWrite::InputTimeIntoWeightFile(){
	struct tm *t;
	time_t timer;

	timer = time(NULL);
	t = localtime(&timer);

	m_WeightResFile << endl;
	m_WeightResFile << "Weight time : " << t->tm_year + 1900 << "�� " << t->tm_mon + 1 << "�� " << t->tm_mday << "�� " << t->tm_hour << "�� " << t->tm_min << "�� " << t->tm_sec << "�� " << endl;
	m_WeightResFile << endl;
}

void CFileWrite::WriteDRFile(char arrInputString[], ofstream &insDRResFile){
	insDRResFile << arrInputString << " ";
}

void CFileWrite::WriteDRFile(int sInputString, ofstream &insDRResFile){
	insDRResFile << sInputString << " ";
}

void CFileWrite::WriteNewLine(ofstream &insDRResFile){
	insDRResFile << endl;
}

void CFileWrite::WriteWeightFile(string sInputString){
	m_WeightResFile << sInputString << endl;
}

void CFileWrite::FileClose(){
	m_DRResFile.close();
	m_WeightResFile.close();
}