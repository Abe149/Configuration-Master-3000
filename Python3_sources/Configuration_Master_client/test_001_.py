#!/usr/bin/env python3

### testing of keys that are expected to be in all namespaces in ML 9 ###

import Configuration_Master_client, os

os.environ["CONFIG_SERVER_URL"]     = "https://localhost:4430/"
os.environ["CONFIG_MATURITY_LEVEL"] = '9'

def test(key):
  print (("%-70s" % ("in maturity level " + os.environ["CONFIG_MATURITY_LEVEL"] + ", namespace '*' [i.e. only matches config.s that are present in all namespaces at least in this ML], for key = ''" + key + "'':") + "result = ''" + Configuration_Master_client.get_config(key=key) + "''"))
  print ()

print ()
test("test good URL")
test("test good url")
test("test good nonempty_string")
test("test good nonnegative_integer")
test("test good positive_integer")
test("test key name for a positive integer with redundancy")
# test("")
