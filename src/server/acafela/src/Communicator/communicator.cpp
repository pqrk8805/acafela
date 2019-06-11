#include <stdio.h>
#include "communicator.h"
int PortHandler::portNo = CTRLSERVERPORT;
void pingpongCommunicator_init() {
	WSADATA wsa;
	//Initialise winsock
	printf("\nInitialising Winsock...\n");
	if (WSAStartup(MAKEWORD(2, 2), &wsa) != 0)
	{
		printf("Failed.Error Code : %d", WSAGetLastError());
		exit(EXIT_FAILURE);
	}
	ConversationManager *convManager = new ConversationManager();
	convManager->createControlServer();

	Conversation * conversation = new Conversation();
	
	//conversation->addCommunicator("10.0.1.230", { 0,0 }, { 5001,5000 });
	//conversation->addCommunicator("10.0.2.41", { 0,0 }, { 5003,5002 });
}


