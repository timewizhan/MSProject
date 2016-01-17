import DataBase
import random

class AbstractPattern:
	TOTAL_TIME_COUNT = 24
	INIT_COUNT = 0

	def __init__(self):
		self.dataBase = PyDatabase()
		self.dataBase.connectToDB()

		def startToMakePattern(self):
			pass
		
class TimePattern(AbstractPattern):
	def __init__(self, userID):
		self.userID = userID

		self.jobCountByTimeList = []
		for i in range(0, TOTAL_TIME_COUNT):
			self.jobCountByTimeList.append(INIT_COUNT)

	def startToMakePattern(self):
			return self.startToMakeTimePattern()

	def startToMakeTimePattern(self):
		writtenTimeList = self.getAllDataFromDataBase()
		if len(writtenTimeList) == 0:
			return

		self.generalizeAllDataAsOneDay(writtenTimeList)
		return self.getWorkCountByEachHour()

	def getAllDataFromDataBase(self):
		sql = "SELECT \"TweetTime\" FROM \"UserProperty\" WHERE userid=" + self.userID
		return self.dataBase.querySQL(sql)

	def generalizeAllDataAsOneDay(self, writtenTimeList):
		lengthOfWrittenTimeList = len(writtenTimeList)
		for i in range(0, lengthOfWrittenTimeList):
			dateTimeValue = writtenTimeList[i][0]

			hourValue = dateTimeValue.hour
			if not checkProperHourValue(hourValue):
				continue

			self.inputValueByHour(hourValue)

	def checkProperHourValue(self, hourValue):
		if hourValue >= TOTAL_TIME_COUNT:
			return False
		return True

	def inputValueByHour(self, hourValue):
		self.jobCountByTimeList[hourValue] += 1

	def getWorkCountByEachHour(self):
		totalWrittenCount = self.getTotalWrittenCountInList()

		countlistToWorkInOneDay = []
		for selectedTime in range(0, TOTAL_TIME_COUNT):
			writtenCount 	= self.getWrittenCountBySelectedTime(selectedTime)
			randomCount 	= self.getWrittenValueByRandom(writtenCount)
			finalCount 		= self.getWrittenValueByRatio(randomCount)

			countlistToWorkInOneDay.append(finalCount)

		return countlistToWorkInOneDay

	def getTotalWrittenCountInList(self):
		totalValue = 0
		for i in range(0, TOTAL_TIME_COUNT):	
			totalValue += self.jobCountByTimeList[i]

		return totalValue

	def getWrittenCountBySelectedTime(self, totalWrittenCount, selectedTime):
		TOTAL_MINUTE_COUNT = 60

		selectedTimeCount = self.jobCountByTimeList(selectedTime)
		firstValue = (selectedTimeCount * TOTAL_MINUTE_COUNT) / totalWrittenCount
		return int(firstValue)

	def getWrittenValueByRandom(self, countValue):
		if countValue == 0:
			return countValue

		return random.randrange(1, countValue + 1)

	def getWrittenValueByRatio(self, totalWrittenCount, selectedTime, countValue):
		selectedTimeCount = self.jobCountByTimeList[selectedTime]
		selectedTimeRatio = (selectedTimeCount * 100) / totalWrittenCount
		return round(selectedTimeRatio)


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
		self.userID = userID

	def startToMakeBehaviorPattern(self, countlistToWorkInOneDay):
		if len(countlistToWorkInOneDay) == 0:
			return

		return self.decideBehaviorByEachHour(countlistToWorkInOneDay)

	def decideBehaviorByEachHour(self, countlistToWorkInOneDay):
		workListInOneDay = []

		for selectedHour in range(1, TOTAL_TIME_COUNT):
			countToWork = countlistToWorkInOneDay[selectedHour]

			workListInHour = self.decideWorkByBehaviorCount(countToWork)
			workListInOneDay.append(workListInHour)

		return workListInOneDay

	def decideWorkByBehaviorCount(self, countToWork):
		friendListByRatio = self.getFriendByRatio()

		jobToWorkInHour = []
		for i in range(1, countToWork):
			jobToWork = []

			forType = self.selectBetweenMeAndYou()
			jobToWork.append(forType)

			forRWType = self.selectBetweenReadAndWrite()
			jobToWork.append(forRWType)

			nameToWork = ""
			if forType == WORK_FOR_YOU:
				nameToWork = selectFriendInRatioList(friendListByRatio)
			else:
				jobToWork.append(nameToWork)

			if forRWType == WRITE_TYPE:
				writeType = self.selectAmongWriteType()
				jobToWork.append(writeType)				
			else:
				jobToWork.append(MSG_NOTHING)

			jobToWorkInHour.append(jobToWork)

		return jobToWorkInHour

	def getFriendByRatio(self):
		friendList = self.getAllDataFromDataBase()
		
		lengthOfFriendList = len(friendList)
		if lengthOfFriendList < 1:
			return

		DEFAULT_RATIO = 12
		ratioValue = round((lengthOfFriendList * 12) / 100)
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
		sql = "SELECT \"DestinationName\" FROM \"UserLink\" WHERE SourceName=" + self.userID
		return self.dataBase.querySQL(sql)

	def selectFriendInRatioList(self, friendListByRatio):
		randValue = random.randrange(1, len(friendListByRatio))
		return friendListByRatio[randValue]


	def selectBetweenMeAndYou(self):
		FOR_ME = 45
		randomValue = random.randrange(1, 100)
		if randomValue <= FOR_ME:
			return WORK_FOR_ME
		else:
			return WORK_FOR_YOU

	def selectBetweenReadAndWrite(self):
		FOR_READ = 70
		randomValue = random.randrange(1, 100)
		if randomValue <= FOR_READ:
			return READ_TYPE
		else:
			return WRITE_TYPE

	def selectAmongWriteType(self):
		randomValue = random.randrange(1, 100)
		if randomValue <= 70:
			return MSG_WRITE
		elif randomValue <= 90:
			return MSG_REPLY
		else:
			return MSG_LIKE

class LocationPattern(AbstractPattern):
	def __init__(self):
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

	def getOneDayList(self):
		return self.timePattern.startToMakePattern()

	def getOneDayWorkList(self, countlistToWorkInOneDay):
		return self.behaviorPattern.startToMakeBehaviorPattern(countlistToWorkInOneDay)

	def makeHashMap(self, jobHashMap ,workListInOneDay):
		for hour in range(1, len(workListInOneDay)):
			workListInOneHour = workListInOneDay[hour]

			for workInHour in range(1, len(workListInOneHour)):
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

