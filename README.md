# Local_Search

Local Search Project 

Project Overview

This project is a practical application of Local Search algorithms . The core task is to solve a fuel distribution and routing problem.

The objective is to find the optimal daily assignment of pending fuel requests to available tanker trucks and their routes. The solution must maximize overall profit by minimizing both the financial penalty from unserved requests and the total distance traveled.

The implemented algorithms are Hill Climbing (HC) and Simulated Annealing (SA).

This repository contains the necessary files for the Local Search Practice. This document provides the minimal steps required to compile and run the project from the root directory, assuming all .java files and required JAR libraries are present in the same directory structure.

Requirements

Java Development Kit (JDK) installed and accessible in your system's PATH.

A compatible shell (e.g., zsh, bash).

Execution Steps

Run the following commands sequentially from the project root directory:

Prepare the build directory:

rm -rf bin
mkdir -p bin


Compile all Java sources:
This command compiles all .java files recursively, including necessary JARs in the current directory as part of the classpath.

javac -d bin -cp "./*" $(find . -name "*.java")


Run the application (Main class):
The command below runs the practica.Main class, which accepts algorithm-specific parameters.

java -cp "bin:./*" practica.Main


Example Commands

Hill Climbing (HC)

java -cp "bin:./*" practica.Main HC greedy 1234 10 100 1
(HC algorithm, greedy initial solution, seed 1234, 10 centers, 100 gas stations, 1 truck/center)

Simulated Annealing (SA)

java -cp "bin:./*" practica.Main SA greedy 1234 10 100 1 25 0.0001 50000 100
(SA algorithm, greedy initial solution, seed 1234, 10 centers, 100 gas stations, 1 truck/center, k=25, lambda=0.0001, 50000 total iterations, 100 iterations/temp step)
