/**
 *	Vacation Lighting Director  (based off of tslagle's original)
 *	Supports Longer interval times (up to 180 mins)
 *	Only turns off lights it turned on (vs calling to turn all off)
 * 
 *	Updated to turn on a set of lights during active time, and turn them off at end of vacation time
 *
 *	Source code can be found here:
 *	https://github.com/imnotbob/vacation-lighting-director/blob/master/smartapps/imnotbob/vacation-lighting-director.src/vacation-lighting-director.groovy
 *
 *	Copyright 2017 Eric Schott
 *	Last update July 13, 2020
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 */

import java.text.SimpleDateFormat
import groovy.transform.Field

// Automatically generated. Make future change here.
definition(
	name: "Vacation Lighting Director",
	namespace: "imnotbob",
	author: "ERS",
	category: "Safety & Security",
	description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
	iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png"
)

preferences {
	page(name:"pageSetup")
	page(name:"Setup")
	page(name:"Settings")
	page(name:"timeIntervalPage")

}

// Show setup page
def pageSetup(){

	Map pageProperties=[
		name:		"pageSetup",
		title:		"Status",
		nextPage:	null,
		install:	true,
		uninstall:	true
	]

	return dynamicPage(pageProperties){
		section(""){
			paragraph "This app can be used to make your home seem occupied anytime you are away from your home. " +
				"Please use each of the the sections below to setup the different preferences to your liking. "
		}
		section("Setup Menu"){
			href "Setup", title: "Setup", description: "", state:greyedOut()
			href "Settings", title: "Settings", description: "", state: greyedOutSettings()
		}
		section([title:"Options", mobileOnly:true]){
			label title:"Assign a name", required:false
		}
	}
}

// Show "Setup" page
def Setup(){

	Map newMode=[
		name:				"newMode",
		type:				"mode",
		title:				"Modes",
		multiple:			true,
		required:			true
	]
	Map switches=[
		name:				"switches",
		type:				"capability.switch",
		title:				"Switches",
		multiple:			true,
		required:			true
	]

	Map frequency_minutes=[
		name:				"frequency_minutes",
		type:				"number",
		title:				"Minutes? (5-180)",
		range:				"5..180",
		required:			true
	]

	Map number_of_active_lights=[
		name:				"number_of_active_lights",
		type:				"number",
		title:				"Number of active lights",
		required:			true,
	]

	Map on_during_active_lights=[
		name:				"on_during_active_lights",
		type:				"capability.switch",
		title:				"On during active times",
		multiple:			true,
		required:			false
	]

	def pageName="Setup"

	Map pageProperties=[
		name:		"Setup",
		title:		"Setup",
		nextPage:	"pageSetup"
	]

	return dynamicPage(pageProperties){

		section(""){
			paragraph "Setup the details of how you want your lighting to be affected while " +
				"you are away.	All of these settings are required in order for the simulator to run correctly."
		}
		section("Simulator Triggers"){
			input newMode
			href "timeIntervalPage", title: "Times", description: timeIntervalLabel()    //, refreshAfterSelection:true
		}
		section("Light switches to cycle on/off"){
			input switches
		}
		section("How often to cycle the lights"){
			input frequency_minutes
		}
		section("Number of active lights at any given time"){
			input number_of_active_lights
		}
		section("Lights to be on during active times?"){
			input on_during_active_lights
		}
	}
}

