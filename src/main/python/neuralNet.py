import torch
import torch.nn as nn
import torch.nn.functional as F


class NeuralNet(nn.Module):
    def __init__(self):
        super(NeuralNet, self).__init__()
        self.device = torch.device(
            "cuda") if torch.cuda.is_available() else torch.device("cpu")

    def forward(self, *input, **kwargs):
        return super().forward(*input, **kwargs)