#!/usr/bin/env python3

### BOOLEAN-VALUED CONFIG.S ONLY ###

import Configuration_Master_client, os

def test(ML, key):

  print ("in maturity level " + str(ML) + ", namespace '*' [i.e. only matches config.s that are present in all namespaces at least in this ML]:\n\t " + "for key = ''" + key + "'': textual result = ''" + str(Configuration_Master_client.get_config(key=key, ML=ML)) + "'', result = " + str(Configuration_Master_client.get_config_as_Boolean(key=key, ML=ML)))
  print ()

print ()
test( 0, "test key name for a Boolean")
test( 1, "test key name for a Boolean")
test( 2, "test key name for a Boolean")
test( 3, "test key name for a Boolean")
test( 4, "test key name for a Boolean")
test( 5, "test key name for a Boolean")
test( 6, "test key name for a Boolean")
test( 7, "test key name for a Boolean")
test( 8, "test key name for a Boolean")
test( 9, "test key name for a Boolean")
test(10, "test key name for a Boolean")

