from subprocess import Popen
from cv2 import imread, IMREAD_UNCHANGED
from os import stat
from os.path import join, dirname, exists


class Environment():

    def __init__(self):
        self.pathToImages = join(dirname(__file__), "..", "interfaces\\")
        self.__clearState()
        process = Popen("java -cp target/classes controller.Controller -env")

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

        self.__clearState()
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

        self.__clearState()
        return ownAmazons, oppAmazons, arrows


if __name__ == "__main__":
    env = Environment()
