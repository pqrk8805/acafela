#include "UserProfileRpc.h"
#include <grpcpp/server_builder.h>
#include "Hislog.h"

#define LOG_TAG "UP_RPC"


UserProfileRpc::UserProfileRpc()
  : mServer(nullptr)
{
}

UserProfileRpc::~UserProfileRpc()
{
    if (mServer) {
        mServer->Shutdown();
        delete mServer;
        mServer = nullptr;
    }
}

int UserProfileRpc::start(const std::string& addressUri)
{
    ::grpc::ServerBuilder builder;
    builder.AddListeningPort(
                        addressUri,
                        ::grpc::InsecureServerCredentials());
    builder.RegisterService(this);
    std::unique_ptr<::grpc::Server> server(builder.BuildAndStart());
    if (server == nullptr) {
        FUNC_LOGE("ERROR: fail to create server: %s", addressUri.c_str());
        return -1;
    }

    mServer = server.release();
    FUNC_LOGI("UserProfile RPC server listen on: %s", addressUri.c_str());
    mServer->Wait();
    return 0;
}

void UserProfileRpc::shutdown()
{
    mServer->Shutdown();
}

::grpc::Status UserProfileRpc::getVersion(
                                    ::grpc::ServerContext* context,
                                    const ::acafela::rpc::Empty* request,
                                    ::acafela::rpc::VersionInfo* response)
{
    response->set_version("Acafela v0.0.5");
    return grpc::Status::OK;
}                                                                               
