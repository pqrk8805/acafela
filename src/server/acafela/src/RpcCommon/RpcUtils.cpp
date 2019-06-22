#include "RpcUtils.h"
#include <fstream>
#include <sstream>
#include <grpcpp/server_builder.h>


static
std::string read(const std::string& filename)
{
    std::string data;
    std::ifstream file (filename.c_str(), std::ios::in);

	if (file.is_open()) {
		std::stringstream ss;
		ss << file.rdbuf ();
		file.close ();
		data = ss.str();
	}
    return data;
}

::grpc::Server* RpcUtils::initSecureServer(
                                    const std::string& addressUri,
                                    ::grpc::Service* service)
{
    std::string cert = read("certs/server.crt");
	std::string key = read ("certs/server.key");
	std::string root = read ("certs/ca.crt");

    std::shared_ptr<grpc::ServerCredentials> creds;
    grpc::SslServerCredentialsOptions::PemKeyCertPair pkcp = {key, cert};
    grpc::SslServerCredentialsOptions ssl_opts;
    ssl_opts.pem_root_certs = root;
    ssl_opts.pem_key_cert_pairs.push_back(pkcp);
    creds = grpc::SslServerCredentials(ssl_opts);

    ::grpc::ServerBuilder builder;
    builder.AddListeningPort(
                        addressUri, creds);
    builder.RegisterService(service);
    std::unique_ptr<::grpc::Server> server(builder.BuildAndStart());

    return server.release();
}

::grpc::Server* RpcUtils::initInsecureServer(
                                    const std::string& addressUri,
                                    ::grpc::Service* service)
{
	::grpc::ServerBuilder builder;
    builder.AddListeningPort(
                        addressUri,
                        ::grpc::InsecureServerCredentials());
    builder.RegisterService(service);
    std::unique_ptr<::grpc::Server> server(builder.BuildAndStart());

	return server.release();
}

