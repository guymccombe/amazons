from environment import Environment

import torch
import torch.nn as nn
import torch.nn.functional as F
import torchvision

import visdom


class Agent():

    def __init__(self):
        self.env = Environment()
        vis = visdom.Visdom(port=8097)
        if not self.env.isGameFinished():
            print("Getting initial images")
            img1, img2, img3 = self.env.getState()
            vis.image(img1, win="Own amazons")
            vis.image(img2, win="Opp amazons")
            vis.image(img3, win="Arrows")

            print("Moving!")
            self.env.move((0, 3), (0, 4), (0, 3))

            self.env.isGameFinished()
            print("Getting new images")
            img1, img2, img3 = self.env.getState()
            vis.image(img1, win="Own amazons 2")
            vis.image(img2, win="Opp amazons 2")
            vis.image(img3, win="Arrows 2")


if __name__ == "__main__":
    Agent()
