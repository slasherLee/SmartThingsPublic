/**
 *  AirKorea DTH
 *
 *  Copyright 2018 SeungCheol Lee
 *  Version 0.0.1 3/15/18
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
	definition (name: "AirKorea", namespace: "slasehrLee", author: "SeungCheol Lee") {
		capability "Polling"
        capability "Refresh"
        capability "Sensor"
        capability "capability.airQualitySensor"
        
    }
    preferences {
        input "station_name", "text", title: "관측소 이름", description: "관측소 이름을 입력해주세요", required: true
        input "refreshRate", "enum", title: "갱신 주기", defaultValue: 0, options:[0: "안함" ,10: "10분", 30: "30분", 60 : "1시간", 240 :"4시간",
        	360: "6시간", 720: "12시간", 1440: "하루"], displayDuringSetup: true
    }
	simulator {
		// TODO: define status and reply messages here
	}
	tiles (scale: 2){   
        multiAttributeTile(name:"airQuality", type:"generic", width:6, height:4) {
            tileAttribute("device.airQuality", key: "PRIMARY_CONTROL") {
    			attributeState("default", label:'${currentValue}', unit:"", /*icon:"st.Weather.weather13",*/ backgroundColors:[
                    [value: 0, color: "#153591"],
            		[value: 1, color: "#1d9114"],
            		[value: 51, color: "#f7f709"],
            		[value: 100, color: "#f7a709"],
            		[value: 150, color: "#f70909"],
            		[value: 200, color: "#5100a3"]                
                ])
  			}
            tileAttribute("device.data_time", key: "SECONDARY_CONTROL") {
           		attributeState("default", label:'${currentValue}')
            }
		}
        valueTile("pm10_value", "device.pm10_value", width: 2, height: 2) {
        	state "default", label:'PM10\n ${currentValue} \n μg/m³', unit:"μg/m³", backgroundColors:[
				[value: -1, color: "#1e9cbb"],
            	[value: 0, color: "#1d9114"],
            	[value: 55, color: "#f7f709"],
            	[value: 155, color: "#f7a709"],
            	[value: 255, color: "#f70909"],
            	[value: 355, color: "#5100a3"]
            ]
        }
        valueTile("pm25_value", "device.pm25_value", width: 2, height: 2) {
        	state "default", label:'PM2.5\n ${currentValue} \n μg/m³', unit:"μg/m³", backgroundColors:[
				[value: -1, color: "#1e9cbb"],
            	[value: 0, color: "#1d9114"],
            	[value: 10, color: "#f7f709"],
            	[value: 40, color: "#f7a709"],
            	[value: 66, color: "#f70909"],
            	[value: 150, color: "#5100a3"]
            ]
        }
        valueTile("o3_value", "device.o3_value", width: 2, height: 2) {
            state "default", label:'O3\n ${currentValue}', unit:"ppm", backgroundColor:"#00A0DC"
        }
        valueTile("no2_value", "device.no2_value", width: 2, height: 2) {
            state "default", label:'NO2\n ${currentValue}', unit:"ppm", backgroundColor:"#00A0DC"
        }
        valueTile("so2_value", "device.so2_value", width: 2, height: 2) {
            state "default", label:'SO2\n ${currentValue}', unit:"ppm", backgroundColor:"#00A0DC"
        }
        valueTile("co_value", "device.co_value", width: 2, height: 2) {
            state "default", label:' CO\n ${currentValue}', unit:"ppm", backgroundColor:"#00A0DC"
        }
        standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
            state "default", action:"refresh.refresh", icon:"st.secondary.refresh"
        }
        main "airQuality"
        details(["airQuality","pm10_value","pm25_value","o3_value","no2_value","so2_value","co_value","refresh"])
	}
}
private getAPIKey() {
    return ""
}
def parse(String description) {
	log.debug "Parsing '${description}'"
}
def refresh() { 
	poll()
}
// handle commands
def poll() {
    if (station_name){
        def refreshTime =  refreshRate ? (refreshRate as int) * 60 : 0
        if (refreshTime > 0) {
            runIn (refreshTime, poll)
            log.debug "Data will repoll every ${refreshRate} minutes"   
        }
        else log.debug "Data will never repoll" 
        def accessKey = getAPIKey()
        def params = [
    	    uri: "http://openapi.airkorea.or.kr/openapi/services/rest/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?stationName=" + station_name + 
                  "&dataTerm=month&pageNo=1&numOfRows=1&ServiceKey=${accessKey}&_returnType=json",
        	contentType: 'application/json'
    	]
        try {
            httpGet(params) {resp ->
                resp.headers.each {
                    log.debug "${it.name} : ${it.value}"
                }
                // get the contentType of the response
                log.debug "response contentType: ${resp.contentType}"
                // get the status code of the response
                log.debug "response status code: ${resp.status}"
                if (resp.status == 200){
                    // get the data from the response body
                    //log.debug "response data: ${resp.data}"
              
                    if( resp.data.list[0].pm10Value != "-" )
                    {
                        log.debug "PM10: ${resp.data.list[0].pm10Value}"
                        sendEvent(name: "pm10_value", value: resp.data.list[0].pm10Value, unit: "μg/m³", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "pm10_value", value: "--", unit: "μg/m³", isStateChange: true)
                    
                    if( resp.data.list[0].pm25Value != "-" )
                    { 
                        log.debug "PM25: ${resp.data.list[0].pm25Value}"
                        sendEvent(name: "pm25_value", value: resp.data.list[0].pm25Value, unit: "μg/m³", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "pm25_value", value: "--", unit: "μg/m³", isStateChange: true)
                    
                    def display_value
                    def unit_str = " \n ppm"
                    if( resp.data.list[0].o3Value != "-" )
                    {
                    	log.debug "Ozone: ${resp.data.list[0].o3Value}"
                        display_value = resp.data.list[0].o3Value + unit_str
                        sendEvent(name: "o3_value", value: display_value as String, unit: "ppm", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "o3_value", value: "--" + unit_str, unit: "ppm", isStateChange: true)
                    
                    if( resp.data.list[0].no2Value != "-" )
                    {
                        log.debug "NO2: ${resp.data.list[0].no2Value}"
                        display_value = resp.data.list[0].no2Value + unit_str
                        sendEvent(name: "no2_value", value: display_value as String, unit: "ppm", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "no2_value", value: "--" + unit_str, unit: "ppm", isStateChange: true)
                    
                    if( resp.data.list[0].so2Value != "-" )
                    {
                        log.debug "SO2: ${resp.data.list[0].so2Value}"
                        display_value = resp.data.list[0].so2Value + unit_str
                        sendEvent(name: "so2_value", value: display_value as String, unit: "ppm", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "so2_value", value: "--" + unit_str, unit: "ppm", isStateChange: true)
                    
                    if( resp.data.list[0].coValue != "-" )
                    {
                        log.debug "CO: ${resp.data.list[0].coValue}"
                        display_value = resp.data.list[0].coValue + unit_str
                        sendEvent(name: "co_value", value: display_value as String, unit: "ppm", isStateChange: true)
                    }
                    else
                    	sendEvent(name: "co_value", value: "--" + unit_str, unit: "ppm", isStateChange: true)
                    
                    def khai_text = "알수없음"
                    if( resp.data.list[0].khaiValue != "-" )
                    {
                        def khai = resp.data.list[0].khaiValue as Integer
                        log.debug "Khai value: ${khai}"
                    	sendEvent(name: "airQuality", value: khai, unit: "", isStateChange: true)

                        if (khai > 200) khai_text="매우 나쁨"
                        else if (khai > 150 ) khai_text="나쁨"
                        else if (khai > 100) khai_text="보통"
                        else if (khai > 50) khai_text="좋음"
                        else if (khai >= 0) khai_text="매우 좋음"
                    }
                    else
                    	sendEvent(name: "airQuality", value: "--", unit: "", isStateChange: true)
                        
                    sendEvent(name:"data_time", value: khai_text + "(" + resp.data.parm.stationName + ") - Last Updated: " + resp.data.list[0].dataTime, isStateChange: true)
          		}
            	else if (resp.status==429) log.debug "You have exceeded the maximum number of refreshes today"	
                else if (resp.status==500) log.debug "Internal server error"
            }
        } catch (e) {
            log.error "error: $e"
        }
	}
    else log.debug "The station name is missing from the device settings"
}
