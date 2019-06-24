@echo off
chcp 437

set CMD_PROTOC=..\..\..\external\gRPC\bin\protoc.exe
set INC_PROTO=..\..\..\external\gRPC\include\third_party\protobuf\src
set GRPC_PLUGIN_CPP=..\..\..\external\gRPC\bin\grpc_cpp_plugin.exe
set GRPC_PLUGIN_CSHARP=..\..\..\external\gRPC\bin\grpc_csharp_plugin.exe

set PROTO_FILES=Common.proto UserProfile.proto DirectoryService.proto CryptoKey.proto UserAdmin.proto ConCallReserv.proto
set PROTO_RPC_FILES=UserProfile.proto DirectoryService.proto CryptoKey.proto UserAdmin.proto ConCallReserv.proto


%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --cpp_out=.\ ^
        --csharp_out=.\ ^
		%PROTO_FILES%

%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --grpc_out=.\ ^
        --plugin=protoc-gen-grpc=%GRPC_PLUGIN_CPP% ^
        %PROTO_RPC_FILES%

%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --grpc_out=.\ ^
        --plugin=protoc-gen-grpc=%GRPC_PLUGIN_CSHARP% ^
        UserAdmin.proto


copy Common.proto ..\..\android\app\src\main\proto\
copy UserProfile.proto ..\..\android\app\src\main\proto\
copy DirectoryService.proto ..\..\android\app\src\main\proto\
copy CryptoKey.proto ..\..\android\app\src\main\proto\

move Common*.h ..\..\server\acafela\src\RpcCommon\
move Common*.cc ..\..\server\acafela\src\RpcCommon\

move UserProfile*.h ..\..\server\acafela\src\UserProfile\
move UserProfile*.cc ..\..\server\acafela\src\UserProfile\

move DirectoryService*.h ..\..\server\acafela\src\DirectoryService\
move DirectoryService*.cc ..\..\server\acafela\src\DirectoryService\

move CryptoKey*.h ..\..\server\acafela\src\CryptoKey\
move CryptoKey*.cc ..\..\server\acafela\src\CryptoKey\

move UserAdmin*.h ..\..\server\acafela\src\UserAdmin\
move UserAdmin*.cc ..\..\server\acafela\src\UserAdmin\

move ConCall*.h ..\..\server\acafela\src\ConCall\
move ConCall*.cc ..\..\server\acafela\src\ConCall\

move Common.cs ..\..\server\AcafelaUserAdmin\
move UserAdmin*.cs ..\..\server\AcafelaUserAdmin\

del *.cs