# Elevator Control System - Iteration 4

## Overview

This project iteration was completed by Group 2 for SYSC 3303 - Section A3. It involves designing and implementing a multi-threaded elevator control system and simulator. The system simulates the operations of an elevator system, including floor buttons, elevator buttons, doors, and simulated passengers. The project aims to simulate real-world operations, including the movement of elevators between floors in real-time and handling passenger traffic.

## Iteration 4 Updates

- Fault Detection: Implemented fault detection for stuck elevators (FT - Floor Timer Fault), doors not opening/closing properly (DOF - Door Open Fault), and a general No Fault (NF) condition for normal operations.
- ElevatorDataPacket Class: Introduced to enhance communication between the Elevator and Scheduler subsystems, enabling the transfer of elevator status and fault information.
- Distributed System Simulation: Further refined the system's distribution across different computers, emphasizing fault tolerance in a distributed environment.


## Components and Files

- **MainSystem (MainSystem.java)**: Initializes and coordinates the threads for Floor, Elevator, and Scheduler
  subsystems.
- **Floor (Floor.java)**: Simulates the arrival of passengers and button presses. It reads input events from a file and
  sends data packets representing these events to the Scheduler.
- **Scheduler (Scheduler.java)**: Acts as a server or a communication channel between the Floor and the Elevator. It
  forwards the data packets from the Floor to the Elevator and vice versa. The Scheduler is also responsible for
  coordinating elevators to minimize waiting times and handle possible faults.
- **Elevator (Elevator.java)**: Simulates the elevator car, including buttons, lamps, doors, and motor. It receives data
  packets from the Scheduler, processes them based on the state machine, and sends back the response.
- **DataPacket (DataPacket.java)**: Represents the data structure used to pass information between the Floor, Scheduler,
  and Elevator subsystems.
- **ElevatorDataPacket (ElevatorDataPacket.java)**: New class facilitating the transfer of detailed elevator status and fault information to the Scheduler.

## Test Files

- **MainSystemTest (MainSystemTest.java)**: Tests the overall integration and coordination between the Scheduler,
  Elevator, and Floor subsystems. It ensures that the Main System correctly initializes these components and handles
  their interactions effectively, simulating scenarios to verify the system's behaviour under various conditions.
- **SchedulerTest (SchedulerTest.java)**: Focuses on the Scheduler component, ensuring that it accurately receives and
  processes requests from both the Floor and Elevator subsystems. It tests the scheduler's ability to prioritize and
  assign elevator movements effectively, based on the requests and system state.
- **ElevatorTest (ElevatorTest.java)**: Verifies the functionality of the Elevator subsystem, including its ability to
  move between floors, open and close doors, and respond to both internal and external requests as expected. This test
  ensures that the elevator behaves correctly in response to the commands issued by the Scheduler.
- **DataPacketTest (DataPacketTest.java)**: Ensures the integrity and accuracy of the data packets used for
  communication between the system's components. This test checks that the data packets correctly represent requests,
  states, and notifications, and are properly parsed and handled by the receiving components.
- **FloorTest (FloorTest.java)**: Tests the Floor subsystem's ability to generate and send requests to the Scheduler. It
  ensures that floor events are correctly read from the input file, and that the Floor subsystem can accurately simulate
  the arrival of passengers and send requests based on these events.

## Networking

The subsystems will ultimately communicate using DatagramSocket objects, allowing the components to run on separate computers. Ensure that your code supports this configuration for future iterations.

## Real-time Operation

The system should simulate real-time operations of elevators. Elevators take time to move from floor to floor, and this behaviour should be accurately represented in the simulation.

## Setup Instructions

1. **Open IntelliJ IDEA**
   Open IntelliJ IDEA and select 'Open or Import'.

2. **Open the Project**
   Navigate to the directory containing the project files and select the root directory of the project.

3. **Configure the Project**
   - Ensure the JDK is correctly set for the project (File -> Project Structure -> SDK).

4. **Build the Project**
   - Use the 'Build' menu to compile the project.

5. **Run Scheduler**
   - Navigate to 'Scheduler.java' in the Project view.
   - Right-click on the file and select 'Run 'Scheduler.main()'' to start the scheduler.

6. **Run Floor**
   - Navigate to 'Floor.java' in the Project view.
   - Right-click on the file and select 'Run 'Floor.main()'' to start the floor.

7. **Run Elevator**
   - Navigate to 'Elevator.java' in the Project view.
   - Right-click on the file and select 'Run 'Elevator.main()'' to start the scheduler.

## Input File Format

The input file should contain lines with the following format:

```
Time (hh:mm:ss.mmm) Floor (n) Floor Button (Up/Down) Car Button (n) Fault Code (FT/DOF/NF)
```

Each line represents an event where a passenger arrives at a floor at a specific time, presses a floor button to request
an elevator, and then presses a car button to choose a destination floor.

## Authors

This project was created by 5 authors:

- Humam Khalil
- Imad Mohamed
- Michael Rochefort
- Kieran Rourke
- Kyle Taticek

## Breakdown of Work

Kieran Rourke: supported functionality for the Elevator, DataPacket, Scheduler. Extended project capabilities to support multiple elevators cars and supported implementation of fault detection capabilities.<br>
Imad Mohamed: supported the functionality for the Floor, Elevator, DataPacket, Scheduler and state machine additions. Supported implementation of fault detection capabilities.<br>
Kyle: implemented the functionality for the MainSystem and supported the Floor, Elevator, DataPacket, and Scheduler classes, helped with state machine additions. Supported addition of fault detection capabilities.<br>
Humam: Testing work. Supported implementation of fault detection capabilities.<br>
Michael: created UML Diagrams, state machine diagrams, and supported the addition of fault detection capabilities.<br>

## Future Iterations

- **Iteration 5**: Implement capacity limits for each elevator car and develop a user interface to visually represent
  the position of all elevators in the system at any given time.

## Notes

- Ensure that the input file (default name: "input.txt") is present in the same directory as the executable files before running the MainSystem.
