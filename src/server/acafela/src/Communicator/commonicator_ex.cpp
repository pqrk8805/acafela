#include <stdio.h>
#include <winsock2.h>
#include <ws2tcpip.h>
#include <functional>
#include <vector>
#include <tuple>
#include <thread>
#include <windows.h>
#pragma comment(lib,"ws2_32.lib")
#define BUFLEN 512
extern std::vector<std::thread *> additionalThreadList;
std::vector<std::tuple<int, char *>> bufferSample;
CRITICAL_SECTION crit;
void pingpongCommunicator_snd(int port);
void pingpongCommunicator_rcv(int port);
void pingpongCommunicator_init() {
	WSADATA wsa;
	//Initialise winsock
	printf("\nInitialising Winsock...");
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		printf("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	printf("Initialised.\n");
	InitializeCriticalSection(&crit);
	additionalThreadList.push_back(new std::thread(pingpongCommunicator_rcv, 5000));
	additionalThreadList.push_back(new std::thread(pingpongCommunicator_snd, 5001));
}

void pingpongCommunicator_snd(int port)
{
	SOCKET s;
	struct sockaddr_in server;
	//Create a socket
	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
	{
		printf("Could not create socket : %d", WSAGetLastError());
	}
	printf("Socket created.\n");
	//Prepare the sockaddr_in structure
	server.sin_family = AF_INET;
	inet_pton(AF_INET, "10.0.1.230", &(server.sin_addr));
	server.sin_port = htons(port);
	printf("Bind done\n");

	while (1) {
		EnterCriticalSection(&crit);
		if (bufferSample.size() == 0) {
			LeaveCriticalSection(&crit);
			continue;
		}
		int recv_len = std::get<0>(bufferSample.front());
		char * buf = std::get<1>(bufferSample.front());
		bufferSample.erase(bufferSample.begin());
		LeaveCriticalSection(&crit);

		if (sendto(s, buf, recv_len, 0, (struct sockaddr*) &server, sizeof(server)) == SOCKET_ERROR)
		{
			printf("sendto() failed with error code : %d\n", WSAGetLastError());
			//exit(EXIT_FAILURE);
		}
	}
	closesocket(s);
}


void pingpongCommunicator_rcv(int port)
{
	SOCKET s;
	struct sockaddr_in server;

	//Create a socket
	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == INVALID_SOCKET)
	{
		printf("Could not create socket : %d", WSAGetLastError());
	}
	printf("Socket created.\n");

	server.sin_family = AF_INET;
	server.sin_addr.s_addr = INADDR_ANY;
	server.sin_port = htons(port);

	if (bind(s, (struct sockaddr *)&server, sizeof(server)) == SOCKET_ERROR)
	{
		printf("Bind failed with error code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	printf("Bind done\n");

	while (1) {
		fflush(stdout);
		int recv_len;
		char * buf = new char[BUFLEN];
		memset(buf, NULL, BUFLEN);
		struct sockaddr_in si_other;
		int slen = sizeof(si_other);
		if ((recv_len = recvfrom(s, buf, BUFLEN, 0, (struct sockaddr *) &si_other, &slen)) == SOCKET_ERROR)
		{
			printf("recvfrom() failed with error code : %d\n", WSAGetLastError());
			//exit(EXIT_FAILURE);
		}
		EnterCriticalSection(&crit);
		bufferSample.push_back(std::make_tuple(recv_len, buf));
		LeaveCriticalSection(&crit);
	}

	closesocket(s);
}