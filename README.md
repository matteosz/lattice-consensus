# Lattice Consensus

# Overview
This project implements a weak form of consensus [Lattice Consensus](https://moodle.epfl.ch/pluginfile.php/3155791/mod_resource/content/1/Lattice_Agreement___DA2022%20%281%29.pdf) by implementing certain building blocks necessary for a decentralized system:

  - Fair-Loss Links
  - Stubborn Links
  - Perfect Links
  - Best-effort Broadcast
  - Lattice Consensus

Various applications (e.g., a payment system) can be built upon these lower-level abstractions.

The project has been done for the course Distributed Algorithms 2022/23, more details [here](https://docs.google.com/presentation/d/1VkJSjSkLK29qQjlxUWtoqp20I-An72BZI_2CaLVh8Cs).

# Project Requirements

## Basics
The implementation takes into account that **messages** exchanged between processes **may be dropped, delayed or reordered by the network**. The execution of processes may be paused for an arbitrary amount of time and resumed later. Processes may also fail by crashing at arbitrary points of their execution.

## Communication Primitive
Inter-process point-to-point messages (at the low level) are carried exclusively by UDP packets in their most basic form, not utilizing any additional features (e.g., any form of feedback about packet delivery) provided by the network stack, the operating system or external libraries. Everything is implemented on top of these low-level point to point messages.

### Code structure:
```sh
.
├── bin
│   ├── deploy
│   │   └── README
│   ├── logs
│   │   └── README
│   └── README
├── build.sh
├── cleanup.sh
├── pom.xml
├── run.sh
└── src
    └── main
        └── java
            └── cs451
                └── ...
```

## Application Interface
The supported arguments are:
```sh
./run.sh --id ID --hosts HOSTS --output OUTPUT CONFIG
```

Where:
  - `ID` specifies the unique identifier of the process. In a system of `n` processes, the identifiers are `1`...`n`.
  - `HOSTS` specifies the path to a file that contains the information about every process in the system, i.e., it describes the system membership. The file contains as many lines as processes in the system. A process identity consists of a numerical process identifier, the IP address or name of the process and the port number on which the process is listening for incoming messages. The entries of each process identity are separated by white space character. The following is an example of the contents of a `HOSTS` file for a system of 5 processes:
  ```
  1 localhost 11001
  2 localhost 11002
  3 localhost 11003
  4 localhost 11004
  5 localhost 11005
  ```
  **Note**: The processes should listen for incoming messages in the port range `11000` to `11999` inclusive. Each process should use only 1 port.

  - `OUTPUT` specifies the path to a text file where a process stores its output.

  - `CONFIG` specifies the path to a file that contains additional information for the experimented abstraction (e.g. how many message to broadcast).

## Process Crashes
We simulate process crashes by relying on Linux's signals.
A process that receives a `SIGTERM` or `SIGINT` signal must immediately stop its execution with the exception of writing to an output log file (described above). In particular, it must not send or handle any received network packets. You can assume that at most a minority (e.g., 1 out of 3; 2 out of 5; 4 out of 10, ...) processes may crash in one execution.
You can assume that a process crash will be simulated only by the `SIGINT` or `SIGTERM` signals.

## Running the project
Use the provided bash scripts in this [folder](testing_tool/), where there's a performance version (no crashes) and a stress version, both with a test suite of 15 cases.

You can edit `testConfig` at the bottom of `stress_perf.py` in order to test different scenarios.
```py
testConfig = {
  'concurrency' : 8, # How many threads are interfering with the running processes.
  'attempts' : 8, # How many successful operations (SIGCONT, SIGSTOP, SIGTERM) each thread will attempt before stopping. Threads stop if a minority of processes has been terminated.
  'attemptsDistribution' : { # Each thread selects a process randomly and issues one of these operations with the given probability.
    'STOP': 0.48,
    'CONT': 0.48,
    'TERM':0.04
    }
}
```

To change the rate at which interfering threads interfere with the processes modify this line: 
```py
time.sleep(float(random.randint(50, 500)) / 1000.0)
```
under the `StressTest` class.

## Limits
* You are given 2 CPU cores.
* You are given 2GiB of memory.
* You are allowed to spawn up to 1024 threads.