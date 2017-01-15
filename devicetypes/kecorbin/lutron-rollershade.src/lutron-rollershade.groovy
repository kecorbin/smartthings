/**
 *  Lutron Rollershade
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
	definition (name: "Lutron Rollershade", namespace: "kecorbin", author: "Kevin Corbin") {

		// capabilities and their corresponding commands
        capability "Switch Level"
        command "setLevel"
        command "raise"
        command "lower"
        capability "Switch"
        command "on"
        command "off"
        
        capability "Polling"
        command "poll"
        capability "Refresh"
        command "refresh"
        
        
        // list the commands this device type supports
        
      
	}

	simulator {
		// TODO: define status and reply messages here
        status "on": "#OUTPUT,1,1,100"
        status "off": "#OUTPUT,1,1,0"
        
        reply "on": "~OUTPUT,1,1,100.0"
        reply "off": "~OUTPUT,1,1,0.0"
	}
tiles(scale: 2) {
        multiAttributeTile(name:"shade", type: "lighting", width: 4, height: 4) {
            tileAttribute("device.switch", key: "PRIMARY_CONTROL") {
                attributeState("unknown", label:'${name}', action:"refresh.refresh", icon:"st.Home.home9-icn", backgroundColor:"#ffa81e")
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
    	//sendEvent(name: "windowShade", value: device.deviceNetworkId + ".refresh")
        // sendEvent(name: "motion", value: lastState);
}

def refresh() {
	log.debug "Executing 'refresh'" 
	poll();
}

def open() {
 	log.debug "enterting RollerShade.on() "
    setLevel(100)
}

def close() {
	log.debug "entering Rollershade.off() "
    setLevel(0)
}

def on() {
 	log.debug "enterting RollerShade.on() "
    setLevel(100)
}

def off() {
	log.debug "entering RollerSHade.off() "
    setLevel(0)
}


def setLevel(level) {
    log.debug "Entering setLevel with level " + level
    def id = device.deviceNetworkId.split(":")[2]
    RestRequest("/api/output/" + id + "/1/" + level, "POST", null)    
}
	

// main request function, helper for specific commands
def RestRequest(Path, method, device_id) {
	log.debug "Executing REST request"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    try {
        def resp = new physicalgraph.device.HubAction(
            method: method,
            path: Path,
            headers: headers)
            // support query params
            //query: [param1: "value1", param2: "value2"]
			
        //return resp
            
    }   
    
    // catch exceptions here
    catch (Exception e) {
        log.debug "Hit Exception $e on $hubAction"
    }
}



// gets the address of the hub
private getCallBackAddress() {
    return device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
}

// gets the address of the gateway for this devicedevice
private getHostAddress() {
	log.debug "deriving gatway address from " + device.deviceNetworkId

    def parts = device.deviceNetworkId.split(":")
    if (parts.length == 3) {
            def ip = parts[0]
            def port = parts[1]
			def id = parts[2]
            return convertIPtoHex(ip) + ":" + convertPortToHex(port)
	
    } else {
            log.warn "Can't figure out ip and port for device: ${device.id}"
    }
    
}
  




// conversion utils

private Integer convertHexToInt(hex) {
    return Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
    return [convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private String convertIPtoHex(ipAddress) {
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex

}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}