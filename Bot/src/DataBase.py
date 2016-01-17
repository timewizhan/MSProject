import psycopg2

class PyDatabase:
    def __init__(self):
        self.connection = None

    def connectToDB(self):
        try:
            self.connection = psycopg2.connect("user='postgres' password='xeros' dbname='postgres' hostaddr='165.132.120.152' port=5432'")   
            if self.connection:
                print "[Info] DB connection is initialized"
        except psycopg2.DatabaseError, e:
            if self.connection:
                self.connection.rollback()
    
            print '[Error] %s' % e
            sys.exit(1)

    def disconnectFromDB(self):
        if self.connection:
            self.connection.close()

    def querySQL(self, sql):

        try:
            cur = self.connection.cursor()
            cur.execute(sql)

            return cur.fetchall()
        except psycopg2.DatabaseError, e:
            if self.connection:
                self.connection.rollback()
    
            print '[Error] %s' % e    

    