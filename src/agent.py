import csv
from os.path import join, dirname
import numpy as np
import pandas as pd
from random import randint
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

    def train(self, loops=1, games=0, searchesPerMove=5, numberOfSamples=1):
        nnets = self.__loadNNets(self.CURRENT_BEST_NNET)
        for loop in range(loops):
            for game in range(games):
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

                with open(join(dirname(__file__), "actions.csv"), "a") as file:
                    writer = csv.writer(file, delimiter="|")
                    for action in actionsTaken:
                        wasBlackTurn = action[2]
                        action[2] = reward if wasBlackTurn != isBlackWinner else -reward
                        writer.writerow(action)

            # Loss
            actions = pd.read_csv(
                join(dirname(__file__), "actions.csv"), delimiter="|")

            numberOfActions = len(actions.index)
            if numberOfActions > 5e5:
                actions = actions.tail(5e5)
                numberOfActions = 5e5

            samples, env = [], Environment()
            for _ in range(numberOfSamples):
                # Randomly sample from DF
                sampleIndex = randint(0, numberOfActions-1)
                while sampleIndex in samples:
                    sampleIndex = randint(0, numberOfActions-1)

                samples += [sampleIndex]

            for sample in samples:
                state, policy, value = actions.iloc[sample]
                own, opp, arr, sel, mov = env.parseState(state)

                if sel is not None:
                    active = np.zeros((10, 10), dtype=np.uint8)
                    if mov is None:
                        active[sel] = 1
                        nnetIndex = 1
                    else:
                        active[mov] = 1
                        own[sel] = 0
                        own[mov] = 1

                        nnetIndex = 2

                    state = (own, opp, arr, active)
                else:
                    state = (own, opp, arr)
                    nnetIndex = 0

                state = (torch.tensor(state, dtype=torch.float, device=self.device)
                         .unsqueeze(0))

                predictedPolicy, predictedValue = nnets[nnetIndex](state)
                print(predictedPolicy, policy)
                print(predictedValue, value)
                # TODO xentropy policies and MSE value

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
