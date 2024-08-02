import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from domain.namingServer import NamingServer
from domain.serverEntry import ServerEntry
from domain.serviceEntry import ServiceEntry

class NameServerServiceImpl(pb2_grpc.TupleSpacesNameServerServicer):

    def __init__(self, *args, **kwargs):
        self.naming_server = NamingServer()
        self.client_id = 0
        pass

    def Register(self, request, context):
        # get service name, qualifier, and host:port
        service_name = request.serviceName
        qualifier = request.qualifier
        target = request.target

        # create new server entry
        server_entry = ServerEntry(qualifier, target)

        # check if the service entry exists, and if
        service = self.naming_server.get_service_entry(service_name)
        if service is not None:
            # check if there is a server with the same qualifier (return error if the qualifier is the same)
            for entry in service.get_server_entries():
                if entry.get_qualifier() == qualifier and entry.get_target() == target:
                    return pb2.RegisterResponse(error="Server already exists.")

            # append the new server to the service
            service.add_server_entry(server_entry)
        else:
            # if the service was not yet created, add it, along with the server
            self.naming_server.add_service_entry(service_name, ServiceEntry([server_entry]))

        # create response
        response = pb2.RegisterResponse(error="")

        return response

    def Lookup(self, request, context):
        # get service name, and qualifier
        service_name_given = request.serviceName
        qualifier = request.qualifier
        servers = []
        services = self.naming_server.get_service_entry(service_name_given)
        #if qualifier == "":

        qualifier_matched = False
        entries = services.get_server_entries()

        if qualifier != "" and qualifier is not None:
            if services is not None:
                for entry in entries:
                    if qualifier == entry.get_qualifier():
                        servers.append(entry)
                        qualifier_matched = True
            else:
                return pb2.LookupResponse(servers=[])
            if qualifier_matched:
                result = list(map(lambda x: pb2.ServerInfo(qualifier=x.get_qualifier(), target=x.get_target()), servers))
                return pb2.LookupResponse(servers=result)
            else :
                return pb2.LookupResponse(servers=[])
        else:
            servers = services.get_server_entries()
            result = list(map(lambda x: pb2.ServerInfo(qualifier=x.get_qualifier(), target=x.get_target()), servers))
            return pb2.LookupResponse(servers=result)

    def GenerateId(self, request, context):
        self.client_id += 1
        return pb2.GenerateIdResponse(clientId=self.client_id, serverNumber=self.naming_server.get_server_number())

    def Delete(self, request, context):
        service_name = request.serviceName
        target = request.target
        service = self.naming_server.get_service_entry(service_name)

        if service is None:
            return pb2.DeleteResponse(error="No service found.")

        entries = service.get_server_entries()

        for entry in entries:
            if entry.get_target() == target:
                service.remove_server_entry(entry)
                return pb2.DeleteResponse(error=None)

        # create response
        response = pb2.DeleteResponse(error="Not possible to remove the server")

        return response
