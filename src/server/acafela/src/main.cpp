#include <iostream>
#include <thread>
#include <vector>
#include "UserProfile.h"
#include "UserProfileRpc.h"
#include "Hislog.h"

#define LOG_TAG "MAIN"

std::vector<std::thread *> additionalThreadList; 
void pingpongCommunicator_init();


int main(int argc, char** argv)
{
    FUNC_LOGI("Acafela Server started");

    UserProfile userProfile;
    UserProfileRpc userProfileRpc(userProfile);
    int err = userProfileRpc.start("localhost:9000");
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start UserProfileRpc server", err);
        return err;
    }
	
	pingpongCommunicator_init();
	for(auto * th : additionalThreadList)
		th->join();

    return 0;
}

