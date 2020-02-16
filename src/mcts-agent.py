from mcts import MCTS
from environment import Environment
from neuralNet import NeuralNet

import torch


class Agent():

    def __init__(self, currentBestNNet=None):
        self.CURRENT_BEST_NNET = currentBestNNet

    def train(self, loops=100, games=2500, searchesPerMove=1000):
        nnets = self.__loadNNets(self.CURRENT_BEST_NNET)
        for loop in range(loops):

            for game in range(games):
                env = Environment()
                while not env.isGameFinished():
                    mcts = MCTS(env, nnets)
                    print("Saving new checkpoint")
                    env.saveCheckpoint()
                    for search in range(searchesPerMove):
                        print(f"Entering state:\n{env.getState()}")
                        mcts.search()
                        env.loadCheckpoint()

    def __loadNNets(self, name):
        nNetA = NeuralNet(in_channels=3)
        nNetB = NeuralNet(in_channels=4)
        nNetC = NeuralNet(in_channels=4)

        if name is not None:
            name.replace(".pth", "a.pth")
            nNetA.load(name)

            name.replace("a.pth", "b.pth")
            nNetB.load(name)

            name.replace("b.pth", "c.pth")
            nNetC.load(name)

        return nNetA, nNetB, nNetC


if __name__ == "__main__":
    Agent().train()