// Show "Setup" page
def Settings(){

	Map falseAlarmThreshold=[
		name:		"falseAlarmThreshold",
		type:		"decimal",
		title:		"Default is 2 minutes",
		required:	false
	]
	Map days=[
		name:		"days",
		type:		"enum",
		title:		"Only on certain days of the week",
		multiple:	true,
		required:	false,
		options:	["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
	]

	def pageName="Settings"

	Map pageProperties=[
		name:		"Settings",
		title:		"Settings",
		nextPage:	"pageSetup"
	]

	Map people=[
		name:		"people",
		type:		"capability.presenceSensor",
		title:		"If these people are home do not change light status",
		required:	false,
		multiple:	true
	]

	return dynamicPage(pageProperties){

		section(""){
			paragraph "Restrict how your simulator runs.  For instance you can restrict on which days it will run " +
				"as well as a delay for the simulator to start after it is in the correct mode.	Delaying the simulator helps with false starts based on a incorrect mode change."
		}
		section("Delay to start simulator"){
			input falseAlarmThreshold
		}
		section("People"){
			paragraph "Not using this setting may cause some lights to remain on when you arrive home"
			input people
		}
		section("More options"){
			input days
		}
	}
}

def timeIntervalPage(){
	dynamicPage(name: "timeIntervalPage", title: "Only during a certain time"){
		section {
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if(startTimeType in [sSUNRISE,sSUNSET]){
				input "startTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "starting", "time", title: "Start time", required: false
			}
		}
		section {
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if(endTimeType in [sSUNRISE,sSUNSET]){
				input "endTimeOffset", "number", title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else {
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}

@Field static final String sNULL=(String)null
@Field static final String sBLK=""
@Field static final String sLONG='long'
@Field static final String sNUMBR='number'
@Field static final String sTRUE='true'
@Field static final String sFALSE='false'
@Field static final String sTIME='time'
@Field static final String sSUNRISE='sunrise'
@Field static final String sSUNSET='sunset'

void installed(){
	atomicState.Running=false
	atomicState.schedRunning=false
	atomicState.startendRunning=false
	initialize()
}

void updated(){
	unsubscribe()
	clearState(true)
	initialize()
}

void initialize(){
	if(newMode != null){
		subscribe(location, "mode", modeChangeHandler)
	}
	schedStartEnd()
	if(people){
		subscribe(people, "presence", modeChangeHandler)
	}
	log.debug "Installed with settings: ${settings}"
	setSched()
}

void clearState(Boolean turnOff=false){
	if(turnOff && (Boolean)atomicState.Running){
 		switches.off()
		atomicState.vacactive_switches=[]
		if(on_during_active_lights){
 			on_during_active_lights.off()
		}
		log.trace "All OFF"
	}
	atomicState.Running=false
	atomicState.schedRunning=false
	atomicState.startendRunning=false
	atomicState.lastUpdDt=sNULL
	unschedule()
}


void schedStartEnd(){
	def sunTimes=app.getSunriseAndSunset()
	if(!sunTimes.sunrise){
		log.warn "Actual sunrise and sunset times unavailable, please reset hub location"
		return
	}
	Boolean running=(Boolean)atomicState.startendRunning
	Date start
	if(starting != null || startTimeType != sNULL){
		start=timeWindowStart(sunTimes, true)
		if(start && !((Long)start.time > now()) ) start=new Date((Long)start.time+(24*60*60*1000L))
		log.debug "Scheduling start $start"
		schedule(start, startTimeCheck)
		running=true
	}
	if(ending != null || endTimeType != sNULL){
		Date end=timeWindowStop(sunTimes, start, true)
		if(end && !((Long)end.time > now()) ) end=new Date((Long)end.time+(24*60*60*1000L))
		log.debug "Scheduling end $end"
		schedule(end, endTimeCheck)
		running=true
	}
/*	if(!running){
		schedule(timeTodayAfter('23:59', '00:01', location.timezone), initCheck1)
		running=true
	}*/
	atomicState.startendRunning=running
}

void setSched(){
	atomicState.schedRunning=true
/*
	def maxMin=60
	def timgcd=gcd([frequency_minutes, maxMin])
	atomicState.timegcd=timgcd
	def random=new Random()
	def random_int=random.nextInt(60)
	def random_dint=random.nextInt(timgcd.toInteger())

	def newDate=new Date()
	def curMin=newDate.format("m", getTimeZone())

	def timestr="${random_dint}/${timgcd}"
	if(timgcd == 60){ timestr="${curMin}" }

	log.trace "scheduled using Cron (${random_int} ${timestr} * 1/1 * ? *)"
	schedule("${random_int} ${timestr} * 1/1 * ? *", scheduleCheck)	// this runs every timgcd minutes
*/
	Integer delay=(falseAlarmThreshold != null && falseAlarmThreshold != sBLK) ? falseAlarmThreshold * 60 : 2 * 60
	runIn(delay, initCheck)
}
/*
private gcd(a, b){
	while (b > 0){
		long temp=b;
		b=a % b;
		a=temp;
	}
	return a;
}

private gcd(input=[]){
	Long result=input[0];
	for(Integer i=1; i < input.size; i++) result=gcd(result, input[i]);
	return result;
}
*/
void modeChangeHandler(evt){
	log.trace "modeChangeHandler Event Name ${evt.name} event value: ${evt.value}"
	setSched()
}

void initCheck1(){
	unschedule()
	atomicState.startendRunning=false
	scheduleCheck(null)
}

void initCheck(){
	scheduleCheck(null)
}

void failsafe(){
	scheduleCheck(null)
}

void startTimeCheck(){
	log.trace "startTimeCheck"
	setSched()
}

void endTimeCheck(){
	log.trace "endTimeCheck"
	scheduleCheck(null)
}

String getDtNow(){
	Date now=new Date()
	return formatDt(now)
}

String formatDt(Date dt){
	SimpleDateFormat tf=new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) tf.setTimeZone(getTimeZone())
	else log.warn "TimeZone is not found or is not set... Please Try to open your location and Press Save..."
	return (String)tf.format(dt)
}

static Long GetTimeDiffSeconds(String lastDate){
	if(lastDate?.contains("dtNow")){ return 10000L }
	Date now=new Date()
	Long start=Date.parse("E MMM dd HH:mm:ss z yyyy", lastDate).getTime()
	Long stop=now.getTime()
	Long diff=(Long)((stop - start) / 1000L)
	return diff
}

def getTimeZone(){
	def tz=null
	if(location?.timeZone) tz=location.timeZone
	if(!tz){ log.warn "getTimeZone: TimeZone is not found or is not set... Please Try to open your location and Press Save..." }
	return tz
}

Integer getLastUpdSec(){ return !(String)atomicState.lastUpdDt ? 100000 : GetTimeDiffSeconds((String)atomicState.lastUpdDt).toInteger() }

//Main logic to pick a random set of lights from the large set of lights to turn on and then turn the rest off

void scheduleCheck(evt){
	Boolean mTimeOk=timeOk
	Boolean mHomeIsEmpty=homeIsEmpty
	Boolean mDaysOk=daysOk

	Boolean someoneIsHome=!mHomeIsEmpty
	Boolean allOk=modeOk && mDaysOk && mTimeOk && mHomeIsEmpty

	if(allOk && getLastUpdSec() > ((frequency_minutes - 1) * 60) ){
		atomicState.lastUpdDt=getDtNow()
		log.debug("Running")
		atomicState.Running=true

		// turn off switches
		def inactive_switches=switches
		List<Integer> vacactive_switches=[]
		if((Boolean)atomicState.Running){
			vacactive_switches=(List)atomicState.vacactive_switches
			Integer sz= vacactive_switches ? vacactive_switches.size() : 0
			for (Integer i=0; i < sz ; i++){
 				inactive_switches[ vacactive_switches[i] ].off()
				log.trace "turned off ${inactive_switches[ vacactive_switches[i] ]}"
			}
			atomicState.vacactive_switches=[]
			vacactive_switches=[]
		}

		Random random=new Random()
		Integer numlight=number_of_active_lights
		sz=inactive_switches.size()
		if(numlight > sz) numlight=sz
		log.trace "inactive switches: ${sz} numlight: ${numlight}"
		for (Integer i=0 ; i < numlight ; i++){
			// grab a random switch to turn on
			Integer random_int=random.nextInt(sz)
			while (vacactive_switches?.contains(random_int)){
				random_int=random.nextInt(sz)
			}
			vacactive_switches << random_int
		}
		for (Integer i=0 ; i < numlight; i++){
 			inactive_switches[ vacactive_switches[i] ].on()
			log.trace "turned on ${inactive_switches[ vacactive_switches[i] ]}"
		}
		atomicState.vacactive_switches=vacactive_switches
		//log.trace "vacactive ${vacactive_switches} inactive ${inactive_switches}"

		if(on_during_active_lights){
 			on_during_active_lights.on()
			log.trace "turned on ${on_during_active_lights}"
		}
		Integer delay=frequency_minutes
		Integer random_int=random.nextInt(14)
		log.trace "reschedule  ${delay} + ${random_int} minutes"
		runIn( (delay+random_int)*60, initCheck, [overwrite: true])
		runIn( (delay+random_int + 10)*60, failsafe, [overwrite: true])

	} else {
		if(allOk && getLastUpdSec() <= ((frequency_minutes - 1) * 60) ){
			log.trace "had to reschedule  ${getLastUpdSec()}, ${frequency_minutes*60}"
			runIn( (frequency_minutes*60-getLastUpdSec()), initCheck, [overwrite: true])
		} else {
			Boolean mySchedRunning=(Boolean)atomicState.schedRunning
			if(people && someoneIsHome){
				//don't turn off lights if anyone is home
				if(mySchedRunning){
					log.debug("Someone is home - Stopping Schedule Vacation Lights")
					clearState()
				}
			} else {
				Boolean myRunning=(Boolean)atomicState.Running
				if(!modeOk || !mDaysOk){
					if(myRunning || mySchedRunning){
						log.debug("wrong mode or day Stopping Vacation Lights")
						clearState(true)
					}
				} else if(modeOk && mDaysOk && !mTimeOk){
					if(myRunning || mySchedRunning){
						log.debug("wrong time - Stopping Vacation Lights")
						clearState(true)
					}
				}
			}
		}
	}
	if(!(Boolean)atomicState.startendRunning){
		schedStartEnd()
	}
}

Boolean getModeOk(){
	Boolean result=!newMode || newMode.contains(location.mode)
	//log.trace "modeOk=$result"
	result
}

Boolean getDaysOk(){
	Boolean result=true
	if(days){
		SimpleDateFormat df=new SimpleDateFormat("EEEE")
		if(getTimeZone()) df.setTimeZone(getTimeZone())
		else {
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		String day=(String)df.format(new Date())
		result=days.contains(day)
	}
	//log.trace "daysOk=$result"
	result
}

Boolean getHomeIsEmpty(){
	Boolean result=true
	if(people?.findAll { it?.currentPresence == "present" }){
		result=false
	}
	//log.debug("homeIsEmpty: ${result}")
	return result
}

Boolean getTimeOk(){
	Boolean result=true
	def sunTimes=app.getSunriseAndSunset()
	if(!sunTimes.sunrise){
		log.warn "Actual sunrise and sunset times unavailable, please reset hub location"
		return false
	}
	Date start=timeWindowStart(sunTimes)
	Date stop=timeWindowStop(sunTimes, start)
	if(start && stop && getTimeZone()){
		result=checkTimeCondition(startTimeType, starting, startTimeOffset, endTimeType, ending, endTimeOffset, sunTimes)
		//result=timeOfDayIsBetween( (start), (stop), new Date(), getTimeZone())
	}
	log.debug "timeOk=$result start: $start   stop: $stop"
	result
}


Date timeWindowStart(LinkedHashMap sunTimes, Boolean usehhmm=false){
	Date result=timeWindowMgmt(startTimeType, startTimeOffset, starting, sunTimes, usehhmm)
//	log.debug "timeWindowStart=${result}  ${formatDt(result)}"
}

Date timeWindowMgmt(String strtTimeType, Long strtTimeOffset, strting, LinkedHashMap sunTimes, Boolean usehhmm, Boolean useST=false, Date st=null){
	Long lresult
	Date result=null
	if(strtTimeType == sSUNRISE){
		lresult=(Long)sunTimes.sunrise.time
		if(lresult && strtTimeOffset){
			lresult=lresult + Math.round(strtTimeOffset * 60000L)
		}
		result=new Date(lresult)
	}
	else if(strtTimeType == sSUNSET){
		lresult=(Long)sunTimes.sunset.time
		if(lresult && strtTimeOffset){
			lresult=lresult + Math.round(strtTimeOffset * 60000L)
		}
		result=new Date(lresult)
	}
	else if(strting && getTimeZone()){
		if(usehhmm){ result=timeToday(hhmm(strting), getTimeZone()) }
		else { result=timeToday(strting, getTimeZone()) }
	}
	if(useST && result && st && (Long)st.time > (Long)result.time){
		result=new Date(result.time+86400000L)
	}
	result
}

Date timeWindowStop(LinkedHashMap sunTimes, Date st, Boolean usehhmm=false){
	Date result=timeWindowMgmt(endTimeType, endTimeOffset, ending, sunTimes, usehhmm, true, st)
//	log.debug "timeWindowStop=${result} ${formatDt(result)}"
}

String hhmm(String time, String fmt="HH:mm"){
	Date t=timeToday(time, getTimeZone())
	SimpleDateFormat f=new SimpleDateFormat(fmt)
	f.setTimeZone(getTimeZone())
	f.format(t)
}

//adjusts the time to local timezone
Date adjustTime(time=null){
	Long ltime
	if(time instanceof Long){
		ltime=time
	} else if(time instanceof String){
		//get UTC time
		ltime=timeToday(time, location.timeZone).getTime()
	} else if(time instanceof Date){
		//get unix time
		ltime=time.getTime()
	} else if(!ltime){
		ltime=now()
	}
	if(ltime){
		if(ltime > now()) return new Date(ltime + (Integer)location.timeZone.getOffset(ltime) - (Integer)location.timeZone.getOffset(now()))
		return new Date(ltime)
	}
	return null
}

Boolean checkTimeCondition(String timeFrom, timeFromCustom, Long timeFromOffset, String timeTo, timeToCustom, Long timeToOffset, sunTimes){
	Date time=new Date()
	//convert to minutes since midnight
	Integer tc=(Integer)time.hours * 60 + (Integer)time.minutes
	Integer tf
	Integer tt
	Integer i=0

	while (i < 2){
		Date t=null
		Integer h=null
		Integer m=0
		switch(i == 0 ? timeFrom : timeTo){
			case "custom time":
			case sTIME:
				t=adjustTime(i == 0 ? timeFromCustom : timeToCustom)
				if(i == 0){
					timeFromOffset=0L
				} else {
					timeToOffset=0L
				}
				break
			case sSUNRISE:
				t=getSunrise(sunTimes)
				break
			case sSUNSET:
				t=getSunset(sunTimes)
				break
			case "noon":
				h=12
				break
			case "midnight":
				h=(i == 0 ? 0 : 24)
			break
		}
		if(h == null){
			h=(Integer)t.hours
			m=(Integer)t.minutes
		}
		switch (i){
			case 0:
				tf=h * 60 + m + (Integer)cast(timeFromOffset, sNUMBR)
				break
			case 1:
				tt=h * 60 + m + (Integer)cast(timeToOffset, sNUMBR)
				break
		}
		i += 1
	}
	//due to offsets, let's make sure all times are within 0-1440 minutes
	while (tf < 0) tf += 1440
	while (tf > 1440) tf -= 1440
	while (tt < 0) tt += 1440
	while (tt > 1440) tt -= 1440
	if(tf < tt){
		return (tc >= tf) && (tc < tt)
	} else {
		return (tc < tt) || (tc >= tf)
	}
}

@Field static final List<String> trueStrings= [ '1', 'true',  "on",  "open",   "locked",   "active",   "wet",             "detected",     "present",     "occupied",     "muted",   "sleeping"]
@Field static final List<String> falseStrings=[ '0', 'false', "off", "closed", "unlocked", "inactive", "dry", "clear",    "not detected", "not present", "not occupied", "unmuted", "not sleeping", "null"]

private static cast(value, String dataType){
	switch (dataType){
		case sLONG:
			if(value == null) return 0L
			if(value instanceof String){
				if(value.isInteger())
					return (Long) value.toInteger()
				if(value.isFloat())
					return (Long) Math.round(value.toFloat())
				if(value in trueStrings)
					return 1L
			}
			Long result
			try {
				result=(Long) value
			} catch(all){
			}
			return result ? result : 0L
		case sNUMBR:
			if(value == null) return (Integer) 0
			if(value instanceof String){
				if(value.isInteger())
					return value.toInteger()
				if(value.isFloat())
					return (Integer) Math.floor(value.toFloat())
				if(value in trueStrings)
					return (Integer) 1
			}
			def result
			try {
				result=(Integer) value
			} catch(all){
				result=(Integer) 0
			}
			return result ? result : (Integer) 0
		case "string":
		case "text":
			if(value instanceof Boolean){
				return value ? sTRUE : sFALSE
			}
			return value ? "$value" : sBLK
		case "decimal":
			if(value == null) return (Float)0
			if(value instanceof String){
				if(value.isFloat())
					return (Float) value.toFloat()
				if(value.isInteger())
					return (Float) value.toInteger()
				if(value in trueStrings)
					return (Float) 1
			}
			def result
			try {
				result=(Float) value
			} catch(all){
			}
			return result ? result : (Float) 0
		case 'boolean':
			if(value instanceof String){
				if(!value || (value in falseStrings))
					return false
				return true
			}
			return !!value
	}
	return value
}

//TODO is this expensive?
Date getSunrise(sunTimes){
	return adjustTime(sunTimes.sunrise)
}

Date getSunset(sunTimes){
	return adjustTime(sunTimes.sunset)
}

String timeIntervalLabel(){
	String start=sBLK
	start += myStrFix(startTimeType, starting, startTimeOffset)


	String finish=sBLK
	finish += myStrFix(endTimeType, ending, endTimeOffset)

	start && finish ? start+' to '+finish : sBLK
}

String myStrFix(String strtTimeType, String strting, Long strtTimeOffset){
	String start=sBLK
	switch (strtTimeType){
		case sTIME:
			if(strting){
				start += hhmm(strting)
			}
			break
		case sSUNRISE:
		case sSUNSET:
			start += strtTimeType[0].toUpperCase() + strtTimeType[1..-1]
			if(strtTimeOffset){
				start += (strtTimeOffset > 0 ? '+' : sBLK) +strtTimeOffset.toString()+' min'
			}
			break
	}
	return start
}

static String myObj(obj){
	if(obj instanceof String){return 'String'}
	else if(obj instanceof Map){return 'Map'}
	else if(obj instanceof List){return 'List'}
	else if(obj instanceof ArrayList){return 'ArrayList'}
	else if(obj instanceof Integer){return 'Int'}
	else if(obj instanceof BigInteger){return 'BigInt'}
	else if(obj instanceof Long){return 'Long'}
	else if(obj instanceof Boolean){return 'Bool'}
	else if(obj instanceof BigDecimal){return 'BigDec'}
	else if(obj instanceof Float){return 'Float'}
	else if(obj instanceof Byte){return 'Byte'}
	else{ return 'unknown'}
}

//sets complete/not complete for the setup section on the main dynamic page
String greyedOut(){
	String result=sBLK
	if(switches){
		result="complete"		
	}
	result
}

//sets complete/not complete for the settings section on the main dynamic page
String greyedOutSettings(){
	String result=sBLK
	if(people || days || falseAlarmThreshold ){
		result="complete"		
	}
	result
}
