/**
 *  Lutron Output
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
	definition (name: "Lutron Dimmer", namespace: "kecorbin", author: "Kevin Corbin") {

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

tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			state "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
			state "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
		}
		controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 3, inactiveLabel: false) {
			state "level", action:"switch level.setLevel"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:"", action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		
        // the function of the button in "things" list
		main(["switch"])
        // device details
		details(["switch", "levelSliderControl", "refresh"])
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

def on() {
 	log.debug "enterting Dimmer.on() "
    setLevel(100)
}

def off() {
	log.debug "entering Dimmer.off() "
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