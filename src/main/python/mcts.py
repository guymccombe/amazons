class mcts():

    def __init__(self, env, nnet):
        self.env = env
        self.nnet = nnet

        self.qValues = {}
        self.nVisited = {}
        self.policies = {}

    def search(self):
        if self.env.isGameFinished():
            return self.env.getReward()

        state = self.env.getGameState()
        if state not in self.policies:
            self.policies[state], value = self.nnet.forward(state)
