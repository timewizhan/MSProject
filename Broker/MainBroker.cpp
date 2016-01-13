/*
 * MainBroker.cpp
 *
 *  Created on: Jan 13, 2016
 *      Author: ms-dev
 */

#include "Common.h"
#include "Broker_Test4.h"

int main(){

	Broker_Test4 bt;
	bt.initBroker();
	bt.acceptEP();

	return 0;
}
