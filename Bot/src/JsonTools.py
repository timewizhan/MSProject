import json  

class JsonGenerator:
	def __init__(self):
		self.jsonDic = {}

	def appendElement(self, key, value):
		self.jsonDic[key] = value

	def toString(self):
		return json.dumps(self.jsonDic)
