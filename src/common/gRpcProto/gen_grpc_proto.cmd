@echo off
chcp 437

set CMD_PROTOC=..\..\..\external\gRPC\bin\protoc.exe
set INC_PROTO=..\..\..\external\gRPC\include\third_party\protobuf\src
set GRPC_PLUGIN_CPP=..\..\..\external\gRPC\bin\grpc_cpp_plugin.exe

set PROTO_RPC_FILES=UserProfile.proto


%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --cpp_out=.\ ^
        --csharp_out=.\ ^
		%PROTO_RPC_FILES%

%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --grpc_out=.\ ^
        --plugin=protoc-gen-grpc=%GRPC_PLUGIN_CPP% ^
        %PROTO_RPC_FILES%


copy *.h ..\..\server\acafela\src\UserProfile\
copy *.cc ..\..\server\acafela\src\UserProfile\