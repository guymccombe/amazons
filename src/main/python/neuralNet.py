import torch
import torch.nn as nn
import torch.nn.functional as F
from os.path import join, dirname, exists


class NeuralNet(nn.Module):
    NUMBER_OF_RESIDUAL_LAYERS = 40

    def __init__(self):
        super(NeuralNet, self).__init__()

        # input layer
        inputLayer = nn.ModuleList()
        inputLayer.append(nn.Conv2d(in_channels=3, out_channels=300,
                                    kernel_size=3, padding=1, bias=False))
        inputLayer.append(nn.BatchNorm2d(300))
        inputLayer.append(nn.ReLU())

        # residual layer
        residualLayer = nn.ModuleList()
        residualLayer.append(nn.Conv2d(in_channels=300, out_channels=300,
                                       kernel_size=3, padding=1, bias=False))
        residualLayer.append(nn.BatchNorm2d(300))
        residualLayer.append(nn.ReLU())
        residualLayer.append(nn.Conv2d(in_channels=300, out_channels=300,
                                       kernel_size=3, padding=1, bias=False))
        residualLayer.append(nn.BatchNorm2d(300))

        # output layer
        outputLayers = nn.ModuleList()
        outputLayers.append(nn.Conv2d(in_channels=300, out_channels=1,
                                      kernel_size=1, padding=1, bias=False))
        outputLayers.append(nn.BatchNorm2d(1))
        outputLayers.append(nn.ReLU())
        outputLayers.append(nn.Linear(12, 25))

        self.inputBlock = inputLayer
        self.residualBlock = residualLayer
        self.policyHead = outputLayers

    def forward(self, networkInput):
        for layer in self.inputBlock:
            networkInput = layer(networkInput)

        for _ in range(self.NUMBER_OF_RESIDUAL_LAYERS):
            original = networkInput     # For skip connection
            for layer in self.residualBlock:
                networkInput = layer(networkInput)
            networkInput = torch.relu(networkInput + original)

        for layer in self.policyHead:
            networkInput = layer(networkInput)

        return networkInput

    def save(self):
        path = join(dirname(__file__), "models\\model.pth")
        torch.save(self.state_dict(), path)
