from environment import Environment
from neuralNet import NeuralNet

import time
import numpy as np
import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision

import visdom


class Agent():
    EPISODES = 1

    def __init__(self, modelByRecent=False, modelByName=""):
        self.visdom = visdom.Visdom(port=8097)
        self.env = Environment()
        self.device = torch.device(
            "cuda") if torch.cuda.is_available() else torch.device("cpu")

        self.selectionNNet = NeuralNet().to(self.device)
        self.movementNNet = NeuralNet(4).to(self.device)
        self.arrowShotNNet = NeuralNet(4).to(self.device)

        # TODO adapt for 3x nnets
        if modelByRecent:
            self.net.loadMostRecent()
        if modelByName != "":
            self.net.load(modelByName)

        self.train()

        self.env.kill()

    def train(self):
        optimiser = torch.optim.Adam(self.selectionNNet.parameters(), lr=0.001)
        modelName = time.time()

        episode = 0
        while episode < self.EPISODES:
            self.env.isGameFinished()
            state = self.env.getState()
            tensor = torch.tensor(state, dtype=torch.float,
                                  device=self.device) / 255

            self.visdom.image(
                self.__resizeTensorForDisplay(tensor), win="State")

            tensor.unsqueeze_(0)

            optimiser.zero_grad()
            policy, value = self.selectionNNet(tensor)
            argmax = torch.argmax(policy).item()
            x1, y1 = argmax // 10, argmax % 10

            selectionRepresentation = torch.zeros_like(policy)
            selectionRepresentation[x1, y1] = 1

            tensor = torch.cat(
                (tensor[0], selectionRepresentation.view(1, 10, 10)), 0)

            tensor.unsqueeze_(0)

            print(tensor.size())

            policy, value = self.movementNNet(tensor)
            argmax = torch.argmax(policy).item()
            x2, y2 = argmax // 10, argmax % 10

            tensor[0, 3, x1, y1] = 0
            tensor[0, 3, x2, y2] = 1

            policy, value = self.arrowShotNNet(tensor)
            argmax = torch.argmax(policy).item()
            x3, y3 = argmax // 10, argmax % 10

            visualisation = np.zeros((3, 10, 10))
            visualisation[0, x1, y1] = 255
            visualisation[1, x2, y2] = 255
            visualisation[2, x3, y3] = 255

            self.visdom.image(self.__resizeTensorForDisplay(
                visualisation), win="Next move")
            # self.visdom.image(self.__resizeTensorForDisplay(value), win="Value")

            '''
            target = torch.tensor(self.env.getValidMoves(), dtype=torch.float)
            self.visdom.image(self.__resizeTensorForDisplay(
                target) * 4, win="Valid")


            loss = F.kl_div(policy, target)
            loss.backward()
            optimiser.step()

            self.env.move(*maxes)

            '''

            print(f"Episode {episode} finished.")
            episode += 1

            # self.net.save(modelName)

    def __resizeTensorForDisplay(self, tensor):
        return tensor

    def eval(self):
        pass


if __name__ == "__main__":
    Agent(modelByRecent=False)
