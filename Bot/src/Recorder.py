import time

class CRecorder:
	def __init__(self):
		pass

	def startRecord(self):
		self.beforeTime = time.time()

	def endRecord(self):
		self.afterTime = time.time()

	def gerResultTime(self):
		return self.afterTime - self.beforeTime
