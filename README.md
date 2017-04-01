# vacation-lighting-director
ERS update to tslagle's vacation-lighting-director

https://community.smartthings.com/t/new-app-vacation-light-director/7230/265

Inputs:

Setup
 * Modes - to operate in (ST Modes this could be active in)  (required)
 * Time - restrictions to operate in   (optional)
     * Can set specific time or sunrise, sunset with offsets
 * Switches - List of switches that will be chosen from randomly for on/off   (required)
 * Cycle time (mins) - time switch is left on (5- 180 minutes) if randomly selected  (required)
 * number of active lights - number of lights turned on for a cycle from the list of switches  (required)
 * lights - to be on entire active time; (optional)
     * these are lights that will stay on during the active times defined by Modes, Time, presence sensors
         * Examples are garage/porch lights, Holiday lights, etc.
 
Settings
 * Presence sensors that will disable operation; (optional)
     * If cycle was active, lights will be left on if someone in list arrives;  
     * If cycle wanted to become active, if any of these sensors are present, the cycle will not start. 
 
Modes, Time, and presence sensors are **AND'ed** together to determine if vacation can run now

Example:
* Setup
    * is run in ST **AWAY** mode;  
    * Time is sunset - until 11:30;
    * Lights are entrance, bedroom, livingroom, kitchen, hallway
    * Cycle time is 33 mins
    * number of active lights is 2
    * presence sensors are Bob, Jane
    
 * Operation
     * At sunset, check if we are in **AWAY** mode, within Time, and  Bob and Jane are not present
         * if all are true, turn on 2 randomly selected switches from the list; turn on *lights* list
         * in 33 mins;   select 2 randomly selected lights from list, turn off old selection and on new selection;  *lights* remain on
         * if time expires, or ST mode changes;  turn off all lights we have on (2 + *lights*)
         
         * if Bob or Jane arrive while active;  stop processing, but leave lights on that are on (we don't want them to come in and have the lights go out)
