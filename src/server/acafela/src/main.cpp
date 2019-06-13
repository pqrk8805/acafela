#include <iostream>
#include <thread>
#include <vector>
#include "DirectoryService.h"
#include "DirectoryServiceRpc.h"
#include "UserProfile.h"
#include "UserProfileRpc.h"
#include "Hislog.h"

#define LOG_TAG "MAIN"

#define SERVER_IP "10.0.1.4"
#define RPC_PORT_USERVER_PROFILE    "9000"
#define RPC_PORT_DIRECTORY_SERVICE  "9100"
#define RPC_PORT_CRYPTO_KEY         "9200"


std::vector<std::thread *> additionalThreadList; 
void pingpongCommunicator_init();


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
	
	pingpongCommunicator_init();
	for(auto * th : additionalThreadList)
		th->join();

    return 0;
}

