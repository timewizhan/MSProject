#include <Windows.h>
#include <stdio.h>
#include <string>
#include <vector>

typedef std::string BasicFileName;
std::vector<BasicFileName> vecBasicFileName;

enum E_ANAL_TYPE
{
	E_ANAL_DBCP = 1,
	E_ANAL_BROKER,
	E_ANAL_SNS,

	E_ANAL_UNKNOWN
};

bool IsFile(std::string &refstrFilePath)
{
	DWORD dwRet = GetFileAttributesA(refstrFilePath.c_str());
	if (dwRet == INVALID_FILE_ATTRIBUTES)
		return false;

	if (dwRet & FILE_ATTRIBUTE_ARCHIVE)
		return true;

	return false;
}

bool IsDirectory(std::string &refstrLogPath)
{
	DWORD dwRet = GetFileAttributesA(refstrLogPath.c_str());
	if (dwRet == INVALID_FILE_ATTRIBUTES) 
		return false;
	
	if (dwRet & FILE_ATTRIBUTE_DIRECTORY) 
		return true;   

	return false;    
}

DWORD GetFileListInDIR(std::string &refstrLogPath)
{
	if (!IsDirectory(refstrLogPath)) {
		return 0;
	}

	WIN32_FIND_DATAA FindFileData;
	HANDLE hFind = INVALID_HANDLE_VALUE;

	refstrLogPath += "\\*";

	hFind = FindFirstFileA(refstrLogPath.c_str(), &FindFileData);
	if (hFind == INVALID_HANDLE_VALUE) {
		printf("Invalid file handle. Error is %u\n", GetLastError());
		return 0;
	}

	do
	{
		BasicFileName strBasicFileName = FindFileData.cFileName;
		if (strBasicFileName.compare(".") == 0 || strBasicFileName.compare("..") == 0)
			continue;

		vecBasicFileName.push_back(strBasicFileName);
	}
	while (FindNextFileA(hFind, &FindFileData) != 0);

	DWORD dwError;
	dwError = GetLastError();
	FindClose(hFind);
	if (dwError != ERROR_NO_MORE_FILES) {
		printf("FindNextFile error. Error is %u\n", dwError);
		return 0;
	}

	return vecBasicFileName.size();
}


DWORD AnalyseOneFile(std::string &refstrFullFilePath, std::string &refstrAnalysis, std::string &refstrTotalResultPath, FILE *pResultFile) {
	FILE *pFile = NULL;
	errno_t err;

	err = ::fopen_s(&pFile, refstrFullFilePath.c_str(), "r");
	if (err != 0) {
		return 0;
	}

	char szBuf[512] = { 0 };
	while (!::feof(pFile)) {
		::fgets(szBuf, sizeof(szBuf), pFile);
		std::string strBuf = szBuf;

		DWORD dwPos = strBuf.find(refstrAnalysis);
		if (dwPos == std::string::npos) {
			::memset(szBuf, 0x00, sizeof(szBuf));
			continue;
		}
		::fputs(szBuf, pResultFile);
		::memset(szBuf, 0x00, sizeof(szBuf));
	}
	::fclose(pFile);

	return 1;
}

DWORD AnalyseFiles(std::string &refstrLogPath, std::string &refstrResultPath, int nType) {
	std::string strAnalysis;

	if (nType == E_ANAL_DBCP) {
		strAnalysis = "DBPOOLSERVER";
	}
	else if (nType == E_ANAL_BROKER) {
		strAnalysis = "BROKER";
	}
	else if (nType == E_ANAL_SNS) {
		strAnalysis = "ENTRYPOINT";
	}
	else {
		printf("Invalid Type");
		return 0;
	}

	std::string strResultName = "Result.txt";
	std::string strTotalResultPath = refstrResultPath + "\\" + strResultName;
	
	bool bExist = IsFile(strTotalResultPath);
	if (bExist) {
		::DeleteFileA(strTotalResultPath.c_str());
	}
	
	FILE *pFile = NULL;
	errno_t err;
	err = ::fopen_s(&pFile, strTotalResultPath.c_str(), "a+");
	if (err != 0) {
		return 0;
	}

	DWORD dwRet;
	DWORD dwTotalFiles = vecBasicFileName.size();
	for (unsigned int i = 0; i < dwTotalFiles; i++) {
		printf("[%d/%d] file is analysing ... \n", i + 1, dwTotalFiles);

		std::string strFullFilePath = refstrLogPath + "\\" + vecBasicFileName[i];
		dwRet = AnalyseOneFile(strFullFilePath, strAnalysis, strTotalResultPath, pFile);
		if (dwRet < 1)
			continue;

	}
	::fclose(pFile);
	return 1;
}


void Usage() {
	printf("[Usage] :	[Log Path]	[Result Path]	[Analyis Type]");
	printf("										DBPool Type	:  1");
	printf("										Broker Type	:  2");
	printf("										SNS Type	:  3");
}

int main(int argc, char **argv)
{
	if (argc < 4) {
		Usage();
		return 0;
	}

	std::string strBasicLogPath = argv[1];
	std::string strResultPath = argv[2];
	DWORD dwType = static_cast<DWORD>(::atoi(argv[3]));
	if (dwType > 3) {
		Usage();
		return 0;
	}

	printf("========================================== \n");
	printf("============ Start to analyze ============ \n");
	printf("========================================== \n");

	DWORD dwTotalCount;
	std::string strLogPath = strBasicLogPath;
	dwTotalCount = GetFileListInDIR(strLogPath);
	if (dwTotalCount < 1) {
		printf("========================================== \n");
		printf("============= Fail to analyze ============ \n");
		printf("========================================== \n");

		return 0;
	}
	printf("Total File Count : %d\n", dwTotalCount);

	AnalyseFiles(strBasicLogPath, strResultPath, dwType);

	printf("========================================== \n");
	printf("============== End analyze ============== \n");
	printf("========================================== \n");

	return 0;
}