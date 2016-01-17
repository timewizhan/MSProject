import Timer
import Structure
import JsonTools
import log
import Pattern

class Scheduler:
	def __init__(self, userID):
		self.jobHashMap = JobHashMap()
		self.patternDelegator = PatternDelegator(userID)

	def start(self):
		Log.debug("Start to scheduler")

		timer = Timer()
		timer.setCurrentDateAndTime()

		Log.debug("Start to operate bot")

		while timer.compareDayWithNowDay():
			Log.debug("Start to make patterns to operate bot")
			self.patternDelegator.startToGetPattern(self.jobHashMap)
	

			while timer.compareHourWithNowHour():
				currentHour = timer.getCurrentHour()

				Log.debug("Start to deque for next work")
				nextJobToWork = self.jobHashMap.dequeJobValueByKey(currentHour)
				if nextJobToWork == 0:
					Log.debug("Wait for 60 seconds")
					time.sleep(60)
					continue

				Log.debug("Start to communicate with servers")
				self.startToCommunicateWithServer()

				Log.debug("Start to save results")
				self.saveResultToDataBase()

	def startToCommunicateWithServer(self):
		dataToSend = self.makeJsonFromJob(nextJobToWork)
		self.networkingWithBroker(dataToSend)

		dataToSend = self.makeJsonFromJob(nextJobToWork)
		self.networkingWithEntryPoint(dataToSend)

	def saveResultToDataBase(self):
		'''
			To do.
			Apply to database
		'''
		pass

	def makeJsonFromJob(self, jobToWork):
		jsonGenerator = JsonGenerator()
		jsonGenerator.appendElement("type", jobToWork.getJonType())

		return jsonGenerator.toString()

	def networkingWithEntryPoint(self, dataToSend):
		epServer = EntryPoint()
		epServer.startNetworkingWithData()

	def networkingWithBroker(self, dataToSend):
		brServer = Broker()
		brServer.startNetworkingWithData()

		