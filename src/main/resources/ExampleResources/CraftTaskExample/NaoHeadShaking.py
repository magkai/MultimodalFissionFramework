import time
import motion
import argparse
from naoqi import ALProxy

def main(robotIP, PORT=9559):
    motionProxy  = ALProxy("ALMotion", robotIP, PORT)
   
    names = ['HeadYaw', 'HeadPitch']
    times = [[0.5], [0.5]]
    motionProxy.angleInterpolation(names, [0.0, 0.0], times, True)

    motionProxy.angleInterpolation(names, [0.3, 0.0], times, True)
    motionProxy.angleInterpolation(names, [-0.3, 0.0], times, True)
        
    motionProxy.angleInterpolation(names, [0.0, 0.0], times, True)



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--ip", type=str, default="192.168.0.2",
                        help="Robot ip address")
    parser.add_argument("--port", type=int, default=9559,
                        help="Robot port number")
    args = parser.parse_args()
    main(args.ip, args.port)
