# vacation-lighting-director
ERS update to tslagle's vacation-lighting-director

https://community.smartthings.com/t/new-app-vacation-light-director/7230/265

Inputs:

Setup
 * Modes - to operate in (ST Modes this could be active in)  (required)
 * Time - restrictions to operate in   (optional)   Can set specific time or sunrise, sunset with offsets
 * Switches - List of switches that will be chosen from randomly for on/off   (required)
 * Cycle time (mins) - time switch is left on (5- 180 minutes) if selected  (required)
 * number of active lights - number of lights turned on for a cycle from the list of switches  (required)
 * lights - to be on entire active time; these are lights that will stay on during the active times defined by Modes, Time, presence sensors (optional)
 
Settings
 * Presence sensors that will disable operation; (optional)
     * If cycle was active, lights will be left on if someone in list arrives;  
     * If cycle wanted to become active, if any of these sensors are present, the cycle will not start. 
 
Modes, Time, and presence sensors are **AND'ed** together to determine if vacation can run now
