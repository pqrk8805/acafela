#include "DirectoryServiceRpc.h"
#include <grpcpp/server_builder.h>
#include "RpcUtils.h"
#include "Hislog.h"

#define LOG_TAG "DS_RPC"


DirectoryServiceRpc::DirectoryServiceRpc(IDirectoryService& directoryService)
  : mServer(nullptr),
    mDirectoryService(directoryService)
{
}

DirectoryServiceRpc::~DirectoryServiceRpc()
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

int DirectoryServiceRpc::start(const std::string& addressUri)
{
    mServer = RpcUtils::initSecureServer(addressUri, this);
    FUNC_LOGI("DirectoryService RPC server listen on: %s", addressUri.c_str());
    std::thread t ( [this]() { this->wait(); } );
    mWorker.swap(t);

    return 0;
}

 void DirectoryServiceRpc::wait()
 {
     if (mServer) mServer->Wait();
 }

void DirectoryServiceRpc::shutdown()
{
    mServer->Shutdown();
}

::grpc::Status DirectoryServiceRpc::update(
                                    ::grpc::ServerContext* context,
                                    const ::acafela::rpc::DirInfo* request,
                                    ::acafela::rpc::Error* response)
{
    int err = mDirectoryService.update(
                                    request->phone_number(),
                                    request->password(),
                                    request->address());
    response->set_err(err);
    response->set_message(
                        err == 0 ? "OK"
                      : err == -998 ? "There is no entry"
                      : err == -999 ? "Password mismatch"
                                    : "Fail - Unknown");

    return grpc::Status::OK;
}

