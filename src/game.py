import numpy as np


class Game:
    def __init__(self):
        # suppress np divide by zero errors for moves
        np.seterr(divide='ignore', invalid='ignore')

        self.board = np.zeros((3, 10, 10), dtype=np.uint8)
        self.isBlackTurn = False
        self.__loadStartingBoard()

    def __loadStartingBoard(self):
        # White amazons
        self.board[0, 0, 3] = 1
        self.board[0, 3, 0] = 1
        self.board[0, 6, 0] = 1
        self.board[0, 9, 3] = 1

        # Black amazons
        self.board[1, 0, 6] = 1
        self.board[1, 3, 9] = 1
        self.board[1, 6, 9] = 1
        self.board[1, 9, 6] = 1

    def move(self, fromXY, toXY, shootAT):
        unchangedBoard = self.board

        if self.board[int(self.isBlackTurn), fromXY[0], fromXY[1]] == 1:
            if self.__isValidMove(fromXY, toXY):
                self.board[int(self.isBlackTurn), fromXY[0], fromXY[1]] = 0
                self.board[int(self.isBlackTurn), toXY[0], toXY[1]] = 1
                if self.__isValidMove(toXY, shootAT):
                    self.board[2, shootAT[0], shootAT[1]] = 1
                    self.isBlackTurn = not self.isBlackTurn
                    return

        # if any invalidities
        self.board = unchangedBoard
        raise Exception("That move is invalid.")

    def __isValidMove(self, position, destination):
        direction = np.subtract(destination, position)
        direction = direction / np.abs(direction)
        direction = np.array(np.nan_to_num(direction), dtype=np.int8)

        valid = True
        checkingPosition = position + direction

        while tuple(checkingPosition) != destination:
            for i in range(3):
                valid &= (
                    self.board[i, checkingPosition[0], checkingPosition[1]] == 0)
            checkingPosition = checkingPosition + direction

        return valid

    def isGameFinished(self):
        currentPlayersAmazons = np.nonzero(self.board[int(self.isBlackTurn)])
        currentPlayersAmazons = np.transpose(currentPlayersAmazons)
        canMove = False

        # Check if there exists an empty cell neighbouring an amazon
        for amazon in currentPlayersAmazons:
            for i in range(-1, 2):
                for j in range(-1, 2):
                    if (i, j) != (0, 0):
                        pos = amazon[0] + i, amazon[1] + j
                        if self.__isInBoard(pos):
                            for k in range(3):
                                if self.board[k, pos[0], pos[1]] == 0:
                                    return False
        return True

    def __isInBoard(self, pos):
        isIn = pos[0] > -1 and pos[0] < 10
        isIn &= pos[1] > -1 and pos[1] < 10
        return isIn

    def calculateReward(self):
        winner = int(not self.isBlackTurn)
        winnersAmazons = np.nonzero(self.board[winner])
        winnersAmazons = np.transpose(winnersAmazons)

        rewardCells = np.zeros((10, 10), dtype=np.uint8)
        for amazon in winnersAmazons:
            rewardCells = self.__rewardHelper(amazon, rewardCells)

        return(np.count_nonzero(rewardCells))

    def __rewardHelper(self, start, cells):
        for i in range(-1, 2):
            for j in range(-1, 2):
                if (i, j) != (0, 0):
                    pos = start[0] + i, start[1] + j
                    if self.__isInBoard(pos):
                        if cells[pos[0], pos[1]] == 0:
                            empty = True
                            for k in range(3):
                                empty &= self.board[k, pos[0], pos[1]] == 0

                            if empty:
                                cells[pos[0], pos[1]] = 1
                                cells = self.__rewardHelper(pos, cells)
        return cells

    def rollbackTo(self, board, turn):
        self.board = board
        self.isBlackTurn = turn
