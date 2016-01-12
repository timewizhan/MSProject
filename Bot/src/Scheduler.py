import Timer
import Structure
import JsonTools
import log

class Scheduler:
	def __init__(self):
		self.jobHashMap = JobHashMap()

	def start(self):
		timer = Timer()
		timer.setCurrentDateAndTime()

		while timer.compareDayWithNowDay():
			'''
				To do.
				Apply to pattern algorithm
			'''

			while timer.compareHourWithNowHour():
				currentHour = timer.getCurrentHour()

				nextJobToWork = self.jobHashMap.dequeJobValueByKey(currentHour)
				if nextJobToWork == 0:
					Log.debug("Wait for 60 seconds")
					time.sleep(60)
					continue

				self.startToCommunicateWithServer()

				Log.debug()
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

		