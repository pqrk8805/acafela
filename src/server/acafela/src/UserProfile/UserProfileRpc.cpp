#include "UserProfileRpc.h"
#include <fstream>
#include <sstream>
#include "RpcUtils.h"
#include "Hislog.h"

#define LOG_TAG "UP_RPC"


UserProfileRpc::UserProfileRpc(IUserProfile& userProfile)
  : mServer(nullptr),
    mUserProfile(userProfile)
{
}

UserProfileRpc::~UserProfileRpc()
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

int UserProfileRpc::start(const std::string& addressUri)
{
    mServer = RpcUtils::initSecureServer(addressUri, this);
    FUNC_LOGI("UserProfile RPC server listen on: %s", addressUri.c_str());
    std::thread t ( [this]() { this->wait(); } );
    mWorker.swap(t);

    return 0;
}

void UserProfileRpc::wait()
{
    if (mServer) mServer->Wait();
}

void UserProfileRpc::shutdown()
{
    if (mServer) mServer->Shutdown();
}

::grpc::Status UserProfileRpc::getVersion(
                                    ::grpc::ServerContext* context,
                                    const ::acafela::rpc::Empty* request,
                                    ::acafela::rpc::VersionInfo* response)
{
    response->set_version("Acafela v0.0.5");
    return grpc::Status::OK;
}

::grpc::Status UserProfileRpc::registerUser(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::RegisterParam* request,
                        ::acafela::rpc::RegisterResp* response)
{
    std::string phoneNumber = mUserProfile.registerUser(
                                                request->email_address(),
                                                request->password());
    auto error = new ::acafela::rpc::Error;
    int err = phoneNumber.size() > 0 ? 0 : -1;
    error->set_err(err);

    response->set_allocated_error(error);
    response->set_phone_number(phoneNumber);

    return grpc::Status::OK;
}

::grpc::Status UserProfileRpc::changePassword(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::ChangePasswordParam* request,
                        ::acafela::rpc::Error* response)
{
    int err = mUserProfile.changePassword(
                                    request->email_address(),
                                    request->old_password(),
                                    request->new_password());
    response->set_err(err);
    return grpc::Status::OK;
}

::grpc::Status UserProfileRpc::restorePassword(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::RestorePasswordParam* request,
                        ::acafela::rpc::Error* response)
{
    int err = mUserProfile.restorePassword(
                                    request->email_address(),
                                    request->phone_number());
    response->set_err(err);
    return grpc::Status::OK;
}

