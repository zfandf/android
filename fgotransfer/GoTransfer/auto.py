#!python
import subprocess 
import sys
import re

channel_path = "channels.txt"
menifest_path = "AndroidManifest.xml"
ant_properties_path = "ant.properties"
# read channels and parse them
f = open(channel_path, "r")
channels = f.readlines()
f.close()

# read AndroidManifest.xml    
f = open(menifest_path, "r")
menifest = f.read()
f.close()

# read ant.properties
f = open(ant_properties_path, "r")
ant_properties = f.read()
f.close()

count = 0
for c in channels:
    arr = c.split(",")
    if len(arr) <> 2: continue
    name = arr[1].strip()
    number = arr[0].strip()

    print "start building %s" % name
	
	#replace channel_name in menifest
    pattern = re.compile('(?<=\sandroid:name=\"UMENG_CHANNEL\"\sandroid:value=\")[^"]+')
    buf_menifest = re.sub(pattern, name, menifest)
    
    #replace channel_number in menifest
    pattern = re.compile('(?<=\sandroid:name=\"USER_AGENT\"\sandroid:value=\")[^"]+')
    buf_final_menifest = re.sub(pattern, number, buf_menifest)
    
    f = open(menifest_path, "w")
    f.write(buf_final_menifest)
    f.close()

    #replace channel_name in ant_properties
    pattern = re.compile('(?<=app_channel=)[^"].*')
    buf_ant_properties = re.sub(pattern, name, ant_properties)
    
    f = open(ant_properties_path, "w")
    f.write(buf_ant_properties)
    f.close()

    sp=subprocess.Popen("ant deploy", stdout=subprocess.PIPE, shell=True)
    while True:
        buff = sp.stdout.readline()
        if buff == '' and sp.poll() != None:
            break
        else:
            print buff

    count += 1

# repair menifest file
f = open(menifest_path, "w")
f.write(menifest)
f.close()

# repair ant.properties file
f = open(ant_properties_path, "w")
f.write(ant_properties)
f.close()

print "complete"
sys.exit()