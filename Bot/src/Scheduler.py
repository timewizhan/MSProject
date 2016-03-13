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

	def __init__(self, userID, userPlace):
		self.jobHashMap = JobHashMap()
		self.patternDelegator = PatternDelegator(userID)
		self.userID = userID
		self.userPlace = userPlace

	def start(self):
		from Log import *
		Log.debug("Start to scheduler")

		timer = Timer()
		timer.setCurrentDateAndTime()

		Log.debug("Start to operate bot")

		continued =	firstStep = True
		
		# Make an initial pattern
		self.patternDelegator.startToGetPattern(self.jobHashMap)

		while continued:
			try:
				Log.debug("Check whether day is changed or not")
				self.checkNextDay(firstStep)

				while timer.compareHourWithNowHour():
					currentHour = timer.getCurrentHour()

					Log.debug("Start to deque for next work")
					nextJobToWork = self.jobHashMap.dequeJobValueByKey(currentHour)
					if nextJobToWork == 0:
						Log.debug("Wait for 60 seconds")
						time.sleep(self.ONE_MINUTE)
						continue

					#pdb.set_trace()
					Log.debug("Start to communicate with servers")
					self.startToCommunicateWithServer(nextJobToWork)

					Log.debug("Start to save results")
					#self.saveResultToDataBase()

				timer.setCurrentDateAndTime()

				Log.debug("Next Hour")
			except Exception as e:
				Log.error("There is error in scheduler")
				Log.error(e)

				continued = False
			
	def checkNextDay(self, firstStep):
		if firstStep == True:
			firstStep = False
			return

		if timer.compareDayWithNowDay():
			return
		
		timer.setCurrentDateAndTime()
		self.patternDelegator.startToGetPattern(self.jobHashMap)
		
	def startToCommunicateWithServer(self, nextJobToWork):
		# 1. communicate with Broker
		Log.debug("Start to build data for Broker")
		dataToSend = makeBrokerJsonData(self.userID, nextJobToWork)
		Log.debug(dataToSend)

		Log.debug("Start to send to data to Broker")
		recvDataFromBroker = self.networkingWithBroker(dataToSend)
		Log.debug(recvDataFromBroker)

		dstIPAddress = self.getResponseData(recvDataFromBroker)

		# 2. communicate with Server
		Log.debug("Start to build data for SNS Server")
		dataToSend = makeEntryPointJsonData(self.userID, nextJobToWork, self.userPlace)
		Log.debug(dataToSend)

		Log.debug("Start to send to data to SNS Server")
		recvDataFromEP = self.networkingWithEntryPoint(dstIPAddress, dataToSend)
		Log.debug(recvDataFromEP)

		# 3. For saving data to database.
		# Todo.
		#self.getResponseData(recvDataFromEP)

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

def makeEntryPointJsonData(userID, jobToWork, userPlace):
	jsonGenerator = JsonGenerator()

	randValue = random.randrange(1, 5)
	opType = getCommonType(jobToWork)
	if opType == 1 or opType == 3:
		msgToSend = getWriteMsgToSend(randValue)
	else:
		msgToSend = getReplyMsgToSend(randValue)


	jsonGenerator.appendElement("TYPE", str(opType))
	jsonGenerator.appendElement("SRC", userID)

	if len(jobToWork.getWhoName()) > 0:
		whoName = jobToWork.getWhoName()[0]
	else:
		whoName = userID
	jsonGenerator.appendElement("DST", whoName)
	jsonGenerator.appendElement("LOC", userPlace)
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