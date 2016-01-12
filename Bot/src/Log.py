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













import logging
logging.basicConfig(filename='example.log',level=logging.DEBUG)
logging.debug('This message should go to the log file')
logging.info('So should this')
logging.warning('And this, too')