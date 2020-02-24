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

    def train(self, loops=1, games=1, searchesPerMove=5):
        nnets = self.__loadNNets(self.CURRENT_BEST_NNET)
        for loop in (range(loops)):
            for game in (range(games)):
                print(f"Playing game {game+1} of {games}")
                env = Environment()
                actionsTaken = []
                while not env.isGameFinished():
                    mcts = MCTS(env, nnets, self.device)
                    env.saveCheckpoint()
                    for search in tqdm(range(searchesPerMove)):
                        mcts.search()
                        env.loadCheckpoint()

                    nextMove, actions = mcts.getRandomMove()
                    env.move(*nextMove)
                    actionsTaken += actions

                reward = env.getReward()
                isBlackWinner = not env.isBlackTurn()

                for action in actionsTaken:
                    wasBlackTurn = action[2]
                    action[2] = reward if wasBlackTurn != isBlackWinner else -reward

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
