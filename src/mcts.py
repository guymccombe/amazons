from environment import Environment
from neuralNet import NeuralNet

from math import sqrt
import numpy as np
from random import random
import torch


class MCTS():

    def __init__(self, env, nets, device):
        self.env = env
        self.nets = nets
        self.device = device

        self.qValues = {}
        self.policies = {}
        self.edgeVisitQuantity = {}
        self.nodeVisitQuantity = {}

        self.valids = {}

    def search(self):
        if self.env.isGameFinished():
            # Returns 3-tuple reward
            return (-self.env.getReward(),) * 3

        values = [0, 0, 0]

        state = self.env.getState()
        stateString = self.env.toString(state)
        stateTensor = torch.tensor(
            state, dtype=torch.float, device=self.device).unsqueeze_(0)

        if stateString not in self.policies:
            self.policies[stateString], values[0] = self.nets[0](stateTensor)
            validSelections = torch.tensor(
                self.env.getSelectionMask(), device=self.device)

            self.policies[stateString] *= validSelections  # mask out invalids

            validCoordinates = np.nonzero(validSelections)
            self.valids[stateString] = validCoordinates

        validCoordinates = self.valids[stateString]
        bestScore = float("-inf")
        bestSelection = None
        bestSelectionStr = ""
        bestSelectionArr = None

        for selection in validCoordinates:
            selection = tuple(coord.item() for coord in selection)
            selectionArr = np.zeros((10, 10), dtype=np.uint8)
            selectionArr[selection] = 1
            selectionTuple = state + (selectionArr,)
            selectionString = stateString + \
                str(selection[0]) + str(selection[1])

            if selectionString not in self.valids:
                self.valids[selectionString] = self.env.getMovementMask(
                    selection)

            if (len(np.transpose(np.nonzero(self.valids[selectionString]))) < 1 and len(np.nonzero(self.valids[selectionString] < 1))):
                valid = self.valids[stateString]
                selectionTensor = torch.tensor(
                    selection, device=self.device, dtype=torch.float)

                for i in range(len(valid)):
                    if torch.all(torch.eq(valid[i],  selectionTensor)):
                        valid = torch.cat([valid[:i], valid[i+1:]])
                        break

                self.valids[stateString] = valid

            else:
                if (stateString, selection) in self.qValues:
                    score = self.qValues[(stateString, selection)] + \
                        self.policies[stateString][selection] * \
                        sqrt(self.nodeVisitQuantity.get(stateString, 0)) / \
                        (self.edgeVisitQuantity.get((stateString, selection), 0) + 1)

                else:
                    score = self.policies[stateString][selection] * \
                        sqrt(self.nodeVisitQuantity.get(stateString, 0))

                if score > bestScore:
                    bestScore = score
                    bestSelection = selection
                    bestSelectionStr = selectionString
                    bestSelectionArr = selectionTuple

        if bestSelectionStr not in self.policies:
            movementTensor = torch.tensor(
                bestSelectionArr, dtype=torch.float, device=self.device).unsqueeze(0)
            self.policies[bestSelectionStr], values[1] = self.nets[1](
                movementTensor)

            # mask out invalids
            self.policies[bestSelectionStr] *= torch.tensor(
                self.valids[bestSelectionStr], device=self.device)

        validCoordinates = np.transpose(
            np.nonzero(self.valids[bestSelectionStr]))
        bestScore = float("-inf")
        bestMove = None

        # Choose coord to move to
        for moveTo in validCoordinates:
            moveTo = tuple(coord.item() for coord in moveTo)

            if (bestSelectionStr, moveTo) in self.qValues:
                score = self.qValues[(bestSelectionStr, moveTo)] + \
                    self.policies[bestSelectionStr][moveTo] * \
                    sqrt(self.nodeVisitQuantity.get(bestSelectionStr, 0)) / \
                    (self.edgeVisitQuantity.get((bestSelectionStr, moveTo), 0) + 1)
            else:
                score = self.policies[bestSelectionStr][moveTo] * \
                    sqrt(self.nodeVisitQuantity.get(bestSelectionStr, 0))

            if score > bestScore:
                bestScore = score
                bestMove = moveTo

        bestMoveString = bestSelectionStr + str(bestMove[0]) + str(bestMove[1])
        shotTensor = torch.tensor(
            bestSelectionArr, dtype=torch.float, device=self.device)
        shotTensor[0][bestSelection], shotTensor[0][bestMove] = 0, 1
        shotTensor[3][bestSelection], shotTensor[3][bestMove] = 0, 1
        shotTensor.unsqueeze_(0)

        # New leaf node
        if bestMoveString not in self.policies:
            self.policies[bestMoveString], values[2] = self.nets[2](shotTensor)
            validSelections = torch.tensor(
                self.env.getShotMask(bestMove, bestSelection), device=self.device)

            # mask out invalids
            self.policies[bestMoveString] *= validSelections

            validCoordinates = np.nonzero(validSelections)
            self.valids[bestMoveString] = validCoordinates
            return [-value for value in values]

        validShots = self.valids[bestMoveString]
        bestScore = float("-inf")
        bestShot = None

        for shot in validShots:
            shot = tuple(coord.item() for coord in shot)
            if (bestMoveString, shot) in self.qValues:
                score = self.qValues[(bestMoveString, shot)] + \
                    self.policies[bestMoveString][shot] * \
                    sqrt(self.nodeVisitQuantity.get(bestMoveString, 0)) / \
                    (self.edgeVisitQuantity.get((bestMoveString, shot), 0) + 1)
            else:
                score = self.policies[bestMoveString][shot] * \
                    sqrt(self.nodeVisitQuantity.get(bestMoveString, 0))

            if score > bestScore:
                bestScore = score
                bestShot = shot

        self.env.move(bestSelection, bestMove, bestShot)

        values = self.search()
        pairs = ((stateString, bestSelection),
                 (bestSelectionStr, bestMove),
                 (bestMoveString, bestShot))

        for i in range(3):
            if pairs[i] in self.qValues:
                self.qValues[pairs[i]] = (
                    self.qValues[pairs[i]] *
                    self.edgeVisitQuantity[pairs[i]] +
                    values[i] /
                    (self.edgeVisitQuantity[pairs[i]] + 1))

                self.edgeVisitQuantity[pairs[i]] += 1

            else:
                x, y = pairs[i][1]
                self.qValues[pairs[i]] = values[i]
                self.edgeVisitQuantity[pairs[i]] = 1

            if pairs[i][0] in self.nodeVisitQuantity:
                self.nodeVisitQuantity[pairs[i][0]] += 1
            else:
                self.nodeVisitQuantity[pairs[i][0]] = 1

        return [-value for value in values]

    def getRandomMove(self):
        selectionState = self.env.toString()
        selection, selPolicy = self.__weightedRandomAction(selectionState)

        movementState = selectionState + str(selection[0]) + str(selection[1])
        moveTo, movePolicy = self.__weightedRandomAction(movementState)

        shootAtState = movementState + str(moveTo[0]) + str(moveTo[1])
        shootAt, shotPolicy = self.__weightedRandomAction(shootAtState)

        return (selection, moveTo, shootAt), [[selectionState, selPolicy, self.env.isBlackTurn()],
                                              [movementState, movePolicy,
                                               self.env.isBlackTurn()],
                                              [shootAtState, shotPolicy, self.env.isBlackTurn()]]

    def __weightedRandomAction(self, state):
        filtered = {key: value for (key, value)
                    in self.edgeVisitQuantity.items() if key[0] == state}

        total = sum(filtered.values())
        adjusted = {key: value / total
                    for (key, value) in filtered.items()}

        randomChoice, total, action = random(), 0, None
        for key, value in adjusted.items():
            total += value
            if randomChoice <= total:
                action = key
                break

        policy = {}
        for key in adjusted.keys():
            policy[key[1]] = adjusted[key]

        return action[1], policy

    def getBestMove(self):
        selectionState = self.env.toString()
        selection = self.__bestAction(selectionState)

        movementState = selectionState + str(selection[0]) + str(selection[1])
        moveTo = self.__bestAction(movementState)

        shootAtState = movementState + str(moveTo[0]) + str(moveTo[1])
        shootAt = self.__bestAction(shootAtState)

        return selection, moveTo, shootAt

    def __bestAction(self, state):
        filtered = {key: value for (key, value)
                    in self.edgeVisitQuantity.items() if key[0] == state}

        action = max(filtered, key=(lambda key: filtered[key]))
        return action[1]
