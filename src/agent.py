from ast import literal_eval
import csv
from datetime import datetime
import numpy as np
from os.path import join, dirname
import pandas as pd
from random import randint, getrandbits
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

    def train(self, loops=250, games=2000, searchesPerMove=75, numberOfSamples=500):
        nnets, optimisers = self.__loadNNets(self.CURRENT_BEST_NNET, True)
        for loop in range(loops):
            print(f"Self-play phase:")
            for game in tqdm(range(games)):
                env = Environment()
                actionsTaken = []
                while not env.isGameFinished():
                    mcts = MCTS(env, nnets, self.device)
                    env.saveCheckpoint()
                    for search in range(searchesPerMove):
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

            print("Weight updating phase")

            actions = pd.read_csv(
                join(dirname(__file__), "actions.csv"), delimiter="|")

            numberOfActions = len(actions.index)
            if numberOfActions > 5e6:
                actions = actions.tail(5e6)
                numberOfActions = 5e6

            actions.to_csv(join(dirname(__file__), "actions.csv"),
                           sep="|", index=False, header=False)

            samples, env = [], Environment()
            for _ in range(numberOfSamples):
                # Randomly sample from DF
                sampleIndex = randint(0, numberOfActions-1)
                while sampleIndex in samples:
                    sampleIndex = randint(0, numberOfActions-1)

                samples += [sampleIndex]

            for sample in tqdm(samples):
                state, policy, value = actions.iloc[sample]
                policy = literal_eval(policy)
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

                optimisers[nnetIndex].zero_grad()
                predictedPolicy, predictedValue = nnets[nnetIndex](state)

                policyT = torch.zeros((10, 10),
                                      dtype=torch.float, device=self.device)
                for action in policy.keys():
                    policyT[action] = policy[action]

                valueT = torch.tensor(value,
                                      dtype=torch.float, device=self.device)

                # Cross entropy
                xEntropy = -torch.log((1-policyT)-predictedPolicy)
                squareErr = (predictedValue - valueT)**2    # Square error

                loss = xEntropy + squareErr

                loss.mean().backward()
                optimisers[nnetIndex].step()

        wins, losses = self.__compareToCurrentBest(nnets)
        print(
            f"Evaluation results: {wins}W and {losses}L --> {100*(wins/(wins+losses))}%")

        if wins/(wins+losses) >= 0.55:
            name = str(datetime.now()) + ".pth"
            print("New best network is {name}")
            self.CURRENT_BEST_NNET = name
            self.__saveNNets(self, nnets, name)

    def __loadNNets(self, name, includeOptimisers=False):
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
        else:
            nNetA.loadMostRecent("a.pth")
            nNetB.loadMostRecent("b.pth")
            nNetC.loadMostRecent("c.pth")

        nnets = nNetA, nNetB, nNetC
        if includeOptimisers:
            optimisers = tuple(torch.optim.Adam(
                N.parameters(), lr=0.0001) for N in nnets)
            return nnets, optimisers
        else:
            return nnets

    def __compareToCurrentBest(self, trainedNets, numberOfGames=500, searchesPerMove=75):
        print("Evaluating network")
        previousNets = self.__loadNNets(self.CURRENT_BEST_NNET)
        wins, losses = 0, 0

        for game in tqdm(range(numberOfGames)):
            isTrainedBlack = bool(getrandbits(1))
            isBlacksMove = False
            env = Environment()

            while not env.isGameFinished():
                if isBlacksMove != isTrainedBlack:
                    mcts = MCTS(env, previousNets, self.device)
                else:
                    mcts = MCTS(env, trainedNets, self.device)

                env.saveCheckpoint()
                for search in range(searchesPerMove):
                    mcts.search()
                    env.loadCheckpoint()

                nextMove = mcts.getBestMove()
                env.move(*nextMove)

                isBlacksMove = not isBlacksMove

            isBlackWinner = not env.isBlackTurn
            if isBlackWinner != isTrainedBlack:
                losses += 1
            else:
                wins += 1

        return wins, losses

    def __saveNNets(self, nnets, name):
        name.replace(".pth", "a.pth")
        nnets[0].save(name)

        name.replace("a.pth", "b.pth")
        nnets[1].save(name)

        name.replace("b.pth", "c.pth")
        nnets[2].save(name)


if __name__ == "__main__":
    Agent().train()
