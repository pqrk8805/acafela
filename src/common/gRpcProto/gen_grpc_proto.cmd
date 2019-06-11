@echo off
chcp 437

set CMD_PROTOC=..\..\..\external\gRPC\bin\protoc.exe
set INC_PROTO=..\..\..\external\gRPC\include\third_party\protobuf\src
set GRPC_PLUGIN_CPP=..\..\..\external\gRPC\bin\grpc_cpp_plugin.exe

set PROTO_FILES=Common.proto UserProfile.proto DirectoryService.proto
set PROTO_RPC_FILES=UserProfile.proto DirectoryService.proto


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


copy Common.proto ..\..\android\app\src\main\proto\
copy UserProfile.proto ..\..\android\app\src\main\proto\
copy DirectoryService.proto ..\..\android\app\src\main\proto\

move Common*.h ..\..\server\acafela\src\RpcCommon\
move Common*.cc ..\..\server\acafela\src\RpcCommon\

move UserProfile*.h ..\..\server\acafela\src\UserProfile\
move UserProfile*.cc ..\..\server\acafela\src\UserProfile\

move DirectoryService*.h ..\..\server\acafela\src\DirectoryService\
move DirectoryService*.cc ..\..\server\acafela\src\DirectoryService\
