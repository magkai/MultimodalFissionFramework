###
# ACKNOWLEDGEMENT:
# This file is based on the nao-looking-and-pointing project of Thomas Weng:
# https://github.com/thomasweng15/nao-looking-and-pointing
###

import sys
import getopt
import time
import math
import numpy
import motion
import argparse
from naoqi import ALProxy

class NaoGestures():
    def __init__(self, arm, vector):
        # Get the Nao's IP and port
        ipAdd = "192.168.0.2"
        port = 9559
        if arm is None or not(arm == "LArm" or arm == "RArm"):
            print "Error: not specified which arm should be used."
            sys.exit()
        if vector is None:
            print "Error: not specified where to point."
            sys.exit()
    
        # Set motionProxy
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

        self.point(arm, vector)


    def point(self, pointingArm, torsoObjectVector):
        shoulderOffset, initArmPosition = self.setArmVars(pointingArm)
        IKTarget = self.getIKTarget(torsoObjectVector, shoulderOffset)
        sleepTime = 2.0 # seconds
        
        self.moveArm(pointingArm, IKTarget, sleepTime) # Move arm to point
        self.moveArm(pointingArm, initArmPosition, 0.0) # Move arm back

    def magn(self, v):
        return math.sqrt(v[0]**2 + v[1]**2 + v[2]**2)


    def setArmVars(self, pointingArm):
        shoulderOffset = None
        initArmPosition = None
        if pointingArm == "LArm":
            shoulderOffset = self.torsoLShoulderOffset
            initArmPosition = self.lArmInitPos
        elif pointingArm == "RArm":
            shoulderOffset = self.torsoRShoulderOffset
            initArmPosition = self.rArmInitPos
        else:
            print "ERROR: Must provide point() with LArm or RArm"
            sys.exit(1)
        return shoulderOffset, initArmPosition

    def getIKTarget(self, torsoObjectVector, shoulderOffset):
        # vector from shoulder to object
        shoulderObjectVector = torsoObjectVector - shoulderOffset

        # scale vector by arm length
        shoulderObjectVectorMagn = self.magn(shoulderObjectVector)
        ratio = self.armLength / shoulderObjectVectorMagn
        IKTarget = [x*ratio for x in shoulderObjectVector]

        # get scaled vector in torso coordinate frame
        IKTarget += shoulderOffset
        IKTarget = list(numpy.append(IKTarget, [0.0, 0.0, 0.0]))
        return IKTarget

    def moveArm(self, pointingArm, IKTarget, sleepTime):
        fractionMaxSpeed = 0.9
        self.motionProxy.setPosition(pointingArm, self.frame, IKTarget, fractionMaxSpeed, self.axisMask)
        time.sleep(sleepTime)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--arm", type=str, default="LArm",
                        help="Robots arm to use")
    parser.add_argument("--xposition", type=float, default=0.5,
                        help="x position to point to")
    parser.add_argument("--yposition", type=float, default=0.4,
                        help="y position to point to")
    parser.add_argument("--zposition", type=float, default=0.01,
                        help="z position to point to")

    args = parser.parse_args()
    vector = [args.xposition, args.yposition, args.zposition]
    naoGestures = NaoGestures(args.arm, vector)

  

