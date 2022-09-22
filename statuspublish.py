import RPi.GPIO as GPIO
import time
import statistics
from pubnub.callbacks import SubscribeCallback
from pubnub.enums import PNStatusCategory, PNOperationType
from pubnub.pnconfiguration import PNConfiguration
from pubnub.pubnub import PubNub

#GPIO Mode (BOARD / BCM)
GPIO.setmode(GPIO.BCM)

#set GPIO Pins
GPIO_TRIGGER = 23
GPIO_ECHO = 24

#set GPIO direction (IN / OUT)
GPIO.setup(GPIO_TRIGGER, GPIO.OUT)
GPIO.setup(GPIO_ECHO, GPIO.IN)

pnconfig = PNConfiguration()

pnconfig.subscribe_key = 'sub-c-840d3687-421d-40f7-856e-595581f68d2e'
pnconfig.publish_key = 'pub-c-d038582a-e09c-48eb-b5ef-62bd6d7aa15a'
pnconfig.user_id = "playtime1"
pubnub = PubNub(pnconfig)

def my_publish_callback(envelope, status):
    # Check whether request successfully completed or not
    if not status.is_error():
        pass  # Message successfully published to specified channel.
    else:
        pass  # Handle message publish error. Check 'category' property to find out possible issue
        # because of which request did fail.
        # Request can be resent using: [status retry];

class MySubscribeCallback(SubscribeCallback):
    def presence(self, pubnub, presence):
        pass  # handle incoming presence data
       
    def status(self, pubnub, status):
        if status.category == PNStatusCategory.PNUnexpectedDisconnectCategory:
            pass  # This event happens when radio / connectivity is lost

        elif status.category == PNStatusCategory.PNConnectedCategory:
            # Connect event. You can do stuff like publish, and know you'll get it.
            # Or just use the connected event to confirm you are subscribed for
            # UI / internal notifications, etc
            try:
                while True:
                    occupiedStatus = 'Unoccupied'
                    dist = []
                    i = 0
                    while (i<10):
                        dist.append(distance())
                        print ("Measured Distance = %.1f cm" % dist[i])
                        time.sleep(1)
                        i+=1
                    stdev = statistics.stdev(dist)
                    print("Stdev of sample set is % s" % (stdev))
                    if (stdev>100):
                        occupiedStatus = 'Occupied'
                    pubnub.publish().channel('playtime').message(occupiedStatus).pn_async(my_publish_callback)
                    
            # Reset by pressing CTRL + C
            except KeyboardInterrupt:
                print("Measurement stopped by User")
                GPIO.cleanup()
            
        elif status.category == PNStatusCategory.PNReconnectedCategory:
            pass
            # Happens as part of our regular operation. This event happens when
            # radio / connectivity is lost, then regained.
        elif status.category == PNStatusCategory.PNDecryptionErrorCategory:
            pass
            # Handle message decryption error. Probably client configured to
            # encrypt messages and on live data feed it received plain text.
            
    def message(self, pubnub, message):
        # Handle new message stored in message.message
        print(message.message)

def distance():
    # set Trigger to HIGH
    GPIO.output(GPIO_TRIGGER, True)
    
    # set Trigger after 0.01ms to LOW
    time.sleep(0.00001)
    GPIO.output(GPIO_TRIGGER, False)
    
    StartTime = time.time()
    StopTime = time.time()
    
    # save StartTime
    while GPIO.input(GPIO_ECHO) == 0:
        StartTime = time.time()

    # save time of arrival
    while GPIO.input(GPIO_ECHO) == 1:
	    StopTime = time.time()
        
    # time difference between start and arrival
    TimeElapsed = StopTime - StartTime
    # multiply with the sonic speed (34300 cm/s)
    # and divide by 2, because there and back
    distance = (TimeElapsed * 34300) / 2
    
    return distance

if __name__ == '__main__':
    pubnub.add_listener(MySubscribeCallback())
    pubnub.subscribe().channels('playtime').execute()

