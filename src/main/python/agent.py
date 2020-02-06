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
    EPISODES = 40

    def __init__(self, modelByRecent=False, modelByName=""):
        self.visdom = visdom.Visdom(port=8097)
        self.env = Environment()
        self.device = torch.device(
            "cuda") if torch.cuda.is_available() else torch.device("cpu")

        self.net = NeuralNet().to(self.device)
        if modelByRecent:
            self.net.loadMostRecent()
        if modelByName != "":
            self.net.load(modelByName)

        self.env.isGameFinished()
        self.env.getState()
        print(self.env.getValidMoves())

        # self.train()

    def train(self):
        optimiser = torch.optim.Adam(self.net.parameters(), lr=0.001)
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
            policy, value = self.net(tensor)

            policy = policy.view(3, 10, 10)
            visualisation = torch.zeros([3, 10, 10], dtype=torch.uint8)
            maxes = []
            for i in range(3):
                argmax = torch.argmax(policy[i]).item()
                maxes.append((argmax // 10, argmax % 10))
                visualisation[i][maxes[i][0]][maxes[i][1]] = 255

            self.visdom.image(self.__resizeTensorForDisplay(
                visualisation), win="Next move")
            #self.visdom.image(self.__resizeTensorForDisplay(value), win="Value")

            fromXY, toXY, shootAt = maxes

            target = torch.zeros_like(policy)
            target[0] = torch.tensor(self.env.currentState[0]/255/4)
            loss = F.kl_div(policy, target)
            loss.backward()
            optimiser.step()

            self.env.move(fromXY, toXY, shootAt)

            print(f"Episode {episode} finished.")
            episode += 1
            self.net.save(modelName)

        self.env.kill()
        print("Fin.")

    def __resizeTensorForDisplay(self, tensor):
        print(tensor.size())
        return tensor.detach().cpu().numpy().repeat(
            30, axis=1).repeat(30, axis=2)

    def eval(self):
        print("TODO")


if __name__ == "__main__":
    Agent(modelByRecent=False)
