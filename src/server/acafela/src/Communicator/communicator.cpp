#include <stdio.h>
#include "communicator.h"
#include "../Hislog.h"
#include "../CryptoKey/ICryptoKeyMgr.h"
#define LOG_TAG "COMMINIT"

int PortHandler::portNo = CTRLSERVERSNDPORT; 
void pingpongCommunicator_init(ICryptoKeyMgr * keyManager) {
	WSADATA wsa;
	FUNC_LOGI("Initialising Winsock...");

	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		FUNC_LOGE("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	ConversationManager *convManager = new ConversationManager();
	convManager->createControlServer(keyManager);
}


