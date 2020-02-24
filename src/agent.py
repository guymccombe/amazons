import torch
from tqdm import tqdm

from environment import Environment
from mcts import MCTS
from neuralNet import NeuralNet


class Agent():

    def __init__(self, currentBestNNet=None):
        self.CURRENT_BEST_NNET = currentBestNNet
        self.device = (torch.device("cuda") if torch.cuda.is_available()
                       else torch.device("cpu"))

    def train(self, loops=100, games=2500, searchesPerMove=100):
        nnets = self.__loadNNets(self.CURRENT_BEST_NNET)
        for loop in (range(loops)):
            for game in (range(games)):
                print(f"Playing game {game+1} of {games}")
                env = Environment()
                movesTaken = []
                while not env.isGameFinished():
                    mcts = MCTS(env, nnets, self.device)
                    env.saveCheckpoint()
                    for search in tqdm(range(searchesPerMove)):
                        mcts.search()
                        env.loadCheckpoint()

                    nextMove, statesVisited = self.__randomlySampleMove(
                        env, mcts)
                    env.move(*nextMove)

    def __randomlySampleMove(self, env, tree):
        selectionState = env.toString()
        selection = tree.weightedRandomAction(selectionState)

        movementState = selectionState + "".join(str(selection))
        moveTo = tree.weightedRandomAction(movementState)

        shootAtState = movementState + "".join(str(moveTo))
        shootAt = tree.weightedRandomAction(shootAtState)

        return (selection, moveTo, shootAt), (selectionState, movementState, shootAtState)

    def __loadNNets(self, name):
        nNetA = NeuralNet(in_channels=3).to(self.device)
        nNetB = NeuralNet(in_channels=4).to(self.device)
        nNetC = NeuralNet(in_channels=4).to(self.device)

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
