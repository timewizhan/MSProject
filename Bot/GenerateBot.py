from multiprocessing import Process
import os, sys, time

####################################
def getCurrentDir():
	return os.getcwd()

def setEnvPath(path):
	path += "\\src"
	sys.path.append(path)
####################################

BOT_FILE="Bot.py"

def findBotFile():
	global BOT_FILE

	currentPath = os.getcwd()
	botFilePath = currentPath + "\\" + BOT_FILE

	return os.path.exists(botFilePath)

def getUsersInfoFromDB():
	dataBase = PyDatabase()
	dataBase.connectToDB()

	sql = "SELECT \"userName\", \"userPlace\" FROM public.\"completeUserid\""
	usersInfoList = dataBase.querySQL(sql)

	dataBase.disconnectFromDB()

	return usersInfoList

def fn_process(*argv):
	command_argv_name	= argv[0]
	command_argv_place	= argv[1]
	
	currentPath = os.getcwd()
	botFilePath = currentPath + "\\" + BOT_FILE

	completedCommand = botFilePath + " " + command_argv_name + " " + command_argv_place
	os.system(completedCommand)

def operateMultiProcess(usersInfoList):
	procList = []

	for i in range(0, len(usersInfoList)):
		procList.append(Process(target = fn_process, args = (usersInfoList[i][0], usersInfoList[i][1],)))

	procNumber = 0
	for eachBot in procList:
		eachBot.start()
		print "[Debug] Process [%d] : [%s] is started" % (procNumber + 1, usersInfoList[procNumber][0])
		procNumber += 1
		time.sleep(1)

	for eachBot in procList:
		eachBot.join()
	 

def mainStart():
	if not findBotFile():
		print "[Error] Can't find botFile"
		sys.exit(1)

	try:
		usersInfoList = getUsersInfoFromDB()
		if len(usersInfoList) < 1:
			print "[Error] There is no user data"
			sys.exit(1)

		operateMultiProcess(usersInfoList)
	except Exception as e:
		print "[Error] Abnormally exit"
		sys.exit(1)


if __name__ == "__main__":
	currentPath = getCurrentDir()
	setEnvPath(currentPath)
	from DataBase import *

	print "**************************************"
	print "*************Generate Bot*************"
	print "**************************************"

	mainStart()

	print "**************************************"
	print "****************Finish****************"
	print "**************************************"