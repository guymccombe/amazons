from game import Game

from copy import copy
import numpy as np


class Environment():
    def __init__(self):
        self.game = Game()
        self.currentCheckpoint = None

    def isGameFinished(self):
        return self.game.isGameFinished()

    def getState(self):
        return tuple(self.game.board)

    def isBlackTurn(self):
        return self.game.isBlackTurn

    def getReward(self):
        return self.game.calculateReward()/50

    def move(self, fromXY, toXY, shotXY):
        return self.game.move(fromXY, toXY, shotXY)

    def saveCheckpoint(self):
        self.currentCheckpoint = {
            "board": self.game.board,
            "turn": self.game.isBlackTurn
        }

    def loadCheckpoint(self):
        self.game.board = copy(self.currentCheckpoint["board"])
        self.game.isBlackTurn = copy(self.currentCheckpoint["turn"])

    def getSelectionMask(self):
        return self.game.board[int(self.game.isBlackTurn)]

    def getMovementMask(self, moveFrom):
        occupiedCells = []
        for stateImg in self.game.board:
            occupiedCells += list(map(tuple, np.argwhere(stateImg > 0)))
        return self.__validityPoller(moveFrom, occupiedCells)

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

    def getShotMask(self, newAmazonPos, oldAmazonPos):
        occupiedCells = []
        for stateImg in self.game.board:
            occupiedCells += list(map(tuple, np.argwhere(stateImg > 0)))

        if oldAmazonPos in occupiedCells:
            occupiedCells.remove(oldAmazonPos)

        return self.__validityPoller(newAmazonPos, occupiedCells)

    def toString(self, state=None):

        if state is None:
            state = self.currentCheckpoint["board"]

        string = ""
        for arr in state[:-1]:
            for point in np.transpose(np.nonzero(arr)):
                string += str(point)

        for char in np.nditer(state[-1]):
            string += str(char)

        return string

    def parseState(self, string):
        amazons = []
        for i in range(8):
            amazons += [(int(string[4*i+1+i]), int(string[4*i+3+i]))]

        ownAmazons = np.zeros((10, 10), dtype=np.uint8)
        for amazon in amazons[:4]:
            ownAmazons[amazon] = 1

        oppAmazons = np.zeros((10, 10), dtype=np.uint8)
        for amazon in amazons[4:]:
            oppAmazons[amazon] = 1

        arrows = np.fromstring(string[40:140], dtype=np.uint8)
        arrows -= ord('0')  # Convert from unicode to binary
        arrows = np.reshape(arrows, (10, 10))

        selection, movement = None, None

        if len(string) > 140:
            selection = (int(string[141]), int(string[144]))

        if len(string) > 146:
            movement = (int(string[147]), int(string[150]))

        return ownAmazons, oppAmazons, arrows, selection, movement
