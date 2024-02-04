
# Elevator Control System - Iteration 1

## Overview
This project iteration was completed by Group 2 for SYSC 3303 - Section A3. It involves designing and implementing a multi-threaded elevator control system and simulator. The system simulates the operations of an elevator system, including floor buttons, elevator buttons, doors, and simulated passengers. The project aims to simulate real-world operations, including the movement of elevators between floors in real-time and handling passenger traffic.

## Components and Files
- **MainSystem (MainSystem.java)**: Initializes and coordinates the threads for Floor, Elevator, and Scheduler subsystems.
- **Floor (Floor.java)**: Simulates the arrival of passengers and button presses. It reads input events from a file and sends data packets representing these events to the Scheduler.
- **Scheduler (Scheduler.java)**: Acts as a server or a communication channel between the Floor and the Elevator. It forwards the data packets from the Floor to the Elevator and vice versa. The Scheduler is also responsible for coordinating elevators to minimize waiting times and handle possible faults.
- **Elevator (Elevator.java)**: Simulates the elevator car, including buttons, lamps, doors, and motor. It receives data packets from the Scheduler, processes them based on the state machine, and sends back the response.
- **DataPacket (DataPacket.java)**: Represents the data structure used to pass information between the Floor, Scheduler, and Elevator subsystems.

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

5. **Run MainSystem**
    - Navigate to 'MainSystem.java' in the Project view.
    - Right-click on the file and select 'Run 'MainSystem.main()'' to start the simulation.

## Input File Format
The input file should contain lines with the following format:
```
Time (hh:mm:ss.mmm) Floor (n) Floor Button (Up/Down) Car Button (n)
```
Each line represents an event where a passenger arrives at a floor at a specific time, presses a floor button to request an elevator, and then presses a car button to choose a destination floor.

## Authors

This project was created by 5 authors:

- Humam Khalil
- Imad Mohamed
- Michael Rochefort
- Kieran Rourke
- Kyle Taticek

## Breakdown of Work
Kieran Rourke: implemented the base functionality for the Elevator, DataPacket, Scheduler
Mowgli: implemented the functionality for the Floor and helped with the other classes
Kyle: implemented the functionality for the MainSystem and helped with the other classes
Humam: Testing work
Michael: UML Diagrams


## Future Iterations
- **Iteration 2**: Enhance the Scheduler and Elevator subsystems by introducing state machines for each, assuming only one elevator. Plan for coordinating multiple elevators in future iterations.
- **Iteration 3**: Incorporate multiple elevator cars and distribute the system across different computers to simulate a more realistic, distributed environment.
- **Iteration 4**: Introduce error detection and correction mechanisms to handle faults like stuck elevators or doors not opening/closing properly.
- **Iteration 5**: Implement capacity limits for each elevator car and develop a user interface to visually represent the position of all elevators in the system at any given time.

## Notes
- Ensure that the input file (default name: "input.txt") is present in the same directory as the executable files before running the MainSystem.
