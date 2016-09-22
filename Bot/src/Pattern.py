from Structure import *
from Network import *
from Log import *
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
	MAX_TWEET_NUM = 150

	def __init__(self, userID):
		self.initialize()
		self.userID = userID		

		self.jobCountByTimeList = []
		for i in range(0, self.TOTAL_TIME_COUNT):
			self.jobCountByTimeList.append(self.INIT_COUNT)

	def __del__(self):
		self.deinitailize()

	def startToMakePattern(self):
		Log.debug("=============================================")
		Log.debug("=========== Generate Time Pattern ===========")
		Log.debug("=============================================")

		return self.startToMakeTimePattern()

	def startToMakeTimePattern(self):
		writtenNumInHouList = self.getAllDataFromDataBase()
		if not writtenNumInHouList:
			return

		self.generalizeAllDataAsOneDay(writtenNumInHouList)
		return self.getWorkCountByEachHour()

	def getAllDataFromDataBase(self):
		sql = "SELECT \"TweetTime\" FROM public.\"UserProperty\" WHERE \"UserName\"=\'" + self.userID + "\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		DBPSServer.closeConnection()

		dateList = recvFromServer.split()
		hourList = []
		for i in range(0, len(dateList)):
			if i % 2 == 0:
				continue
				
			timeList = dateList[i].split(':')
			hourList.append(int(timeList[0]))

		return hourList

	def generalizeAllDataAsOneDay(self, writtenNumInHourList):
		for i in range(0, len(writtenNumInHourList)):
			hourValue = writtenNumInHourList[i]

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
		totalWrittenNum = self.getTotalWrittenCountInList()
		
		writtenNumInOneDayList = []
		for selectedTime in range(0, self.TOTAL_TIME_COUNT):
			finalWrittenNum = self.getWrittenValueByRatio(totalWrittenNum, selectedTime)

			writtenNumInOneDayList.append(finalWrittenNum)

		return writtenNumInOneDayList

	def getTotalWrittenCountInList(self):
		sql = "SELECT COUNT(\"TweetTime\") FROM public.\"UserProperty\" WHERE \"UserName\"=\'" + self.userID + "\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		del DBPSServer

		return int(recvFromServer)

	def getWrittenValueByRatio(self, totalWrittenNum, selectedTime):
		selectedTimeCount = self.jobCountByTimeList[selectedTime]
		selectedTimeRatio = float(selectedTimeCount) / float(totalWrittenNum) * float(self.MAX_TWEET_NUM)
		Log.debug("Total written num in [" + str(selectedTime) + "]: " + str(selectedTimeCount) + "/" + str(int(round(selectedTimeRatio))))
		return int(round(selectedTimeRatio))

