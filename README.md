# vacation-lighting-director
ERS update to tslagle's vacation-lighting-director

Inputs:

Setup
 * Modes - to operate in (ST Modes this could be active in)
 * Time - restrictions to operate in
 * Switches - List of switches that will be chosen from randomly for on/off
 * Cycle time (mins) - time switch is left on (5- 180 minutes) if selected
 * number of active lights - number of lights turned on for a cycle (from the list of switches)
 * lights - to be on entire active time; these are lights that will stay on during the active times (defined by Modes, Time)
 
Settings
 * Presence sensors that will disable operation;  If cycle was active, lights will be left on if someone in list arrives;  If cycle wanted to become active, any of these sensors are present, the cycle will not start. 
 
