#include "ConCallReservRpc.h"
#include "RpcUtils.h"
#include "Hislog.h"
#include "ConferenceCallManager.h"
#define LOG_TAG "ConCallReserv"


ConCallReservRpc::ConCallReservRpc()
{
	mCCM = std::make_unique<ConferenceCallManager>();
}

ConCallReservRpc::~ConCallReservRpc()
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

int ConCallReservRpc::start(const std::string& addressUri)
{
    mServer = RpcUtils::initInsecureServer(addressUri, this);
    FUNC_LOGI("ConCallReserve RPC server listen on: %s", addressUri.c_str());
    std::thread t ( [this]() { this->wait(); } );
    mWorker.swap(t);

    return 0;
}

 void ConCallReservRpc::wait()
 {
     if (mServer) mServer->Wait();
 }

void ConCallReservRpc::shutdown()
{
    mServer->Shutdown();
}


::grpc::Status ConCallReservRpc::reserve(
                            ::grpc::ServerContext* context,
                            const ::acafela::rpc::ConCallResrvInfo* request,
                            ::acafela::rpc::Error* response)
{
    FUNC_LOGI("");
    // from
    //
    const std::string& from = request->from();

    // to
    //
    const std::string& to = request->to();

    // 주최자
    //
    const std::string& host = request->host_phonenumber();

    // 참석자 목록
    //
	std::vector<std::string> participants;
    for (int i = 0; i < request->participants_size(); ++i) {
        //const std::string participant = request->participants(i);
		participants.push_back(request->participants(i));
    }

	int err = mCCM->MakeConferenceCall(from, to, participants);
    response->set_err(-1);
    response->set_message("not implemented yet");

    return grpc::Status::OK;
}

