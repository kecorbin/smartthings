/**
 *  Lutron Pico Remote - used for lutron DEVICE things (buttons, etc)
 * 
 * 
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
	definition (name: "Lutron Pico", namespace: "kecorbin", author: "Kevin Corbin") {

		// capabilities and their corresponding commands
        capability "switch"
  		command "button1"
        command "button2"
        command "button3"
        command "raise"
        command "lower"
        command "center"
        
      
        
        // list the commands this device type supports
        
      
	}

	simulator {
		// TODO: define status and reply messages here
        status "on": "#DEV,1,1,100"
        status "off": "#OUTPUT,1,1,0"
        
        reply "on": "~OUTPUT,1,1,100.0"
        reply "off": "~OUTPUT,1,1,0.0"
	}

tiles {
		// icons can be found at 
        // http://scripts.3dgo.net/smartthings/icons/
        
        valueTile("blank", "device.power", decoration: "flat") {
            state "blank", label:''
        }
        standardTile("switch", "device.switch", width: 3) {
        	state "default", label: "Lutron Pico Remote", backgroundColor:"#0000ff"
        }
		standardTile("open", "device.switch", decoration: "flat", canChangeIcon: true) {
        	state "default", label:'open', action:"button1", icon:"st.Weather.weather14", backgroundColor:"#ffffff"
    	}
        standardTile("center", "device.switch", decoration: "flat", canChangeIcon: true) {
        	state "default", label:'center', action:"button2", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff"
    	}
        standardTile("close", "device.switch", decoration: "flat", canChangeIcon: true) {
        	state "default", label:'close', action:"button3", icon:"st.Bath.bath10", backgroundColor:"#ffffff"
    	}
        standardTile("lower", "device.switch", decoration: "flat", canChangeIcon: true) {
        	state "default", label:'lower', action:"lower", icon:"st.thermostat.thermostat-down", backgroundColor:"#ffffff"
    	}
        standardTile("raise", "device.switch", decoration: "flat", canChangeIcon: true) {
        	state "default", label:'raise', action:"raise", icon:"st.thermostat.thermostat-up", backgroundColor:"#ffffff"
    	}
        // the function of the button in "things" list
		main(["switch"])
        // device details
        
        // button layout 3xUnlimited Grid
		details(["switch", // this tile is 3x width
				 "blank","open","blank",
        		 "raise","center","lower",
                 "blank","close","blank"])
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

def on(){
	button1()
}
def off(){
	button3()
}


// lutron supports press and release, ST does not so we have to fool it
def press(button) {
    log.debug "Pressing button lutron_id=" + dev_id
    def id = device.deviceNetworkId.split(":")[2]
    def pressActionUri = "/api/device/" + id + "/" + button + "/3"
    def releaseActionUri = "/api/device/" + id + "/" + button + "/4"
    def press = RestRequest(pressActionUri, "POST", id)
    def release = RestRequest(releaseActionUri, "POST", id) 
}

def button1() {
	// open/on/top button on pico ID =2 
    // equivalent of #DEVICE,<id>,2,3
    log.debug "Button1 Pressed"
    press("2")    
}

def button2() {
	// center/preset button on pico ID = 3
    // equivalent of #DEVICE,<id>,3,3
	log.debug "Button2 Pressed"
    press("3")
}
def button3() {
	// close/off/bottom button on pico ID = 4
    // equivalent of #DEVICE,<id>,4,3
    log.debug "Button3 Pressed"
    press("4") 

}

def raise() {
	// raise/up button on pico ID = 5
	// equivalent to #DEVICE,<id>,5,3
    log.debug "Raise Button Pressed"
    press("5")
}


def lower() {
	// raise/up button on pico ID = 5
	// equivalent to #DEVICE,<id>,5,3
    log.debug "Lower Button Pressed"
    press("6")
}
// main request function, helper for specific commands

def RestRequest(Path, method, device_id) {
	log.debug "Executing REST request"
    def headers = [:]
    def addr = getHostAddress()
    headers.put("HOST", addr)
    log.debug method + addr + " " + Path 
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