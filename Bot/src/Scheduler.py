from Structure import JobHashMap
from Timer import *
from JsonTools import *
from Pattern import *
from DataBase import *
import time

import pdb

class Scheduler:
	ONE_MINUTE = 60

	def __init__(self, userID):
		self.jobHashMap = JobHashMap()
		self.patternDelegator = PatternDelegator(userID)

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
						#time.sleep(self.ONE_MINUTE)
						time.sleep(1)
						continue

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
		'''
			To do.
			communicate with Broker server
		'''
		#self.networkingWithBroker(dataToSend)

		dataToSend = self.makeJsonFromJob(nextJobToWork)
		self.networkingWithEntryPoint(dataToSend)

	def saveResultToDataBase(self):
		self.dataBase = PyDatabase()
		self.dataBase.connectToDB()

		sql = ""
		self.dataBase.updateSQL(sql)
		self.dataBase.disconnectFromDB()

	def makeJsonFromJob(self, jobToWork):
		jsonGenerator = JsonGenerator()
		jsonGenerator.appendElement("whotype", jobToWork.getWhoType())
		jsonGenerator.appendElement("rw", jobToWork.getRWType())
		jsonGenerator.appendElement("whoname", jobToWork.getWhoName())
		jsonGenerator.appendElement("wtype", jobToWork.getWriteType())

		return jsonGenerator.toString()

	def networkingWithEntryPoint(self, dataToSend):
		epServer = EntryPoint()
		epServer.startNetworkingWithData()

	def networkingWithBroker(self, dataToSend):
		brServer = Broker()
		brServer.startNetworkingWithData()

