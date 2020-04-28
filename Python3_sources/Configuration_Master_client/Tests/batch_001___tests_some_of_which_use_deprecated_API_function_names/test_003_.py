#!/usr/bin/env python3

### testing of keys that are expected to be in all namespaces in ML 9 ###

### INTEGER-VALUED CONFIG.S ONLY ###

import Configuration_Master_client, os

def test(key):
  print ("in maturity level " + str(Configuration_Master_client.get_ML()) + ", namespace '*' [i.e. only matches config.s that are present in all namespaces at least in this ML]:\n\t " + ("%-70s" % ("for key = ''" + key + "'':")) + " result = (integer: " + str(Configuration_Master_client.get_config_as_integer(key=key)) + ')')
  print ()

print ()
test("test good nonnegative_integer")
test("test good positive_integer")
test("test key name for a positive integer with redundancy")
# test("")

