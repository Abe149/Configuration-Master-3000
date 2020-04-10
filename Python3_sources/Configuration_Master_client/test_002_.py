#!/usr/bin/env python3

### testing of keys that are expected to be in all namespaces in ML 9 ###

import Configuration_Master_client, os

os.environ["CONFIG_SERVER_URL"] = "https://localhost:4430/"

print ()
print ("test results")
print ("------------")
print (Configuration_Master_client.get_test())
print ()

