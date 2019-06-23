#include "UserAdminRpc.h"
#include <grpcpp/server_builder.h>
#include "RpcUtils.h"
#include "Hislog.h"

#define LOG_TAG "DS_RPC"


UserAdminRpc::UserAdminRpc(IUserAdmin& userAdmin)
  : mServer(nullptr),
    mUserAdmin(userAdmin)
{
}

UserAdminRpc::~UserAdminRpc()
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

int UserAdminRpc::start(const std::string& addressUri)
{
    mServer = RpcUtils::initInsecureServer(addressUri, this);
    FUNC_LOGI("UserAdmin RPC server listen on: %s", addressUri.c_str());
    std::thread t ( [this]() { this->wait(); } );
    mWorker.swap(t);

    return 0;
}

 void UserAdminRpc::wait()
 {
     if (mServer) mServer->Wait();
 }

void UserAdminRpc::shutdown()
{
    mServer->Shutdown();
}


::grpc::Status UserAdminRpc::getUserInfoList(
									::grpc::ServerContext* context,
									const ::acafela::rpc::Empty* request,
									::acafela::rpc::UserInfoList* response)
{
	std::vector<UserInfo> userInfoList = mUserAdmin.getUserInfoList();
	for (const auto& userInfo : userInfoList) {
		::acafela::rpc::UserInfo* info = response->add_user_info();
		info->set_email(userInfo.emailAddress);
		info->set_phone_number(userInfo.phoneNumber);
		info->set_ip_address(userInfo.ipAddress);
		info->set_enabled(userInfo.enabled);
	}
    return grpc::Status::OK;
}

::grpc::Status UserAdminRpc::deleteUser(
									::grpc::ServerContext* context,
									const ::acafela::rpc::Email* request,
									::acafela::rpc::Error* response)
{
	const std::string& email = request->email();
	FUNC_LOGD("%s", email.c_str());

	int err = mUserAdmin.deleteUser(email);
	response->set_err(err);
    return grpc::Status::OK;
}

::grpc::Status UserAdminRpc::disableUser(
									::grpc::ServerContext* context,
									const ::acafela::rpc::Email* request,
									::acafela::rpc::Error* response)
{
	const std::string& email = request->email();
	FUNC_LOGD("%s", email.c_str());

	int err = mUserAdmin.disableUser(email);
	response->set_err(err);
    return grpc::Status::OK;
}

::grpc::Status UserAdminRpc::enableUser(
									::grpc::ServerContext* context,
									const ::acafela::rpc::Email* request,
									::acafela::rpc::Error* response)
{
	const std::string& email = request->email();
	FUNC_LOGD("%s", email.c_str());

	int err = mUserAdmin.enableUser(email);
	response->set_err(err);
    return grpc::Status::OK;
}

