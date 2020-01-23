from environment import Environment
from neuralNet import NeuralNet

import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision

import visdom


class Agent():

    def __init__(self):
        self.visdom = visdom.Visdom(port=8097)
        self.env = Environment()
        self.net = NeuralNet()


if __name__ == "__main__":
    Agent()
