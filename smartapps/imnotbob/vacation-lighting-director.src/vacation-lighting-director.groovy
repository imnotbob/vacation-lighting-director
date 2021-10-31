/**
 *	Vacation Lighting Director  (based off of tslagle's original)
 *	Optimized for Hubitat
 *	Supports Longer interval times (up to 180 mins)
 *	Only turns off lights it turned on (vs calling to turn all off) during normal cycling
 *
 *	Updated to turn on a set of lights during active time, and turn them off at end of active time
 *
 *	Source code can be found here:
 *	https://github.com/imnotbob/vacation-lighting-director/blob/beta/smartapps/imnotbob/vacation-lighting-director.src/vacation-lighting-director.groovy
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
//file:noinspection GrDeprecatedAPIUsage
//file:noinspection GroovySillyAssignment
//file:noinspection unused

import java.text.SimpleDateFormat
import groovy.transform.Field

@Field static final String sMyName = 'Vacation Lighting Director'
@Field static final String appVersionFLD ='1.1.0.1'
//@Field static final String appModifiedFLD='2021-10-31'

// Below can remove two comments '//' to allow multiple instances to be deployed (for example daytime instance and nighttime instance)
definition(
	name: "Vacation Lighting Director",
	namespace: "imnotbob",
	author: "ERS",
	category: "Safety & Security",
	description: "Randomly turn on/off lights to simulate the appearance of a occupied home while you are away.",
	iconUrl: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png",
	iconX2Url: "http://icons.iconarchive.com/icons/custom-icon-design/mono-general-2/512/settings-icon.png" //,
	// singleInstance: false
)

preferences{
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
		type:				sNUMBR,
		title:				"Minutes? (5-180)",
		range:				"5..180",
		required:			true
	]

	Map number_of_active_lights=[
		name:				"number_of_active_lights",
		type:				sNUMBR,
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

//	def pageName="Setup"

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

//	def pageName="Settings"

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
			paragraph "Restrict how your simulator runs. For instance you can restrict on which days it will run " +
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
		section("Debug & Command options"){
			input(
					'debugLogging',
					'bool',
					title: 'Enable debug logging',
					defaultValue: false,
					submitOnChange: true
			)
			input(
					'dco',
					'bool',
					title: 'Disable Command Optimizations',
					defaultValue: false,
					submitOnChange: true
			)
			input(
					'cmdDelay',
					'bool',
					title: 'Enable delays between switch commands to reduce zwave/zigbee timing',
					defaultValue: true,
					submitOnChange: true
			)
		}
	}
}

def timeIntervalPage(){
	dynamicPage(name: "timeIntervalPage", title: "Only during a certain time"){
		section{
			input "startTimeType", "enum", title: "Starting at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if((String)settings.startTimeType in [sSUNRISE,sSUNSET]){
				input "startTimeOffset", sNUMBR, title: "Offset in minutes (+/-)", range: "*..*", required: false
			}
			else{
				input "starting", "time", title: "Start time", required: false
			}
		}
		section{
			input "endTimeType", "enum", title: "Ending at", options: [["time": "A specific time"], ["sunrise": "Sunrise"], ["sunset": "Sunset"]], submitOnChange:true
			if((String)settings.endTimeType in [sSUNRISE,sSUNSET]){
				input "endTimeOffset", sNUMBR, title: "Offset in minutes (+/-)", range: "*..*", required: false
			}else{
				input "ending", "time", title: "End time", required: false
			}
		}
	}
}

@Field static final String sNULL=(String)null
@Field static final String sBLK=''
@Field static final String sLONG='long'
@Field static final String sNUMBR='number'
@Field static final String sTRUE='true'
@Field static final String sFALSE='false'
@Field static final String sTIME='time'
@Field static final String sSUNRISE='sunrise'
@Field static final String sSUNSET='sunset'

@Field volatile static Map<String,Map> theCacheVFLD=[:]

void installed(){
	clearState()
	initialize()
}

void updated(){
	logTrace "updated"
	unsubscribe()
	clearState(true)
	initialize()
	if((Boolean)settings.debugLogging) runIn(7200, logsOff)
}

void logsOff(){
	logWarn "${app.label} debug logging disabled..."
	app.updateSetting('debugLogging',[value:sFALSE,type:'bool'])
}

void initialize(){
	subscribe(location, "systemStart", modeChangeHandler)
	if(settings.newMode != null){
		subscribe(location, "mode", modeChangeHandler)
	}
	schedStartEnd()
	if(settings.people){
		subscribe(settings.people, "presence", modeChangeHandler)
	}
	logDebug "Initialized with settings: ${settings}"
	setSched()
}

void clearState(Boolean turnOff=false){
	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]

	Boolean running=((Boolean)fld.Running || (Boolean)state.Running)

	if(turnOff && running){
		Boolean first=true
		String cmd='off'
		((List)settings.switches).each { it ->
			first=changeSwitch(it,cmd, first)
		}
		//settings.switches*.off()
		if(settings.on_during_active_lights){
			((List)settings.on_during_active_lights).each{ it ->
				first=changeSwitch(it,cmd, first)
			}
		//	settings.on_during_active_lights*.off()
		}
		logInfo "All OFF"
	}
	clearSched()
	fld.Running=false
	fld.lastUpdDt=sNULL
	fld.vacactive_switches=[]
	theCacheVFLD[myId]=fld
	theCacheVFLD=theCacheVFLD
	state.Running=false
	state.lastUpdDt=sNULL
	state.vacactive_switches=[]
}


void schedStartEnd(){
	logTrace "schedStartEnd"
	Map sunTimes=app.getSunriseAndSunset()
	if(!sunTimes.sunrise){
		logWarn "Actual sunrise and sunset times unavailable, please reset hub location"
		return
	}
	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]

	Boolean running= ((Boolean)fld.startendRunning || (Boolean)state.startendRunning)
	Date start=null
	if(settings.starting != null || (String)settings.startTimeType != sNULL){
		start=timeWindowStart(sunTimes, true)
		if(start && !((Long)start.getTime() > now()) ) start=new Date((Long)start.getTime()+(24*60*60*1000L))
		logDebug "Scheduling start $start"
		schedule(start, startTimeCheck)
		running=true
	}
	if(settings.ending != null || (String)settings.endTimeType != sNULL){
		Date end=timeWindowStop(sunTimes, start, true)
		if(end && !((Long)end.getTime() > now()) ) end=new Date((Long)end.getTime()+(24*60*60*1000L))
		logDebug "Scheduling end $end"
		schedule(end, endTimeCheck)
		running=true
	}
	fld.startendRunning=running
	theCacheVFLD[myId]=fld
	theCacheVFLD=theCacheVFLD
	state.startendRunning=running
}

void setSched(){
	Integer delay=(settings.falseAlarmThreshold != null && settings.falseAlarmThreshold != sBLK) ? settings.falseAlarmThreshold * 60 : 120 // 2 * 60
	delay= delay<0 || delay>300 ? 120 : delay
	logTrace "setSched - schedule a check in $delay seconds"
	if(delay>0) {
		runIn(delay, initCheck)
		String myId=app.id.toString()
		Map fld=theCacheVFLD[myId] ?: [:]
		fld.schedRunning=true
		theCacheVFLD[myId]=fld
		theCacheVFLD=theCacheVFLD
		state.schedRunning=true
	}
	else initCheck()
}

void modeChangeHandler(evt){
	logTrace "modeChangeHandler Event Name ${evt.name} event value: ${evt.value}"
	setSched()
}

void clearSched(){
	unschedule('endTimeCheck')
	unschedule('startTimeCheck')
	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]
	fld.startendRunning=false
	theCacheVFLD[myId]=fld
	theCacheVFLD=theCacheVFLD
	state.startendRunning=false
	clearSched1()
}

void clearSched1(){
	unschedule('initCheck')
	unschedule('failsafe')
	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]
	fld.schedRunning=false
	theCacheVFLD[myId]=fld
	theCacheVFLD=theCacheVFLD
	state.schedRunning=false
}

// likely not needed
/*
void initCheck1(){
	logTrace "initCheck1"
	clearSched()
	scheduleCheck()
} */

