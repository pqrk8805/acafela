// <auto-generated>
//     Generated by the protocol buffer compiler.  DO NOT EDIT!
//     source: UserAdmin.proto
// </auto-generated>
#pragma warning disable 0414, 1591
#region Designer generated code

using grpc = global::Grpc.Core;

namespace harmony.usradmin.rpc {
  public static partial class UserAdmin
  {
    static readonly string __ServiceName = "acafela.rpc.UserAdmin";

    static readonly grpc::Marshaller<global::Acafela.Rpc.Empty> __Marshaller_acafela_rpc_Empty = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::Acafela.Rpc.Empty.Parser.ParseFrom);
    static readonly grpc::Marshaller<global::harmony.usradmin.rpc.UserInfoList> __Marshaller_acafela_rpc_UserInfoList = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::harmony.usradmin.rpc.UserInfoList.Parser.ParseFrom);
    static readonly grpc::Marshaller<global::harmony.usradmin.rpc.UserInfo> __Marshaller_acafela_rpc_UserInfo = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::harmony.usradmin.rpc.UserInfo.Parser.ParseFrom);
    static readonly grpc::Marshaller<global::Acafela.Rpc.Error> __Marshaller_acafela_rpc_Error = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::Acafela.Rpc.Error.Parser.ParseFrom);

    static readonly grpc::Method<global::Acafela.Rpc.Empty, global::harmony.usradmin.rpc.UserInfoList> __Method_getUserInfoList = new grpc::Method<global::Acafela.Rpc.Empty, global::harmony.usradmin.rpc.UserInfoList>(
        grpc::MethodType.Unary,
        __ServiceName,
        "getUserInfoList",
        __Marshaller_acafela_rpc_Empty,
        __Marshaller_acafela_rpc_UserInfoList);

    static readonly grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error> __Method_deleteUser = new grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error>(
        grpc::MethodType.Unary,
        __ServiceName,
        "deleteUser",
        __Marshaller_acafela_rpc_UserInfo,
        __Marshaller_acafela_rpc_Error);

    static readonly grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error> __Method_disableUser = new grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error>(
        grpc::MethodType.Unary,
        __ServiceName,
        "disableUser",
        __Marshaller_acafela_rpc_UserInfo,
        __Marshaller_acafela_rpc_Error);

    static readonly grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error> __Method_enableUser = new grpc::Method<global::harmony.usradmin.rpc.UserInfo, global::Acafela.Rpc.Error>(
        grpc::MethodType.Unary,
        __ServiceName,
        "enableUser",
        __Marshaller_acafela_rpc_UserInfo,
        __Marshaller_acafela_rpc_Error);

    /// <summary>Service descriptor</summary>
    public static global::Google.Protobuf.Reflection.ServiceDescriptor Descriptor
    {
      get { return global::harmony.usradmin.rpc.UserAdminReflection.Descriptor.Services[0]; }
    }

    /// <summary>Base class for server-side implementations of UserAdmin</summary>
    public abstract partial class UserAdminBase
    {
      public virtual global::System.Threading.Tasks.Task<global::harmony.usradmin.rpc.UserInfoList> getUserInfoList(global::Acafela.Rpc.Empty request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      public virtual global::System.Threading.Tasks.Task<global::Acafela.Rpc.Error> deleteUser(global::harmony.usradmin.rpc.UserInfo request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      public virtual global::System.Threading.Tasks.Task<global::Acafela.Rpc.Error> disableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      public virtual global::System.Threading.Tasks.Task<global::Acafela.Rpc.Error> enableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

    }

    /// <summary>Client for UserAdmin</summary>
    public partial class UserAdminClient : grpc::ClientBase<UserAdminClient>
    {
      /// <summary>Creates a new client for UserAdmin</summary>
      /// <param name="channel">The channel to use to make remote calls.</param>
      public UserAdminClient(grpc::Channel channel) : base(channel)
      {
      }
      /// <summary>Creates a new client for UserAdmin that uses a custom <c>CallInvoker</c>.</summary>
      /// <param name="callInvoker">The callInvoker to use to make remote calls.</param>
      public UserAdminClient(grpc::CallInvoker callInvoker) : base(callInvoker)
      {
      }
      /// <summary>Protected parameterless constructor to allow creation of test doubles.</summary>
      protected UserAdminClient() : base()
      {
      }
      /// <summary>Protected constructor to allow creation of configured clients.</summary>
      /// <param name="configuration">The client configuration.</param>
      protected UserAdminClient(ClientBaseConfiguration configuration) : base(configuration)
      {
      }

      public virtual global::harmony.usradmin.rpc.UserInfoList getUserInfoList(global::Acafela.Rpc.Empty request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return getUserInfoList(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::harmony.usradmin.rpc.UserInfoList getUserInfoList(global::Acafela.Rpc.Empty request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_getUserInfoList, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::harmony.usradmin.rpc.UserInfoList> getUserInfoListAsync(global::Acafela.Rpc.Empty request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return getUserInfoListAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::harmony.usradmin.rpc.UserInfoList> getUserInfoListAsync(global::Acafela.Rpc.Empty request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_getUserInfoList, null, options, request);
      }
      public virtual global::Acafela.Rpc.Error deleteUser(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return deleteUser(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::Acafela.Rpc.Error deleteUser(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_deleteUser, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> deleteUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return deleteUserAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> deleteUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_deleteUser, null, options, request);
      }
      public virtual global::Acafela.Rpc.Error disableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return disableUser(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::Acafela.Rpc.Error disableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_disableUser, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> disableUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return disableUserAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> disableUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_disableUser, null, options, request);
      }
      public virtual global::Acafela.Rpc.Error enableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return enableUser(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual global::Acafela.Rpc.Error enableUser(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_enableUser, null, options, request);
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> enableUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return enableUserAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      public virtual grpc::AsyncUnaryCall<global::Acafela.Rpc.Error> enableUserAsync(global::harmony.usradmin.rpc.UserInfo request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_enableUser, null, options, request);
      }
      /// <summary>Creates a new instance of client from given <c>ClientBaseConfiguration</c>.</summary>
      protected override UserAdminClient NewInstance(ClientBaseConfiguration configuration)
      {
        return new UserAdminClient(configuration);
      }
    }

    /// <summary>Creates service definition that can be registered with a server</summary>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static grpc::ServerServiceDefinition BindService(UserAdminBase serviceImpl)
    {
      return grpc::ServerServiceDefinition.CreateBuilder()
          .AddMethod(__Method_getUserInfoList, serviceImpl.getUserInfoList)
          .AddMethod(__Method_deleteUser, serviceImpl.deleteUser)
          .AddMethod(__Method_disableUser, serviceImpl.disableUser)
          .AddMethod(__Method_enableUser, serviceImpl.enableUser).Build();
    }

  }
}
#endregion