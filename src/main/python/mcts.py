from environment import Environment
from neuralNet import NeuralNet

from math import sqrt
import numpy as np
import torch


class mcts():

    def __init__(self, env, nets):
        self.env = env
        self.nets = nets

        self.qValues = {}
        self.policies = {}
        self.edgeVisitQuantity = {}
        self.nodeVisitQuantity = {}

        self.valids = {}

    def search(self):
        if self.env.isGameFinished():
            return self.env.getReward()

        state = self.env.getState()
        stateString = self.env.toString(state)
        stateTensor = torch.tensor(state, dtype=torch.float).unsqueeze_(0)

        if stateString not in self.policies:  # new leaf node
            self.policies[stateString], value = self.nets[0](stateTensor)
            validSelections = torch.tensor(self.env.getPositionOfAmazons())

            self.policies[stateString] *= validSelections  # mask out invalids

            validCoordinates = np.nonzero(validSelections)
            self.valids[stateString] = validCoordinates
            return -value

        validCoordinates = self.valids[stateString]
        bestScore = float("-inf")
        bestAction = None
        bestActionStr = ""
        bestActionArr = None

        for action in validCoordinates:
            action = tuple(coord.item() for coord in action)
            actionArr = np.zeros((10, 10), dtype=np.uint8)
            actionArr[action] = 1
            actionStateTuple = state + (actionArr,)
            actionString = self.env.toString(actionStateTuple)

            if actionString not in self.valids:
                self.valids[actionString] = self.env.getPossibleMovesFrom(
                    action)

            if len(np.nonzero(self.valids[actionString])) < 1:
                self.policies[stateString][action] = 0
            else:
                if (stateString, action) in self.qValues:
                    exploreOrExploit = self.qValues[(stateString, action)] + \
                        self.policies[stateString][action] * \
                        sqrt(self.nodeVisitQuantity.get(stateString, 0)) / \
                        (self.edgeVisitQuantity.get((stateString, action), 0) + 1)
                else:
                    exploreOrExploit = self.policies[stateString][action] * \
                        sqrt(self.nodeVisitQuantity.get(stateString, 0))

                if exploreOrExploit > bestScore:
                    bestScore = exploreOrExploit
                    bestAction = action
                    bestActionStr = actionString
                    bestActionArr = actionStateTuple

        if bestActionStr not in self.policies:
            movementTensor = torch.tensor(bestActionArr, dtype=torch.float)
            self.policies[stateString], value = self.nets[0](bestActionArr)
            validSelections = torch.tensor(self.env.getPositionOfAmazons())

            self.policies[stateString] *= validSelections  # mask out invalids

            validCoordinates = np.nonzero(validSelections)
            self.valids[stateString] = validCoordinates
            return -value


if __name__ == "__main__":
    env = Environment()
    n1 = NeuralNet()
    n2 = NeuralNet(4)
    n3 = NeuralNet(4)
    mcts = mcts(env, (n1, n2, n3))
    mcts.search()
    mcts.search()
