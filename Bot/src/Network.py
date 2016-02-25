from socket import *
import time

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


class AbstractNetwork:
	DEFAULT_RECV_BUF_SIZE = 8192

	def __init__(self, ipAddress, Port):
		networkSettingComponent = NetworkSettingComponent()

		networkSettingComponent.setIPAddressToConnect(ipAddress)
		networkSettingComponent.setPortToConnect(Port)
		networkSettingComponent.setAddressFamily(TYPE_AF_INET)
		networkSettingComponent.setProtocolType(TYPE_PROTOCOL_TCP)

		self.configureEnviromentAndSocket(networkSettingComponent)

	def __del__(self):
		self.socketToConnect.close()

	def configureEnviromentAndSocket(self, networkSettingComponent):
		serverIPAddress = networkSettingComponent.getIPAddressToConnect()
		serverPort = networkSettingComponent.getPortToConnect()
		self.serverConnectionAddress = (serverIPAddress, serverPort)

		serverAFType = networkSettingComponent.getAddressFamily()
		serverProtoType = networkSettingComponent.getProtocolType()
		self.socketToConnect = socket(serverAFType, serverProtoType)

	def connectToServer(self):
		try:
			self.socketToConnect.connect(self.serverConnectionAddress)
		except Exception as e:
			return 0
		return 1

	def sendDataToServer(self, jsonData):
		sizeOfDataToSend = len(jsonData)
		totalSizeOfDataSent = 0

		while totalSizeOfDataSent < sizeOfDataToSend:
			remainedSize = sizeOfDataToSend - totalSizeOfDataSent
			firstPos = sizeOfDataToSend - remainedSize

			lengthOfDataSent = self.socketToConnect.send(jsonData[firstPos:])
			totalSizeOfDataSent += lengthOfDataSent

		if totalSizeOfDataSent > sizeOfDataToSend:
			pass

		return 1

	def recvDataFromServer(self):
		DEFAULT_RECV_BUF_SIZE = 4096
		return self.socketToConnect.recv(DEFAULT_RECV_BUF_SIZE)

	def startNetworkingWithData(self, data):
		recvData = ""

		if not self.connectToServer():
			return recvData

		try:
			ret = self.sendDataToServer(data)
			if ret != 1:
				raise NetworkError(ret)

			time.sleep(1)

			recvData = self.recvDataFromServer()
			sizeOfRecvData = len(recvData)
			if sizeOfRecvData < 1:
				raise NetworkError(sizeOfRecvData)				

		except NetworkError as e:
			pass

		return recvData

class Broker(AbstractNetwork):
	def __init__(self):
		'''
			Broker IP, Port are fixed
		'''
		brokerIPAddress = "165.132.122.243"
		brokerPort = 7500
		AbstractNetwork.__init__(self, brokerIPAddress, brokerPort)

	def __del__(self):
		AbstractNetwork.__del__(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data)

class EntryPoint(AbstractNetwork):
	def __init__(self, ipAddress):
		'''
			EntryPoint Port are fixed
		'''
		entrypointPort = 7777
		AbstractNetwork.__init__(self, ipAddress, entrypointPort)

	def __del__(self):
		AbstractNetwork.__del__(self)

	def startNetworkingWithData(self, data):
		return AbstractNetwork.startNetworkingWithData(self, data)



