import Timer
import Structure

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
					time.sleep(60)
					continue

				'''
					To do.
					Apply to network communication
				'''

				'''
					To do.
					Save data to Database
				'''



		