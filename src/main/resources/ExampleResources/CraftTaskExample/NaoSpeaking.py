import sys
import motion
import argparse
from naoqi import ALProxy

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--out", type=str, default="Hello",
                        help="what Nao should say")
    args = parser.parse_args()
    ipAdd = "192.168.0.2"
    port = 9559
    postureProxy = ALProxy("ALRobotPosture", ipAdd, port)
    tts    = ALProxy("ALTextToSpeech", ipAdd, port)
    tts.setParameter("speed", 80)
    tts.say(args.out)