void initCheck(){
	logTrace "initCheck"
	clearSched1()
	scheduleCheck()
}

// likely not needed
void failsafe(){
	logWarn "failsafe run"
	clearSched1()
	scheduleCheck()
}

void startTimeCheck(){
	logTrace "startTimeCheck"
	setSched()
}

void endTimeCheck(){
	logTrace "endTimeCheck"
	scheduleCheck()
}

String getDtNow(){
	Date now=new Date()
	return formatDt(now)
}

String formatDt(Date dt){
	SimpleDateFormat tf=new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy")
	if(getTimeZone()) tf.setTimeZone(getTimeZone())
	else logWarn "TimeZone is not found or is not set... Please Try to open your location and Press Save..."
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

TimeZone getTimeZone(){
	TimeZone tz=null
	if(location?.timeZone) tz=(TimeZone)location.timeZone
	if(!tz){ logWarn "getTimeZone: TimeZone is not found or is not set... Please Try to open your location and Press Save..." }
	return tz
}

Integer getLastUpdSec(){
	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]
	String tm= fld.lastUpdDt ?: state.lastUpdDt
	return !tm ? 100000 : GetTimeDiffSeconds(tm).toInteger()
}

//Main logic to pick a random set of lights from the large set of lights to turn on and then turn the rest off

