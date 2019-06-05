#pragma once

#include <string>
#include <grpcpp/server.h>
#include "UserProfile.grpc.pb.h"

class UserProfileRpc final : public acafela::rpc::UserProfile::Service
{
private:
    ::grpc::Server* mServer;

public:
    UserProfileRpc();
    ~UserProfileRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status getVersion(
                            ::grpc::ServerContext* context,
                            const ::acafela::rpc::Empty* request,
                            ::acafela::rpc::VersionInfo* response) override;


};
