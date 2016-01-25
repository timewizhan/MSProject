# -*- coding: utf-8 -*-
import requests as rq
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC # available since 2.26.0
from selenium.webdriver.support.ui import WebDriverWait # available since 2.4.0
from selenium.webdriver.common.action_chains import ActionChains

import sys,os,time
from bs4 import BeautifulSoup
import requests as rs
import Queue

facebook_url = ''
facebook_id = ''
facebook_passwd = ''

timeLineScrollDown = 5

#class PyDatabase:


class FacebookCrawler:
    nameQueue = Queue.Queue()
    urlQueue = Queue.Queue()

    driver = None

    initialPageAddress = None
    profilePageAddress = None
    friendsPageAddress = None

    htmlSource = None
    navigator = None
    
    SHARED = 0
    WRITTEN_ALONE = 1
    WRITTEN_OTHER = 2

    def __init__(self):
        self.driver = webdriver.PhantomJS(service_args=['--ssl-protocol=any'])
        self.driver.get(facebook_url)
        self.driver.maximize_window()

    def __del__(self):
        self.driver.close()

    def login(self):
        self.driver.find_element(By.ID, value='email').send_keys(facebook_id)
        self.driver.find_element(By.ID, value='pass').send_keys(facebook_passwd)
        self.driver.find_element(By.ID, value='loginbutton').click()

        time.sleep(2)

    def get_friends(self):
        self.driver.get(self.friendsPageAddress)
        self.htmlSource = self.driver.page_source
        self.navigator = BeautifulSoup(self.htmlSource, "lxml")

        basic_friend_count = 20
        friend_count = self.navigator.find_all('span', class_="_3d0")[0].text
        cycle = (int(friend_count) - basic_friend_count) / 20 + 1

        for i in range(0,cycle):
            self.driver.execute_script("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));")
            time.sleep(1)

        # As html file is updated need to get html again
        self.htmlSource = self.driver.page_source
        self.navigator = BeautifulSoup(self.htmlSource, "lxml")

        # Get fiends information (name, page url)
        friendSection = self.navigator.select('.fsl.fwb.fcb a')

        for lists in friendSection:
            self.nameQueue.put(lists.text)
            self.urlQueue.put(lists['href'])   

    def get_profile(self):
        self.driver.get(self.profilePageAddress)
        self.htmlSource = self.driver.page_source
        self.navigator = BeautifulSoup(self.htmlSource, "lxml")

        # 디비에 연결할 때 프로필 정보 넣는 곳으로 넣어야함.
        print self.navigator.select('.profileLink')[0].text
        
    def get_timeLines(self):
        time.sleep(2)
        self.driver.get(self.initialPageAddress)
        print self.initialPageAddress

        self.htmlSource = self.driver.page_source
        self.navigator = BeautifulSoup(self.htmlSource, "lxml")
        print self.navigator.title.text

        time.sleep(2)
        self.driver.save_screenshot('22222.png')

        
        for i in range(0, timeLineScrollDown):
            self.driver.execute_script("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));")
            time.sleep(1)
        
        self.driver.save_screenshot('timelines.png')        
        
        self.htmlSource = self.driver.page_source
        self.navigator = BeautifulSoup(self.htmlSource, "lxml")

        # Get timeline posts and reply, like
        if self.navigator.select('._4-u2.mbm._5jmm._5pat._5v3q._4-u8._x72._50nb'):
            for outSector in self.navigator.select('._5pcb._4b0l'):
                for inSector in outSector.select('.userContentWrapper._5pcr'):
                    
                    # Get posts
                    numberOfWriter = len(inSector.select('.fwb.fcg a'))
                    #print numberOfWriter

                    name = inSector.select('.fwb.fcg a')

                    if numberOfWriter == self.SHARED:
                        writer = inSector.select('.fwb a')[0].text
                        sharedWriter = inSector.select('.fcg a')[1].text
                        print writer, sharedWriter

                    elif numberOfWriter == self.WRITTEN_ALONE:
                        writer = name[0].text
                        print writer

                    elif numberOfWriter == self.WRITTEN_OTHER:  
                        writer = name[0].text
                        receiver = name[1].text
                        print writer, receiver

                    else :
                        pass
                        
                    writtenDate = inSector.select('._5pcq abbr')[0].get('title')
                    print writtenDate

                    
                    # Get likes
                    if inSector.select('.UFINoWrap'):
                        self.driver.find_element(By.CLASS_NAME, value='UFINoWrap').click()
                       
                        time.sleep(2)
                        self.driver.save_screenshot('11111.png')

                        # local variables
                        popUpPage = self.driver.page_source
                        navigator = BeautifulSoup(popUpPage, 'lxml') 

                        likeUser = navigator.select('.fsl.fwb.fcb a')

                        for user in likeUser:
                            print user.text

                        self.driver.find_element_by_css_selector('._42ft._5upp._50zy.layerCancel._51-t._50-0._50z-').click()

                        time.sleep(2)
                        closePage = self.driver.page_source
                        self.driver.save_screenshot('22222.png')

                    elif inSector.select('.UFILikeSentenceText span'):
                        for user in inSector.select('.UFILikeSentenceText a'):
                            print user.text

                    else :
                        pass
    

                    #Get reply
                    if inSector.select('.UFICommentContentBlock'):
                        if not inSector.select('.UFIPagerLink'):
                            for reply in inSector.select('.UFICommentContentBlock'):
                                print reply.select('.UFICommentActorName')[0].text
                                print reply.select('.livetimestamp')[0].get('title')
                        
                        else :
                            flag=0

                            if flag == 0:
                                tmp_inSector = inSector.select('.UFIPagerLink')
                                print tmp_inSector[0].text
                                flag = 1

                            while tmp_inSector:
                                self.driver.find_element_by_css_selector('.UFIPagerLink').click()

                                time.sleep(2)

                                afterPage = self.driver.page_source
                                afterSoup = BeautifulSoup(afterPage, "lxml")

                                print afterSoup.select('.UFIPagerLink')

                                if flag == 1:
                                    tmp_inSector = afterSoup.select('.UFIPagerLink')


                            for reply in afterSoup.select('.UFICommentContentBlock'):
                                print reply.select('.UFICommentActorName')[0].text
                                print reply.select('.livetimestamp')[0].get('title') 

                    else :
                        pass

        else :
            print "Timeline is private mode or there is not posting"

    def goToTimeLine(self):
        self.initialPageAddress = 'https://www.facebook.com/kyehee.kim.3'
        self.friendsPageAddress = self.initialPageAddress + '/friends'
        self.profilePageAddress = self.initialPageAddress + '/about'


    def crawling(self):
        self.login()
        self.goToTimeLine()
        self.get_timeLines()
        self.get_friends()
        self.get_profile()

if __name__ == '__main__':
    crawler = FacebookCrawler()
    crawler.crawling()
