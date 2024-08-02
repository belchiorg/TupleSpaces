# TupleSpaces

Distributed Systems Project 2024

**Group A50**

**Difficulty level: I am Death incarnate!**


### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name               | User                               | Email                                                |
|--------|--------------------|------------------------------------|------------------------------------------------------|
| 102908 | Luana Ferraz       | <https://github.com/luanaaferraaz> | <mailto:luana.ferraz@tecnico.ulisboa.pt>             |
| 103540 | Gonçalo Alves      | <https://github.com/GSAprod>       | <mailto:goncalo.santana.alves@tecnico.ulisboa.pt>    |
| 102447 | Guilherme Belchior | <https://github.com/belchiorg>     | <mailto:guilherme.belchior.souza@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3). 
The clients is in _Client_.
The definition of messages and services is in _Contract_. The future naming server
is in _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Running the project

 Create the python virtual environment

Go to the root of the project and run the following commands:
```s
python -m venv .venv
source .venv/bin/activate
python -m pip install grpcio grpcio-tools
mvn clean
```

Then change your directory do the Contract module. Then, run the following command:
```s
mvn install compile exec:exec
```

Next, go to the name server module and run the following command to start the name server:
```s
python server.py
```

In another terminal, navigate to the ServerR1 folder and run the following command to start each server:
```s
mvn compile exec:java -Dexec.args="<port> <qualifier> [-Debug]"
```
Repeat this process with three servers (using qualifiers A, B and C), in order to achieve the desired number of
servers as required by this project.

In another terminal, navigate to the Sequencer folder and run the following command to start the sequencer:
```s
mvn compile exec:java
```

Finally, in another terminal, navigate to the Client folder and run the following command to start the client:
```s
mvn compile exec:java -Dexec.args="[-Debug]"
```
You can run many clients at the same time by running this command in different shells.

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
