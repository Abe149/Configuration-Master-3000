#!/usr/bin/env python3

### testing of just the "/test" endpoint ###

import Configuration_Master_client, os

os.environ["CONFIG_SERVER_URL"] = "https://localhost:4430/"

print ()
print ("test results")
print ("------------")
print (Configuration_Master_client.get_test())
print ()

