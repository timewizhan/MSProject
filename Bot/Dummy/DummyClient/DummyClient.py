from multiprocessing import Process
from socket import *
import sys, os, time
import pdb

#pdb.set_trace()	

SUCCESS = 1
FAIL = 0

class NetworkConnection:
	MSG_BUF = 4096

	def __init__(self):
		pass

	def initializeConnection(self):
		self.socketToConnect = socket(AF_INET, SOCK_STREAM)

	def makeConnectionWithServer(self, serverIP, serverPort):
		serverConnectionAddress = (serverIP, int(serverPort))

		try:
			self.socketToConnect.connect(serverConnectionAddress)
		except Exception as e:
			print "[Error] Cannot connect to server : %s" % e
			return FAIL

		return SUCCESS

	def sendMsg(self, msg):
		return self.socketToConnect.sendall(msg)

	def receiveMsg(self):
		return self.socketToConnect.recv(self.MSG_BUF)

	def destoryConnection(self):
		self.socketToConnect.close()

class DummyBotProcess:
	def __init__(self):
		self.networkConnection = NetworkConnection()

	def startDummyBotProcess(self, serverIP, serverPort):
		self.startMainLoop(serverIP, serverPort)
		self.networkConnection.destoryConnection()

	def startMainLoop(self, serverIP, serverPort):
		'''
			The timecount is used for sleeping of process

			The timecount is calculate as below
			timecount = before_timecount * 2

			if timecount is greater than threshold, timecount is initialized as init value 1

			threshold value is 600
		'''

		TIMECOUNT_THRESHOLD = 600
		timeCount = 1
		
		JSON_FILE_NAME = "json.txt"
		currentPath = os.getcwd()
		JSON_FILE_PATH = currentPath + "\\" + JSON_FILE_NAME

		with open(JSON_FILE_PATH, "r") as jsonFile: 
			self.readData = jsonFile.read()

		while True:
			self.networkConnection.initializeConnection()

			ret = self.networkConnection.makeConnectionWithServer(serverIP, serverPort)
			if ret != SUCCESS:
				self.networkConnection.destoryConnection()
				return

			ret = self.networkConnection.sendMsg(self.readData)
			
			ret = self.networkConnection.receiveMsg()
			print ret
			if ret < 1:
				continue

			if timeCount > TIMECOUNT_THRESHOLD:
				timeCount = 1
			else:
				timeCount *= 2

			self.networkConnection.destoryConnection()

			print "[Info] Wait for %d seconds" % timeCount
			time.sleep(timeCount)

def fn_process(*argv):
	serverIP 			= argv[0]
	serverPort 			= argv[1]
	currentBotNumber 	= argv[2]

	print "[Info] Dummy Bot Process is made [%d]" % currentBotNumber

	try:
		dummyBotProcess = DummyBotProcess()
		dummyBotProcess.startDummyBotProcess(serverIP, serverPort)
	except Exception as e:
		print "[Error] Cannot make dummybots"

	print "[Info] Dummy Bot Process is normally finished"


class DummyBot:
	def __init__(self, argv):
		self.serverIP 		= argv[1]
		self.serverPort 	= argv[2]
		self.numberOfBot 	= int(argv[3])


	def startDummyBot(self):
		processesToOperate = self.readyForProcess()
		self.startProcess(processesToOperate)
		self.waitForProcess(processesToOperate)

	def readyForProcess(self):
		procList = []

		for eachBot in range(0, self.numberOfBot):
			procList.append(Process(target = fn_process, args = (self.serverIP, self.serverPort, eachBot,)))

		return procList

	def startProcess(self, processList):
		for p in processList:
			p.start()

	def waitForProcess(self, processList):
		for p in processList:
			p.join()


def usage():
	print "Usage : [server_ip] [server_port] [number_of_bot]"

def checkToJsonFile():
	JSON_FILE_NAME = "json.txt"
	currentPath = os.getcwd()

	global JSON_FILE_PATH
	JSON_FILE_PATH = currentPath + "\\" + JSON_FILE_NAME
	return os.path.isfile(JSON_FILE_PATH)

if __name__ == "__main__":

	if len(sys.argv) != 4:
		usage()
		sys.exit(1)

	if not checkToJsonFile():
		print "Cannot find json file"
		sys.exit(1)

	try:
		dummyBot = DummyBot(sys.argv)
		dummyBot.startDummyBot()
	except Exception as e:
		print "[Error] %s" % e
		sys.exit(1)
		
	print "Success to finish dummy bots"
