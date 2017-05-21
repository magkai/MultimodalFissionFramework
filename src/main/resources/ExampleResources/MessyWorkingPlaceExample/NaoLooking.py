###
# ACKNOWLEDGEMENT:
# This file is based on the nao-looking-and-pointing project of Thomas Weng:
# https://github.com/thomasweng15/nao-looking-and-pointing
###

import sys
import time
import math
import numpy
import motion
import argparse
from naoqi import ALProxy

class NaoGestures():
    def __init__(self, vector):
        # Get the Nao's IP and port
        ipAdd = "192.168.0.2"
        port = 9559

        if vector is None:
            print "Error: not specified where to point."
            sys.exit()
            
        try:
            self.motionProxy = ALProxy("ALMotion", ipAdd, port)
        except Exception as e:
            print "Could not create proxy to ALMotion"
            print "Error was: ", e
            sys.exit()

        # Set postureProxy
        try:
            self.postureProxy = ALProxy("ALRobotPosture", ipAdd, port)
        except Exception, e:
            print "Could not create proxy to ALRobotPosture"
            print "Error was: ", e
            sys.exit()

        # Set constants
        self.torsoHeadOffset = numpy.array([0.0, 0.0, 0.1264999955892563])
        self.torsoLShoulderOffset = numpy.array([0.0, 0.09000000357627869, 0.10599999874830246])
        self.torsoRShoulderOffset = numpy.array([0.0, -0.09000000357627869, 0.10599999874830246])
        self.lArmInitPos = [0.11841137707233429, 0.13498550653457642, -0.04563630372285843, -1.2062638998031616, 0.4280231297016144, 0.03072221577167511]
        self.rArmInitPos = [0.11877211928367615, -0.13329118490219116, -0.04420270770788193, 1.2169694900512695, 0.4153063893318176, -0.012792877852916718]
        self.armLength = 0.22 # in meters, rounded down
        self.frame = motion.FRAME_TORSO
        self.axisMask = 7 # just control position
        self.useSensorValues = False

        self.look(vector)


    def look(self, torsoObjectVector):
        pitch, yaw = self.getPitchAndYaw(torsoObjectVector)
        print pitch
        print yaw
        sleepTime = 2.0 # seconds
        self.moveHead(pitch, yaw, sleepTime) # Move head to look
        self.moveHead(0, 0, sleepTime) # Move head back


    def getPitchAndYaw(self, torsoObjectVector):
        # Get unit vector from head to object
        headObjectVector = torsoObjectVector - self.torsoHeadOffset
        headObjectUnitVector = [x / self.magn(headObjectVector) for x in headObjectVector]

        # Compute pitch and yaw of unit vector
        pitch = -math.asin(headObjectUnitVector[2])
        yaw = math.acos(headObjectUnitVector[0])
        if headObjectUnitVector[1] < 0:
            yaw *= -1.0
        return pitch, yaw

    def magn(self, v):
        return math.sqrt(v[0]**2 + v[1]**2 + v[2]**2)

    def moveHead(self, pitch, yaw, sleepTime):
        head = ["HeadPitch", "HeadYaw"]
        fractionMaxSpeed = 0.1
        self.motionProxy.setAngles(head, [pitch, yaw], fractionMaxSpeed)
        time.sleep(sleepTime)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--xposition", type=float, default=1.1,
                        help="x position to look at")
    parser.add_argument("--yposition", type=float, default=0.25,
                        help="y position to look at")
    parser.add_argument("--zposition", type=float, default=0.2,
                        help="z position to look at")

    args = parser.parse_args()
    vector = [args.xposition, args.yposition, args.zposition]
    naoGestures = NaoGestures(vector)
