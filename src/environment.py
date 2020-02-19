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

    def getReward(self):
        return self.game.calculateReward()

    def move(self, fromXY, toXY, shotXY):
        return self.game.move(fromXY, toXY, shotXY)

    def saveCheckpoint(self):
        self.currentCheckpoint = {
            "board": self.game.board,
            "turn": self.game.isBlackTurn
        }

    def loadCheckpoint(self):
        self.game.board = copy(self.currentCheckpoint["board"])
        self.game.isBlackTurn = self.currentCheckpoint["turn"]

    def getSelectionMask(self):
        return self.game.board[0]

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
        for arr in state:
            for char in np.nditer(arr):
                string += str(char)
            string += " "
        return string