void scheduleCheck(){
	logTrace "scheduleCheck"
	Boolean mTimeOk=getTimeOk()
	Boolean mHomeIsEmpty=getHomeIsEmpty()
	Boolean mDaysOk=getDaysOk()

	Boolean someoneIsHome=!mHomeIsEmpty
	Boolean allOk=getModeOk() && mDaysOk && mTimeOk && mHomeIsEmpty
	Integer setFreq= (Integer)settings.frequency_minutes

	Integer lastUpd=getLastUpdSec()

	String myId=app.id.toString()
	Map fld=theCacheVFLD[myId] ?: [:]

	if(allOk && lastUpd > ((setFreq - 1) * 60) ){
		fld.lastUpdDt=getDtNow()
		fld.Running=true
		theCacheVFLD[myId]=fld
		theCacheVFLD=theCacheVFLD
		state.lastUpdDt=fld.lastUpdDt
		state.Running=true

		logDebug("Running")

		List inactive_switches=(List)settings.switches
		List<Integer> vacactive_switches=[]

		Boolean ba= ((Boolean)fld.Running || (Boolean)state.Running)
		if(ba){
			vacactive_switches=(List<Integer>)fld.vacactive_switches
			vacactive_switches= vacactive_switches!=null ? vacactive_switches : (List<Integer>)state.vacactive_switches
		}

		Random random=new Random()
		Integer numlight=(Integer)settings.number_of_active_lights
		Integer sz=inactive_switches.size()
		if(numlight > sz) numlight=sz
		logTrace "available switches: ${sz} number to turn on (numlight): ${numlight}"
		List<Integer> new_vacactive_switches=[]

		for (Integer i=0 ; i < numlight ; i++){
			// grab a random switch to turn on
			Integer random_int=random.nextInt(sz)
			while (new_vacactive_switches?.contains(random_int)){
				random_int=random.nextInt(sz)
			}
			new_vacactive_switches << random_int
			// if selected on switch is in turn-off list, remove it from turn off list
			if(vacactive_switches.contains(random_int)) {
				vacactive_switches.remove(vacactive_switches.indexOf(random_int))
			}
		}

		fld.vacactive_switches=new_vacactive_switches
		theCacheVFLD[myId]=fld
		theCacheVFLD=theCacheVFLD
		state.vacactive_switches=new_vacactive_switches

		//logTrace "vacactive ${new_vacactive_switches} inactive ${inactive_switches}"

		// turn on switches
		Boolean first=true
		String cmd='on'
		for (Integer i=0 ; i < numlight; i++){
			def dev = inactive_switches[ new_vacactive_switches[i] ]
			first=changeSwitch(dev,cmd, first)
//			inactive_switches[ new_vacactive_switches[i] ].on()
//			logInfo "turned on ${inactive_switches[ new_vacactive_switches[i] ]}"
		}

		if(settings.on_during_active_lights){
			((List)settings.on_during_active_lights).each{ it ->
				first=changeSwitch(it,cmd,first)
			}
			//settings.on_during_active_lights*.on()
			//logInfo "turned on ${settings.on_during_active_lights}"
		}

		cmd='off'
		// turn off switches
		sz= vacactive_switches ? vacactive_switches.size() : 0
		for (Integer i=0; i < sz ; i++){
			def dev = inactive_switches[ vacactive_switches[i] ]
			first=changeSwitch(dev,cmd,first)
//			inactive_switches[ vacactive_switches[i] ].off()
//			logInfo "turned off ${inactive_switches[ vacactive_switches[i] ]}"
		}

		Integer random_int=random.nextInt(14)
		logTrace "reschedule ${setFreq} + ${random_int} minutes"
		fld.schedRunning=true
		theCacheVFLD[myId]=fld
		theCacheVFLD=theCacheVFLD
		state.schedRunning=true
		runIn( (setFreq+random_int)*60, initCheck, [overwrite: true])
		runIn( (setFreq+random_int + 10)*60, failsafe, [overwrite: true])

	}else{

		if(allOk && lastUpd <= ((setFreq - 1) * 60) ){
			logTrace "had to reschedule ${lastUpd}, ${setFreq}"
			runIn( (setFreq*60-lastUpd), initCheck, [overwrite: true])
			fld.schedRunning=true
			theCacheVFLD[myId]=fld
			theCacheVFLD=theCacheVFLD
			state.schedRunning=true

		}else{
			Boolean mySchedRunning=((Boolean)fld.schedRunning || (Boolean)state.schedRunning)
			Boolean myRunning= ((Boolean)fld.Running || (Boolean)state.Running)
			if(settings.people && someoneIsHome){
				//don't turn off lights if anyone is home
				if(myRunning || mySchedRunning){
					logDebug("Someone is home - Stopping Schedule Vacation Lights")
					clearState()
				}
			}else{
				if(!getModeOk() || !mDaysOk){
					if(myRunning || mySchedRunning){
						logDebug("wrong mode or day Stopping Vacation Lights")
						clearState(true)
					}
				}else if(getModeOk() && mDaysOk && !mTimeOk){
					if(myRunning || mySchedRunning){
						logDebug("wrong time - Stopping Vacation Lights")
						clearState(true)
					}
				}
			}
		}
	}
	Boolean aa=((Boolean)fld.startendRunning || (Boolean)state.startendRunning)
	if(!aa){
		schedStartEnd()
	}
}

