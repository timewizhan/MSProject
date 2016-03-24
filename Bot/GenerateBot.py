from multiprocessing import Process
import os, sys, time
import pdb
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

def getUsersInfoFromDB(botNumber):
	#pdb.set_trace()
	sql = "SELECT \"userName\", \"userPlace\" FROM public.\"completeUserid\" WHERE \"classifier\"=" + botNumber

	DBPSServer = DBPoolServer()
	recvFromServer = DBPSServer.startNetworkingWithData(sql)
	del DBPSServer

	usersInfoList = []
	userInfoList = recvFromServer.split()
	for i in range(0, len(userInfoList)):
		userInfo = userInfoList[i].split('=')
		usersInfoList.append(userInfo)

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
		if procNumber > 500:
			break
		time.sleep(1)

	for eachBot in procList:
		eachBot.join()
	 

def mainStart(botNumber):
	if not findBotFile():
		print "[Error] Can't find botFile"
		sys.exit(1)

	try:
		usersInfoList = getUsersInfoFromDB(botNumber)
		if len(usersInfoList) < 1:
			print "[Error] There is no user data"
			sys.exit(1)

		operateMultiProcess(usersInfoList)
	except Exception as e:
		print "[Error] Abnormally exit"
		sys.exit(1)


if __name__ == "__main__":
	if len(sys.argv) < 2:
		print "Usage : [Bot Number]"
		sys.exit(1)

	currentPath = getCurrentDir()
	setEnvPath(currentPath)
	#from DataBase import *
	from Network import *

	print "**************************************"
	print "*************Generate Bot*************"
	print "**************************************"

	botNumber = sys.argv[1]
	mainStart(botNumber)

	print "**************************************"
	print "****************Finish****************"
	print "**************************************"