#from DataBase import *
from Structure import *
from Network import *
import random
import pdb

class AbstractPattern:
	TOTAL_TIME_COUNT = 24
	INIT_COUNT = 0

	def initialize(self):
		pass

	def deinitailize(self):
		pass

	def startToMakePattern(self):
		pass
		
class TimePattern(AbstractPattern):
	def __init__(self, userID):
		self.initialize()
		self.userID = userID

		self.jobCountByTimeList = []
		for i in range(0, self.TOTAL_TIME_COUNT):
			self.jobCountByTimeList.append(self.INIT_COUNT)

	def __del__(self):
		self.deinitailize()

	def startToMakePattern(self):
			return self.startToMakeTimePattern()

	def startToMakeTimePattern(self):
		writtenTimeHourList = self.getAllDataFromDataBase()
		if len(writtenTimeHourList) == 0:
			return
		
		self.generalizeAllDataAsOneDay(writtenTimeHourList)
		return self.getWorkCountByEachHour()

	def getAllDataFromDataBase(self):
		sql = "SELECT \"TweetTime\" FROM public.\"UserProperty\" WHERE \"UserName\"=\'" + self.userID +"\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		del DBPSServer

		datalist = recvFromServer.split()
		hourList = []
		for i in range(0, len(datalist)):
			if i % 2 == 0:
				continue
			
			timevaluelist = datalist[i].split(':')
			hourList.append(int(timevaluelist[0]))
		
		return hourList

	def generalizeAllDataAsOneDay(self, writtenTimeHourList):
		lengthOfWrittenTimeHourList = len(writtenTimeHourList)
		for i in range(0, lengthOfWrittenTimeHourList):
			hourValue = writtenTimeHourList[i]

			if not self.checkProperHourValue(hourValue):
				continue

			self.inputValueByHour(hourValue)

	def checkProperHourValue(self, hourValue):
		if hourValue >= self.TOTAL_TIME_COUNT:
			return False
		return True

	def inputValueByHour(self, hourValue):
		self.jobCountByTimeList[hourValue] += 1

	def getWorkCountByEachHour(self):
		totalWrittenCount = self.getTotalWrittenCountInList()
		
		countlistToWorkInOneDay = []
		for selectedTime in range(0, self.TOTAL_TIME_COUNT):
			writtenCount 	= self.getWrittenCountBySelectedTime(totalWrittenCount, selectedTime)
			randomCount 	= self.getWrittenValueByRandom(writtenCount)
			finalCount 		= self.getWrittenValueByRatio(totalWrittenCount, selectedTime, randomCount)

			countlistToWorkInOneDay.append(finalCount)

		return countlistToWorkInOneDay

	def getTotalWrittenCountInList(self):
		totalValue = 0
		for i in range(0, self.TOTAL_TIME_COUNT):	
			totalValue += self.jobCountByTimeList[i]

		return totalValue

	def getWrittenCountBySelectedTime(self, totalWrittenCount, selectedTime):
		TOTAL_MINUTE_COUNT = 60

		selectedTimeCount = self.jobCountByTimeList[selectedTime]
		firstValue = (selectedTimeCount * TOTAL_MINUTE_COUNT) / totalWrittenCount
		return int(firstValue)

	def getWrittenValueByRandom(self, countValue):
		if countValue == 0:
			return countValue

		return random.randrange(1, countValue + 1)

	def getWrittenValueByRatio(self, totalWrittenCount, selectedTime, countValue):
		selectedTimeCount = self.jobCountByTimeList[selectedTime]
		selectedTimeRatio = (selectedTimeCount * 100/ totalWrittenCount)
		return int(round(selectedTimeRatio))


