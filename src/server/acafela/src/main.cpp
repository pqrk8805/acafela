#include <iostream>
#include <thread>
#include <vector>
void pingpongCommunicator_ex();

int main()
{
	std::vector<std::thread *> additionalThreadList;
    std::cout << "Hello World!\n"; 
	additionalThreadList.push_back(new std::thread(pingpongCommunicator_ex));
	for(auto * th : additionalThreadList)
		th->join();
	return 0;
}

