#include <iostream>
#include <thread>
#include <vector>
#include "UserProfileRpc.h"
#include "Hislog.h"

#define LOG_TAG "MAIN"

void pingpongCommunicator_ex();


int main(int argc, char** argv)
{
    FUNC_LOGI("Acafela Server started");

	std::vector<std::thread *> additionalThreadList;
    std::cout << "Hello World!\n"; 
	additionalThreadList.push_back(new std::thread(pingpongCommunicator_ex));
	for(auto * th : additionalThreadList)
		th->join();


    UserProfileRpc userProfileRpc;
    int err = userProfileRpc.start("localhost:9000");
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start UserProfileRpc server");
        return -1;
    }

    return 0;
}

