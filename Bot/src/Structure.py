
E_JOB_TYPE_READ		= 0
E_JOB_TYPE_WRITE	= 1
E_JOB_TYPE_UNKNOWN	= -1

class Job:
	def __init__(self):
		self.jobType = E_JOB_TYPE_UNKNOWN

	def setJobType(self, jobType):
		self.jobType = jobType		

	def getJonType(self):
		return self.jobType

class JobHashMap:
	TOTAL_TIME_COUNT = 24

	def __init__(self):
		self.oneDayTimeListPerOneHour = []
		timeListPerOneHour = []

		for i in range(0, TOTAL_TIME_COUNT):
			oneDayTimeListPerOneHour.apppend(timeListPerOneHour)

	def insertJobValueByKey(self, jobValue, mapKey):
		if !(self.checkLengthOfDayTimeList()):
			return 0

		if !(self.checkMapKey(mapKey)):
			return 0

		self.oneDayTimeListPerOneHour[mapKey].apppend(jobValue)
		return 1

	def dequeJobValueByKey(self, mapKey):
		if !(self.checkMapKey(mapKey)):
			return 0

		selectedTimeList = self.oneDayTimeListPerOneHour(mapKey)
		if self.checkLengthOfTimeList(selectedTimeList) < 1:
			return 0

		FIRST_QUEUE_IN_LIST = 0
		selectedJob = selectedTimeList[FIRST_QUEUE_IN_LIST]
		selectedTimeList.remove(FIRST_QUEUE_IN_LIST)
		return selectedJob

	def checkLengthOfDayTimeList(self):
		return len(self.oneDayTimeListPerOneHour) < self.TOTAL_TIME_COUNT

	def checkMapKey(self, mapKey):
		return mapKey < self.TOTAL_TIME_COUNT

	def checkLengthOfTimeList(self, timeList):
		return len(timeList)