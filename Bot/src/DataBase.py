import psycopg2
from Log import *

class PyDatabase:
    def __init__(self):
        self.connection = None

    def connectToDB(self):
        try:
            self.connection = psycopg2.connect("user='postgres' password='1111' dbname='import' hostaddr='165.132.123.83' port=5432'")   
            if self.connection:
                Log.debug("DB connection is initialized")

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
                self.connection.close()
    
            Log.error("%s" % e)

    def disconnectFromDB(self):
        if self.connection:
            self.connection.close()

    def querySQL(self, sql):
        try:
            cur = self.connection.cursor()
            cur.execute(sql)

            rows = cur.fetchall()
            self.connection.commit()

            return rows
        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            Log.error("%s" % e)

    def insertSQL(self, sql):
        try:
            cur = self.connection.cursor()
            cur.execute(sql)                
            self.connection.commit()

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            Log.error("%s" % e)

    def updateSQL(self, sql):
        try:
            cur = self.connection.cursor()
            cur.execute(sql)                
            self.connection.commit()

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            Log.error("%s" % e)

   

    