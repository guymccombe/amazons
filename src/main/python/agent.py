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

    def __init__(self):
        self.visdom = visdom.Visdom(port=8097)
        self.env = Environment()
        self.device = torch.device(
            "cuda") if torch.cuda.is_available() else torch.device("cpu")

        self.net = NeuralNet().to(self.device)
        self.train()

    def train(self):
        optimiser = torch.optim.Adam(self.net.parameters(), lr=0.001)

        while not self.env.isGameFinished():
            state = self.env.getState()
            tensor = torch.tensor(state, dtype=torch.float,
                                  device=self.device) / 255
            self.visdom.image(tensor, win="State")
            tensor.unsqueeze_(0)

            optimiser.zero_grad()
            prediction = self.net(tensor).view(3, 10, 10)

            visualisation = torch.zeros([3, 10, 10], dtype=torch.uint8)
            maxes = []

            # TODO selection from policy by weighted random
            for i in range(3):
                argmax = torch.argmax(prediction[i]).item()
                maxes.append((argmax // 10, argmax % 10))
                visualisation[i][maxes[i][0]][maxes[i][1]] = 255
            self.visdom.image(visualisation, win="Next move")
            fromXY, toXY, shootAt = maxes

            # TODO loss function for teaching valid moves

            self.net.save()

    def eval(self):
        print("TODO")


if __name__ == "__main__":
    Agent()
