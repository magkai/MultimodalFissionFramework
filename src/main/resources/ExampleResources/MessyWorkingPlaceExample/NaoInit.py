# -*- encoding: UTF-8 -*-

import time
import motion
import argparse
from naoqi import ALProxy

def main(robotIP, PORT=9559):
    motionProxy  = ALProxy("ALMotion", robotIP, PORT)
    postureProxy = ALProxy("ALRobotPosture", robotIP, PORT)
    ttsProxy    = ALProxy("ALTextToSpeech", robotIP, PORT)

    # Wake up robot
    motionProxy.wakeUp()

    # Send robot to Pose Init
    postureProxy.goToPosture("StandInit", 0.5)
    motionProxy.openHand("LHand")
    motionProxy.openHand("RHand")
    ttsProxy.setParameter("speed", 70)
    ttsProxy.say("Hello! My name is Nao. I am your personal assistant. Nice to meet you. \\pau=1000\\ Let's start!")
       

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", type=str, default="192.168.0.2",
                        help="Robot ip address")
    parser.add_argument("--port", type=int, default=9559,
                        help="Robot port number")

    args = parser.parse_args()
    main(args.ip, args.port)
