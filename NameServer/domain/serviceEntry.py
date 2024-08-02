
class ServiceEntry:
    def __init__(self, server_entries):
        self.__server_entries = server_entries

    def add_server_entry(self, entry):
        self.__server_entries.append(entry)

    def remove_server_entry(self, entry):
        self.__server_entries.remove(entry)

    def get_server_entries(self):
        return self.__server_entries
