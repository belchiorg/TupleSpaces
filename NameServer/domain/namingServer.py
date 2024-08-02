
class NamingServer:
    def __init__(self):
        self.services = {}

    def add_service_entry(self, name, entry):
        self.services[name] = entry

    def remove_service_entry(self, name):
        del self.services[name]

    def get_service_entry(self, name):
        if name in self.services.keys():
            return self.services[name]
        return None

    def get_server_number(self):
        return len(self.services)