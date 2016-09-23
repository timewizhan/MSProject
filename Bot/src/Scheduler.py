from Structure import JobList
from Timer import *
from JsonTools import *
from Pattern import *
from MsgSets import *
from Network import *
from Recorder import *
import time
import os
import socket
import sys
import pdb

class Scheduler:
	ONE_MINUTE = 60

	def __init__(self, userID, userPlace):
		self.jobHashMap = JobHashMap()		
		self.patternDelegator = PatternDelegator(userID)
		self.userID = userID
		self.userPlace = userPlace
		self.continued = True
		self.firstStep = True
		self.oneDayCounter = 0

		self.hash_dstIPAddress = {}

	def start(self):
		from Log import *
		Log.debug("Start scheduler")

		timer = Timer()
		timer.setCurrentDateAndTime()

		processID = os.getpid()
		Log.debug("Start to operate bot [" + str(processID) + "]\n")
		
		# Make a pattern		
		self.patternDelegator.startToGetPattern(self.jobHashMap)

		Log.debug("=============================================")
		Log.debug("=================== Ready ===================")
		Log.debug("=============================================")

		while self.continued:
			try:				
				start = self.checkNextHour(timer)
								
				# After generating the pattern
				# Send the report to the manager
				# and wait for the start message				
				if self.firstStep:
					#Log.debug("Send the action message to the manager\n")
					#reportToSend = makeManagerJsonData(self.userID)
					#recvMsgFromManager = self.networkingWithManager(reportToSend)
					self.firstStep = False
				
					#if len(recvMsgFromManager) == 0:
					#	Log.error("Fail to receive the action message from the manager")
					#	self.continued = False
					#	continue							

				if start:
					Log.debug("=============================================")
					Log.debug("=================== Start ===================")
					Log.debug("=============================================")

				currentHour = timer.getCurrentHour()

				while start:					
					nextJobToWork = self.jobHashMap.dequeJobValueByKey(currentHour)

					if nextJobToWork == 0:						
						start = False
						Log.debug("Complete sending requests")
						break
							
					delay = random.randrange(1, 6)
					time.sleep(delay)

					Log.debug("Start to communicate with servers\n")
					dstName = self.getDstName(nextJobToWork)

					Log.debug("Start to find dstName in Hash\n")
					if not self.isIncludedInHash(dstName):
						dstIPAddress = self.startToCommunicationWithBroker(nextJobToWork)
						self.addInHash(dstName, dstIPAddress)

					if self.hash_dstIPAddress[dstName]:
						self.startToCommunicateWithService(nextJobToWork, dstIPAddress)

				if not self.continued:
					break
				
				Log.debug("Wait for next hour\n")
				wait = timer.getWaitTimeForNextHour()
				time.sleep(wait)
				timer.setCurrentDateAndTime()
			except Exception as e:
				Log.error("There is error in scheduler")
				Log.error(e)
				sys.exit(1)

		Log.debug("Successfully end the one day job")
		sys.exit(0)

	def getDstName(self, jobToWork):
		if len(jobToWork.getWhoName()) > 0:
			dstName = jobToWork.getWhoName()
		else:
			dstName = self.userID

		return dstName

	def isIncludedInHash(self, dstName):
		for k in self.hash_dstIPAddress.keys():
			if k != dstName:
				continue
			return True
		return False

	def addInHash(self, key, value):
		dstName = getDstName(jobToWork)
		self.hash_dstIPAddress[key] = value
		
	def checkNextHour(self, timer):
		if self.firstStep == True:
			return True

		if self.oneDayCounter == 24:
			self.continued = False
			return False

		self.oneDayCounter += 1
		self.hash_dstIPAddress.clear()
		return True

	# 1. communicate with Broker
	def startToCommunicationWithBroker(self, nextJobToWork):
		Log.debug("Start to build data for Broker")
		dataToSend = makeBrokerJsonData(self.userID, nextJobToWork)
		Log.debug(dataToSend)

		Log.debug("Start to send to data to Broker")
		recvDataFromBroker = self.networkingWithBroker(dataToSend)
		Log.debug(recvDataFromBroker)
		
		dstIPAddress = self.getResponseData(recvDataFromBroker)

		return dstIPAddress

	# 2. communicate with Server
	def startToCommunicateWithService(self, nextJobToWork, dstIPAddress):
		Log.debug("Start to build data for SNS Server")
		dataToSend = makeEntryPointJsonData(self.userID, nextJobToWork, self.userPlace)
		Log.debug(dataToSend)

		Log.debug("Start to send to data to SNS Server")
		
		recvDataFromEP = self.networkingWithEntryPoint(dstIPAddress, dataToSend)
		Log.debug(recvDataFromEP)

		delayTime = self.getDelayData(recvDataFromEP)
		Log.debug("ENTRYPOINT RTT: " + str(delayTime))
				
	def getResponseData(self, recvDataFromBroker):
		jsonParser = JsonParser(recvDataFromBroker)
		return jsonParser.getValue("RESPONSE")

	def getDelayData(self, recvDataFromEP):
		jsonParser = JsonParser(recvDataFromEP)
		return jsonParser.getValue("RTT")

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
		recvData = brServer.startNetworkingWithData(dataToSend)
		brServer.closeConnection()

		return recvData

	def networkingWithEntryPoint(self, dstIPAddress, dataToSend):
		epServer = EntryPoint(dstIPAddress)
		recvData = epServer.startNetworkingWithData(dataToSend)
		epServer.closeConnection()

		return recvData

	def networkingWithManager(self, reportToSend):
		managerIPAddress = socket.gethostbyname(socket.gethostname())
		mnServer = Manager(managerIPAddress)
		recvData = mnServer.startNetworkingWithData(reportToSend)
		mnServer.closeConnection()

		return recvData

def getCommonType(jobToWork):
	'''
		Protocol (Bot <-> Broker)

		TYPE 1 : WRITE
		TYPE 2 : READ
		TYPE 3 : REPLY
		TYPE 4 : SHARE
	'''


	'''
		READ_TYPE 		= 1
		WRITE_TYPE 		= 2

		MSG_WRITE 		= 1
		MSG_REPLY 		= 2
		MSG_LIKE 		= 3
		MSG_NOTHING		= 0
	'''

	# READ_TYPE
	if jobToWork.getRWType() == 1:
		opType = 2
	# WRITE_TYPE
	else:
		writeType = jobToWork.getWriteType()
		#MSG_WRITE
		if writeType == 1:
			opType = 1
		#MSG_REPLY
		elif writeType == 2:
			opType = 3
		#MSG_LIKE
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
		whoName = jobToWork.getWhoName()
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
		whoName = jobToWork.getWhoName()
	else:
		whoName = userID
	jsonGenerator.appendElement("DST", whoName)

	return jsonGenerator.toString()

def makeManagerJsonData(userID):
	jsonGenerator = JsonGenerator()

	processID = os.getpid()
	jsonGenerator.appendElement("pid", processID)
	jsonGenerator.appendElement("uid", userID)
	jsonGenerator.appendElement("action", 1)
	
	return jsonGenerator.toString()
