#include <iostream>
#include <thread>
#include <vector>
#include "ConCallReservRpc.h"
#include "DirectoryService.h"
#include "DirectoryServiceRpc.h"
#include "CryptoKey.h"
#include "CryptoKeyRpc.h"
#include "Communicator/communicator.h"
#include "UserProfile.h"
#include "UserProfileRpc.h"
#include "UserAdmin.h"
#include "UserAdminMock.h"
#include "UserAdminRpc.h"
#include "FileStorageAccessor.h"
#include "Hislog.h"
#include "Config.h"

#define LOG_TAG "MAIN"

int PortHandler::portNo = CTRLSERVERSNDPORT;
int RoomNoHandler::roomNo = 100;
std::vector<std::thread *> additionalThreadList; 

int main(int argc, char** argv)
{
    FUNC_LOGI("Acafela Server started");

    int err = 0;

	FileStorageAccessor storageAccessor;

    UserProfile userProfile(storageAccessor);
    UserProfileRpc userProfileRpc(userProfile);
    err = userProfileRpc.start(SERVER_IP ":" RPC_PORT_USERVER_PROFILE);
    if (err) {
        FUNC_LOGE("ERROR(%d): fail to start UserProfileRpc server", err);
        return err;
    }

    DirectoryService directoryService(storageAccessor);
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

	UserAdmin userAdmin(storageAccessor, directoryService);
	UserAdminMock userAdminMock;
	UserAdminRpc userAdminRpc(userAdmin);
	err = userAdminRpc.start(SERVER_IP ":" RPC_PORT_USER_ADMIN);
	if (err) {
		FUNC_LOGE("ERROR(%d): fail to start UserAdminRpc server", err);
		return err;
	}

	ConferenceCallManager confManager;
    ConCallReservRpc conCallReservRpc;
    err = conCallReservRpc.start(SERVER_IP ":" RPC_PORT_CONCALL_RESERVE);
	if (err) {
		FUNC_LOGE("ERROR(%d): fail to start ConCallReservRpc server", err);
		return err;
	}

	WSADATA wsa;
	FUNC_LOGI("Initialising Winsock...");
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0){
		FUNC_LOGE("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	ConversationManager *convManager = new ConversationManager();
	convManager->createControlServer(&cryptoKey, &confManager);
	for(auto * th : additionalThreadList)
		th->join();

    return 0;
}

