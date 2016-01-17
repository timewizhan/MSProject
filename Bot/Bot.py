import sys
import Log
import Scheduler

if __name__ == "__main__":
	'''
		To do.
		extract user name to operate bot
	'''

	if not sys.argv[2]:
		Log.error("Invalid argument")
		sys.exit(1)

	userID = sys.argv[2]

	Log.debug("=============================================")
	Log.debug("================= Start Bot =================")
	Log.debug("=============================================")

	try:
		schedulerForBot = Scheduler(userID)
		if schedulerForBot == None:
			raise Exception

		schedulerForBot.start()
	except Exception as e:
		Log.error("Fail to operate bot")
		
		Log.debug("=========================================================")
		Log.debug("================= Abnormally Finish Bot =================")
		Log.debug("=========================================================")
		sys.exit(1)

	Log.debug("==============================================")
	Log.debug("================= Finish Bot =================")
	Log.debug("==============================================")	