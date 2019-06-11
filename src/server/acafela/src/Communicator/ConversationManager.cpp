#include <stdio.h>
#include "communicator.h"

void ConversationManager::createControlServer() {
	// join part_1
	Participant * part_1 = createParticipant("10.0.2.41");
	// join part_2
	Participant * part_2 = createParticipant("10.0.2.157");
	// create conversation
	Conversation * conversation = new Conversation();
	conversation->makeConversation({ 
		//std::make_tuple(part_1,PortHandler().getPortNumber()),
		//std::make_tuple(part_2,PortHandler().getPortNumber()) 
		//Port : ReceivePort
		// set Client 1 port - rcv : 5001, snd : 5000
		std::make_tuple(part_1,5000),
		// set Client 2 port - rcv : 5000, snd : 5001
		std::make_tuple(part_2,5001)
		});
}
void ConversationManager::createClientCtrlPath() {
	;
}
Participant * ConversationManager::createParticipant(std::string clientIP) {
	Participant * participant = new Participant(clientIP);
	return participant;
}
void ConversationManager::request_CreateControlPath(Participant *) {
	;
}
void ConversationManager::request_CreateDataPath(Participant *) {
	;
}