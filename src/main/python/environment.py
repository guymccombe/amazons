from subprocess import Popen
from cv2 import imread, IMREAD_UNCHANGED
from os import stat
from os.path import join, dirname, exists
import time
import numpy as np


class Environment():

    def __init__(self):
        self.currentState = None
        self.pathToImages = join(dirname(__file__), "..", "interfaces\\")
        self.__clearStateFile()
        self.process = Popen(
            "java -cp target/classes controller.Controller -env")

    def getPositionOfAmazons(self):
        return self.currentState[0]

    def getPossibleMovesFrom(self, point):
        occupiedCells = []
        for stateImg in self.currentState:
            occupiedCells += list(map(tuple, np.argwhere(stateImg > 0)))
        return self.__validityPoller(point, occupiedCells)

    def __validityPoller(self, start, blockers):
        valid = np.zeros((10, 10))
        for dirX in range(-1, 2):
            for dirY in range(-1, 2):
                if dirX == 0 and dirY == 0:
                    continue    # Goes nowhere

                x, y = start
                x += dirX
                y += dirY

                while x > -1 and x < 10 and y > -1 and y < 10:
                    if (x, y) in blockers:
                        break

                    valid[x, y] = 1

                    x += dirX
                    y += dirY

        return valid

    def getValidShotsFromNewPos(self, newAmazonPos, oldAmazonPos):
        occupiedCells = []
        for stateImg in self.currentState:
            occupiedCells += list(map(tuple, np.argwhere(stateImg > 0)))

        if oldAmazonPos in occupiedCells:
            occupiedCells.remove(oldAmazonPos)

        return self.__validityPoller(newAmazonPos, occupiedCells)

    def move(self, fromXY, toXY, shotXY):
        ''' Sends specified move to environment. '''
        self.currentState = None
        self.__writeMove((fromXY, toXY, shotXY))

    def __writeMove(self, move):
        with open(self.pathToImages + "move/next.MOVE", "w+") as moveFile:
            moveFile.write("\n".join("%s %s" % coord for coord in move))

    def isGameFinished(self):
        if self.currentState != None:
            return self.currentState[0].shape[1] == 1

        img = None
        while img is None:
            img = imread(self.pathToImages + "state/0.png")
        return img.shape[1] == 1

    def getReward(self):
        ''' Returns a tuple of (white reward, black reward) '''
        img = imread(self.pathToImages + 'state/0.png', IMREAD_UNCHANGED)
        if img.shape[1] != 1:   # Check game is complete
            return -1

        rewardMagnitude = img.shape[0]
        if img[0, 0] > 0:  # If white won
            rewards = rewardMagnitude, -rewardMagnitude
        else:
            rewards = -rewardMagnitude, rewardMagnitude
        return rewards

    def __clearStateFile(self):
        open(self.pathToImages + "state/0.png", "w").close()
        open(self.pathToImages + "state/1.png", "w").close()
        open(self.pathToImages + "state/2.png", "w").close()

    def getState(self):
        ''' Returns a tuple of state images '''
        if self.currentState != None:
            return self.currentState

        ownAmazons = imread(self.pathToImages +
                            'state/0.png', IMREAD_UNCHANGED)
        if ownAmazons.shape[1] == 1:  # Check game is not complete
            return -1

        oppAmazons, arrows = None, None
        while oppAmazons is None or arrows is None:
            oppAmazons = imread(self.pathToImages +
                                'state/1.png', IMREAD_UNCHANGED)
            arrows = imread(self.pathToImages +
                            'state/2.png', IMREAD_UNCHANGED)

        self.currentState = ownAmazons // 255, oppAmazons // 255, arrows // 255
        self.__clearStateFile()
        return self.currentState

    def kill(self):
        self.process.terminate()

    def toString(self, state):
        string = ""
        for arr in state:
            for char in np.nditer(arr):
                string += str(char)
            string += " "
        return string


if __name__ == "__main__":
    env = Environment()
