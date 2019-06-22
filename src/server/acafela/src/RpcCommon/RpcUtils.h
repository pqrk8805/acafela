#pragma once

#include <string>
#include <grpcpp/server.h>

class RpcUtils
{
private:
    RpcUtils();

public:
    static ::grpc::Server* initSecureServer(
                                    const std::string& addressUri,
                                    ::grpc::Service* service);

    static ::grpc::Server* initInsecureServer(
                                    const std::string& addressUri,
                                    ::grpc::Service* service);
};
