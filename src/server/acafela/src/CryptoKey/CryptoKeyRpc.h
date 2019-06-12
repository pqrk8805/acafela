#pragma once

#include <thread>
#include <grpcpp/server.h>
#include "CryptoKey.grpc.pb.h"
#include "ICryptoKeyRepo.h"

class CryptoKeyRpc final : public acafela::rpc::CryptoKey::Service
{
private:
    ::grpc::Server* mServer;
    std::thread mWorker;
    ICryptoKeyRepo& mKeyRepo;

    void wait();

public:
    CryptoKeyRpc(ICryptoKeyRepo& keyRepo);
    ~CryptoKeyRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status request(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::Session* request,
                        ::acafela::rpc::Key* response) override;
};
