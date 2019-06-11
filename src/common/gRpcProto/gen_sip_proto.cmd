@echo off
chcp 437

set CMD_PROTOC=..\..\..\external\gRPC\bin\protoc.exe
set INC_PROTO=..\..\..\external\gRPC\include\third_party\protobuf\src
set GRPC_PLUGIN_CPP=..\..\..\external\gRPC\bin\grpc_cpp_plugin.exe

set PROTO_RPC_FILES=SipMessage.proto


%CMD_PROTOC% ^
        -I%INC_PROTO% ^
        -I.\ ^
        --cpp_out=.\ ^
        --csharp_out=.\ ^
		%PROTO_RPC_FILES%




copy %PROTO_RPC_FILES% ..\..\android\app\src\main\proto\
move *.h ..\..\server\acafela\src\SipMessage\
move *.cc ..\..\server\acafela\src\SipMessage\