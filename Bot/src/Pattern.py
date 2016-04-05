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
	MAX_TWEET_NUM = 1000

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
			finalCount 		= self.getWrittenValueByRatio(totalWrittenCount, selectedTime)

			countlistToWorkInOneDay.append(finalCount)

		return countlistToWorkInOneDay

	def getTotalWrittenCountInList(self):
		totalValue = 0
		for i in range(0, self.TOTAL_TIME_COUNT):	
			totalValue += self.jobCountByTimeList[i]

		return totalValue	

	def getWrittenValueByRatio(self, totalWrittenCount, selectedTime):
		selectedTimeCount = self.jobCountByTimeList[selectedTime]
		selectedTimeRatio = (selectedTimeCount * self.MAX_TWEET_NUM/ totalWrittenCount)
		return int(round(selectedTimeRatio))

class BehaviorPattern(AbstractPattern):
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
		numData = self.getNumDataFromDataBase()
		totalTweetMyself = numData[0]
		totalTweetOther = numData[1]
		totalReTweet = numData[2]
		sumTotal = totalTweetMyself + totalTweetOther + totalReTweet

		myselfRatio = totalTweetMyself / sumTotal
		friendRatio = (totalTweetOther + totalReTweet) / sumTotal

		workListInOneDay = []
		#pdb.set_trace()		

		friendInfoList = self.getAllDataFromDataBase()		
		
		for selectedHour in range(0, self.TOTAL_TIME_COUNT):
			countToWork = countlistToWorkInOneDay[selectedHour]

			finalWriteMyself = int(round(countToWork * myselfRatio))
			
			writeFriend = int(round(countToWork * friendRatio))

			#assume that read/write ratio = 70 / 30			
			readFriend = int(round(countToWork * (70/30)))

			finalWriteFriend = 0
			wFriendList = []
			for i in friendInfoList:
				friendName = i.getName()
				socialLevel = i.getSocialLevel()

				numWrite = int(round(writeFriend * socialLevel))
				if numWrite == 0:
					continue
				
				friendWork = FriendWork(friendName, numWrite)
				wFriendList.append(friendWork)
				
				finalWriteFriend += numWrite				
			

			finalReadFriend = 0
			rFriendList = []						
			for i in friendInfoList:
				friendName = i.getName()
				socialLevel = i.getSocialLevel()

				numRead = int(round(readFriend * socialLevel))
				if numRead == 0:
					continue
								
				friendWork = FriendWork(friendName, numRead)
				rFriendList.append(friendWork)
				
				finalReadFriend += numRead							

			operationList = []
			
			if finalWriteFriend != 0:
				operationList.append("MW")

			if finalWriteFriend != 0:
				operationList.append("FW")

			if finalReadFriend != 0:
				operationList.append("FR")

			workListInHour = []

			while(operationList):
				operationIndex = random.randrange(0, len(operationList))
				operationValue = operationList[operationIndex]

				if operationValue == "MW":
					workListInHour.append("")
					workListInHour.append(self.MSG_WRITE)

					finalWriteMyself -= 1

					if finalWriteMyself == 0:
						operationList.pop(operationIndex)

				if operationValue == "FW":
					friendIndex = random.randrange(0, len(wFriendList))
					friendName = wFriendList[friendIndex].getName()

					workListInHour.append(friendName)
					workListInHour.append(self.MSG_REPLY)

					wFriendList[friendIndex].decreaseNumOperation()

					if wFriendList[friendIndex].getNumOperation() == 0:
						wFriendList.pop(friendIndex)

					finalWriteFriend -= 1

					if finalWriteFriend == 0:
						operationList.pop(operationIndex)

				if operationValue == "FR":
					friendIndex = random.randrange(0, len(rFriendList))
					friendName = rFriendList[friendIndex].getName()

					workListInHour.append(friendName)
					workListInHour.append(self.MSG_NOTHING)

					rFriendList[friendIndex].decreaseNumOperation()

					if rFriendList[friendIndex].getNumOperation() == 0:
						rFriendList.pop(friendIndex)

					finalReadFriend -= 1

					if finalReadFriend == 0:
						operationList.pop(operationIndex)
			
			workListInOneDay.append(workListInHour)

		return workListInOneDay

	def getAllDataFromDataBase(self):		
		sql = "SELECT \"DestinationName\", \"SocialLevel\" FROM public.\"SocialLevelPerUser\" WHERE \"SourceName\"=\'" + self.userID +"\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		del DBPSServer

		splittedList = recvFromServer.split()

		friendInfoList = []
		for i in range(len(splittedList)):
			finalSplittedList = splittedList[i].split('=')
			
			friendName = finalSplittedList[0]
			socialLevel = finalSplittedList[1]
			
			friendInfo = FriendInfo(friendName, socialLevel)
			friendInfoList.append(friendInfo)

		return friendInfoList

	def getNumDataFromDataBase(self):
		sql = "SELECT SUM(\"TweetMyself\"), SUM(\"TweetOther\"), SUM(\"ReTweet\") FROM public.\"UserLink\" WHERE \"SourceName\"=\'" + self.userID + "\' GROUP BY \"SourceName\""

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		del DBPSServer

		return recvFromServer.split('=')

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