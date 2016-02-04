import logging

class Log:
	def __init__(self):
		pass

	@staticmethod
	def debug(message):
		print "DEBUG : %s" % message
	
	@staticmethod
	def info(message):
		print "INFO : %s" % message

	@staticmethod
	def warning(message):
		print "WARNING : %s" % message

	@staticmethod
	def error(message):
		print "ERROR : %s" % message

	@staticmethod
	def critical(message):
		print "CRITICAL : %s" % message












