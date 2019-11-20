import numpy as np
from cv2 import imread
from os import path, stat, remove

thisFileDir = path.dirname(__file__)
ROOT_PATH = path.join(thisFileDir, "../interfaces/")


def getGameStateOrReward():
    ''' Returns tuple of game state images, or white/black reward tuple.'''
    global ROOT_PATH

    if stat(ROOT_PATH + 'state/0.gif').st_size == 0:
        return getGameStateOrReward()
    else:
        image1 = imread(ROOT_PATH + 'state/0.gif')
        width = image1.shape[1]
        if width == 1:
            return calculateReward(image1)
        else:
            image2 = imread(ROOT_PATH + 'state/1.gif')
            image3 = imread(ROOT_PATH + 'state/2.gif')

            remove(ROOT_PATH + 'state/0.gif')
            remove(ROOT_PATH + 'state/1.gif')
            remove(ROOT_PATH + 'state/2.gif')

            return (image1, image2, image3)


def calculateReward(rewardImage):
    reward = rewardImage.shape[0]    # Image height
    if rewardImage[0, 0][0] == 255:  # If white won
        return (reward, -reward)
    else:
        return (-reward, reward)


getGameStateOrReward()