private Boolean changeSwitch(dev, String val, Boolean first){
	Boolean res=first
	String curVal=sNULL
	if(!(Boolean)settings.dco) curVal=dev.currentValue('switch')
	if(!curVal || curVal != val){
		String a=sBLK
		if((Boolean)settings.cmdDelay && !first) { a=' with delay'; pauseExecution(130L) }
		dev."${val}"()
		res=false
		logInfo "turned ${val} ${dev}"+a
	}
	return res
}

Boolean getModeOk(){
	Boolean result=!settings.newMode || ((List)settings.newMode).contains(location.mode)
	//logTrace "modeOk=$result"
	result
}

Boolean getDaysOk(){
	Boolean result=true
	if((List)settings.days){
		SimpleDateFormat df=new SimpleDateFormat("EEEE")
		if(getTimeZone()) df.setTimeZone(getTimeZone())
		else{
			df.setTimeZone(TimeZone.getTimeZone("America/New_York"))
		}
		String day=(String)df.format(new Date())
		result=((List<String>)settings.days).contains(day)
	}
	//logTrace "daysOk=$result"
	result
}

Boolean getHomeIsEmpty(){
	Boolean result=true
	if(settings.people?.findAll { it?.currentPresence == "present" }){
		result=false
	}
	//logDebug("homeIsEmpty: ${result}")
	return result
}

Boolean getTimeOk(){
	Boolean result=true
	Map sunTimes=app.getSunriseAndSunset()
	if(!sunTimes.sunrise){
		logWarn "Actual sunrise and sunset times unavailable, please reset hub location"
		return false
	}
	Date start=timeWindowStart(sunTimes)
	Date stop=timeWindowStop(sunTimes, start)
	if(start && stop && getTimeZone()){
		result=checkTimeCondition((String)settings.startTimeType, (String)settings.starting, (Integer)settings.startTimeOffset, (String)settings.endTimeType, (String)settings.ending, (Integer)settings.endTimeOffset, sunTimes)
		//result=timeOfDayIsBetween( (start), (stop), new Date(), getTimeZone())
	}
	logDebug "timeOk=$result start: $start   stop: $stop"
	result
}


Date timeWindowStart(Map sunTimes, Boolean usehhmm=false){
	Date result=timeWindowMgmt((String)settings.startTimeType, (Integer)settings.startTimeOffset, (String)settings.starting, sunTimes, usehhmm)
//	logDebug "timeWindowStart=${result}  ${formatDt(result)}"
	return result
}

Date timeWindowMgmt(String strtTimeType, Long strtTimeOffset, strting, Map sunTimes, Boolean usehhmm, Boolean useST=false, Date st=null){
	Long lresult
	Date result=null
	if(strtTimeType == sSUNRISE){
		lresult=(Long)((Date)sunTimes.sunrise).getTime()
		if(lresult && strtTimeOffset){
			lresult=lresult + Math.round(strtTimeOffset * 60000L)
		}
		result=new Date(lresult)
	}
	else if(strtTimeType == sSUNSET){
		lresult=(Long)((Date)sunTimes.sunset).getTime()
		if(lresult && strtTimeOffset){
			lresult=lresult + Math.round(strtTimeOffset * 60000L)
		}
		result=new Date(lresult)
	}
	else if(strting && getTimeZone()){
		if(usehhmm){ result=timeToday(hhmm(strting), getTimeZone()) }
		else{ result=timeToday(strting, getTimeZone()) }
	}
	if(useST && result && st && (Long)st.getTime() > (Long)result.getTime()){
		result=new Date(result.getTime()+86400000L)
	}
	result
}

