import numpy as np
from cv2 import imread
from os import path, stat, remove, pardir

thisFileDir = path.dirname(__file__)
ROOT_PATH = path.join(thisFileDir, pardir, "interfaces/")


def playGame():
    stateOrReward = getGameStateOrReward()
    while len(stateOrReward) > 2:
        selection, move, shot = getNextAction(stateOrReward)
        writeMoveToFile(selection, move, shot)
        stateOrReward = getGameStateOrReward()

    backpropagateReward(stateOrReward)


def getGameStateOrReward():
    ''' Returns tuple of game state images, or white/black reward tuple.'''
    global ROOT_PATH

    if stat(ROOT_PATH + 'state/0.png').st_size == 0:
        return getGameStateOrReward()
    else:
        image1 = imread(ROOT_PATH + 'state/0.png')
        width = image1.shape[1]
        if width == 1:
            clearContentsOfState()
            return calculateReward(image1)
        else:
            image2 = imread(ROOT_PATH + 'state/1.png')
            image3 = imread(ROOT_PATH + 'state/2.png')

            clearContentsOfState()
            return image1, image2, image3


def calculateReward(rewardImage):
    ''' Returns a tuple of (white reward, black reward) '''
    reward = rewardImage.shape[0]    # Image height
    if rewardImage[0, 0][0] == 255:  # If white won
        return reward, -reward
    else:
        return -reward, reward


def clearContentsOfState():
    ''' Wipes contents of all state files, while preserving the files. '''
    global ROOT_PATH
    open(ROOT_PATH + "state/0.png", "w").close()
    open(ROOT_PATH + "state/1.png", "w").close()
    open(ROOT_PATH + "state/2.png", "w").close()


def writeMoveToFile(selectTarget, moveTarget, shotTarget):
    ''' Writes three tuple parameters to file for processing '''
    global ROOT_PATH
    moveList = [selectTarget, moveTarget, shotTarget]

    with open(ROOT_PATH + "next.MOVE", "w+") as moveFile:
        moveFile.write("\n".join("%s %s" %
                                 coordinate for coordinate in moveList))


def getNextAction():
    # TODO Get policy, choose action
    return


def backpropagateReward(reward):
    whiteReward, blackReward = reward
    # TODO Backpropagation to update DQN
    return


playGame()
