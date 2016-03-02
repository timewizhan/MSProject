from Structure import JobHashMap
from Timer import *
from JsonTools import *
from Pattern import *
from DataBase import *
from MsgSets import *
from Network import *
from Recorder import *
import time

import pdb

class Scheduler:
	ONE_MINUTE = 60

	def __init__(self, userID):
		self.jobHashMap = JobHashMap()
		self.patternDelegator = PatternDelegator(userID)
		self.userID = userID

	def start(self):
		from Log import *
		Log.debug("Start to scheduler")

		timer = Timer()
		timer.setCurrentDateAndTime()

		Log.debug("Start to operate bot")

		continued =	firstStep = True
		lastHourClock = False
		
		while continued:
			try:
				self.checkNextWork(firstStep, lastHourClock)

				while timer.compareHourWithNowHour():
					currentHour = timer.getCurrentHour()
					if currentHour == 23:
						lastHourClock = True

					Log.debug("Start to deque for next work")
					nextJobToWork = self.jobHashMap.dequeJobValueByKey(currentHour)
					if nextJobToWork == 0:
						Log.debug("Wait for 60 seconds")
						time.sleep(self.ONE_MINUTE)
						#time.sleep(1)
						continue
					#pdb.set_trace()
					Log.debug("Start to communicate with servers")
					self.startToCommunicateWithServer(nextJobToWork)

					Log.debug("Start to save results")
					#self.saveResultToDataBase()

				Log.debug("Next Hour")
			except Exception as e:
				continued = False

				Log.error("There is error in scheduler")
			
	def checkNextWork(self, firstStep, lastHourClock):
		if firstStep == True:
			firstStep = False

			self.patternDelegator.startToGetPattern(self.jobHashMap)

		elif lastHourClock == True and timer.compareDayWithNowDay() == False:
			lastHourClock = False
			timer.setCurrentDateAndTime()

			self.patternDelegator.startToGetPattern(self.jobHashMap)
		elif timer.compareDayWithNowDay() == True:
			timer.setCurrentDateAndTime()

	def startToCommunicateWithServer(self, nextJobToWork):
		recorder = CRecorder()

		# 1. communicate with Broker
		Log.debug("Start to send to data to Broker")
		dataToSend = makeBrokerJsonData(self.userID, nextJobToWork)

		recorder.startRecord()
		recvDataFromBroker = self.networkingWithBroker(dataToSend)

		dstIPAddress = self.getResponseData(recvDataFromBroker)

		# 2. communicate with Server
		Log.debug("Start to send to data to EP")
		dataToSend = makeEntryPointJsonData(self.userID, nextJobToWork)
		recvDataFromBroker = self.networkingWithEntryPoint(dstIPAddress, dataToSend)
		networkTimeMsg = "NetworkTime " + str(recorder.endRecord())

		Log.debug(networkTimeMsg)

		recorder.gerResultTime()

		return self.getResponseData(recvDataFromBroker)

	def getResponseData(self, recvDataFromBroker):
		jsonParser = JsonParser(recvDataFromBroker)
		return jsonParser.getValue("RESPONSE")

	def saveResultToDataBase(self):
		'''
			TODO.
			After completing database, have to work it
		'''
		self.dataBase = PyDatabase()
		self.dataBase.connectToDB()

		sql = ""
		self.dataBase.updateSQL(sql)
		self.dataBase.disconnectFromDB()

	def networkingWithBroker(self, dataToSend):
		brServer = Broker()
		return brServer.startNetworkingWithData(dataToSend)

	def networkingWithEntryPoint(self, dstIPAddress, dataToSend):
		epServer = EntryPoint(dstIPAddress)
		return epServer.startNetworkingWithData(dataToSend)

def getCommonType(jobToWork):
	'''
		Protocol (Bot <-> Broker)

		TYPE 1 : WRITE
		TYPE 2 : READ
		TYPE 3 : REPLY
		TYPE 4 : SHARE
	'''

	if jobToWork.getRWType() == 1:
		opType = 2
	else:
		writeType = jobToWork.getWriteType()
		if writeType == 1:
			opType = 1
		elif writeType == 2:
			opType = 3
		else:
			opType = 4

	return opType

def makeEntryPointJsonData(userID, jobToWork):
	jsonGenerator = JsonGenerator()

	randValue = random.randrange(1, 5)
	opType = getCommonType(jobToWork)
	if opType == 1 or opType == 3:
		msgToSend = getWriteMsgToSend(randValue)
	else:
		msgToSend = getReplyMsgToSend(randValue)


	jsonGenerator.appendElement("TYPE", opType)
	jsonGenerator.appendElement("SRC", userID)

	if len(jobToWork.getWhoName()) > 0:
		whoName = jobToWork.getWhoName()[0]
	else:
		whoName = userID
	jsonGenerator.appendElement("DST", whoName)
	jsonGenerator.appendElement("LOC", "")
	jsonGenerator.appendElement("MSG", msgToSend)

	return jsonGenerator.toString()

def makeBrokerJsonData(userID, jobToWork):
	jsonGenerator = JsonGenerator()

	jsonGenerator.appendElement("TYPE", getCommonType(jobToWork))
	jsonGenerator.appendElement("SRC", userID)

	if len(jobToWork.getWhoName()) > 0:
		whoName = jobToWork.getWhoName()[0]
	else:
		whoName = userID
	jsonGenerator.appendElement("DST", whoName)

	return jsonGenerator.toString()