class BehaviorPattern(AbstractPattern):
	WORK_FOR_ME 	= 1
	WORK_FOR_YOU 	= 2

	READ_TYPE 		= 1
	WRITE_TYPE 		= 2

	MSG_WRITE 		= 1
	MSG_REPLY 		= 2
	MSG_LIKE 		= 3
	MSG_NOTHING		= 0

	def __init__(self, userID):
		self.initialize()
		self.userID = userID

	def __del__(self):
		self.deinitailize()

	def startToMakeBehaviorPattern(self, countlistToWorkInOneDay):
		if len(countlistToWorkInOneDay) == 0:
			return

		return self.decideBehaviorByEachHour(countlistToWorkInOneDay)

	def decideBehaviorByEachHour(self, countlistToWorkInOneDay):
		workListInOneDay = []
		#pdb.set_trace()

		friendList = self.getAllDataFromDataBase()

		for selectedHour in range(0, self.TOTAL_TIME_COUNT):
			countToWork = countlistToWorkInOneDay[selectedHour]

			friendListByRatio = self.getFriendByRatio(friendList)
			workListInHour = self.decideWorkByBehaviorCount(countToWork, friendListByRatio)
			workListInOneDay.append(workListInHour)

		return workListInOneDay

	def decideWorkByBehaviorCount(self, countToWork, friendListByRatio):
		jobToWorkInHour = []

		for i in range(0, countToWork):
			jobToWork = []

			forType = self.selectBetweenMeAndYou()
			jobToWork.append(forType)

			forRWType = self.selectBetweenReadAndWrite()
			jobToWork.append(forRWType)

			nameToWork = ""
			if forType == self.WORK_FOR_YOU:
				nameToWork = self.selectFriendInRatioList(friendListByRatio)
				jobToWork.append(nameToWork)
			else:
				jobToWork.append(nameToWork)

			if forRWType == self.WRITE_TYPE:
				writeType = self.selectAmongWriteType()
				jobToWork.append(writeType)				
			else:
				jobToWork.append(self.MSG_NOTHING)

			jobToWorkInHour.append(jobToWork)

		return jobToWorkInHour

	def getFriendByRatio(self, friendList):
		lengthOfFriendList = len(friendList)
		if lengthOfFriendList < 1:
			return

		DEFAULT_RATIO = 12
		ratioValue = int(round((lengthOfFriendList * 12) / 100))
		return self.selectFriendByRandom(friendList, ratioValue)

	def selectFriendByRandom(self, friendList, ratioValue):
		randomMaxValue = len(friendList)

		selectedFriendList = []
		for i in range(1, ratioValue):
			randomValue = random.randrange(1, randomMaxValue)

			friendName = friendList[randomValue]
			selectedFriendList.append(friendName)

		return selectedFriendList

	def getAllDataFromDataBase(self):
		sql = "SELECT \"DestinationName\" FROM public.\"UserLink\" WHERE \"SourceName\"=\'" + self.userID +"\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		del DBPSServer

		return recvFromServer.split()

	def selectFriendInRatioList(self, friendListByRatio):
		randValue = random.randrange(1, len(friendListByRatio))
		return friendListByRatio[randValue]


	def selectBetweenMeAndYou(self):
		FOR_ME = 45
		randomValue = random.randrange(1, 100)
		if randomValue <= FOR_ME:
			return self.WORK_FOR_ME
		else:
			return self.WORK_FOR_YOU

	def selectBetweenReadAndWrite(self):
		FOR_READ = 70
		randomValue = random.randrange(1, 100)
		if randomValue <= FOR_READ:
			return self.READ_TYPE
		else:
			return self.WRITE_TYPE

	def selectAmongWriteType(self):
		randomValue = random.randrange(1, 100)
		if randomValue <= 70:
			return self.MSG_WRITE
		elif randomValue <= 90:
			return self.MSG_REPLY
		else:
			return self.MSG_LIKE

class LocationPattern(AbstractPattern):
	def __init__(self):
		pass 

	def startToMakePattern(self):
		pass

class PatternDelegator:
	def __init__(self, userID):
		self.userID = userID

	def startToGetPattern(self, jobHashMap):
		self.initializePattern()

		countlistToWorkInOneDay = self.getOneDayList()
		if len(countlistToWorkInOneDay) == 0:
			return

		workListInOneDay = self.getOneDayWorkList(countlistToWorkInOneDay)
		if len(workListInOneDay) == 0:
			return

		self.makeHashMap(jobHashMap, workListInOneDay)

	def initializePattern(self):
		self.timePattern = TimePattern(self.userID)
		self.behaviorPattern = BehaviorPattern(self.userID)

		# TODO : decide user's location
		self.LocationPattern = LocationPattern()

	def getOneDayList(self):
		return self.timePattern.startToMakePattern()

	def getOneDayWorkList(self, countlistToWorkInOneDay):
		return self.behaviorPattern.startToMakeBehaviorPattern(countlistToWorkInOneDay)

	def makeHashMap(self, jobHashMap ,workListInOneDay):
		for hour in range(0, len(workListInOneDay)):
			workListInOneHour = workListInOneDay[hour]

			for workInHour in range(0, len(workListInOneHour)):
				eachWork = workListInOneHour[workInHour]
				jobForEachWork = Job()
				self.makeOneJob(jobForEachWork, eachWork)

				jobHashMap.insertJobValueByKey(jobForEachWork, hour)

	def makeOneJob(self, jobForEachWork, eachWork):
		if len(eachWork) != 4:
			return

		jobForEachWork.setWhoType(eachWork[0])
		jobForEachWork.setRWType(eachWork[1])
		jobForEachWork.setWhoName(eachWork[2])
		jobForEachWork.setWriteType(eachWork[3])

