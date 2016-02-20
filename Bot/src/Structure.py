
class Job:
	def __init__(self):
		pass

	def setWhoType(self, whoType):
		self.whoType = whoType

	def getWhoType(self):
		return self.whoType

	def setRWType(self, rwType):
		self.rwType = rwType

	def getRWType(self):
		return self.rwType

	def setWhoName(self, whoName):
		self.whoName = whoName

	def getWhoName(self):
		return self.whoName

	def setWriteType(self, writeType):
		self.writeType = writeType

	def getWriteType(self):
		return self.writeType

	def setLocation(self, location):
		self.location = location

	def getLocation(self):
		return location

class JobHashMap:
	TOTAL_TIME_COUNT = 24

	def __init__(self):
		self.oneDayTimeListPerOneHour = []

		for i in range(0, self.TOTAL_TIME_COUNT):
			timeListPerOneHour = []
			self.oneDayTimeListPerOneHour.append(timeListPerOneHour)

	def insertJobValueByKey(self, jobValue, mapKey):
		if not self.checkLengthOfDayTimeList():
			return 0

		if not self.checkMapKey(mapKey):
			return 0

		listInOneHour = self.oneDayTimeListPerOneHour[mapKey]
		listInOneHour.append(jobValue)
		return 1

	def dequeJobValueByKey(self, mapKey):
		if not self.checkMapKey(mapKey):
			return 0

		selectedTimeList = self.oneDayTimeListPerOneHour[mapKey]
		if self.checkLengthOfTimeList(selectedTimeList) < 1:
			return 0

		FIRST_QUEUE_IN_LIST = 0
		selectedJob = selectedTimeList.pop(FIRST_QUEUE_IN_LIST)
		return selectedJob

	def checkLengthOfDayTimeList(self):
		return (len(self.oneDayTimeListPerOneHour) == self.TOTAL_TIME_COUNT)

	def checkMapKey(self, mapKey):
		return (mapKey < self.TOTAL_TIME_COUNT)

	def checkLengthOfTimeList(self, timeList):
		return len(timeList)