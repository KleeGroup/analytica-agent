# Project Analytica - System Monitoring Tool.

## API CLient/Server 

Analytica client and server communicate via an event object called KProcess.

A process is an event with
 - a category defined by 
 	--a type 
 	--an array of subTypes	
 - a start date
 - a list of sub processes
 - a duration (cf.measures)
 - a list of measures  with a DURATION  measure 
 - a list of metadatas