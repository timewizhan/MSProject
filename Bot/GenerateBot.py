from multiprocessing import Process
from DataBase import *
import os, sys, time


BOT_FILE="Bot.py"

def findBotFile():
	global BOT_FILE

	currentPath = os.getcwd()
	botFilePath = currentPath + "\\" + BOT_FILE

	return os.path.exists(botFilePath)

def getUsersFromDB():
	dataBase = PyDatabase()
	dataBase.connectToDB()
	
	# TODO : find a table to get user's data
	sql = "SELECT userName from completeUserid"
	userList = self.dataBase.querySQL(sql)
	
	dataBase.disconnectFromDB()

	return userList

def fn_process(*argv):
	command_argv = argv[0]
	
	currentPath = os.getcwd()
	botFilePath = currentPath + "\\" + BOT_FILE

	completedCommand = botFilePath + " " + command_argv
	execfile(completedCommand)

def operateMultiProcess(userList):
	procList = []

	for i in range(0, len(userList)):
		procList.append(Process(target = fn_process, args = (userList[i], )))

	for eachBot in procList:
		eachBot.start()
		time.sleep(1)

	for eachBot in procList:
		eachBot.join()
	 

def mainStart():
	if not findBotFile():
		print "[Error] Can't find botFile"
		sys.exit(1)

	try:
		userList = getUsersFromDB()
		if len(userList) < 1:
			print "[Error] There is no user data"
			sys.exit(1)

		operateMultiProcess(userList)
	except Exception as e:
		print "[Error] Abnormally exit"
		sys.exit(1)


if __name__ == "__main__":
	print "**************************************"
	print "*************Generate Bot*************"
	print "**************************************"

	mainStart()

	print "**************************************"
	print "****************Finish****************"
	print "**************************************"