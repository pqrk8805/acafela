// Generated by the gRPC C++ plugin.
// If you make any local change, they will be lost.
// source: UserProfile.proto
#ifndef GRPC_UserProfile_2eproto__INCLUDED
#define GRPC_UserProfile_2eproto__INCLUDED

#include "UserProfile.pb.h"

#include <functional>
#include <grpcpp/impl/codegen/async_generic_service.h>
#include <grpcpp/impl/codegen/async_stream.h>
#include <grpcpp/impl/codegen/async_unary_call.h>
#include <grpcpp/impl/codegen/method_handler_impl.h>
#include <grpcpp/impl/codegen/proto_utils.h>
#include <grpcpp/impl/codegen/rpc_method.h>
#include <grpcpp/impl/codegen/service_type.h>
#include <grpcpp/impl/codegen/status.h>
#include <grpcpp/impl/codegen/stub_options.h>
#include <grpcpp/impl/codegen/sync_stream.h>

namespace grpc {
class CompletionQueue;
class Channel;
class ServerCompletionQueue;
class ServerContext;
}  // namespace grpc

namespace acafela {
namespace rpc {

class UserProfile final {
 public:
  static constexpr char const* service_full_name() {
    return "acafela.rpc.UserProfile";
  }
  class StubInterface {
   public:
    virtual ~StubInterface() {}
    virtual ::grpc::Status getVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::acafela::rpc::VersionInfo* response) = 0;
    std::unique_ptr< ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>> AsyncgetVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) {
      return std::unique_ptr< ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>>(AsyncgetVersionRaw(context, request, cq));
    }
    std::unique_ptr< ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>> PrepareAsyncgetVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) {
      return std::unique_ptr< ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>>(PrepareAsyncgetVersionRaw(context, request, cq));
    }
    class experimental_async_interface {
     public:
      virtual ~experimental_async_interface() {}
      virtual void getVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response, std::function<void(::grpc::Status)>) = 0;
    };
    virtual class experimental_async_interface* experimental_async() { return nullptr; }
  private:
    virtual ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>* AsyncgetVersionRaw(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) = 0;
    virtual ::grpc::ClientAsyncResponseReaderInterface< ::acafela::rpc::VersionInfo>* PrepareAsyncgetVersionRaw(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) = 0;
  };
  class Stub final : public StubInterface {
   public:
    Stub(const std::shared_ptr< ::grpc::ChannelInterface>& channel);
    ::grpc::Status getVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::acafela::rpc::VersionInfo* response) override;
    std::unique_ptr< ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>> AsyncgetVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) {
      return std::unique_ptr< ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>>(AsyncgetVersionRaw(context, request, cq));
    }
    std::unique_ptr< ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>> PrepareAsyncgetVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) {
      return std::unique_ptr< ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>>(PrepareAsyncgetVersionRaw(context, request, cq));
    }
    class experimental_async final :
      public StubInterface::experimental_async_interface {
     public:
      void getVersion(::grpc::ClientContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response, std::function<void(::grpc::Status)>) override;
     private:
      friend class Stub;
      explicit experimental_async(Stub* stub): stub_(stub) { }
      Stub* stub() { return stub_; }
      Stub* stub_;
    };
    class experimental_async_interface* experimental_async() override { return &async_stub_; }

   private:
    std::shared_ptr< ::grpc::ChannelInterface> channel_;
    class experimental_async async_stub_{this};
    ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>* AsyncgetVersionRaw(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) override;
    ::grpc::ClientAsyncResponseReader< ::acafela::rpc::VersionInfo>* PrepareAsyncgetVersionRaw(::grpc::ClientContext* context, const ::acafela::rpc::Empty& request, ::grpc::CompletionQueue* cq) override;
    const ::grpc::internal::RpcMethod rpcmethod_getVersion_;
  };
  static std::unique_ptr<Stub> NewStub(const std::shared_ptr< ::grpc::ChannelInterface>& channel, const ::grpc::StubOptions& options = ::grpc::StubOptions());

  class Service : public ::grpc::Service {
   public:
    Service();
    virtual ~Service();
    virtual ::grpc::Status getVersion(::grpc::ServerContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response);
  };
  template <class BaseClass>
  class WithAsyncMethod_getVersion : public BaseClass {
   private:
    void BaseClassMustBeDerivedFromService(const Service *service) {}
   public:
    WithAsyncMethod_getVersion() {
      ::grpc::Service::MarkMethodAsync(0);
    }
    ~WithAsyncMethod_getVersion() override {
      BaseClassMustBeDerivedFromService(this);
    }
    // disable synchronous version of this method
    ::grpc::Status getVersion(::grpc::ServerContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response) override {
      abort();
      return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
    }
    void RequestgetVersion(::grpc::ServerContext* context, ::acafela::rpc::Empty* request, ::grpc::ServerAsyncResponseWriter< ::acafela::rpc::VersionInfo>* response, ::grpc::CompletionQueue* new_call_cq, ::grpc::ServerCompletionQueue* notification_cq, void *tag) {
      ::grpc::Service::RequestAsyncUnary(0, context, request, response, new_call_cq, notification_cq, tag);
    }
  };
  typedef WithAsyncMethod_getVersion<Service > AsyncService;
  template <class BaseClass>
  class WithGenericMethod_getVersion : public BaseClass {
   private:
    void BaseClassMustBeDerivedFromService(const Service *service) {}
   public:
    WithGenericMethod_getVersion() {
      ::grpc::Service::MarkMethodGeneric(0);
    }
    ~WithGenericMethod_getVersion() override {
      BaseClassMustBeDerivedFromService(this);
    }
    // disable synchronous version of this method
    ::grpc::Status getVersion(::grpc::ServerContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response) override {
      abort();
      return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
    }
  };
  template <class BaseClass>
  class WithRawMethod_getVersion : public BaseClass {
   private:
    void BaseClassMustBeDerivedFromService(const Service *service) {}
   public:
    WithRawMethod_getVersion() {
      ::grpc::Service::MarkMethodRaw(0);
    }
    ~WithRawMethod_getVersion() override {
      BaseClassMustBeDerivedFromService(this);
    }
    // disable synchronous version of this method
    ::grpc::Status getVersion(::grpc::ServerContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response) override {
      abort();
      return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
    }
    void RequestgetVersion(::grpc::ServerContext* context, ::grpc::ByteBuffer* request, ::grpc::ServerAsyncResponseWriter< ::grpc::ByteBuffer>* response, ::grpc::CompletionQueue* new_call_cq, ::grpc::ServerCompletionQueue* notification_cq, void *tag) {
      ::grpc::Service::RequestAsyncUnary(0, context, request, response, new_call_cq, notification_cq, tag);
    }
  };
  template <class BaseClass>
  class WithStreamedUnaryMethod_getVersion : public BaseClass {
   private:
    void BaseClassMustBeDerivedFromService(const Service *service) {}
   public:
    WithStreamedUnaryMethod_getVersion() {
      ::grpc::Service::MarkMethodStreamed(0,
        new ::grpc::internal::StreamedUnaryHandler< ::acafela::rpc::Empty, ::acafela::rpc::VersionInfo>(std::bind(&WithStreamedUnaryMethod_getVersion<BaseClass>::StreamedgetVersion, this, std::placeholders::_1, std::placeholders::_2)));
    }
    ~WithStreamedUnaryMethod_getVersion() override {
      BaseClassMustBeDerivedFromService(this);
    }
    // disable regular version of this method
    ::grpc::Status getVersion(::grpc::ServerContext* context, const ::acafela::rpc::Empty* request, ::acafela::rpc::VersionInfo* response) override {
      abort();
      return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
    }
    // replace default version of method with streamed unary
    virtual ::grpc::Status StreamedgetVersion(::grpc::ServerContext* context, ::grpc::ServerUnaryStreamer< ::acafela::rpc::Empty,::acafela::rpc::VersionInfo>* server_unary_streamer) = 0;
  };
  typedef WithStreamedUnaryMethod_getVersion<Service > StreamedUnaryService;
  typedef Service SplitStreamedService;
  typedef WithStreamedUnaryMethod_getVersion<Service > StreamedService;
};

}  // namespace rpc
}  // namespace acafela


#endif  // GRPC_UserProfile_2eproto__INCLUDED