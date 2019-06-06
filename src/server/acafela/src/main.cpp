#include <iostream>
#include <thread>
#include <vector>
#include "UserProfileRpc.h"
#include "Hislog.h"
#define LOG_TAG "MAIN"
#define BUFLEN 512
std::vector<std::thread *> additionalThreadList; 
void pingpongCommunicator_init();
int main(int argc, char** argv)
{
    FUNC_LOGI("Acafela Server started");
    UserProfileRpc userProfileRpc;
    int err = userProfileRpc.start("localhost:9000");
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start UserProfileRpc server");
        return -1;
    }
    std::cout << "Hello World!\n";
	pingpongCommunicator_init();
	for(auto * th : additionalThreadList)
		th->join();

    return 0;
}

