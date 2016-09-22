from socket import *
from Log import *
from Recorder import *
import time
import select

TYPE_AF_INET 	= AF_INET
TYPE_AF_INET6 	= AF_INET6
TYPE_AF_LOCAL 	= AF_INET

TYPE_PROTOCOL_TCP	= SOCK_STREAM
TYPE_PROTOCOL_UDP	= SOCK_DGRAM

class NetworkError(Exception):
	def __init__(self, type):
		self.type = type

class NetworkSettingComponent:
	def __init__(self):
		pass

	def setIPAddressToConnect(self, ipAddressToConnect):
		self.serverIPAddress = ipAddressToConnect

	def getIPAddressToConnect(self):
		return self.serverIPAddress

	def setPortToConnect(self, portToConnect):
		self.serverPort = portToConnect

	def getPortToConnect(self):
		return self.serverPort

	def setAddressFamily(self, type):
		if type == TYPE_AF_INET:
			self.AFType = type
		elif type == TYPE_AF_INET6:
			self.AFType = type
		elif type == TYPE_AF_LOCAL:
			self.AFType = type
		else:
			self.AFType = TYPE_AF_INET

	def getAddressFamily(self):
		return self.AFType

	def setProtocolType(self, type):
		if type == TYPE_PROTOCOL_TCP:
			self.ProtocolType = type
		elif type == TYPE_PROTOCOL_UDP:
			self.ProtocolType = type
		else:
			self.ProtocolType = TYPE_PROTOCOL_TCP

	def getProtocolType(self):
		return self.ProtocolType

TYPE_NT_BROKER = 1
TYPE_NT_MANAGER = 2
TYPE_NT_EP = 3
TYPE_NT_DBCS = 4

class AbstractNetwork:
	DEFAULT_RECV_BUF_SIZE = 8192

	def __init__(self, ipAddress, Port):
		networkSettingComponent = NetworkSettingComponent()

		networkSettingComponent.setIPAddressToConnect(ipAddress)
		networkSettingComponent.setPortToConnect(Port)
		networkSettingComponent.setAddressFamily(TYPE_AF_INET)
		networkSettingComponent.setProtocolType(TYPE_PROTOCOL_TCP)

		self.configureEnviromentAndSocket(networkSettingComponent)

	'''
	def __del__(self):
		self.socketToConnect.close()
	'''

	def closeConnection(self):
		self.socketToConnect.close()

	def configureEnviromentAndSocket(self, networkSettingComponent):
		serverIPAddress = networkSettingComponent.getIPAddressToConnect()
		serverPort = networkSettingComponent.getPortToConnect()
		self.serverConnectionAddress = (serverIPAddress, serverPort)

		serverAFType = networkSettingComponent.getAddressFamily()
		serverProtoType = networkSettingComponent.getProtocolType()
		self.socketToConnect = socket(serverAFType, serverProtoType)

	def connectToServer(self):
		ONE_SECOND = 1
		SleepCount = 1

		bConnected = True
		while bConnected:
			try:
				self.socketToConnect.connect(self.serverConnectionAddress)
				bConnected = False
			except Exception as e: 
				Log.error("Connection ReTry and Sleep [" + str(SleepCount) + "]")
				Log.error(e)
				time.sleep(ONE_SECOND * SleepCount)
				SleepCount = SleepCount * 2
				if SleepCount > 60:
					SleepCount = 1
         
		return True      

	def sendDataToServer(self, jsonData):
		sizeOfDataToSend = len(jsonData)
		totalSizeOfDataSent = 0

		try:
			firstPos = 0
			self.socketToConnect.sendall(jsonData[firstPos:] + "\r\n")	

		except NetworkError as e:
			print e	

		return 1
	
	def recvDataFromServer(self):
		DEFAULT_RECV_BUF_SIZE = 2 << 16	
		BLOCK_TIME = 5

		ConnectionReadSet = [self.socketToConnect]
		recvData = ""

		Running = True
		while Running:
			FD_ReadSet, FD_WriteSet, FD_ExceptionSet = select.select(ConnectionReadSet, [], [], BLOCK_TIME) 

			dataFromServer = ""

			for ReadSocket in FD_ReadSet:
				dataFromServer = ReadSocket.recv(DEFAULT_RECV_BUF_SIZE) 
				if dataFromServer:
					recvData += dataFromServer
				else:
					Running = False

		Log.debug("Received data From server [%d]" % len(recvData))
		return recvData

	def startNetworkingWithData(self, data, nttype):
		recvData = ""

		if not self.connectToServer():
			return recvData

		try:
			Recorder = CRecorder()
			Recorder.startRecord()

			ret = self.sendDataToServer(data)
			if ret != 1:
				raise NetworkError(ret)			

			recvData = self.recvDataFromServer()			
			
			sizeOfRecvData = len(recvData)
			if sizeOfRecvData < 1:
				raise NetworkError(sizeOfRecvData)

			Recorder.endRecord()
			responseTime = Recorder.getResponseTime()

			if nttype == TYPE_NT_BROKER:
				Log.debug("BROKER RES : " + str(responseTime))
			elif nttype == TYPE_NT_EP:
				Log.debug("ENTRYPOINT RES : " + str(responseTime))
			elif nttype == TYPE_NT_DBCS:
				Log.debug("DBPOOLSERVER RES : " + str(responseTime))

		except NetworkError as e:
			print e

		return recvData

class Broker(AbstractNetwork):
	def __init__(self):
		'''
			Broker IP, Port are fixed
		'''
		brokerIPAddress = "165.132.122.242"
		brokerPort = 7500
		AbstractNetwork.__init__(self, brokerIPAddress, brokerPort)

	'''
	def __del__(self):
		AbstractNetwork.__del__(self)
	'''

	def closeConnection(self):
		AbstractNetwork.closeConnection(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data, TYPE_NT_BROKER)

class EntryPoint(AbstractNetwork):
	def __init__(self, ipAddress):
		'''
			EntryPoint Port are fixed
		'''
		entrypointPort = 7777
		AbstractNetwork.__init__(self, ipAddress, entrypointPort)

	'''
	def __del__(self):
		AbstractNetwork.__del__(self)
	'''

	def closeConnection(self):
		AbstractNetwork.closeConnection(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data, TYPE_NT_EP)

class DBPoolServer(AbstractNetwork):
	def __init__(self):
		'''
			DBPoolServer Port are fixed
		'''
		#dbPoolServerIPAddress = "192.168.56.1"
		dbPoolServerIPAddress = "165.132.123.83"
		dbPoolServerPort = 6000
		AbstractNetwork.__init__(self, dbPoolServerIPAddress, dbPoolServerPort)

	'''
	def __del__(self):
		AbstractNetwork.__del__(self)
	'''

	def closeConnection(self):
		AbstractNetwork.closeConnection(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data, TYPE_NT_DBCS)

class Manager(AbstractNetwork):
	def __init__(self, managerIPAddress):
		#managerIPAddress = socket.gethostbyname(socket.gethostname())
		managerPort = 7200
		AbstractNetwork.__init__(self, managerIPAddress, managerPort)
	
	'''
	def __del__(self):
		AbstractNetwork.__del__(self)
	'''

	def closeConnection(self):
		AbstractNetwork.closeConnection(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data, TYPE_NT_MANAGER)
