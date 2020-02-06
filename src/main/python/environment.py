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
        self.__clearState()
        self.process = Popen(
            "java -cp target/classes controller.Controller -env")

    def getValidMoves(self):
        if self.currentState == None:
            raise Exception("Function called before retrieving game state.")
            return -1

        validMoves = np.zeros_like(self.currentState)
        validMoves[0] = self.currentState[0] // 255

        locationOfAmazons = list(
            map(tuple, np.argwhere(self.currentState[0] > 0)))

        occupiedCells = locationOfAmazons

        for i in range(1, 3):
            occupiedCells += list(map(tuple,
                                      np.argwhere(self.currentState[i] > 0)))

        for startingLocation in locationOfAmazons:
            self.__validityPoller(startingLocation, occupiedCells, validMoves)

        validMoves = np.array(validMoves, dtype=np.float)

        for i in range(3):
            validMoves[i] = validMoves[i] / np.count_nonzero(validMoves[i])

        return validMoves

    def __validityPoller(self, start, blockers, validMoves, isMove=True):
        for dirX in range(-1, 2):
            for dirY in range(-1, 2):
                if dirX == 0 and dirY == 0:
                    continue

                x, y = start
                x += dirX
                y += dirY

                while x > -1 and x < 10 and y > -1 and y < 10:
                    if (x, y) in blockers:
                        break

                    if isMove:
                        if validMoves[1][x][y] != 1:
                            self.__validityPoller(
                                (x, y), [blocker for blocker in blockers if blocker != start], validMoves, isMove=False)
                        validMoves[1][x][y] = 1
                    else:
                        validMoves[2][x][y] = 1

                    x += dirX
                    y += dirY

    def move(self, fromXY, toXY, shotXY):
        ''' Sends specified move to environment. '''
        self.__writeMove((fromXY, toXY, shotXY))

    def __writeMove(self, move):
        with open(self.pathToImages + "move/next.MOVE", "w+") as moveFile:
            moveFile.write("\n".join("%s %s" % coord for coord in move))

    def isGameFinished(self):
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
        if img[0, 0][0] == 1:  # If white won
            rewards = rewardMagnitude, -rewardMagnitude
        else:
            rewards = -rewardMagnitude, rewardMagnitude
        return rewards

    def __clearState(self):
        open(self.pathToImages + "state/0.png", "w").close()
        open(self.pathToImages + "state/1.png", "w").close()
        open(self.pathToImages + "state/2.png", "w").close()

    def getState(self):
        ''' Returns a tuple of state images '''
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

        self.currentState = ownAmazons, oppAmazons, arrows
        return self.currentState

    def kill(self):
        self.process.terminate()


if __name__ == "__main__":
    env = Environment()
