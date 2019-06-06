#pragma once

#include <string>
#include <thread>
#include <grpcpp/server.h>
#include "UserProfile.grpc.pb.h"
#include "IUserProfile.h"


class UserProfileRpc final : public acafela::rpc::UserProfile::Service
{
private:
    ::grpc::Server* mServer;
    std::thread mWorker;
    IUserProfile& mUserProfile;

    void wait();

public:
    UserProfileRpc(IUserProfile& userProfile);
    ~UserProfileRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status getVersion(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::Empty* request,
                        ::acafela::rpc::VersionInfo* response) override;
    ::grpc::Status registerUser(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::RegisterParam* request,
                        ::acafela::rpc::RegisterResp* response) override;
    ::grpc::Status changePassword(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::ChangePasswordParam* request,
                        ::acafela::rpc::Error* response) override;
    ::grpc::Status restorePassword(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::RestorePasswordParam* request,
                        ::acafela::rpc::Error* response) override;


};
