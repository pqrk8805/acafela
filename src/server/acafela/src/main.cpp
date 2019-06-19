#include <iostream>
#include <thread>
#include <vector>
#include "DirectoryService.h"
#include "DirectoryServiceRpc.h"
#include "CryptoKey/CryptoKey.h"
#include "CryptoKey/CryptoKeyRpc.h"
#include "Communicator/communicator.h"
#include "UserProfile.h"
#include "UserProfileRpc.h"
#include "Hislog.h"

#define LOG_TAG "MAIN"

#define SERVER_IP "10.0.1.151"
#define RPC_PORT_USERVER_PROFILE    "9000"
#define RPC_PORT_DIRECTORY_SERVICE  "9100"
#define RPC_PORT_CRYPTO_KEY         "9200"

#define CLIENT1_IP "10.0.2.157"
#define CLIENT2_IP "10.0.1.230"
int PortHandler::portNo = CTRLSERVERSNDPORT;
std::vector<std::thread *> additionalThreadList; 
int main(int argc, char** argv)
{
    FUNC_LOGI("Acafela Server started");

    int err = 0;

    UserProfile userProfile;
    UserProfileRpc userProfileRpc(userProfile);
    err = userProfileRpc.start(SERVER_IP ":" RPC_PORT_USERVER_PROFILE);
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start UserProfileRpc server", err);
        return err;
    }

    DirectoryService directoryService;
    DirectoryServiceRpc directoryServiceRpc(directoryService);
    err = directoryServiceRpc.start(SERVER_IP ":" RPC_PORT_DIRECTORY_SERVICE);
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start DirectoryServiceRpc server", err);
        return err;
    }

	CryptoKey cryptoKey;
	CryptoKeyRpc cryptoKeyRpc(cryptoKey);
	err = cryptoKeyRpc.start(SERVER_IP ":" RPC_PORT_CRYPTO_KEY);
	if (err) {
		FUNC_LOGE("ERROR(%d): fail to start CryptoKeyRpc server", err);
		return err;
	}


	//Stub it before implement client side directory service.
	directoryService.update("2222", "????", CLIENT1_IP);
	directoryService.update("1111", "????", CLIENT2_IP);

	WSADATA wsa;
	FUNC_LOGI("Initialising Winsock...");
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0){
		FUNC_LOGE("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	ConversationManager *convManager = new ConversationManager();
	convManager->createControlServer(&cryptoKey);
	for(auto * th : additionalThreadList)
		th->join();

    return 0;
}

