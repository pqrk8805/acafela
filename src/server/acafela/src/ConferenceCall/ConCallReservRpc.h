#pragma once

#include <string>
#include <thread>
#include <grpcpp/server.h>
#include "ConCallReserv.grpc.pb.h"
#include "ConferenceCallManager.h"

class ConCallReservRpc final : public acafela::rpc::ConCallReserve::Service
{
private:
    ::grpc::Server* mServer;
    std::thread mWorker;
	ConferenceCallManager mCCM;

    void wait();

public:
    ConCallReservRpc();
    ~ConCallReservRpc();

    int start(const std::string& addressUri);
    void shutdown();

    ::grpc::Status reserve(
                        ::grpc::ServerContext* context,
                        const ::acafela::rpc::ConCallResrvInfo* request,
                        ::acafela::rpc::Error* response);
};
