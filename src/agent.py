from environment import Environment
from neuralNet import NeuralNet

import numpy as np
import time
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

        self.selectionNNet = NeuralNet().to(self.device)
        self.movementNNet = NeuralNet(4).to(self.device)
        self.arrowShotNNet = NeuralNet(4).to(self.device)

        if modelByRecent:
            self.selectionNNet.loadMostRecent("a")
            self.movementNNet.loadMostRecent("b")
            self.arrowShotNNet.loadMostRecent("c")
        if modelByName != "":
            self.selectionNNet.load(modelByName)
            modelByName.replace("a.pth", "b.pth")
            self.movementNNet.load(modelByName)
            modelByName.replace("b.pth", "c.pth")
            self.arrowShotNNet.load(modelByName)

        self.train()

        self.env.kill()

    def train(self):
        selectionOptimiser = torch.optim.Adam(
            self.selectionNNet.parameters(), lr=0.001)
        movementOptimiser = torch.optim.Adam(
            self.movementNNet.parameters(), lr=0.001)
        arrowShotOptimiser = torch.optim.Adam(
            self.arrowShotNNet.parameters(), lr=0.001)

        episode = 0
        while episode < self.EPISODES:
            while not self.env.isGameFinished():
                state = self.env.getState()
                tensor = torch.tensor(state, dtype=torch.float,
                                      device=self.device)

                self.visdom.image(
                    self.__resizeTensorForDisplay(tensor), win="State")

                tensor.unsqueeze_(0)

                selectionOptimiser.zero_grad()
                movementOptimiser.zero_grad()
                arrowShotOptimiser.zero_grad()

                policy, value = self.selectionNNet(tensor)
                target = torch.tensor(self.env.getPositionOfAmazons(
                ), dtype=torch.float, device=self.device)

                selectionLoss = F.kl_div(policy, target)
                selectionLoss.backward()
                selectionOptimiser.step()

                policy *= target  # filter out invalid moves

                x0, y0, target = -1, -1, -1
                while True:
                    argmax = torch.argmax(policy).item()
                    x0, y0 = argmax // 10, argmax % 10
                    target = torch.tensor(self.env.getPossibleMovesFrom(
                        (x0, y0)), dtype=torch.float, device=self.device)

                    if torch.sum((target > 0).int()).item() > 0:
                        break
                    else:
                        policy[x0, y0] = 0
                        continue

                selectionAsTensor = torch.zeros_like(policy)
                selectionAsTensor[x0, y0] = 1

                tensor = torch.cat(
                    (tensor[0], selectionAsTensor.view(1, 10, 10)), 0).unsqueeze_(0)

                policy, value = self.movementNNet(tensor)
                movementLoss = F.kl_div(policy, target)
                movementLoss.backward()
                movementOptimiser.step()

                policy *= target

                argmax = torch.argmax(policy).item()
                x1, y1 = argmax // 10, argmax % 10

                tensor[0, 3, x0, y0] = 0
                tensor[0, 3, x1, y1] = 1

                policy, value = self.arrowShotNNet(tensor)
                target = torch.tensor(self.env.getValidShotsFromNewPos(
                    (x1, y1), (x0, y0)), dtype=torch.float, device=self.device)

                arrowShotLoss = F.kl_div(policy, target)
                arrowShotLoss.backward()
                arrowShotOptimiser.step()

                policy *= target

                argmax = torch.argmax(policy).item()
                x2, y2 = argmax // 10, argmax % 10

                visualisation = np.zeros((3, 10, 10))
                visualisation[0, x0, y0] = 255
                visualisation[1, x1, y1] = 255
                visualisation[2, x2, y2] = 255

                self.visdom.image(self.__resizeTensorForDisplay(
                    visualisation), win="Next move")
                # self.visdom.image(self.__resizeTensorForDisplay(value), win="Value")

                self.visdom.image(self.__resizeTensorForDisplay(
                    target), win="Shot mapping")

                self.env.move((x0, y0), (x1, y1), (x2, y2))

            reward = self.env.getReward()
            whiteWon = reward[0] > 0
            print(
                f"Episode {episode} finished. {'White' if whiteWon else 'Black'}. The reward pairing is: {reward}")
            episode += 1

            self.env.kill()
            self.env = Environment()

    def __resizeTensorForDisplay(self, tensor):
        return tensor

    def __saveNets(self):
        name = time.time()

        self.selectionNNet.save(name + "a")
        self.movementNNet.save(name + "b")
        self.arrowShotNNet.save(name + "c")

    def eval(self):
        pass


if __name__ == "__main__":
    Agent(modelByRecent=False)
