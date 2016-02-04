import tweepy
import sys
import queue
import time
import imp
import psycopg2
import os

imp.reload(sys)
#sys.setdefaultencoding('utf-8')
 

API_KEY = ''
API_SECRET = ''
ACCESS_KEY = ''
ACCESS_SECRET = ''
 
oAuth = tweepy.OAuthHandler(API_KEY, API_SECRET)
oAuth.set_access_token(ACCESS_KEY, ACCESS_SECRET)
api = tweepy.API(auth_handler = oAuth, api_root = '/1.1')

#   Error Type
ERROR_SUCCESS = 0
ERROR_EXIST = 1
ERROR_FAIL = 2

save_queue = queue.Queue()
#savedFriendInfo = []

#   Database class for Postgres API
class PyDatabase:
    def __init__(self):
        self.connection = None

    def connectToDB(self):
        try:
            self.connection = psycopg2.connect("user='postgres' password='xeros' dbname='postgres' hostaddr='165.132.120.152' port=5432'")   
            if self.connection:
                print ("[Info] DB connection is initialized")
        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)
            sys.exit(1)

    def disconnectFromDB(self):
        if self.connection:
            self.connection.close()

    def InsertUserPropertyToDB(self, userName, userPlace, tweetID, tweetTime):
        ret = ERROR_SUCCESS
        try:
            cur = self.connection.cursor()
            cur.execute("SELECT \"UserName\" FROM \"UserProperty\" WHERE \"UserName\"=\'%s\' AND \"TweetID\"=\'%s\'" % (userName, tweetID))
            rows = cur.fetchone()
            if rows == None:
                #print "[Info] Insert new data to UserProperty"
                cur.execute("INSERT INTO \"UserProperty\"(\"TweetTime\", \"UserName\", \"UserPlace\", \"TweetID\") VALUES(%s, %s, %s, %s)", (tweetTime, userName, userPlace, tweetID))
                self.connection.commit()
            else:
                ret = ERROR_EXIST                

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)    
            return ERROR_FAIL

        return ret

    def UpdateTweetToDB(self, srcUser, dstUser, forWho):
        ret = ERROR_SUCCESS
        try:
            cur = self.connection.cursor()
            if dstUser != None:
                cur.execute("SELECT \"SourceName\", \"DestinationName\", \"TweetMyself\", \"TweetOther\" FROM \"UserLink\" WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (srcUser, dstUser))
            else:
                cur.execute("SELECT \"SourceName\", \"DestinationName\", \"TweetMyself\", \"TweetOther\" FROM \"UserLink\" WHERE \"SourceName\"=\'%s\' AND \"DestinationName\" is NULL" % (srcUser))

            row = cur.fetchone()
            if row != None:
                if forWho == True:
                    tweetForOtherCount = int(row[3]) + 1
                    cur.execute("UPDATE \"UserLink\" SET \"TweetOther\"=%d WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (tweetForOtherCount, srcUser, dstUser))
                else:
                    tweetForMeCount = int(row[2]) + 1
                    cur.execute("UPDATE \"UserLink\" SET \"TweetMyself\"=%d WHERE \"SourceName\"=\'%s\' AND \"DestinationName\" is NULL" % (tweetForMeCount, srcUser))

                self.connection.commit()
            else:
                if forWho == True:
                    cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s)", (srcUser, dstUser, 0, 1, 0))
                else:
                    cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s)", (srcUser, dstUser, 1, 0, 0))

                self.connection.commit()

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)
            return ERROR_FAIL

        return ret

    def UpdateRetweetToDB(self, srcUser, dstUser):
        ret = ERROR_SUCCESS
        try:
            cur = self.connection.cursor()
            cur.execute("SELECT \"SourceName\", \"ReTweet\" FROM \"UserLink\" WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (srcUser, dstUser))

            row = cur.fetchone()
            if row != None:
                retweetCount = int(row[1]) + 1
                cur.execute("UPDATE \"UserLink\" SET \"ReTweet\"=%d WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (retweetCount, srcUser, dstUser))
            else:
                cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s)", (srcUser, dstUser, 0, 0, 1))                

            self.connection.commit()

        except psycopg2.DatabaseError as e:
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)  
            return ERROR_FAIL

        return ret

    def InsertFriendToDB(self, srcName, dstName):
        ret = ERROR_SUCCESS
        try:
            cur = self.connection.cursor()
            cur.execute("SELECT \"SourceName\" FROM \"UserLink\" where \"SourceName\"=%s AND \"DestinationName\"=%s", (srcName, dstName))
            row = cur.fetchone()
            if row != None:
                cur.execute("UPDATE \"UserLink\" SET \"Friend\"=%s WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (True, srcName, dstName))
            else:
                cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"Friend\", \"Follow\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s, %s, %s)", (srcName, dstName, True, False, 0, 0, 0))
            """
            cur.execute("SELECT \"SourceName\" FROM \"UserLink\" where \"SourceName\"=%s AND \"DestinationName\"=%s", (dstName, srcName))
            row = cur.fetchone()
            if row != None:
                cur.execute("UPDATE \"UserLink\" SET \"Friend\"=%s WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (True, dstName, srcName))
            else:
                cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"Friend\", \"Follow\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s, %s, %s)", (dstName, srcName, True, False, 0, 0, 0))
            """
            self.connection.commit()

        except psycopg2.DatabaseError as e:
    
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)
            return ERROR_FAIL

        return ret    
                 
    def InsertfollowerToDB(self, srcName, dstName):
        ret = ERROR_SUCCESS
        try:
            cur = self.connection.cursor()
            cur.execute("SELECT \"SourceName\" FROM \"UserLink\" where \"SourceName\"=%s AND \"DestinationName\"=%s", (srcName, dstName))
            row = cur.fetchone()
            if row != None:
                cur.execute("UPDATE \"UserLink\" SET \"Follow\"=%s WHERE \"SourceName\"=\'%s\' AND \"DestinationName\"=\'%s\'" % (False, srcName, dstName))
            else:
                cur.execute("INSERT INTO \"UserLink\"(\"SourceName\", \"DestinationName\", \"Friend\", \"Follow\", \"TweetMyself\", \"TweetOther\", \"ReTweet\") VALUES(%s, %s, %s, %s, %s, %s, %s)", (srcName, dstName, False, True, 0, 0, 0))

            self.connection.commit()

        except psycopg2.DatabaseError as e:
    
            if self.connection:
                self.connection.rollback()
    
            print ('[Error] %s' % e)
            return ERROR_FAIL

        return ret 

