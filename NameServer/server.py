import sys
sys.path.insert(1, '../Contract/target/generated-sources/protobuf/python')
import grpc
import NameServer_pb2 as pb2
import NameServer_pb2_grpc as pb2_grpc
from namingServerServiceImpl import NameServerServiceImpl
from concurrent import futures

# define the port
PORT = 5001

if __name__ == '__main__':
    try:
        # print received arguments
        print("Received arguments:")
        for i in range(1, len(sys.argv)):
            print("  " + sys.argv[i])

        port = 5001

        # create server
        server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        # create service
        pb2_grpc.add_TupleSpacesNameServerServicer_to_server(NameServerServiceImpl(), server)
        # listen on port
        server.add_insecure_port('[::]:'+str(port))
        # start server
        server.start()
        # print message
        print("NameServer listening on port " + str(port))
        # wait for server to finish
        server.wait_for_termination()

    except KeyboardInterrupt:
        print("NameServer stopped")
        exit(0)