Date timeWindowStop(Map sunTimes, Date st, Boolean usehhmm=false){
	Date result=timeWindowMgmt((String)settings.endTimeType, (Integer)settings.endTimeOffset, (String)settings.ending, sunTimes, usehhmm, true, st)
//	logDebug "timeWindowStop=${result} ${formatDt(result)}"
	return result
}

String hhmm(String time, String fmt="HH:mm"){
	Date t=timeToday(time, getTimeZone())
	SimpleDateFormat f=new SimpleDateFormat(fmt)
	f.setTimeZone(getTimeZone())
	f.format(t)
}

//adjusts the time to local timezone
Date adjustTime(time=null){
	Long ltime=null
	if(time instanceof Long){
		ltime=time
	}else if(time instanceof String){
		//get UTC time
		ltime=timeToday(time, (TimeZone)location.timeZone).getTime()
	}else if(time instanceof Date){
		//get unix time
		ltime=time.getTime()
	}else if(!ltime){
		ltime=now()
	}
	if(ltime){
		if(ltime > now()) return new Date(ltime + (Integer)((TimeZone)location.timeZone).getOffset(ltime) - (Integer)((TimeZone)location.timeZone).getOffset((Long)now()))
		return new Date(ltime)
	}
	return null
}

Boolean checkTimeCondition(String timeFrom, timeFromCustom, Long tfo, String timeTo, timeToCustom, Long tto, sunTimes){
	Long timeFromOffset=tfo
	Long timeToOffset=tto
	Date time=new Date()
	//convert to minutes since midnight
	Integer tc=(Integer)time.hours * 60 + (Integer)time.minutes
	Integer tf=0
	Integer tt=0
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
				}else{
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
	}else{
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
			try{
				result=(Long) value
			}catch(ignored){}
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
			try{
				result=(Integer)value
			}catch(ignored){
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
			try{
				result=(Float)value
			} catch(ignored){}
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
	start += myStrFix((String)settings.startTimeType, (String)settings.starting, (Integer)settings.startTimeOffset)

	String finish=sBLK
	finish += myStrFix((String)settings.endTimeType, (String)settings.ending, (Integer)settings.endTimeOffset)

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

//sets complete/not complete for the setup section on the main dynamic page
String greyedOut(){
	String result=sBLK
	if(settings.switches){
		result="complete"		
	}
	result
}

//sets complete/not complete for the settings section on the main dynamic page
String greyedOutSettings(){
	String result=sBLK
	if(settings.people || settings.days || settings.falseAlarmThreshold ){
		result="complete"		
	}
	result
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

@Field static final String sBLANK        =''
@Field static final String sSPACE        =' '
@Field static final String sLINEBR       ='<br>'
@Field static final String sCLRRED       ='red'
@Field static final String sCLRGRY       ='gray'
@Field static final String sCLRORG       ='orange'

private void logDebug(String msg){ if((Boolean)settings.debugLogging) log.debug logPrefix(msg, "purple") }
private void logInfo(String msg){ if((Boolean)settings.debugLogging) log.info sSPACE + logPrefix(msg, "#0299b1") }
private void logTrace(String msg){ if((Boolean)settings.debugLogging) log.trace logPrefix(msg, sCLRGRY) }
private void logWarn(String msg){ log.warn sSPACE + logPrefix(msg, sCLRORG) }

void logError(String msg, ex=null){
	log.error logPrefix(msg, sCLRRED)
	String a
	try{
		if(ex) a=getExceptionMessageWithLine(ex)
	}catch (ignored){}
	if(a) log.error logPrefix(a, sCLRRED)
}

static String logPrefix(String msg, String color=sNULL){
	return span(sMyName+" (v" + appVersionFLD + ") | ", sCLRGRY) + span(msg, color)
}

static String span(String str, String clr=sNULL, String sz=sNULL, Boolean bld=false, Boolean br=false){ return str ? "<span ${(clr || sz || bld) ? "style='${clr ? "color: ${clr};" : sBLANK}${sz ? "font-size: ${sz};" : sBLANK}${bld ? "font-weight: bold;" : sBLANK}'" : sBLANK}>${str}</span>${br ? sLINEBR : sBLANK}" : sBLANK }
