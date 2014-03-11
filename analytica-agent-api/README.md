# Project Analytica made by Klee Group.

## API CLient/Server 

Analytica clietn and server communicate via an event object called Process.

A process is an event with
 - a category defined by 
 	--a type 
 	--an array of subTypes	
 - a start date
 - a list of sub processes
 - a duration (cf.measures)
 - a list of measures  with a DURATION  measure 
 - a list of metadatas