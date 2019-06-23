#pragma once

#include <string>
#include <thread>
#include <grpcpp/server.h>
#include "UserAdmin.grpc.pb.h"
#include "IUserAdmin.h"


class UserAdminRpc final : public acafela::rpc::UserAdmin::Service
{
private:
    ::grpc::Server* mServer;
    std::thread mWorker;
    IUserAdmin& mUserAdmin;

    void wait();

public:
    UserAdminRpc(IUserAdmin& userAdmin);
    ~UserAdminRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status getUserInfoList(
						::grpc::ServerContext* context,
						const ::acafela::rpc::Empty* request,
						::acafela::rpc::UserInfoList* response);
    ::grpc::Status deleteUser(
						::grpc::ServerContext* context,
						const ::acafela::rpc::UserInfo* request,
						::acafela::rpc::Error* response);
    ::grpc::Status disableUser(
						::grpc::ServerContext* context,
						const ::acafela::rpc::UserInfo* request,
						::acafela::rpc::Error* response);
    ::grpc::Status enableUser(
						::grpc::ServerContext* context,
						const ::acafela::rpc::UserInfo* request,
						::acafela::rpc::Error* response);
};
