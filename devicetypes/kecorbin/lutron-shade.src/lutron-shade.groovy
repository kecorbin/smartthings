/**
 *  Lutron Shade
 *
 *  Copyright 2016 Kevin Corbin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
metadata {
	definition (name: "Lutron Shade", namespace: "kecorbin", author: "Kevin Corbin") {
		capability "Switch Level"
        capability "Switch"
        capability "Window Shade"
        //capability "Polling"
        capability "Refresh"
        
        command "changeShadeState", ["string"]
      
	}

	simulator {
		// TODO: define status and reply messages here
	}

tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 4, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("unknown", label:'${name}', action:"refresh.refresh", icon:"st.doors.garage.garage-open", backgroundColor:"#ffa81e")
                attributeState("closed",  label:'down', action:"open", icon:"st.Home.home9-icn", backgroundColor:"#bbbbdd", nextState: "opening")
                attributeState("open",    label:'up', action:"close", icon:"st.Home.home9-icn", backgroundColor:"#ffcc33", nextState: "closing")
                attributeState("partially open", label:'preset', action:"presetPosition", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
                attributeState("closing", label:'${name}', action:"presetPosition", icon:"st.doors.garage.garage-closing", backgroundColor:"#bbbbdd")
                attributeState("opening", label:'${name}', action:"presetPosition", icon:"st.doors.garage.garage-opening", backgroundColor:"#ffcc33")
            }
            
            tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState("level", action:"switch level.setLevel")
            }
            /**
            tileAttribute ("device.speedLevel", key: "VALUE_CONTROL") {
                attributeState("level", action: "levelOpenClose")
            }
            **/
        }
                // the slider bard for shade position
        controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
            state("level", action:"switch level.setLevel")
        }


		standardTile("on", "device.switch", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("on", label:'open', action:"switch.on", icon:"st.doors.garage.garage-opening")
        }
        standardTile("off", "device.stopStr", width: 2, height: 2, inactiveLabel: false, decoration: "flat") {
            state("close/stop", label:'close/stop', action:"switch.off", icon:"st.doors.garage.garage-closing")
            state("default", label:'close', action:"switch.off", icon:"st.doors.garage.garage-closing")
        }

        standardTile("refresh", "command.refresh", width:2, height:2, inactiveLabel: false, decoration: "flat") {
                state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
        }
		
		// tile for the things view
        standardTile("switchmain", "device.switch") {
            state("unknown", label:'dunno', action:"refresh.refresh", icon:"st.Home.home9-icn", backgroundColor:"#ffa81e")
            state("closed",  label:'down', action:"open", icon:"st.Home.home9-icn", backgroundColor:"#bbbbdd", nextState: "opening")
            state("open",    label:'up', action:"close", icon:"st.Home.home9-icn", backgroundColor:"#ffcc33", nextState: "closing")
            state("partially open", label:'partially open', action:"st.Home.home9-icn", icon:"st.Transportation.transportation13", backgroundColor:"#ffcc33")
            state("closing", label:'closing', action:"presetPosition", icon:"st.Home.home9-icn", backgroundColor:"#bbbbdd")
            state("opening", label:'closing', action:"presetPosition", icon:"st.Home.home9-icn", backgroundColor:"#ffcc33")
            state("default", label:'preset', action:"presetPosition", icon:"st.Home.home9-icn", backgroundColor:"#ffcc33")
        }

       
        // the "switchmain" tile will appear in the Things view
        main(["switchmain"])
        details(["shade", "on", "off", "refresh"])
    }
    
}

// parse events into attributes
def parse(String description) {
	log.error "SHOULD NOT BE HERE"
}

def poll() {
	log.debug "Executing 'poll'"   
        def lastState = device.currentValue("windowShade")
    	sendEvent(name: "windowShade", value: device.deviceNetworkId + ".refresh")
        // sendEvent(name: "motion", value: lastState);
}

def refresh() {
	log.debug "Executing 'refresh'" 
	poll();
}

// not sure if this is needed on a interactive device
def changeShadeState(newState) {

	log.trace "Received update that this sensor is now $newState"
	switch(newState) {
    	case 1:
        	log.trace 'handling case 1'
			sendEvent(name: "windowShade", value: "open")
            break;
    	case 0:
        	log.trace 'handling case 0'
        	sendEvent(name: "windowShade", value: "closed")
            break;
    }
}


def on(){
	log.debug "ON COMMAND RECEIVED"
    sendEvent(name: "switch", value: "open");
 	
}
def off() {
	log.debug "OFF COMMAND RECEIVED"
    sendEvent(name: "switch", value: "closed");

}

def open() {
    log.trace "open()"
    on()
}

def close() {
    log.trace "close()"
    off()
}

def setLevel(lvl) {
	log.debug "SET COMMAND RECEIVED" + lvl
    // if level is > 0 
    if (lvl == 0) {
	sendEvent(name: "switch", value: "closed"); 
    
    } else {
    sendEvent(name: "switch", value: "open")
    }
    sendEvent(name: "level", value: lvl)
   
}

