# Communication Protocol  
## About  
This is a document which keeps track of the communication protocol.    
The _command name_ or _header_ refers to the prefix of the command.  
The _body_ of the command refers to the information, if any, which immeadiately follow the header.  
  
## Client - Server Command table  
This protocol table is for communication between the client and the server.  

|Command Name/Header                 |Direction       |Command Body                            |Response                                |
|:----------------------------------:|:--------------:|:--------------------------------------:|:--------------------------------------:|
|`PING`                              |Either          |(none)                                  |`PING`                                  |
|`SUBMITNAME`                        |Server to Client|(none)                                  |The name of the client is sent          |
|`NAMEACCEPTED`                      |Server to Client|(none)                                  |(none)                                  |
|`NEWCLIENT`                         |Server to Client|`[boolean isNew] [String name]`         |(none)                                  |
|`REMOVECLIENT`                      |Server to Client|`[String name]`                         |(none)                                  |
|`BUSY`                              |Server to Client|`[String name]`                         |(none)                                  |
|`FREE`                              |Server to Client|`[String name]`                         |(none)                                   |
|`CHALLENGE_C` (challenge a client)  |Client to Server|`[String requested]`                    |None, but sends a challenge request     |
|`CHALLENGE_R` (challenge response)  |Client to Server|`[String challenger] [boolean accepted]`|None, but pairs the two if they accept  |
|`CHALLENGE_C` (relaying a challenge)|Server to Client|`[String challenger]`                   |Whether the client accepts the challenge|
|`CHALLENGE_R` (relaying the reponse)|Server to Client|`[boolean accepted]`                    |(none)                                  |

## In-Game Command table  
This protocol table is for communication between clients in-game (for Tetris).  

|Command Name/Header|Command Body                           |Response                                                          |
|:-----------------:|:-------------------------------------:|:----------------------------------------------------------------:|
|`NB` (new bag)     |`[String bagOrder]`                    |None, but adds a new bag to the matrix.                           |
|`LOCK`             |`[int x] [int y]`                      |None, but locks the piece in place in the given coordinates       |
|`M` (move)         |`[String move]` L, R, RR, RL, H, SD, HD|None, but executes the action.                                    |
|`GC` gravitycommand|`[String command]` P, R                |None, but executes the gravity command.                           |
|`SB` (starting bag)|none                                   |Gives the order of both bags, first this person, then the opponent|
|`GL` (garb lines)  |`[int hole] [int lines] ...`           |None, but dumps garbage onto the bottom                           |
|`ST` (start)       |none                                   |None, but starts the game.                                        |
  
This protocol table is for communication between clients for chatting.  
  
|Command Name/Header|Command Body                    |Response                                                 |
|:-----------------:|:------------------------------:|:-------------------------------------------------------:|
|`NLM` (lobby chat) |`[String name] [String message]`|None, but adds message to chat history                   |
|`EXIT`             |(none)                          |None, but adds to chat history that the other person left|