class TwitterHelper:
    LIMIT_CALL_COUNT = 300
    SECOND = 1
    MINUTE = SECOND * 60
    HOUR = MINUTE * 60
    
    friends_list = []
    followers_list = []
    retweets_list = []
    
    def __init__(self, api):
        self.tweepyApi = api
        self.PyDB = PyDatabase()
        self.PyDB.connectToDB()
    
    def __del__(self):
        self.PyDB.disconnectFromDB()
        
    def limit_handled(self, cursor):
        while True:
            try:
                yield cursor.next()
            except tweepy.RateLimitError:
                now =time.localtime() 
                print ("[limit_handled Error] wait for 15 minute")
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                time.sleep(self.MINUTE*15)
                continue
            
            except tweepy.TweepError as e:
                now =time.localtime() 
                print ("[Tweepy Error1] %s" % e)
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                f = open("Error_log.txt", "a")
                data = now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec
                f.write(str(e)+" "+ str(data))
                f.close()
                time.sleep(self.MINUTE * 1)
                continue
            
                        
    def getValid(self, userID):
        validNum = 10000
        try:
            user = api.get_user(userID)
            userFriend_count = user.friends_count
            userFollower_count = user.followers_count
            userTimeline_count = user.statuses_count
        
            if userFriend_count < validNum and userFollower_count < validNum*2 and userTimeline_count < validNum:
                print ("[%s Account Info]: friend_count: %s, follower_count: %s, timeLine_count: %s" % (userID, userFriend_count, userFollower_count, userTimeline_count))
                return 1
            else:
                print ("[%s Account Info]: Invalid Account" % (userID))
                return 0
        except tweepy.RateLimitError:
            now =time.localtime() 
            print ("[Error] wait for 15 minute")
            print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
            time.sleep(self.MINUTE*15) 


    def getFriend(self, userID):
        for friend in self.limit_handled(tweepy.Cursor(api.friends, screen_name=userID).items()):
            try:
                save_queue.put(friend.screen_name)
                self.friends_list.append(friend.screen_name)
            
                print ("[Info] %s's following info %s" % (userID, friend.screen_name))
            
                ret = self.PyDB.InsertFriendToDB(userID, friend.screen_name)
                
                if ret == ERROR_FAIL:
                    print ("[Error] Fail to insert friend data")
                    continue
                
                
                                                   
            except tweepy.RateLimitError:
                now =time.localtime() 
                print ("[getFriend Error] wait for 15 minute")
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                time.sleep(self.MINUTE*15)
                
        #print "[GlobalInfo] Until now %s account saved" % (savedFriendInfo.__len__())    
        return 0;    
  
    def getFollower(self, userID):
        for follower in self.limit_handled(tweepy.Cursor(api.followers, screen_name=userID).items()):
            try:
                self.followers_list.append(follower.screen_name)
            
                print ("[Info] %s's follower info %s" % (userID, follower.screen_name))
            
                ret = self.PyDB.InsertfollowerToDB(userID, follower.screen_name)
                
                if ret == ERROR_FAIL:
                    print ("[Error] Fail to insert follower data")
                    continue
                
            except tweepy.RateLimitError:
                now =time.localtime() 
                print ("[getFollower Error] wait for 15 minute")
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                time.sleep(self.MINUTE*15)
                
        return 0;
    
    def getTimeLine(self, userID):
        user = api.get_user(userID)
        userLocation = user.location
        
        for tweet in self.limit_handled(tweepy.Cursor(api.user_timeline, screen_name=userID).items()): 
            try:
                '''
                    get value from tweet
                    createTime, timelineID, retweetCount, reply name
                '''

                # First, Insert tweet data to DB
                # DB table is UserProperty
                tweetcreateTime = tweet.created_at
                tweetID = tweet.id_str
                #tweetPlace = tweet.place
                print ("[Info] Got a tweet -> User : %s, tweet : %s" % (userID, tweetID))
                ret = self.PyDB.InsertUserPropertyToDB(userID, userLocation, tweetID, tweetcreateTime)
                #print ret
                if ret == ERROR_SUCCESS:
                    # Second, Insert tweet data To DB
                    # DB table is UserLink
                    retweetCount = tweet.retweet_count
                    self.getRetweetlist(userID, tweetID, retweetCount)

                    # Third, Check where this tweet is for me or for other
                    # if it is for other, this data is reply tweet
                    otherUsername = tweet.in_reply_to_screen_name
                    if otherUsername != None:
                        self.PyDB.UpdateTweetToDB(userID, otherUsername, True)
                    else:
                        self.PyDB.UpdateTweetToDB(userID, otherUsername, False)
                    
                elif ret == ERROR_EXIST:
                    print ("[Info] tweet data is already saved")
                else:
                    print ("[ERROR] Fail to insert data to UserProperty DB")
        
                
            except tweepy.RateLimitError:
                now =time.localtime() 
                print ("[getTimeLine Error] wait for 15 minute")
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                time.sleep(self.MINUTE*15)
        return 0;         

    def getRetweetlist(self, userID, tweetID, retweetCount):
        retweets_data = api.retweets(tweetID, retweetCount)
        
        current_count = 0
        for retweet_data in retweets_data:
            try:
                retweet_id = retweet_data._json[u'user'][u'screen_name']
                print ("[Info] Got a retweet -> User : %s, retweeter : %s" % (userID, retweet_id))
                self.retweets_list.append(retweet_id)
                ret = self.PyDB.UpdateRetweetToDB(userID, retweet_id)
                if ret == ERROR_FAIL:
                    print ("[Error] Fail to insert friend data")
                    continue

                current_count += 1
            except tweepy.RateLimitError:
                now =time.localtime() 
                print ("[getRetweetlist Error] wait for 15 minute")
                print ("%04d-%02d-%02d %02d:%02d:%02d" % (now.tm_year, now.tm_mon, now.tm_mday, now.tm_hour, now.tm_min, now.tm_sec))
                time.sleep(self.MINUTE*15)
                 

        print ("[Info] Got count of retweet [%d/%d]" % (current_count, retweetCount))
        
                
    #def getRetweet(self, userID):
  
        
def crawlingTwitter(userCriteriaID):
    bContinue = True;
    bFirst = True;
    
    twitterApi = TwitterHelper(api)
    
    while bContinue:
        if bFirst:
            userID = userCriteriaID
            bFirst = False
        else:
            userID = save_queue.get()
               
        if twitterApi.getValid(userID):
            #savedFriendInfo.append(userID)
            twitterApi.getFriend(userID)
            twitterApi.getFollower(userID)
            twitterApi.getTimeLine(userID)

        else:
            continue
        
        if save_queue.empty():
            print ('crawling end')
            os.system("pause")
                       
        
if __name__ == "__main__":
    userCriteriaID = "gdenny517"
    if len(userCriteriaID) < 1:
        print ("[Error] Invalid argument")
        sys.exit(1)
    
    # main function    
    crawlingTwitter(userCriteriaID)