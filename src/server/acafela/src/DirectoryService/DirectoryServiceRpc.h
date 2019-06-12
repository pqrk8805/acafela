#pragma once

#include <string>
#include <thread>
#include <grpcpp/server.h>
#include "DirectoryService.grpc.pb.h"
#include "IDirectoryService.h"


class DirectoryServiceRpc final : public acafela::rpc::DirectoryService::Service
{
private:
    ::grpc::Server* mServer;
    std::thread mWorker;
    IDirectoryService& mDirectoryService;

    void wait();

public:
    DirectoryServiceRpc(IDirectoryService& directoryService);
    ~DirectoryServiceRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status update(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::DirInfo* request,
                        ::acafela::rpc::Error* response) override;
};
