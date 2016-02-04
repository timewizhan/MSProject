[33mcommit c398566cea1df05f32091c8c1d35db9f006d5ba9[m
Merge: 46e83ce f71e8ad
Author: alphahacker <alphahacker.han@gmail.com>
Date:   Thu Feb 4 19:00:22 2016 +0900

    Merge https://github.com/timewizhan/MSProject

[33mcommit 46e83ce7c95644b7b7122b84d5cd4a821390ce81[m
Author: alphahacker <alphahacker.han@gmail.com>
Date:   Thu Feb 4 18:51:33 2016 +0900

    Initial commit

[33mcommit f71e8ad94f9a3a40fc0db2665176988fbc7ff041[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Thu Feb 4 17:47:51 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    - modify getTrafficLog method
    - now getTrafficLog method returns an array of userInfo objects
    - userInfo object stores UID, UNAME, LOCATION, and TOTAL TRAFFIC of each
    user
    - userInfo object will be used for generating a report message to Broker

[33mcommit ac9413660ec9f0f27be2de56b0e6dbc523dbae3c[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Tue Feb 2 17:46:26 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    - add userInfo.java for a wrapper class
    - add basic method for reporting monitoring results
    - minor changes in codes

[33mcommit 3fb0582f800144eadce717989af5d759dd44b5da[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Mon Feb 1 17:34:57 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    - Deleted unused codes

[33mcommit 935cd68884525dc24bd50f10cb877cd66da3ee41[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Mon Feb 1 17:30:13 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    - Minor changes in trace codes

[33mcommit da296c8264b0332285a8367db0524b9acd50ea52[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Mon Feb 1 17:28:51 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    - added statusInfo.java as the wrapper class
    - Implemented and tested writeStatus(), readStatus(), and writeReply()
    methods
    - DB schema has been changed for monitoring
    - DB now stores the traffic volume for each operation (i.e. total bytes
    for each operation), where each operation includes request and response
    messages

[33mcommit 7be729670e68107142abe213f17acafee3daa320[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Wed Jan 27 23:47:32 2016 +0900

    [MSProject_BrokerGiver][timewizhan][RV:N/A]
    1. Common 파일 추가

[33mcommit 5d6557466b69a38524bd016253c32c5f477bc3ea[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Wed Jan 27 23:46:39 2016 +0900

    [MSProject_BrokerGiver][timewizhan][RV:N/A]
    1. IOCP를 통한 BrokerGiver 생성

[33mcommit f7983736da9bc3ddfdb1a29cb2939c7e43e1bc1a[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Wed Jan 27 23:43:11 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. DummyClient 에러 수정
    - socket close이 후에 socket을 새로 만들어 줘야함
    - close 하면 해당 소켓을 사용 할 수 없음

[33mcommit 384e42c0d0e41375c98e34ff936af1df8b230db4[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Tue Jan 26 23:46:35 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    
    Apply minor code changes

[33mcommit dfdedd8804b5b351e194dc8bb91cebd4806b119c[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Tue Jan 26 23:41:27 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    
    - add DBConnection.java reponsible for handling DB connection (e.g.
    select, insert, and update)
    
    - update ServiceServer code and integrate repeatedly used code as method

[33mcommit 5ef6630b48fffda4138d17f01136a3fedf5be021[m
Author: 김계희 <kkh0590@naver.com>
Date:   Tue Jan 26 21:45:14 2016 +0900

    [MSProject_Crawler][kkh0590][RV:N/A]
    
    1. 기본 크롤링 구현완료(에러 수정)

[33mcommit f60d56dc983edafb23de68946c6f72b68cfe7200[m
Author: 김계희 <kkh0590@naver.com>
Date:   Mon Jan 25 19:57:15 2016 +0900

    [MSProject_Crawler][kkh0590][RV:N/A]
    
    1. 기본 크롤링 구현완료
    2. 디비 미적용

[33mcommit 45172191d0696cdd59d12dc617edfb0d6b3eff9e[m
Author: 김계희 <kkh0590@naver.com>
Date:   Mon Jan 25 12:21:25 2016 +0900

    [MSProject_Crawler][kkh0590][RV:N/A]
    
    1. 트위터 크롤러 토큰 삭제 후 재업로드

[33mcommit a4c39553f207b2828d793236188fd4e02f4bd275[m
Author: 김계희 <kkh0590@naver.com>
Date:   Mon Jan 25 04:25:04 2016 +0900

    [MSProject_Crawler][kkh0590][RV:N/A]
    
    1. 페이스북크롤러올리는데 계정 주소랑 비번안지우고올려서 다시올림;

[33mcommit f325739f07535161b5d6573205963c6577a9167b[m
Author: 김계희 <kkh0590@naver.com>
Date:   Mon Jan 25 04:09:51 2016 +0900

    [MSProject_Crawler][kkh0590][RV:N/A]
    
    1. 트위터 크롤러 첨부
    2. facebook 크롤러 커밋(아직 미완)

[33mcommit dee346748617bf78ad872c00601442a5dc0fcaf4[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 24 18:48:31 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    (트리 변경 후 재커밋)
    1. dummy client 코드 추가
    2. json.txt 파일 추가
    3. dummy server 디렉토리 생성

[33mcommit 8cd6c4cccf8ddcbde50b30d7052feb692c09df04[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 24 18:47:01 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 소스 트리 정리

[33mcommit 3d85e7b8eb27f81e962e417596ba6788e3000233[m
Merge: 9771c16 e11dd7c
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 24 18:42:33 2016 +0900

    Merge branch 'master' of https://github.com/timewizhan/MSProject

[33mcommit 9771c16f56fbeb3872dc5093da83718ec7108374[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 24 18:41:01 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. dummy client 코드 추가
    2. json.txt 파일 추가
    3. dummy server 디렉토리 생성

[33mcommit e11dd7c68d25250eb54001e09d8e2138e3489ea3[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Thu Jan 21 19:33:58 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    
    add JDBC connection code and psedo codes for each operation

[33mcommit 70c816952b1712e4e59a55267f084a7a3e47efa5[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Thu Jan 21 02:19:51 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 내부 동작 오류 수정
    2. DB 종료 코드 추가 (소멸자에서)

[33mcommit 363070a58113c5b03b60c10d27f10dec5af8f4e5[m
Author: alphahacker <alphahacker@alphahacker-desktop>
Date:   Thu Jan 21 00:00:31 2016 +0900

    [MSProject_Broker][Alphahacker][RV:timewizhan]
    1. thread 추가

[33mcommit dd467577b786c67e633613e9f715bc2dbc1c7316[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 17 21:47:35 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. updateSQL 모듈 추가
    2. saveResultToDataBase 모듈 추가

[33mcommit 7683faf2cfa3765b38bf39010a6b53f8cd3b81af[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 17 21:40:43 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 오류 제거

[33mcommit fb331ac6cc16b610d726375625a0fc72fb0338fb[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 17 17:53:21 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 스케줄러 알고리즘 수정
    2. __init__ 파일 추가 (패키지 import 용도)
    3. 에러 수정

[33mcommit 7bfb63f4fb4b3fa136073f988cc83e95f2b29e56[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 17 16:18:15 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. Bot 메인 수정
    2. 데이터베이스 코드 수정

[33mcommit ad6cc9efaaecf9ec74ec04dcc3ee9989016a57e8[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sun Jan 17 15:17:59 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 시간, 행동 패턴 알고리즘 추가
    2. PatternDelegator 코드 추가
    3. 스케줄러에 패턴 코드 적용
    4. job 구조 변경
    5. 데이터 베이스 소스 파일 추가

[33mcommit 994783b7b1f5c1419cdd8f6ab9a2f56304a05557[m
Author: alphahacker <alphahacker.han@gmail.com>
Date:   Fri Jan 15 18:57:33 2016 +0900

    DB Connection 추가
    DB Test Code 추가

[33mcommit 060218927fb9a256b6457f5d52f29b5f0380e7cd[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Thu Jan 14 16:07:58 2016 +0900

    [MSProject_SNS][bokorkim[RV:N/A]
    
    add an example code for inserting delay

[33mcommit 0d81f9248c6ae5ad70035c375f575465fdc3decf[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Thu Jan 14 16:01:48 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/V]
    add mysqlconnector and implemet example code for connecting to the DBMS

[33mcommit f7a9b05561ac0403719880b593f4ee679dcab479[m
Author: alphahacker <alphahacker.han@gmail.com>
Date:   Wed Jan 13 21:53:10 2016 +0900

    [MSProject_EntryPoint][alphahacker][RV:N/A]
    
    1. Broker랑 통신하기위한 소켓프로그래밍
    2. Broker와 마찬가지로 Select 멀티플렉싱 함수 적용

[33mcommit 64341104b4dc15503bc421ea26f539af3563b06c[m
Author: alphahacker <alphahacker.han@gmail.com>
Date:   Wed Jan 13 21:49:38 2016 +0900

    [MSProject_Broker][alphahacker][RV:N/A]
    
    1. Entry Point(EP)들이랑 통신할 수 있게 소켓 프로그래밍 함
    2. EP들이 동시에 접속하는 것에 대처하기 위해 Select 멀티플렉싱 사용

[33mcommit 83bb639a0f27842484c5e412c2251f5ccdf44e40[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Tue Jan 12 22:57:04 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 의미 없이 추가 된 코드 삭제

[33mcommit 46d1cd9a7f7b0b299eb1e8dd09c4c9aba22870ce[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Tue Jan 12 22:55:26 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. 로그 클래스 생성

[33mcommit feb0ab5670c24ce4ad06554ee009a5cc69a1429f[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Tue Jan 12 22:38:17 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. Json 클래스 생성
    2. 네트워킹 내부 클래스 추가
    3. 네트워크 스케줄러에 적용
    4. Job 함수 추가

[33mcommit 0a8ed544731f026184eb141f06324adc1966102e[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Tue Jan 12 21:46:58 2016 +0900

    [MSProjec_SNS][bokorkim][RV:N/A]
    
    Parameter changes in handling operation methods

[33mcommit 79c7cf4e3896a24a35cacaf1180c71e01174fb14[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Tue Jan 12 16:32:50 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    
    update handling operation method

[33mcommit 7782fc250faa27b865e2036dc2e9627a65f8a915[m
Author: Bokor <bokor.kim@gmail.com>
Date:   Mon Jan 11 21:11:13 2016 +0900

    [MSProject_Server][bokorkim][RV:N/A]
    
    add ServiceServer.java

[33mcommit bd4b02c37d6814d2127476f776be948d742bb247[m
Author: bokorkim <bokor.kim@gmail.com>
Date:   Mon Jan 11 20:55:34 2016 +0900

    [MSProject_SNS][bokorkim][RV:N/A]
    
    add RV to the title

[33mcommit 7d23c617ff07e73675a13db65587b8d2db98f54a[m
Author: bokorkim <bokor.kim@gmail.com>
Date:   Mon Jan 11 20:46:39 2016 +0900

    [MSProject_SNS][bokorkim]
    
    modify the name from EntryPoint to

[33mcommit 414ddf7cd97354ea1f58b3b3136523ca571b4574[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Mon Jan 11 20:43:32 2016 +0900

    [MSProject_Server][timewizhan][RV:bokerkim]
    1. 서버 폴더 생성

[33mcommit e0874e39ac7c72a460ffddf865cdc13d61269229[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sat Jan 9 20:55:23 2016 +0900

    [MSProject_Bot][timewizhan][RV:N/A]
    1. Bot 구조 생성
    2. 기본적인 동작 구조 생성

[33mcommit 24e5ddbf5c999756db6f3ae4778ea9571fbc6ec7[m
Author: timewizhan <timewizhan@gmail.com>
Date:   Sat Jan 9 17:44:45 2016 +0900

    [MSProject_ALL][timewizhan][RV:N/A]
    1. MS Project 디렉토리 생성
