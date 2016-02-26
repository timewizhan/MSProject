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

def getUsersFromDB():
	dataBase = PyDatabase()
	dataBase.connectToDB()
	
	# TODO : find a table to get user's data
	sql = "SELECT \"userName\" FROM public.\"completeUserid\""
	userList = dataBase.querySQL(sql)
	
	dataBase.disconnectFromDB()

	return userList

def fn_process(*argv):
	command_argv = argv[0]

	modified_command_argv = ""
	for i in command_argv:
		if i == "_":
			continue

		modified_command_argv += i
	
	currentPath = os.getcwd()
	botFilePath = currentPath + "\\" + BOT_FILE

	completedCommand = botFilePath + " " + modified_command_argv
	os.system(completedCommand)

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