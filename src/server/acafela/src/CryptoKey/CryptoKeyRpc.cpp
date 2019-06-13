#include "CryptoKeyRpc.h"
#include "RpcUtils.h"
#include "Hislog.h"

#define LOG_TAG "CrytoKeyRPC"


CryptoKeyRpc::CryptoKeyRpc(ICryptoKeyRepo& keyRepo)
  : mServer(nullptr),
    mKeyRepo(keyRepo)
{
}

CryptoKeyRpc::~CryptoKeyRpc()
{
    if (mServer) {
        mServer->Shutdown();
        delete mServer;
        mServer = nullptr;
    }

    if (mWorker.joinable()) {
        mWorker.join();
    }
}

int CryptoKeyRpc::start(const std::string& addressUri)
{
    mServer = RpcUtils::initSecureServer(addressUri, this);
    FUNC_LOGI("CryptoKey RPC server listen on: %s", addressUri.c_str());
    std::thread t ( [this]() { this->wait(); } );
    mWorker.swap(t);

    return 0;
}

void CryptoKeyRpc::wait()
{
    if (mServer) mServer->Wait();
}

void CryptoKeyRpc::shutdown()
{
    if (mServer) mServer->Shutdown();
}

::grpc::Status CryptoKeyRpc::request(
                                ::grpc::ServerContext* context,
                                const ::acafela::rpc::Session* request,
                                ::acafela::rpc::Key* response)
{
    std::vector<char> key = mKeyRepo.getKey(request->id());
    char* keyBytes = new char[key.size()];
    std::copy(key.begin(), key.end(), keyBytes);

    response->set_key(keyBytes);
    return ::grpc::Status::OK;
}