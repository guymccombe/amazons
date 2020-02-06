import torch
import torch.nn as nn
import torch.nn.functional as F
from os import listdir
from os.path import join, dirname, exists, getctime


class NeuralNet(nn.Module):
    NUMBER_OF_RESIDUAL_LAYERS = 40

    def __init__(self):
        super(NeuralNet, self).__init__()
        self.inputLayer = self.__inputLayer()
        self.residualBlock = self.__residualBlock()
        self.policyHead = self.__policyHead()
        self.valueHead = self.__valueHead()

    def __inputLayer(self):
        layers = nn.ModuleList()
        layers.append(nn.Conv2d(in_channels=3, out_channels=500,
                                kernel_size=3, padding=1, bias=False))
        layers.append(nn.BatchNorm2d(500))
        layers.append(nn.ReLU())
        return layers

    def __residualBlock(self):
        layers = nn.ModuleList()
        layers.append(nn.Conv2d(in_channels=500, out_channels=500,
                                kernel_size=3, padding=1, bias=False))
        layers.append(nn.BatchNorm2d(500))
        layers.append(nn.ReLU())
        layers.append(nn.Conv2d(in_channels=500, out_channels=500,
                                kernel_size=3, padding=1, bias=False))
        layers.append(nn.BatchNorm2d(500))
        return layers

    def __policyHead(self):
        layers = nn.ModuleList()
        layers.append(nn.Conv2d(in_channels=500, out_channels=1,
                                kernel_size=1, padding=1, bias=False))
        layers.append(nn.BatchNorm2d(1))
        layers.append(nn.ReLU())
        layers.append(nn.Linear(12, 25))
        return layers

    def __valueHead(self):
        # TODO - currently generating wrong dimension
        layers = nn.ModuleList()
        layers.append(nn.Conv2d(in_channels=500, out_channels=1,
                                kernel_size=1, padding=1, bias=False))
        layers.append(nn.BatchNorm2d(1))
        layers.append(nn.ReLU())
        layers.append(nn.Linear(12, 25))
        layers.append(nn.ReLU())
        layers.append(nn.Linear(25, 12))
        layers.append(nn.Tanh())
        return layers

    def forward(self, networkInput):
        for layer in self.inputLayer:
            networkInput = layer(networkInput)

        for _ in range(self.NUMBER_OF_RESIDUAL_LAYERS):
            original = networkInput     # For skip connection
            for layer in self.residualBlock:
                networkInput = layer(networkInput)
            networkInput = torch.relu(networkInput + original)

        policy = networkInput
        value = networkInput
        for layer in self.policyHead:
            policy = layer(policy)

        for layer in self.valueHead:
            value = layer(value)

        return policy, value

    def save(self, name):
        path = join(dirname(__file__), f"models\\{name}.pth")
        torch.save(self.state_dict(), path)

    def __loadPath(self, path):
        self.load_state_dict(torch.load(path))
        self.eval()

    def load(self, name):
        path = join(dirname(__file__), f"models\\{name}.pth")
        self.__loadPath(path)

    def loadMostRecent(self):
        directory = join(dirname(__file__), "models")
        allPaths = [join(directory, name) for name in listdir(directory)]
        if len(allPaths) < 1:
            print("There are no saved models in the models folder. Starting fresh..")
        else:
            self.__loadPath(max(allPaths, key=getctime))
