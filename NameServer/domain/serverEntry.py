
class ServerEntry:
    def __init__(self, qualifier, target):
        self.__target = target
        self.__qualifier = qualifier

    def get_target(self):
        return self.__target

    def get_qualifier(self):
        return self.__qualifier