class BehaviorPattern(AbstractPattern):
	RW_RATIO = 5

	WORK_FOR_ME 	= 1
	WORK_FOR_YOU 	= 2

	READ_TYPE 		= 1
	WRITE_TYPE 		= 2

	MSG_WRITE 		= 1
	MSG_REPLY 		= 2
	MSG_NOTHING		= 0

	def __init__(self, userID):
		self.initialize()
		self.userID = userID

	def __del__(self):
		self.deinitailize()

	def startToMakeBehaviorPattern(self, writtenNumInOneDay):
		if not writtenNumInOneDay:
			return

		Log.debug("=============================================")
		Log.debug("========= Generate Behavior Pattern =========")
		Log.debug("=============================================")

		return self.decideBehaviorByEachHour(writtenNumInOneDay)

	def decideBehaviorByEachHour(self, writtenNumInOneDay):				
		myselfRatio, friendRatio = self.getMFRatio()
		
		friendInfoList = self.getAllDataFromDataBase()
		
		workListInOneDay = []

		for selectedHour in range(0, self.TOTAL_TIME_COUNT):
			writtenNumInHour = writtenNumInOneDay[selectedHour]

			finalWriteMyself = int(round(writtenNumInHour * myselfRatio))
			writeFriend = float(round(writtenNumInHour * friendRatio))

			#assume that read/write ratio = 70 / 30	
			readFriend = float(round(writtenNumInHour * self.RW_RATIO))

			finalWriteFriend, wFriendList = self.getFinalOperationFriend(writeFriend, friendInfoList)
			finalReadFriend, rFriendList = self.getFinalOperationFriend(readFriend, friendInfoList)						

			Log.debug("Final write myself: " + str(finalWriteMyself))
			Log.debug("Final write friend: " + str(writeFriend) + "/" + str(finalWriteFriend))
			Log.debug("Final read friend: " + str(readFriend) + "/" + str(finalReadFriend))

			operationList = []
			if finalWriteMyself != 0:
				operationList.append("MW")

			if finalWriteFriend != 0:
				operationList.append("FW")

			if finalReadFriend != 0:
				operationList.append("FR")

			workInHour = []
			while(operationList):
				toWork = []			

				operationIndex = random.randrange(0, len(operationList))
				operationValue = operationList[operationIndex]

				if operationValue == "MW":
					toWork.append(self.WORK_FOR_ME)
					toWork.append(self.WRITE_TYPE)
					toWork.append("")
					toWork.append(self.MSG_WRITE)

					finalWriteMyself -= 1

					if finalWriteMyself == 0:
						operationList.pop(operationIndex)

				if operationValue == "FW":
					friendIndex = random.randrange(0, len(wFriendList))
					friendName = wFriendList[friendIndex].getName()

					toWork.append(self.WORK_FOR_YOU)
					toWork.append(self.WRITE_TYPE)
					toWork.append(friendName)
					toWork.append(self.MSG_REPLY)

					wFriendList[friendIndex].decreaseNumOperation()

					if wFriendList[friendIndex].getNumOperation() == 0:
						wFriendList.pop(friendIndex)

					finalWriteFriend -= 1

					if finalWriteFriend == 0:
						operationList.pop(operationIndex)

				if operationValue == "FR":
					friendIndex = random.randrange(0, len(rFriendList))
					friendName = rFriendList[friendIndex].getName()

					toWork.append(self.WORK_FOR_YOU)
					toWork.append(self.READ_TYPE)
					toWork.append(friendName)
					toWork.append(self.MSG_NOTHING)

					rFriendList[friendIndex].decreaseNumOperation()

					if rFriendList[friendIndex].getNumOperation() == 0:
						rFriendList.pop(friendIndex)

					finalReadFriend -= 1

					if finalReadFriend == 0:
						operationList.pop(operationIndex)

				workInHour.append(toWork)

			workListInOneDay.append(workInHour)			
			Log.debug("Behavior in [" + str(selectedHour) + "] is generated")
			
			time.sleep(1)

		return workListInOneDay

	def getMFRatio(self):
		numData = self.getNumDataFromDataBase()
		myselfRatio = float(numData[0])
		friendRatio = float(numData[1])

		return (myselfRatio, friendRatio)

	def getNumDataFromDataBase(self):
		sql = "SELECT \"MyselfRatio\", \"FriendRatio\" FROM public.\"MFRatio\" WHERE \"SourceName\"=\'" + self.userID + "\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		DBPSServer.closeConnection()

		return recvFromServer.split('=')

	def getAllDataFromDataBase(self):
		sql = "SELECT \"DestinationName\", \"Portion\" FROM public.\"SocialLevelPerUser\" WHERE \"SourceName\"=\'" + self.userID +"\'"

		DBPSServer = DBPoolServer()
		recvFromServer = DBPSServer.startNetworkingWithData(sql)
		DBPSServer.closeConnection()

		splittedList = recvFromServer.split()

		friendInfoList = []
		for i in range(len(splittedList)):
			finalSplittedList = splittedList[i].split('=')
			
			friendName = finalSplittedList[0]
			socialLevel = float(finalSplittedList[1])
			
			if socialLevel == 0.0:
				continue
			
			friendInfo = FriendInfo(friendName, socialLevel)
			friendInfoList.append(friendInfo)

		return friendInfoList

	def getFinalOperationFriend(self, numOperation, friendInfoList):
		finalOperationFriend = 0
		opFriendList = []
		for i in friendInfoList:
			friendName = i.getName()
			socialLevel = i.getSocialLevel()

			friendNumOperation = round(numOperation * socialLevel, 2)
			if (friendNumOperation > 0) & (friendNumOperation < 1):
				additional = random.randrange(0,3)
				if additional == 1:
					friendNumOperation = 1.0;
			
			friendWork = FriendWork(friendName, int(friendNumOperation))
			opFriendList.append(friendWork)
			
			finalOperationFriend += int(friendNumOperation)

		return (finalOperationFriend, opFriendList)

class PatternDelegator:
	def __init__(self, userID):
		self.userID = userID

	def startToGetPattern(self, jobHashMap):
		self.initializePattern()

		writtenNumInOneDay = self.getOneDayList()
		Log.debug("Complete deciding time pattern\n")
		if not writtenNumInOneDay:
			return		
		
		workListInOneDay = self.getOneDayWorkList(writtenNumInOneDay)
		Log.debug("Complete deciding behavior pattern\n")
		if not workListInOneDay:
			return

		self.makeHashMap(jobHashMap, workListInOneDay)

	def initializePattern(self):
		self.timePattern = TimePattern(self.userID)
		self.behaviorPattern = BehaviorPattern(self.userID)

	def getOneDayList(self):		
		return self.timePattern.startToMakePattern()

	def getOneDayWorkList(self, writtenNumInOneDay):
		return self.behaviorPattern.startToMakeBehaviorPattern(writtenNumInOneDay)

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